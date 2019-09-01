package com.example.kotlinapp.Rubik.Abstract

import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum

interface ILayer {
    var cubiesIds : ArrayList<Int>

    var layerName: LayerEnum

    var direction : Direction

    var id : Int

    //cubies will have such centers
    var centerPoint: Float

    fun rotate(angle: Float)
}