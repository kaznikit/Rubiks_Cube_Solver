package com.example.kotlinapp.Recognition

import com.example.kotlinapp.Enums.Color

class RubikTile {
    var rectangle: Rectangle?
    var tileColor : Color

    constructor(rectangle: Rectangle?, tileColor: Color){
        this.rectangle = rectangle
        this.tileColor = tileColor
    }
}