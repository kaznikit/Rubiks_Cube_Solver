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
    var errorColorDetection : Double = 0.0

    constructor(coordinates : Array<Vertex>, color : Color, direction: Char, normalAxis : Axis){
        this.coordinates = coordinates
        this.color = color
        this.direction = direction
        this.normalAxis = normalAxis
    }

    companion object{
        fun CloneTile(tile : Tile) : Tile{
            var coord = tile.coordinates
            for(i in 0 until 4){
                coord[i] = Vertex.CloneVertex(tile.coordinates[i])
            }
            var cloneTile = Tile(coord, tile.color, tile.direction, tile.normalAxis)
            cloneTile.isActive = tile.isActive
            return cloneTile
        }
    }

    fun setTileColor(color: Color){
        this.color = color
    }

    fun setColorError(error : Double){
        this.errorColorDetection = error
    }

}