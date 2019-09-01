package com.example.kotlinapp.Rubik.LogicSolving

import android.opengl.Matrix
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Tile
import com.example.kotlinapp.Rubik.Vertex

class LogicCubie : ICubie {
    override var isCorner = false
    override var isEdge = false

    override var currentRotation = 0.0f
    override var rotationAngle = 0.0f
    override var rotationX = 0.0f
    override var rotationY = 0.0f
    override var rotationZ = 0.0f
    override var isRotating = false
    override var isRotated = false
    override var centerPoint : Vertex
    override var tiles = arrayListOf<Tile>()
    override var id = 0

    constructor(centerPoint : Vertex, id : Int){
        this.centerPoint = centerPoint
        this.id = id
    }

    override fun rotate(angle: Float, rotationAxis: Axis) {
        rotationAngle = angle

        var vec = Axis.getRotationVector(rotationAxis)
        rotationX = vec[0]
        rotationY = vec[1]
        rotationZ = vec[2]

        while (rotationAngle >= 360f) rotationAngle -= 360.0f
        endAnimation()
    }

    override fun endAnimation() {
        //stop the rotation
        isRotating = false

        //calculate new center point
        var mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)
        var arr = FloatArray(4)
        arr[0] = centerPoint.x
        arr[1] = centerPoint.y
        arr[2] = centerPoint.z
        arr[3] = 0.0f
        Matrix.rotateM(mat, 0, rotationAngle, -rotationX, -rotationY, -rotationZ)
        Matrix.multiplyMV(arr, 0, mat, 0, arr, 0)

        centerPoint.x = Vertex.RoundFloat(arr[0])
        centerPoint.y = Vertex.RoundFloat(arr[1])
        centerPoint.z = Vertex.RoundFloat(arr[2])

        for(tile in tiles){
            if(tile.isActive){
                var normalVec = Axis.getRotationVector(tile.normalAxis)
                Matrix.multiplyMV(normalVec, 0, mat, 0, normalVec, 0)
                normalVec[0] = Vertex.RoundFloat(normalVec[0])
                normalVec[1] = Vertex.RoundFloat(normalVec[1])
                normalVec[2] = Vertex.RoundFloat(normalVec[2])

                tile.normalAxis = Axis.getAxis(normalVec[0], normalVec[1], normalVec[2])
                tile.direction = LayerEnum.getDirectionByVector(normalVec[0], normalVec[1], normalVec[2])
            }
        }
        isRotated = true
    }

    override fun deactivateTiles(direction: Direction) {}
}