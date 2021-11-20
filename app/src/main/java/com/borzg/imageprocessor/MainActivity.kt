package com.borzg.imageprocessor

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.borzg.imageprocessor.filters.Filter
import com.borzg.imageprocessor.ui.theme.ImageProcessorTheme
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainActivityViewModel>()

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.imageLoader = ImageLoader(applicationContext)

        setContent {
            ImageProcessorTheme {
                val bitmap by viewModel.bitmap.collectAsState()
                val selectedFiltersSequence by viewModel.filterSequence.collectAsState()
                val isSelectFiltersVisible by viewModel.isSelectFilterScreenVisible.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "ass",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Button(
                        onClick = viewModel::openSelectFilterScreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_baseline_add_24),
                            contentDescription = "Открыть список фильтров"
                        )
                    }

                    if (isSelectFiltersVisible)
                        FilterSelection(
                            selectedFiltersSequence = selectedFiltersSequence,
                            allFilters = listOf(
                                Filter.Negative,
                                Filter.BlackAndWhite,
                                Filter.ChangeColorIntensity.Red(100),
                                Filter.ChangeColorIntensity.Green(100),
                                Filter.ChangeColorIntensity.Blue(100)
                            ),
                            onFilterClick = viewModel::selectFilter,
                            onStartProcessingClick = viewModel::applyFilters,
                            onResetFiltersClick = viewModel::resetFilters,
                            modifier = Modifier.fillMaxSize()
                        )
                }
            }
        }
    }
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
    Column(
        modifier = modifier
            .background(MaterialTheme.colors.background)
    ) {
        LazyColumn(
            modifier = modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            stickyHeader {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    crossAxisAlignment = FlowCrossAxisAlignment.Center
                ) {
                    selectedFiltersSequence.forEachIndexed { index, filter ->
                        SequenceFilterItem(
                            filter = filter,
                            isArrowVisible = index != selectedFiltersSequence.lastIndex
                        )
                    }
                }
            }

            allFilters.forEachIndexed { index, filter ->
                item {
                    FilterItem(filter = filter, onFilterClick = onFilterClick)
                    if (index != allFilters.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onResetFiltersClick,
                modifier = Modifier
            ) {
                Text(text = "Сбросить фильтры")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartProcessingClick,
                modifier = Modifier
            ) {
                Text(text = "Применить фильтры")
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun FiltersGrid(
    filters: List<Filter>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(4),
        modifier = modifier
    ) {

    }
}

@Composable
fun SequenceFilterItem(filter: Filter, isArrowVisible: Boolean) {
    Row(
        modifier = Modifier.defaultMinSize(minHeight = 30.dp)
    ) {
        Text(text = filter.description)
        if (isArrowVisible) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_24),
                contentDescription = ""
            )
        }
    }
}

@Composable
fun FilterItem(filter: Filter, onFilterClick: (Filter) -> Unit) {
    Button(onClick = {
        onFilterClick(filter)
    }) {
        Text(text = filter.description)
    }
}