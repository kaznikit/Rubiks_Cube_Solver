package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Util.Constants
import java.nio.IntBuffer

class Vertex (x : Float, y : Float, z : Float){
    val x : Float = x
    val y : Float = y
    val z : Float = z

    lateinit var color : Constants.Color

    fun IsEqual(vertex1: Vertex, vertex2: Vertex): Boolean {
        return vertex1.x == vertex2.x && vertex1.y == vertex2.y && vertex1.z == vertex2.z
    }
}