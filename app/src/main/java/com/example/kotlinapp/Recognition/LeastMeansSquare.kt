package com.example.kotlinapp.Recognition

import org.opencv.core.Point

class LeastMeansSquare {
    lateinit var origin: Point

    // =+= Migrate this to Lattice ?
    var alphaLattice: Double = 0.toDouble()

    //
    var errorVectorArray: Array<Array<Point?>>?

    // Sum of all errors (RMS)
    var sigma: Double = 0.toDouble()

    // True if results are mathematically valid.
    var valid: Boolean = false

    constructor(x: Double, y: Double, alphaLatice: Double, errorVectorArray: Array<Array<Point?>>?, sigma: Double, valid: Boolean)
    {
        this.origin = Point(x, y)
        this.alphaLattice = alphaLatice
        this.errorVectorArray = errorVectorArray
        this.sigma = sigma
        this.valid = valid
    }
}