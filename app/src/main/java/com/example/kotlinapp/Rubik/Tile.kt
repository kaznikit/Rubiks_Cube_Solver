package com.example.kotlinapp.Rubik

class Tile {
    val isActive : Boolean = false;

    var coordinates : Array<Vertex> = arrayOf<Vertex>()

    constructor(coordinates : Array<Vertex>){
        this.coordinates = coordinates
    }
}