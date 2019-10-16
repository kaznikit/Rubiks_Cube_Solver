package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import com.example.kotlinapp.Rubik.Abstract.ILayerRotatedCallback
import com.example.kotlinapp.Rubik.LogicSolving.LogicCube
import com.example.kotlinapp.Rubik.LogicSolving.LogicCubie
import com.example.kotlinapp.Rubik.LogicSolving.LogicLayer
import java.util.Collections.synchronizedList

class Layer : ILayer{
    var mCube: Cube
    var cubies = arrayListOf<Cubie>()
    override var cubiesIds = arrayListOf<Int>()

    override var layerName: LayerEnum = LayerEnum.LEFT

    override var direction : Direction

    override var id = 0

    val lockObj = Any()
    var rotatingCubies = arrayListOf<Int>()
    var rotatingCubiesCount = 0

    private var onLayerRotatedCallback : ILayerRotatedCallback? = null

    //cubies will have such centers
    override var centerPoint: Float = 0.0f

    constructor(centerPoint: Float, layerName: LayerEnum, direction: Direction, cube: Cube, id : Int) {
        this.centerPoint = centerPoint
        this.layerName = layerName
        this.direction = direction
        mCube = cube
        this.id = id

        onLayerRotatedCallback = mCube
    }

    companion object{
        fun CloneLayer(layer : Layer, logicCube: LogicCube) : LogicLayer {
            var cloneLayer = LogicLayer(layer.centerPoint, layer.layerName, Direction.CloneDirection(layer.direction), logicCube, layer.id)
            var cloneCubies = ArrayList<LogicCubie>()
            for(qb in layer.cubies){
                cloneCubies.add(Cubie.CloneCubie(qb))
            }

            for(clQb in cloneCubies){
                clQb.setLayerCallback(cloneLayer)
            }
            cloneLayer.cubies = cloneCubies
            cloneLayer.cubiesIds = layer.cubiesIds.toList() as ArrayList<Int>
            //cloneLayer.onLayerRotatedCallback = layer.onLayerRotatedCallback
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

    /**
     * Highlight rotating cubies
     */
    fun turnCubiesGlowing(mode : Boolean){
        for (cubie in mCube.cubies) {
            if (cubiesIds.contains(cubie.id)) {
                cubie.isGlowing = mode
            }
        }
    }
}