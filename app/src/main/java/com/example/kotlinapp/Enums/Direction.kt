package com.example.kotlinapp.Enums

class Direction {

    var charName: Char
    var color: Color

    constructor(charName: Char, faceColor: Color) {
        this.charName = charName
        this.color = faceColor
    }

    companion object{
        fun CloneDirection(direction: Direction) : Direction{
            return Direction(direction.charName, direction.color)
        }
    }

    fun changeColor(color: Color){
        this.color = color
    }
}