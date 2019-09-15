package com.example.kotlinapp.Enums

import org.opencv.core.Scalar

enum class Color(val charNotation : Char,
                 val isRubikColor : Boolean,
                 val cvColor : Scalar,
                 val redComponent:Float,
                 val greenComponent:Float,
                 val blueComponent:Float,
                 val hsvMinValue : Scalar,
                 val hsvMaxValue : Scalar,
                 var hsvCalibrateValue : Scalar?,
                 var medianError : Double) {

    RED('R', true, Scalar(220.0,   20.0,  30.0),
        1.0f, 0.0f, 0.0f,
        Scalar(0.0, 160.0, 60.0, 1.0), Scalar(20.0, 255.0, 140.0, 1.0),null, 0.0),
    ORANGE('O', true, Scalar(240.0,   80.0,   0.0),
        0.9f, 0.4f, 0.0f,
        Scalar(3.0, 150.0, 140.0, 0.0), Scalar(30.0, 230.0, 220.0, 0.0),null, 0.0),
    YELLOW('Y', true, Scalar(230.0,  230.0,  20.0),
        0.9f, 0.9f, 0.2f,
        Scalar(40.0, 110.0, 110.0, 0.0), Scalar(70.0, 160.0, 220.0, 0.0),null, 0.0),
    GREEN('G', true, Scalar(0.0,    200.0,  60.0),
        0.0f, 1.0f, 0.0f,
        Scalar(50.0, 190.0, 80.0, 0.0), Scalar(80.0, 255.0, 150.0, 0.0),null, 0.0),
    BLUE('B', true, Scalar(0.0,     60.0, 220.0),
        0.2f, 0.2f, 1.0f,
        Scalar(100.0, 180.0, 110.0, 1.0), Scalar(130.0, 255.0, 160.0, 1.0),null, 0.0),
    WHITE('W', true, Scalar(225.0,  225.0, 225.0),
        1.0f, 1.0f, 1.0f,
        Scalar(90.0, 50.0, 90.0, 0.0), Scalar(120.0, 130.0, 190.0, 0.0),null, 0.0),
    GRAY('S', false, Scalar(0.0,    0.0,   0.0),
        0.6f, 0.6f, 0.6f,
        Scalar(0.0, 0.0, 0.0, 0.0), Scalar(2.0, 2.0, 2.0, 2.0),null, 0.0),
    BLACK('K', false, Scalar(0.0,    0.0,   0.0),
        0.0f, 0.0f, 0.0f,
        Scalar(0.0, 0.0, 0.0, 0.0), Scalar(2.0, 2.0, 2.0, 2.0),null, 0.0);
}