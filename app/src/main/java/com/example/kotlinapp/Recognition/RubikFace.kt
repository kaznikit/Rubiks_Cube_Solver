package com.example.kotlinapp.Recognition

import com.example.kotlinapp.CurrentState
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Rubik.Tile
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point

class RubikFace {
    var layerName : LayerEnum

    enum class FaceRecognitionStatusEnum {
        UNKNOWN,
        SOLVED
    }

    var faceRecognitionStatus = FaceRecognitionStatusEnum.UNKNOWN


    var ColorDetectionCount = 0
    var averageColorArray = Array(15) { arrayOfNulls<Color>(9) }

    var observedTileArray = Array(3) { arrayOfNulls<RubikTile>(3) }

    var transformedTileArray = Array(3) { arrayOfNulls<RubikTile>(3) }

    var measuredColorArray = Array(3) { Array(3) { DoubleArray(4) } }

    // Least Means Square Result
    var lmsResult = LeastMeansSquare(800.0, 200.0, 50.0, null, 314.0, true)

    //region Calculation variables

    // Angle of Alpha-Axis (N) stored in radians.
    var alphaAngle = 0.0

    // Angle of Beta-Axis (M) stored in radians.
    var betaAngle = 0.0

    // Length in pixels of Alpha Lattice (i.e. a tile size)
    var alphaLatticLength = 0.0

    // Length in pixels of Beta Lattice (i.e. a tile size)
    var betaLatticLength = 0.0

    // Ratio of Beta Lattice to Alpha Lattice
    var gammaRatio = 0.0

    // Number of rhombus that were moved in order to obtain better LMS fit.
    var numRhombusMoves = 0

    var isLayerFilled = false

    //endregion

    var currentState : CurrentState

    var isObservedFilled = false

    fun createObservedTilesArray() {
        for (i in 0..2) {
            for (j in 0..2) {
                observedTileArray[i][j] =
                    RubikTile(null, Color.BLACK)
                transformedTileArray[i][j] =
                    RubikTile(null, Color.BLACK)
                isObservedFilled = true
            }
        }
    }

    constructor(faceName : LayerEnum, currentState: CurrentState){
        this.layerName = faceName
        this.currentState = currentState
    }

    fun calculateTiles(rectanglesList: List<Rectangle>, image: Mat) : Array<Array<RubikTile?>>? {
        if (rectanglesList.size < 5){//7) {
            return null
        }
        if (calculateMetrics(rectanglesList) === false) {
            return null
        }
        if (isObservedFilled) {
            if (RectanglesLayout.MakeLayout(rectanglesList, observedTileArray, alphaAngle, betaAngle) === false)
            {
                return null
            }
        }
        lmsResult = findOptimumFaceFit()
        if (lmsResult.valid === false) {
            return null
        }
        alphaLatticLength = lmsResult.alphaLattice
        betaLatticLength = gammaRatio * lmsResult.alphaLattice
        val lastSigma = lmsResult.sigma

        // Loop until some resolution
        while (lmsResult.sigma > 35) {
            if (numRhombusMoves > 5) {
                return null
            }
            if (findAndMoveRectangleToBetterLocation() === false) {
                return null
            }
            numRhombusMoves++
            lmsResult = findOptimumFaceFit()
            if (lmsResult.valid === false) {
                return null
            }
            alphaLatticLength = lmsResult.alphaLattice
            betaLatticLength = gammaRatio * lmsResult.alphaLattice
        }

        //определение цветов
        return currentState.colorDetector!!.faceTileColorRecognition(image, currentState.activeRubikFace)
    }

    private fun calculateMetrics(rectanglesList: List<Rectangle>): Boolean {
        val numElements = rectanglesList.size
        for (rectangle in rectanglesList) {
            alphaAngle += rectangle.alphaAngle
            betaAngle += rectangle.betaAngle
            gammaRatio += rectangle.gammaRatio
        }
        alphaAngle = alphaAngle / numElements * Math.PI / 180.0
        betaAngle = betaAngle / numElements * Math.PI / 180.0
        gammaRatio = gammaRatio / numElements
        return true
    }

