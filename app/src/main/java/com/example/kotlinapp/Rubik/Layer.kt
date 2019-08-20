package com.example.kotlinapp.Rubik

import android.opengl.Matrix
import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Enums.Direction
import com.example.kotlinapp.Rubik.Enums.LayerEnum

class Layer {
    var mCube: Cube
    var cubies = arrayListOf<Cubie>()
    var cubiesIds = arrayListOf<Int>()

    var layerName: LayerEnum = LayerEnum.LEFT

    var direction : Direction

    //cubies will have such centers
    var centerPoint: Float = 0.0f

    constructor(centerPoint: Float, layerName: LayerEnum, direction: Direction, cube: Cube) {
        this.centerPoint = centerPoint
        this.layerName = layerName
        this.direction = direction
        mCube = cube
    }

    fun addCubie(cubie: Cubie) {
        cubies.add(cubie)
        cubiesIds.add(cubie.id)
    }

    fun verifyTiles(){
        for(cubie in mCube.cubies){
            if(cubiesIds.contains(cubie.id)){
                cubie.deactivateTiles(direction)
            }
        }
    }

    fun rotate(angle: Float) {
        mCube.permutationAllowed = false
        //check if it's possible to rotate layer on this rotationAxis
        for (cubie in mCube.cubies) {
            if (cubiesIds.contains(cubie.id)) {
                cubie.rotate(angle, LayerEnum.getRotationAxisByLayerName(layerName))
            }
        }
    }
}