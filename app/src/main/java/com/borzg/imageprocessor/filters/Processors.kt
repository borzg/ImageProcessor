package com.borzg.imageprocessor.filters

import android.graphics.Bitmap
import com.borzg.imageprocessor.onEachPixel

interface BitmapProcessor {
    operator fun invoke(bitmap: Bitmap): Bitmap
}

interface Processor : BitmapProcessor {

    interface PixelByPixelProcessor : Processor {

        fun process(pixelColor: Int): Int

        override fun invoke(bitmap: Bitmap): Bitmap =
            bitmap.oneWayProcessing(::process)

        object BlackAndWhite : PixelByPixelProcessor {
            override fun process(pixelColor: Int): Int =
                pixelColor.toBlackAndWhite()
        }

        object HardBlackAndWhite : PixelByPixelProcessor {
            override fun process(pixelColor: Int): Int =
                pixelColor.toHardBlackAndWhite()
        }

        object Negative : PixelByPixelProcessor {
            override fun process(pixelColor: Int): Int =
                pixelColor.toNegative()
        }

        class LeaveAlone(
            private val rgb: RGB
        ) : PixelByPixelProcessor {
            override fun process(pixelColor: Int): Int =
                pixelColor.leaveAlone(rgb)
        }

        class ChangeColorIntensity(
            private val delta: Int,
            private val rgb: RGB
        ) : PixelByPixelProcessor {
            override fun process(pixelColor: Int): Int =
                pixelColor.changeIntensity(delta, rgb)
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