package com.borzg.imageprocessor

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borzg.imageprocessor.filters.Filter
import com.borzg.imageprocessor.filters.processor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MainActivityViewModel : ViewModel() {

    lateinit var imageLoader: ImageLoader

    private val _isSelectFilterScreenVisible = MutableStateFlow(false)
    val isSelectFilterScreenVisible = _isSelectFilterScreenVisible.asStateFlow()

    private val uploadedBitmap = MutableStateFlow<Bitmap?>(null)

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    private val _filterSequence = MutableStateFlow<List<Filter>>(emptyList())
    val filterSequence = _filterSequence.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            uploadedBitmap.value =
                imageLoader.loadBitmapFromURL("https://images.ctfassets.net/hrltx12pl8hq/7yQR5uJhwEkRfjwMFJ7bUK/dc52a0913e8ff8b5c276177890eb0129/offset_comp_772626-opt.jpg?fit=fill&w=800&h=300")
            _bitmap.value = uploadedBitmap.value
        }
    }

    fun openSelectFilterScreen() {
        _isSelectFilterScreenVisible.value = true
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

    private fun processImage() {
        val oldBitmap = uploadedBitmap.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _bitmap.value = filterSequence.value.fold(oldBitmap) { bitmap, filter ->
                filter.processor(bitmap)
            }
        }
    }
}



inline fun IntArray.onEachPixel(block: (argbColor: Int) -> Int) {
    for (i in indices) {
        set(i, block(get(i)))
    }
}

private val Int.hexString
    get() = Integer.toHexString(this)