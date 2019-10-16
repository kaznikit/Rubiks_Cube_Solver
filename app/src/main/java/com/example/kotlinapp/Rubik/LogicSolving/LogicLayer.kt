package com.example.kotlinapp.Rubik.LogicSolving

import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import com.example.kotlinapp.Rubik.Abstract.ILayerRotatedCallback
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Cubie
import java.lang.reflect.Array

class LogicLayer : ILayer {

    var mCube : LogicCube
    var cubies = arrayListOf<LogicCubie>()
    override var cubiesIds = arrayListOf<Int>()
    var rotatingCubies = arrayListOf<Int>()
    var rotatingCubiesCount = 0
    override var layerName: LayerEnum = LayerEnum.LEFT
    override var direction : Direction

    override var id: Int

    override var centerPoint: Float = 0.0f

    var onLayerRotatedCallback : ILayerRotatedCallback? = null

    val lockObj = Any()

    constructor(centerPoint: Float, layerName: LayerEnum, direction: Direction, cube: LogicCube, id : Int) {
        this.centerPoint = centerPoint
        this.layerName = layerName
        this.direction = direction
        mCube = cube
        this.id = id

        onLayerRotatedCallback = mCube
    }

    companion object{
        fun CloneLayer(layer : LogicLayer, logicCube: LogicCube) : LogicLayer {
            var cloneLayer = LogicLayer(layer.centerPoint, layer.layerName, Direction.CloneDirection(layer.direction), logicCube, layer.id)
            var cloneCubies = ArrayList<LogicCubie>()
            for(qb in layer.cubies){
                cloneCubies.add(LogicCubie.CloneCubie(qb))
            }
            cloneLayer.cubies = cloneCubies
            cloneLayer.cubiesIds = layer.cubiesIds.toList() as ArrayList<Int>
            return cloneLayer
        }
    }


    fun addCubie(cubie: LogicCubie) {
        cubies.add(cubie)
        cubiesIds.add(cubie.id)
        //cubie.setLayerCallback(this)
    }

    override fun rotate(angle: Float) {
        //check if it's possible to rotate layer on this rotationAxis
        for (cubie in mCube.cubies) {
            if (cubiesIds.contains(cubie.id)) {
                synchronized(lockObj) {
                    rotatingCubies.add(cubie.id)
                }
                cubie.setLayerCallback(this)
                // cubie.rotate(angle, LayerEnum.getRotationAxisByLayerName(layerName))
            }
        }
        synchronized(lockObj) {
            rotatingCubiesCount = rotatingCubies.size
        }
        for (qb in rotatingCubies) {
            mCube.cubies.single { x -> x.id == qb }
                .rotate(angle, LayerEnum.getRotationAxisByLayerName(layerName))
        }
    }

    override fun onCubieRotated(cubieId: Int) {
        synchronized(lockObj){
            rotatingCubiesCount--
        }
        if(rotatingCubiesCount == 0){
            synchronized(lockObj){
                rotatingCubies.clear()
            }
            onLayerRotatedCallback?.onLayerRotated(id)
        }
    }
}