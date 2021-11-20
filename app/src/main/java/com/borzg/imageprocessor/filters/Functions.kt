package com.borzg.imageprocessor.filters

import android.graphics.Color

fun Int.toNegative(): Int =
    xor(0x00FFFFFF)

fun Int.toBlackAndWhite(): Int {
    val common = runWithRgb { red, green, blue ->
        ((green + red + blue) / 3f).toInt()
    }
    return Color.argb(255, common, common, common)
}

fun Int.changeIntensity(delta: Int, rgb: RGB): Int = runWithRgb { red, green, blue ->
    val newRed = if (rgb == RGB.Red) (red + delta).coerceIn(0..255) else red
    val newGreen = if (rgb == RGB.Green) (green + delta).coerceIn(0..255) else green
    val newBlue = if (rgb == RGB.Blue) (blue + delta).coerceIn(0..255) else blue
    return@runWithRgb Color.argb(255, newRed, newGreen, newBlue)
}

private inline fun <reified T> Int.runWithRgb(block: (red: Int, green: Int, blue: Int) -> T): T =
    block(Color.red(this), Color.green(this), Color.blue(this))