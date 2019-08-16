package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Rubik.Enums.Color
import com.example.kotlinapp.Rubik.Enums.Direction

class Tile {
    var isActive : Boolean = false;

    var coordinates : Array<Vertex> = arrayOf<Vertex>()

    var color : Color
    var direction : Direction

    constructor(coordinates : Array<Vertex>, color : Color, direction: Direction){
        this.coordinates = coordinates
        this.color = color
        this.direction = direction
    }
}