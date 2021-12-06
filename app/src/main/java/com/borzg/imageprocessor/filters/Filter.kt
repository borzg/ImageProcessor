package com.borzg.imageprocessor.filters

import androidx.compose.ui.graphics.Color
import com.borzg.imageprocessor.ui.theme.*

sealed interface Filter {

    val description: String
    val representColor: Color
    val contentColor: Color
    val number: Int

    object BlackAndWhite : Filter {
        override val description: String = "Черно-белое"
        override val representColor: Color = Color.Gray
        override val contentColor: Color = Color.White
        override val number: Int = 1
    }

    object HardBlackAndWhite : Filter {
        override val description: String = "Только черный и белый"
        override val representColor: Color = Color.Black
        override val contentColor: Color = Color.White
        override val number: Int = 2
    }

    object Negative : Filter {
        override val description: String = "Негативное"
        override val representColor: Color = Color.DarkGray
        override val contentColor: Color = Color.White
        override val number: Int = 3
    }

    enum class LeaveAlone : Filter {
        Red {
            override val description: String = "Оставить только красный"
            override val representColor: Color = GreyRed
            override val contentColor: Color = Color.White
            override val number: Int = 10
        },
        Green {
            override val description: String = "Оставить только зеленый"
            override val representColor: Color = GreyGreen
            override val contentColor: Color = Color.White
            override val number: Int = 11
        },
        Blue {
            override val description: String = "Оставить только синий"
            override val representColor: Color = GreyBlue
            override val contentColor: Color = Color.White
            override val number: Int = 12
        },
    }

    sealed class ChangeColorIntensity(val delta: Int) : Filter {

        class Red(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Увеличить интенсивность красного"
            override val representColor: Color = HardRed
            override val contentColor: Color = Color.White
            override val number: Int = 4
        }

        class Green(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Увеличить интенсивность зеленого"
            override val representColor: Color = HardGreen
            override val contentColor: Color = Color.White
            override val number: Int = 5
        }

        class Blue(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Увеличить интенсивность синего"
            override val representColor: Color = HardBlue
            override val contentColor: Color = Color.White
            override val number: Int = 6
        }

        class RedNegative(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Уменьшить интенсивность красного"
            override val representColor: Color = SoftRed
            override val contentColor: Color = Color.White
            override val number: Int = 7
        }

        class GreenNegative(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Уменьшить интенсивность зеленого"
            override val representColor: Color = SoftGreen
            override val contentColor: Color = Color.White
            override val number: Int = 8
        }

        class BlueNegative(delta: Int) : ChangeColorIntensity(delta) {
            override val description: String = "Уменьшить интенсивность синего"
            override val representColor: Color = SoftBlue
            override val contentColor: Color = Color.White
            override val number: Int = 9
        }
    }
}

val Filter.processor: Processor
    get() = when (this) {
        Filter.BlackAndWhite -> Processor.PixelByPixelProcessor.BlackAndWhite
        Filter.HardBlackAndWhite -> Processor.PixelByPixelProcessor.HardBlackAndWhite
        Filter.Negative -> Processor.PixelByPixelProcessor.Negative
        is Filter.ChangeColorIntensity.Red -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            delta,
            RGB.Red
        )
        is Filter.ChangeColorIntensity.Green -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            delta,
            RGB.Green
        )
        is Filter.ChangeColorIntensity.Blue -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            delta,
            RGB.Blue
        )
        is Filter.ChangeColorIntensity.BlueNegative -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            -delta,
            RGB.Red
        )
        is Filter.ChangeColorIntensity.GreenNegative -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            -delta,
            RGB.Green
        )
        is Filter.ChangeColorIntensity.RedNegative -> Processor.PixelByPixelProcessor.ChangeColorIntensity(
            -delta,
            RGB.Blue
        )
        Filter.LeaveAlone.Red -> Processor.PixelByPixelProcessor.LeaveAlone(RGB.Red)
        Filter.LeaveAlone.Green -> Processor.PixelByPixelProcessor.LeaveAlone(RGB.Green)
        Filter.LeaveAlone.Blue -> Processor.PixelByPixelProcessor.LeaveAlone(RGB.Blue)
    }