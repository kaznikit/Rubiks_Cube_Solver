package com.example.kotlinapp.Util

class Constants {
    enum class Color(r : Int, g : Int, b : Int){
        RED (0x10000, 0, 0),
        GREEN (0, 0x10000, 0),
        BLUE (0, 0, 0x10000),
        YELLOW (0x10000, 0x10000, 0),
        ORANGE (0x10000, 0x08000, 0),
        WHITE (0x10000, 0x10000, 0x10000),
        BLACK (0, 0, 0)
    }
}