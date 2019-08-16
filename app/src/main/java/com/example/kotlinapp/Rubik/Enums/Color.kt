package com.example.kotlinapp.Rubik.Enums

enum class Color(val charNotation : Char, val redComponent:Float, val greenComponent:Float, val blueComponent:Float) {
    RED('R', 1.0f, 0.0f, 0.0f),
    ORANGE('O',0.9f, 0.4f, 0.0f),
    YELLOW('Y',0.9f, 0.9f, 0.2f),
    GREEN('G',0.0f, 1.0f, 0.0f),
    BLUE('B',0.2f, 0.2f, 1.0f),
    WHITE('W',1.0f, 1.0f, 1.0f),
    BLACK('K',0.0f, 0.0f, 0.0f);
}