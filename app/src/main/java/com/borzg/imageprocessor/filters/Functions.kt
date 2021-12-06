package com.borzg.imageprocessor.filters

import android.graphics.Color
import androidx.annotation.IntRange

fun Int.toNegative(): Int =
    xor(0x00FFFFFF)

fun Int.toBlackAndWhite(): Int {
    val common = runWithRgb { red, green, blue ->
        ((green + red + blue) / 3f).toInt()
    }
    return rgb(common, common, common)
}

fun Int.toHardBlackAndWhite(): Int {
    val common = runWithRgb { red, green, blue ->
        ((green + red + blue) / 3f).toInt()
    }
    return if (common > 255 / 2f) Color.WHITE else Color.BLACK
}

fun Int.leaveAlone(rgb: RGB): Int {
    return runWithRgb { red, green, blue ->
        when (rgb) {
            RGB.Red -> if (red > green && red > blue)
                this
            else
                toBlackAndWhite()
            RGB.Green -> if (green > red && green > blue)
                this
            else
                toBlackAndWhite()
            RGB.Blue -> if (blue > red && blue > green)
                this
            else
                toBlackAndWhite()
        }
    }
}

fun Int.changeIntensity(delta: Int, rgb: RGB): Int = runWithRgb { red, green, blue ->
    return@runWithRgb rgb(
        red = if (rgb == RGB.Red) (red + delta).coerceIn(0, 255) else red,
        green = if (rgb == RGB.Green) (green + delta).coerceIn(0, 255) else green,
        blue = if (rgb == RGB.Blue) (blue + delta).coerceIn(0, 255) else blue
    )
}

fun rgb(
    @IntRange(from = 0, to = 255) red: Int,
    @IntRange(from = 0, to = 255) green: Int,
    @IntRange(from = 0, to = 255) blue: Int
) = argb(255, red, green, blue)

fun argb(
    @IntRange(from = 0, to = 255) alpha: Int,
    @IntRange(from = 0, to = 255) red: Int,
    @IntRange(from = 0, to = 255) green: Int,
    @IntRange(from = 0, to = 255) blue: Int
): Int = Color.argb(alpha, red, green, blue)

private inline fun <T> Int.runWithRgb(block: (red: Int, green: Int, blue: Int) -> T): T =
    block(Color.red(this), Color.green(this), Color.blue(this))