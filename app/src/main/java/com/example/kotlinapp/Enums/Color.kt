package com.example.kotlinapp.Enums

import org.opencv.core.Scalar

enum class Color(val charNotation : Char, val cvColor : Scalar, val redComponent:Float, val greenComponent:Float, val blueComponent:Float, var hsvCalibrateValue : Scalar?) {
    RED('R', Scalar(220.0,   20.0,  30.0),1.0f, 0.0f, 0.0f, null),
    ORANGE('O', Scalar(240.0,   80.0,   0.0), 0.9f, 0.4f, 0.0f, null),
    YELLOW('Y', Scalar(230.0,  230.0,  20.0), 0.9f, 0.9f, 0.2f, null),
    GREEN('G', Scalar(0.0,    140.0,  60.0), 0.0f, 1.0f, 0.0f, null),
    BLUE('B', Scalar(0.0,     60.0, 220.0), 0.2f, 0.2f, 1.0f, null),
    WHITE('W', Scalar(225.0,  225.0, 225.0), 1.0f, 1.0f, 1.0f, null),
    BLACK('K', Scalar(0.0,    0.0,   0.0), 0.0f, 0.0f, 0.0f, null);
}