package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import com.example.kotlinapp.Rubik.LogicSolving.LogicCube
import com.example.kotlinapp.Rubik.LogicSolving.LogicCubie
import com.example.kotlinapp.Rubik.LogicSolving.LogicLayer

class Layer : ILayer{
    var mCube: Cube
    var cubies = arrayListOf<Cubie>()
    override var cubiesIds = arrayListOf<Int>()

    override var layerName: LayerEnum = LayerEnum.LEFT

    override var direction : Direction

    override var id = 0

    //cubies will have such centers
    override var centerPoint: Float = 0.0f

    constructor(centerPoint: Float, layerName: LayerEnum, direction: Direction, cube: Cube, id : Int) {
        this.centerPoint = centerPoint
        this.layerName = layerName
        this.direction = direction
        mCube = cube
        this.id = id
    }

    companion object{
        fun CloneLayer(layer : Layer, logicCube: LogicCube) : LogicLayer {
            var cloneLayer = LogicLayer(layer.centerPoint, layer.layerName, Direction.CloneDirection(layer.direction), logicCube, layer.id)
            var cloneCubies = ArrayList<LogicCubie>()
            for(qb in layer.cubies){
                cloneCubies.add(Cubie.CloneCubie(qb))
            }
            cloneLayer.cubies = cloneCubies
            cloneLayer.cubiesIds = layer.cubiesIds.toList() as ArrayList<Int>
            return cloneLayer
        }
    }

    fun verifyTiles(){
        for(cubie in mCube.cubies){
            if(cubiesIds.contains(cubie.id)){
                cubie.deactivateTiles(direction)
            }
        }
    }

    fun addCubie(cubie: Cubie) {
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

    /**
     * Highlight rotating cubies
     */
    fun turnOnCubieLight(){

    }
}