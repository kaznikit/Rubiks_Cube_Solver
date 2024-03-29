package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Axis

class Vertex (x : Float, y : Float, z : Float){
    var x : Float = RoundFloat(x)
    var y : Float = RoundFloat(y)
    var z : Float = RoundFloat(z)

    companion object {
        fun RoundFloat(num: Float): Float {
            var s = Math.round(num * 100.0f) / 100.0f
            return s
        }

        fun CloneVertex(vertex : Vertex) : Vertex{
            return Vertex(vertex.x, vertex.y, vertex.z)
        }
    }

    //return float value match to the axis
    fun getCoordinateByAxis(axis : Axis):Float{
        if(axis == Axis.xAxis || axis == Axis.xMinusAxis){
            return x
        }
        else if(axis == Axis.yAxis || axis == Axis.yMinusAxis){
            return y
        }
        else{
            return z
        }
    }

    fun isEqual(vertex1: Vertex, vertex2: Vertex): Boolean {
        return vertex1.x == vertex2.x && vertex1.y == vertex2.y && vertex1.z == vertex2.z
    }
}