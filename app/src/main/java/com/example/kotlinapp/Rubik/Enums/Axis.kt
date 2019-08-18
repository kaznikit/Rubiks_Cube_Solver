package com.example.kotlinapp.Rubik.Enums

enum class Axis(val x : Float, val y : Float, val z : Float) {
    xAxis(1f, 0f, 0f),
    xMinusAxis(-1f, 0f, 0f),
    yAxis(0f, 1f, 0f),
    yMinusAxis(0f, -1f, 0f),
    zAxis(0f, 0f, 1f),
    zMinusAxis(0f, 0f, -1f);

    companion object {
        fun getRotationVector(axis: Axis): FloatArray {
            val ax = FloatArray(4)
            if (axis === xAxis) {
                ax[0] = 1f
                ax[1] = 0f
                ax[2] = 0f
            } else if (axis === yAxis) {
                ax[0] = 0f
                ax[1] = 1f
                ax[2] = 0f
            } else if (axis === zAxis) {
                ax[0] = 0f
                ax[1] = 0f
                ax[2] = 1f
            } else if (axis === xMinusAxis) {
                ax[0] = -1f
                ax[1] = 0f
                ax[2] = 0f
            } else if (axis === yMinusAxis) {
                ax[0] = 0f
                ax[1] = -1f
                ax[2] = 0f
            } else if (axis === zMinusAxis) {
                ax[0] = 0f
                ax[1] = 0f
                ax[2] = -1f
            }
            return ax
        }

        fun getAxis(x : Float, y : Float, z : Float) : Axis{
            if(x == 1f && y == 0f && z == 0f){
                return xAxis
            }
            else if(x == -1f && y == 0f && z == 0f){
                return xMinusAxis
            }
            else if(x == 0f && y == 1f && z == 0f){
                return yAxis
            }
            else if(x == 0f && y == -1f && z == 0f){
                return yMinusAxis
            }
            else if(x == 0f && y == 0f && z == 1f){
                return zAxis
            }
            else if(x == 0f && y == 0f && z == -1f){
                return zMinusAxis
            }
            return zAxis
        }
    }
}