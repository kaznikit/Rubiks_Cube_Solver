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
                cubie.deactiveTiles(direction)
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

    //recalculate direction, center point of layer
    fun rotateLayer(angle: Float, rotationAxis: Axis) {
        var mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        var arr = FloatArray(4)
        arr[0] = 0f
        arr[1] = 0f
        arr[2] = 0f
        arr[3] = 0.0f
        if (layerName.rotationAxis == Axis.xAxis || layerName.rotationAxis == Axis.xMinusAxis) {
            arr[0] = centerPoint
        } else if (layerName.rotationAxis == Axis.yAxis || layerName.rotationAxis == Axis.yMinusAxis) {
            arr[1] = centerPoint
        } else {
            arr[2] = centerPoint
        }
        Matrix.rotateM(mat, 0, angle, rotationAxis.x, rotationAxis.y, rotationAxis.z)
        Matrix.multiplyMV(arr, 0, mat, 0, arr, 0)

        if (layerName.rotationAxis == Axis.xAxis || layerName.rotationAxis == Axis.xMinusAxis) {
            centerPoint = Vertex.RoundFloat(arr[0])
        } else if (layerName.rotationAxis == Axis.yAxis || layerName.rotationAxis == Axis.yMinusAxis) {
            centerPoint = Vertex.RoundFloat(arr[1])
        } else {
            centerPoint = Vertex.RoundFloat(arr[2])
        }

        var normalVec = Axis.getRotationVector(layerName.rotationAxis)
        Matrix.multiplyMV(normalVec, 0, mat, 0, normalVec, 0)
        normalVec[0] = Vertex.RoundFloat(normalVec[0])
        normalVec[1] = Vertex.RoundFloat(normalVec[1])
        normalVec[2] = Vertex.RoundFloat(normalVec[2])
    }
}