package com.borzg.imageprocessor.filters

sealed interface Filter {

    val description: String

    object BlackAndWhite : Filter {
        override val description: String  = "Черно-белое"
    }

    object Negative: Filter {
        override val description: String  = "Негативное"
    }

    sealed class ChangeColorIntensity(val delta: Int) : Filter {

        class Red(delta: Int): ChangeColorIntensity(delta) {
            override val description: String  = "Увеличить интенсивность красного"
        }

        class Green(delta: Int): ChangeColorIntensity(delta) {
            override val description: String  = "Увеличить интенсивность зеленого"
        }

        class Blue(delta: Int): ChangeColorIntensity(delta) {
            override val description: String  = "Увеличить интенсивность синего"
        }
    }
}

val Filter.processor: Processor
    get() = when(this) {
        Filter.BlackAndWhite -> Processor.OneWayProcessor.BlackAndWhite
        Filter.Negative -> Processor.OneWayProcessor.Negative
        is Filter.ChangeColorIntensity.Red -> Processor.OneWayProcessor.ChangeColorIntensity(delta, RGB.Red)
        is Filter.ChangeColorIntensity.Green -> Processor.OneWayProcessor.ChangeColorIntensity(delta, RGB.Green)
        is Filter.ChangeColorIntensity.Blue -> Processor.OneWayProcessor.ChangeColorIntensity(delta, RGB.Blue)
    }