    //check if user rotated cube (true is ok)
    fun checkPreviousTileColors(tiles : Array<Array<RubikTile>>): Boolean {
        if (tiles.isEmpty()) {
            return false
        }
        if (currentState.adoptFaceCount !== 0 && tiles != null) {
            //how many tiles have the same colors
            for (face in currentState.rubikFaces) {
                var coincide = 0
                for (i in 0..2) {
                    for (j in 0..2) {
                        if (tiles[i][j].tileColor == Color.BLACK || face == null || face!!.transformedTileArray == null) {
                            continue
                        }
                        if (face!!.transformedTileArray[i][j]?.tileColor === tiles[i][j].tileColor) {
                            coincide++
                        }
                    }
                }
                if (coincide > 5) {
                    return false
                }
            }
        }
        return true
    }

    private fun findOptimumFaceFit(): LeastMeansSquare {
        var k = 0
        for (n in 0..2)
            for (m in 0..2)
                if (observedTileArray[n][m]!!.rectangle != null)
                    k++

        val bigAmatrix = Mat(2 * k, 3, CvType.CV_64FC1)
        val bigYmatrix = Mat(2 * k, 1, CvType.CV_64FC1)
        val bigXmatrix = Mat(3, 1, CvType.CV_64FC1)

        var index = 0
        for (n in 0..2) {
            for (m in 0..2) {
                val rectangle = observedTileArray[n][m]!!.rectangle
                if (rectangle != null) {

                    run {
                        // Actual X axis value of Rhombus in this location
                        val bigY = rectangle.center.x

                        // Express expected X axis value : i.e. x = func( x_origin, n, m, alpha, beta, alphaLattice, gamma)
                        val bigA = n * Math.cos(alphaAngle) + gammaRatio * m.toDouble() * Math.cos(
                            betaAngle
                        )

                        bigYmatrix.put(index, 0, *doubleArrayOf(bigY))

                        bigAmatrix.put(index, 0, *doubleArrayOf(1.0))
                        bigAmatrix.put(index, 1, *doubleArrayOf(0.0))
                        bigAmatrix.put(index, 2, *doubleArrayOf(bigA))

                        index++
                    }


                    run {
                        // Actual Y axis value of Rhombus in this location
                        val bigY = rectangle.center.y

                        // Express expected Y axis value : i.e. y = func( y_origin, n, m, alpha, beta, alphaLattice, gamma)
                        val bigA = n * Math.sin(alphaAngle) + gammaRatio * m.toDouble() * Math.sin(
                            betaAngle
                        )

                        bigYmatrix.put(index, 0, *doubleArrayOf(bigY))

                        bigAmatrix.put(index, 0, *doubleArrayOf(0.0))
                        bigAmatrix.put(index, 1, *doubleArrayOf(1.0))
                        bigAmatrix.put(index, 2, *doubleArrayOf(bigA))

                        index++
                    }
                }
            }
        }
        val solveFlag = Core.solve(bigAmatrix, bigYmatrix, bigXmatrix, Core.DECOMP_NORMAL)

        val bigEmatrix = Mat(2 * k, 1, CvType.CV_64FC1)
        for (r in 0 until 2 * k) {
            val y = bigYmatrix.get(r, 0)[0]
            var error = y
            for (c in 0..2) {
                val a = bigAmatrix.get(r, c)[0]
                val x = bigXmatrix.get(c, 0)[0]
                error -= a * x
            }
            bigEmatrix.put(r, 0, error)
        }

        var sigma = 0.0
        for (r in 0 until 2 * k) {
            val error = bigEmatrix.get(r, 0)[0]
            sigma += error * error
        }
        sigma = Math.sqrt(sigma)

        val errorVectorArray = Array(3) { arrayOfNulls<Point>(3) }
        index = 0
        for (n in 0..2) {
            for (m in 0..2) {
                val rectangle =
                    observedTileArray[n][m]!!.rectangle  // We expect this array to not have change from above.
                if (rectangle != null) {
                    errorVectorArray[n][m] = Point(
                        bigEmatrix.get(index++, 0)[0],
                        bigEmatrix.get(index++, 0)[0]
                    )
                }
            }
        }

        val x = bigXmatrix.get(0, 0)[0]
        val y = bigXmatrix.get(1, 0)[0]
        val alphaLatice = bigXmatrix.get(2, 0)[0]
        val valid = !java.lang.Double.isNaN(x) && !java.lang.Double.isNaN(y)
                && !java.lang.Double.isNaN(alphaLatice) && !java.lang.Double.isNaN(sigma)
        return LeastMeansSquare(x, y, alphaLatice, errorVectorArray, sigma, valid)
    }

