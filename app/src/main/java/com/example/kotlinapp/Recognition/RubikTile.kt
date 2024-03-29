package com.example.kotlinapp.Recognition

import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Util.Constants

class RubikTile {
    var rectangle: Rectangle?
    var tileColor : Color
    var tileState = Constants.TileState.UNSTABLE
    var errorColorDetection = 0.0

    constructor(rectangle: Rectangle?, tileColor: Color){
        this.rectangle = rectangle
        this.tileColor = tileColor
    }
}