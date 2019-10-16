package com.example.kotlinapp.Rubik.Abstract

/**
 * Interface for notifying a layer, that cubie has rotated
 */
interface ICubieRotatedCallback {
    fun onCubieRotated(cubieId : Int)
}