    private fun findAndMoveRectangleToBetterLocation(): Boolean {
        val errorArray = Array(3) { DoubleArray(3) }
        val errorVectorArray = Array(3) { arrayOfNulls<Point>(3) }
        // Identify Tile-Rectangle with largest error
        var largestErrorRhombus: Rectangle? = null
        var largetError = java.lang.Double.NEGATIVE_INFINITY
        var tile_n = 0  // Record current location of rectangle we wish to move.
        var tile_m = 0
        for (n in 0..2) {
            for (m in 0..2) {
                val rectangle = observedTileArray[n][m]!!.rectangle
                if (rectangle != null) {

                    // X and Y location of the center of a tile {n,m}
                    val tile_x =
                        lmsResult.origin.x + n.toDouble() * alphaLatticLength * Math.cos(alphaAngle) + m.toDouble() * betaLatticLength * Math.cos(
                            betaAngle
                        )
                    val tile_y =
                        lmsResult.origin.y + n.toDouble() * alphaLatticLength * Math.sin(alphaAngle) + m.toDouble() * betaLatticLength * Math.sin(
                            betaAngle
                        )

                    // Error from center of tile to reported center of rectangle
                    val error = Math.sqrt(
                        (rectangle.center.x - tile_x) * (rectangle.center.x - tile_x) + (rectangle.center.y - tile_y) * (rectangle.center.y - tile_y)
                    )
                    errorArray[n][m] = error
                    errorVectorArray[n][m] =
                        Point(rectangle.center.x - tile_x, rectangle.center.y - tile_y)

                    // Record largest error found
                    if (error > largetError) {
                        largestErrorRhombus = rectangle
                        tile_n = n
                        tile_m = m
                        largetError = error
                    }
                }
            }
        }

        // Calculate vector error (from Tile to rectangle) components along X and Y axis
        val error_x =
            largestErrorRhombus!!.center.x - (lmsResult.origin.x + tile_n.toDouble() * alphaLatticLength * Math.cos(
                alphaAngle
            ) + tile_m.toDouble() * betaLatticLength * Math.cos(betaAngle))
        val error_y = largestErrorRhombus.center.y - (lmsResult.origin.y +
                tile_n.toDouble() * alphaLatticLength * Math.sin(alphaAngle) +
                tile_m.toDouble() * betaLatticLength * Math.sin(betaAngle))

        // Project vector error (from Tile to Rhombus) components along alpha and beta directions.
        val alphaError = error_x * Math.cos(alphaAngle) + error_y * Math.sin(alphaAngle)
        val betaError = error_x * Math.cos(betaAngle) + error_y * Math.sin(betaAngle)

        // Calculate index vector correction: i.e., preferred direction to move this tile.
        val delta_n = Math.round(alphaError / alphaLatticLength).toInt()
        val delta_m = Math.round(betaError / betaLatticLength).toInt()

        // Calculate new location of tile
        var new_n = tile_n + delta_n
        var new_m = tile_m + delta_m

        // Limit according to dimensions of face
        if (new_n < 0) new_n = 0
        if (new_n > 2) new_n = 2
        if (new_m < 0) new_m = 0
        if (new_m > 2) new_m = 2

        // Cannot move, move is to original location
        if (new_n == tile_n && new_m == tile_m) {
            return false
        } else {
            val tmp = observedTileArray[new_n][new_m]!!.rectangle
            observedTileArray[new_n][new_m]!!.rectangle = observedTileArray[tile_n][tile_m]!!.rectangle
            observedTileArray[tile_n][tile_m]!!.rectangle = tmp
            return true
        }
    }

    fun getTileCenterInPixels(n: Int, m: Int): Point {
        return Point(
            lmsResult.origin.x + n.toDouble() * alphaLatticLength * Math.cos(alphaAngle) + m.toDouble() * betaLatticLength * Math.cos(
                betaAngle
            ),
            lmsResult.origin.y + n.toDouble() * alphaLatticLength * Math.sin(alphaAngle) + m.toDouble() * betaLatticLength * Math.sin(
                betaAngle
            )
        )
    }
}