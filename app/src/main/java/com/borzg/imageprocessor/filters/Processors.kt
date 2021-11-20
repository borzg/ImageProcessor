package com.borzg.imageprocessor.filters

import android.graphics.Bitmap
import com.borzg.imageprocessor.onEachPixel

interface BitmapProcessor {
    operator fun invoke(bitmap: Bitmap): Bitmap
}

sealed interface Processor : BitmapProcessor {

    sealed interface OneWayProcessor : Processor {

        fun process(color: Int): Int

        override fun invoke(bitmap: Bitmap): Bitmap =
            bitmap.oneWayProcessing(::process)

        object BlackAndWhite : OneWayProcessor {
            override fun process(color: Int): Int =
                color.toBlackAndWhite()
        }

        object Negative : OneWayProcessor {
            override fun process(color: Int): Int =
                color.toNegative()
        }

        class ChangeColorIntensity(
            private val delta: Int,
            private val rgb: RGB
        ) : OneWayProcessor {
            override fun process(color: Int): Int =
                color.changeIntensity(delta, rgb)
        }
    }
}

inline fun Bitmap.oneWayProcessing(block: (argbColor: Int) -> Int): Bitmap {
    val newBitmap = copy(Bitmap.Config.ARGB_8888, true)
    val length = width * height;
    val array = IntArray(length);
    getPixels(
        array,
        0,
        width,
        0,
        0,
        width,
        height
    )
    array.onEachPixel(block)
    newBitmap.setPixels(array, 0, width, 0, 0, width, height)
    return newBitmap
}