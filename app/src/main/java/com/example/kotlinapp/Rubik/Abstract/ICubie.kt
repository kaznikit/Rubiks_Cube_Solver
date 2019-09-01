package com.example.kotlinapp.Rubik.Abstract

import android.opengl.Matrix
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Tile
import com.example.kotlinapp.Rubik.Vertex

interface ICubie {
    var currentRotation : Float
    var rotationAngle : Float
    var rotationX : Float
    var rotationY : Float
    var rotationZ : Float
    var isRotating : Boolean
    var isRotated : Boolean

    var centerPoint : Vertex

    val tiles : ArrayList<Tile>

    var id : Int

    var isCorner : Boolean
    var isEdge : Boolean

    fun rotate(angle: Float, rotationAxis: Axis)

    fun endAnimation()

    //check if cubie is on the right place on top layer
    fun isCubieRightOriented() : Boolean{
        for(tile in tiles){
            if(tile.isActive){
                if(tile.color == Color.WHITE && tile.direction == 'U'){
                    return true
                }
            }
        }
        return false
    }

    fun getNormalVectorAfterRotation(tile : Tile, rotationAngle : Float, direction: Char) : Char {
        var mat = FloatArray(16)
        Matrix.setIdentityM(mat, 0)

        var rotateVec = LayerEnum.getVectorByDirection(direction)//Axis.getRotationVector(tile.normalAxis)
        Matrix.rotateM(mat, 0, rotationAngle, -rotateVec[0], -rotateVec[1], -rotateVec[2])
        var normalVec = LayerEnum.getVectorByDirection(tile.direction)
        Matrix.multiplyMV(normalVec, 0, mat, 0, normalVec, 0)
        normalVec[0] = Vertex.RoundFloat(normalVec[0])
        normalVec[1] = Vertex.RoundFloat(normalVec[1])
        normalVec[2] = Vertex.RoundFloat(normalVec[2])
        return LayerEnum.getDirectionByVector(normalVec[0], normalVec[1], normalVec[2])
    }

    fun deactivateTiles(direction: Direction)
}