package com.example.kotlinapp.Rubik.Enums

import com.example.kotlinapp.Rubik.Layer

class Direction {

    var charName: Char
    var color: Color

    constructor(charName: Char, faceColor: Color) {
        this.charName = charName
        this.color = faceColor
    }

    fun changeColor(color: Color){
        this.color = color
    }

    fun changeCharName(charName: Char){
        this.charName = charName
    }

    fun getDirectionByColor(color: Color): Char {
        return charName
    }

    fun changeColorAfterRotation(rotationAxis: Axis) {

    }
}