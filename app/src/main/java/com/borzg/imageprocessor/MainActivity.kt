package com.borzg.imageprocessor

import android.graphics.ImageDecoder
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.borzg.imageprocessor.filters.Filter
import com.borzg.imageprocessor.ui.theme.ImageProcessorTheme
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainActivityViewModel>()
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    uri
                )
            } else {
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            }
            viewModel.pickFromGallery(bitmap)
        } catch (e: Exception) {
            viewModel.setError(e)
        }
    }

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.imageLoader = ImageLoader(applicationContext)
        val display = windowManager.defaultDisplay
        viewModel.screenSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            bounds.width() to bounds.height()
        } else {
            val size = Point()
            display.getSize(size)
            size.x to size.y
        }

        lifecycleScope.launch {
            viewModel.toastMessages
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT)
                        .show()
                }
        }

        setContent {
            ImageProcessorTheme {
                val bitmap by viewModel.image.collectAsState()
                val selectedFiltersSequence by viewModel.filterSequence.collectAsState()
                val isSelectFiltersVisible by viewModel.isSelectFilterScreenVisible.collectAsState()
                val isPickDialogVisible by viewModel.isSelectPictureDialogVisible.collectAsState()
                val isSaveDialogVisible by viewModel.isSavePictureDialogVisible.collectAsState()
                val isInProcess by viewModel.isInProcess.collectAsState()

//                LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher?.addCallback(object : OnBackPressedCallback(isSelectFiltersVisible) {
//                    override fun handleOnBackPressed() {
//                        viewModel.closeSelectFilterScreen()
//                    }
//
//                })

                val context = LocalContext.current
                Box(modifier = Modifier.fillMaxSize()) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Изображение",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable(onClick = viewModel::onImageClick)
                        )
                    } ?: run {
                        Text(
                            text = "Вы пока не выбрали изображение",
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.Center),
                            style = MaterialTheme.typography.h5,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = viewModel::openSelectPictureDialog,
                            modifier = Modifier
                                .defaultMinSize(minHeight = 56.dp)
                                .padding(16.dp)
                                .weight(1f)
                        ) {
                            Text(text = "Выбрать изображение")
                        }

                        if (bitmap != null)
                            FloatingActionButton(
                                onClick = viewModel::openSelectFilterScreen,
                                modifier = Modifier
                                    .padding(
                                        top = 16.dp,
                                        bottom = 16.dp,
                                        end = 16.dp
                                    )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_filter_24),
                                    contentDescription = "Открыть список фильтров"
                                )
                            }
                    }

                    if (isInProcess) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.7f))
                                .pointerInput(Unit) {},
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    if (isSelectFiltersVisible) {
                        BackHandler(onBack = viewModel::closeSelectFilterScreen)
                        FilterSelection(
                            selectedFiltersSequence = selectedFiltersSequence,
                            allFilters = listOf(
                                Filter.Negative,
                                Filter.BlackAndWhite,
                                Filter.HardBlackAndWhite,
                                Filter.ChangeColorIntensity.Red(50),
                                Filter.ChangeColorIntensity.Green(50),
                                Filter.ChangeColorIntensity.Blue(50),
                                Filter.ChangeColorIntensity.RedNegative(50),
                                Filter.ChangeColorIntensity.GreenNegative(50),
                                Filter.ChangeColorIntensity.BlueNegative(50),
                                Filter.LeaveAlone.Red,
                                Filter.LeaveAlone.Green,
                                Filter.LeaveAlone.Blue
                            ).sortedBy(Filter::number),
                            onFilterClick = viewModel::selectFilter,
                            onStartProcessingClick = viewModel::applyFilters,
                            onResetFiltersClick = viewModel::resetFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isSaveDialogVisible)
                        SavePicDialog(
                            onDismissRequest = viewModel::closeSaveImageDialog,
                            onSaveClick = {
                                viewModel.onSaveImage(context)
                            }
                        )

                    if (isPickDialogVisible)
                        SelectPicDialog(
                            onDismissRequest = viewModel::closeSelectPictureDialog,
                            onFromGalleryClick = {
                                getImage.launch("image/*")
                            },
                            onFromUrl = viewModel::pickFromUrl
                        )
                }
            }
        }
    }
}

@Composable
private fun SelectPicDialog(
    onDismissRequest: () -> Unit,
    onFromGalleryClick: () -> Unit,
    onFromUrl: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Введите ссылку на изображение, или выберите из галереи")
        },
        buttons = {
            var text by remember {
                mutableStateOf("")
            }
            Column(modifier = Modifier.padding(16.dp)) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onFromGalleryClick) {
                        Text(text = "ИЗ ГАЛЛЕРЕИ")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        onFromUrl(text)
                    }) {
                        Text(text = "ПРИМЕНИТЬ")
                    }
                }
            }
        }
    )
}

@Composable
private fun SavePicDialog(
    onDismissRequest: () -> Unit,
    onSaveClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Вы хотите сохранить изображение в галлерею?")
        },
        buttons = {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(text = "НЕТ")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = onSaveClick) {
                        Text(text = "ДА")
                    }
                }
            }
        }
    )
}

@ExperimentalFoundationApi
@Composable
fun FilterSelection(
    selectedFiltersSequence: List<Filter>,
    allFilters: List<Filter>,
    onFilterClick: (Filter) -> Unit,
    onStartProcessingClick: () -> Unit,
    onResetFiltersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .background(MaterialTheme.colors.background),
        topBar = {
            TopAppBar(
                actions = {
                    IconButton(onClick = onResetFiltersClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_delete_24),
                            contentDescription = "Сбросить фильтры"
                        )
                    }
                    IconButton(onClick = onStartProcessingClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_check_24),
                            contentDescription = "Применить фильтры"
                        )
                    }
                },
                title = {
                    Text(text = "Фильтры")
                }
            )
        }
    ) {
        Column(
            modifier = modifier
                .background(MaterialTheme.colors.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(16.dp)
                    .defaultMinSize(minHeight = 200.dp)
            ) {
                if (selectedFiltersSequence.isNotEmpty())
                    FlowRow(
                        crossAxisAlignment = FlowCrossAxisAlignment.Center,
                        crossAxisSpacing = 8.dp
                    ) {
                        selectedFiltersSequence.forEachIndexed { index, filter ->
                            SequenceFilterItem(
                                filter = filter,
                                isArrowVisible = index != selectedFiltersSequence.lastIndex
                            )
                        }
                    }
                else
                    Text(
                        text = "Вы пока не выбрали ни одного фильтра",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
            }
            LazyColumn(
                modifier = modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                allFilters.forEachIndexed { index, filter ->
                    item {
                        FilterItem(
                            filter = filter,
                            onFilterClick = onFilterClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (index != allFilters.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SequenceFilterItem(filter: Filter, isArrowVisible: Boolean) {
    Row(
        modifier = Modifier.defaultMinSize(minHeight = 30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterBubble(
            filter = filter,
            modifier = Modifier.size(36.dp)
        )
        if (isArrowVisible) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_24),
                contentDescription = "",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun FilterBubble(filter: Filter, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = filter.representColor,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = filter.number.toString(),
                color = filter.contentColor,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun FilterItem(filter: Filter, onFilterClick: (Filter) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clickable {
                onFilterClick(filter)
            },
        elevation = 2.dp,
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterBubble(
                filter = filter,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = filter.description.uppercase(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.button
            )
        }
    }
}

@Preview
@Composable
private fun FilterBubblePreview() {
    FilterBubble(filter = Filter.ChangeColorIntensity.Blue(50), modifier = Modifier.size(24.dp))
}