package com.example.kotlinapp.Util

import org.opencv.core.Core

class Constants {
    companion object {
        val FontFace = Core.FONT_HERSHEY_PLAIN
    }
    enum class TileState {
        UNSTABLE,
        VALID,
        PROCESSED
    }
}