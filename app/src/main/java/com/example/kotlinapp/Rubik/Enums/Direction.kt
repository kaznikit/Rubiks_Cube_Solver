package com.example.kotlinapp.Rubik.Enums

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
}