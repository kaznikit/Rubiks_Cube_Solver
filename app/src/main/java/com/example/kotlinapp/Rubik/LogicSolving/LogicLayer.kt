package com.example.kotlinapp.Rubik.LogicSolving

import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Cubie

class LogicLayer : ILayer {
    var mCube : LogicCube
    var cubies = arrayListOf<LogicCubie>()
    override var cubiesIds = arrayListOf<Int>()
    override var layerName: LayerEnum = LayerEnum.LEFT
    override var direction : Direction

    override var id: Int

    override var centerPoint: Float = 0.0f


    constructor(centerPoint: Float, layerName: LayerEnum, direction: Direction, cube: LogicCube, id : Int) {
        this.centerPoint = centerPoint
        this.layerName = layerName
        this.direction = direction
        mCube = cube
        this.id = id
    }

    fun addCubie(cubie: LogicCubie) {
        cubies.add(cubie)
        cubiesIds.add(cubie.id)
    }

    override fun rotate(angle: Float) {
        //check if it's possible to rotate layer on this rotationAxis
        for (cubie in mCube.cubies) {
            if (cubiesIds.contains(cubie.id)) {
                cubie.rotate(angle, LayerEnum.getRotationAxisByLayerName(layerName))
            }
        }
    }
}