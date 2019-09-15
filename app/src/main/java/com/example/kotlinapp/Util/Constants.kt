package com.example.kotlinapp.Util

import org.opencv.core.Core
import org.opencv.core.Point

class Constants {
    companion object {
        val FontFace = Core.FONT_HERSHEY_COMPLEX_SMALL
        val StartingTextPoint = Point(50.0, 100.0)
        val StartingPhasesTextPoint = Point(50.0, 220.0)
        val FontSize = 3.0
        val TextThickness = 4
    }
    enum class TileState {
        UNSTABLE,
        VALID,
        PROCESSED
    }
}