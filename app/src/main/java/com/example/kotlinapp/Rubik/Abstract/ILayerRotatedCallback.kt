package com.example.kotlinapp.Rubik.Abstract

/**
 * Interface for notifying the cube, that layer has rotated
 */
interface ILayerRotatedCallback {
    fun onLayerRotated(layerId : Int)
}