package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Recognition.Rectangle

class Tile {
    var isActive : Boolean = false

    var coordinates : Array<Vertex> = arrayOf()

    var color : Color
    var direction : Char
    var normalAxis : Axis

    constructor(coordinates : Array<Vertex>, color : Color, direction: Char, normalAxis : Axis){
        this.coordinates = coordinates
        this.color = color
        this.direction = direction
        this.normalAxis = normalAxis
    }

    fun setTileColor(color: Color){
        this.color = color
    }
}