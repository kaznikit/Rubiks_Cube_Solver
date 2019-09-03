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

        fun GetOppositeDirection(direction: Char) : Char? {
            when(direction){
                'F' -> {
                    return 'B'
                }
                'L' -> {
                    return 'R'
                }
                'B' -> {
                    return 'F'
                }
                'R' -> {
                    return 'L'
                }
                'U' -> {
                    return 'D'
                }
                'D' -> {
                    return 'U'
                }
            }
            return null
        }
    }

    fun changeColor(color: Color){
        this.color = color
    }
}