package com.borzg.imageprocessor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borzg.imageprocessor.filters.Filter
import com.borzg.imageprocessor.filters.Processor
import com.borzg.imageprocessor.filters.processor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.max

class MainActivityViewModel : ViewModel() {

    lateinit var imageLoader: ImageLoader
    lateinit var screenSize: Pair<Int, Int>

    private val _isSelectFilterScreenVisible = MutableStateFlow(false)
    val isSelectFilterScreenVisible = _isSelectFilterScreenVisible.asStateFlow()

    private val _isSelectPictureDialogVisible = MutableStateFlow(false)
    val isSelectPictureDialogVisible = _isSelectPictureDialogVisible.asStateFlow()

    private val _isSavePictureDialogVisible = MutableStateFlow(false)
    val isSavePictureDialogVisible = _isSavePictureDialogVisible.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages = _toastMessages.asSharedFlow()

    private val uploadedBitmap = MutableStateFlow<Bitmap?>(null)

    private val _processingImage = MutableStateFlow<Bitmap?>(null)

    val image = _processingImage
        .map {
            it?.compressToScreenSizeIfNeeded()
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _filterSequence = MutableStateFlow<List<Filter>>(emptyList())
    val filterSequence = _filterSequence.asStateFlow()

    private val _isInProcess = MutableStateFlow(false)
    val isInProcess = _isInProcess.asStateFlow()

    fun openSelectFilterScreen() {
        _isSelectFilterScreenVisible.value = true
    }

    fun closeSelectFilterScreen() {
        _isSelectFilterScreenVisible.value = false
    }

    fun openSelectPictureDialog() {
        _isSelectPictureDialogVisible.value = true
    }

    fun closeSelectPictureDialog() {
        _isSelectPictureDialogVisible.value = false
    }

    fun pickFromUrl(url: String) {
        _isSelectPictureDialogVisible.value = false
        _isSelectFilterScreenVisible.value = false
        if (url.isNotBlank()) {
            resetFilters()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    uploadedBitmap.value =
                        imageLoader.loadBitmapFromURL(url)
                    _processingImage.value = uploadedBitmap.value
                } catch (e: Throwable) {
                    _toastMessages.emit("Что-то пошло не так")
                }
            }
        }
    }

    fun pickFromGallery(bitmap: Bitmap) {
        _isSelectPictureDialogVisible.value = false
        _isSelectFilterScreenVisible.value = false
        resetFilters()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                uploadedBitmap.value = bitmap
                _processingImage.value = uploadedBitmap.value
            } catch (e: Throwable) {
                _toastMessages.emit("Что-то пошло не так")
            }
        }
    }

    fun selectFilter(filter: Filter) {
        _filterSequence.value = filterSequence.value + filter
    }

    fun resetFilters() {
        _filterSequence.value = emptyList()
    }

    fun applyFilters() {
        _isSelectFilterScreenVisible.value = false
        processImage()
    }

    fun setError(e: Throwable) {
        viewModelScope.launch {
            _toastMessages.emit("Что-то пошло не так")
        }
    }

    fun onImageClick() {
        _isSavePictureDialogVisible.value = true
    }

    fun closeSaveImageDialog() {
        _isSavePictureDialogVisible.value = false
    }

    fun onSaveImage(context: Context) {
        _isSavePictureDialogVisible.value = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                saveToGallery(context)
                _toastMessages.emit("Изображение успешно сохранено")
            } catch (e: Throwable) {
                _toastMessages.emit("Не получилось сохранить изображение")
            }
        }
    }

    private fun saveToGallery(context: Context) {
        val bitmap = _processingImage.value ?: return
        val filename = "${System.currentTimeMillis()}.png"
        val write: (OutputStream) -> Boolean = {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            }

            context.contentResolver.let {
                it.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                    it.openOutputStream(uri)?.let(write)
                }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdir()
            }
            val image = File(imagesDir, filename)
            write(FileOutputStream(image))
        }
    }

    private fun processImage() {
        val oldBitmap = uploadedBitmap.value ?: return
        _isInProcess.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _processingImage.value = filterSequence.value
                    .map(Filter::processor)
                    .simplify()
                    .fold(oldBitmap) { bitmap, processor ->
                        processor(bitmap)
                    }
            } catch (e: Throwable) {
                _toastMessages.emit("Что-то пошло не так")
            } finally {
                _isInProcess.value = false
            }
        }
    }

    private fun Bitmap.compressToScreenSizeIfNeeded(): Bitmap {
        return if (width > screenSize.first || height > screenSize.second) {
            val newSize =
                compressWithRationToMaxSize(width, height, max(screenSize.first, screenSize.second))
            Bitmap.createScaledBitmap(this, newSize.first, newSize.second, true)
        } else this
    }
}

fun compressWithRationToMaxSize(x: Int, y: Int, max: Int): Pair<Int, Int> {
    if (x <= max && y <= max) return x to y
    val ratio = x / y.toFloat()
    return if (x >= y)
        max to (max / ratio).toInt()
    else
        max to (max * ratio).toInt()
}

fun List<Processor>.simplify(): List<Processor> {
    val result = mutableListOf<Processor>()
    forEach { processor ->
        val last = result.lastOrNull()
        if (processor is Processor.PixelByPixelProcessor && last is Processor.PixelByPixelProcessor) {
            result[result.lastIndex] = object : Processor.PixelByPixelProcessor {
                override fun process(pixelColor: Int): Int {
                    return processor.process(last.process(pixelColor))
                }
            }
        } else result.add(processor)
    }
    return result
}

inline fun IntArray.onEachPixel(block: (argbColor: Int) -> Int) {
    for (i in indices) {
        set(i, block(get(i)))
    }
}