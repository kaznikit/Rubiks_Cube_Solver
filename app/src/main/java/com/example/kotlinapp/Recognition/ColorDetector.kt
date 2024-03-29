package com.example.kotlinapp.Recognition

import android.util.Log
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Util.Tool
import org.opencv.core.Core
import org.opencv.core.CvException
import org.opencv.core.Mat
import java.lang.Exception

class ColorDetector {
    private var tempTiles: Array<Array<RubikTile?>>

    constructor() {
        tempTiles = Array(3) { arrayOfNulls<RubikTile>(3) }
        for (i in 0..2) {
            for (j in 0..2) {
                tempTiles[i][j] = RubikTile(null, Color.BLACK)
            }
        }
    }

    fun faceTileColorRecognition(image: Mat, rubikFace: RubikFace): Array<Array<RubikTile?>> {
        try {
            // Obtain actual measured tile color from image.
            for (n in 0..2) {
                for (m in 0..2) {
                    val tileCenter = rubikFace.getTileCenterInPixels(n, m)
                    val size = image.size()
                    val width = size.width
                    val height = size.height

                    if (tileCenter.x < 10 || tileCenter.x > width - 10 || tileCenter.y < 10 || tileCenter.y > height - 10) {
                        rubikFace.measuredColorArray[n][m] =
                            DoubleArray(4)  // This will default to back.
                    } else {
                        try {
                            val mat = image.submat(
                                (tileCenter.y - 10).toInt(),
                                (tileCenter.y + 10).toInt(),
                                (tileCenter.x - 10).toInt(),
                                (tileCenter.x + 10).toInt()
                            )
                            rubikFace.measuredColorArray[n][m] = Core.mean(mat).`val`

                        } catch (cvException: CvException) {
                            rubikFace.measuredColorArray[n][m] = DoubleArray(4)
                        }
                    }
                }
            }

            //Find closest logical color using only UV axis.
            var k = 0
            for (n in 0..2) {
                for (m in 0..2) {
                    //if (rubikFace.observedTileArray[n][m].ColorError > 20.0) {
                    val measuredColor = rubikFace.measuredColorArray[n][m]
                    val measuredColorYUV = Tool.getYUVfromRGB(measuredColor)
                    val bestCandidate = Color.BLACK
                    var smallestError = java.lang.Double.MAX_VALUE

                    for (candidateColorTile in Color.values()) {
                        if (candidateColorTile.isRubikColor) {
                            var error: Double

                            //если прошла калибровка, берем эти значения
                            if (candidateColorTile.hsvCalibrateValue != null) {
                                val r =
                                    (measuredColor[0] + candidateColorTile.hsvCalibrateValue!!.`val`[0]) / 2.0
                                error = ((2 + r / 256.0) * Math.pow(
                                    measuredColor[0] -
                                            candidateColorTile.hsvCalibrateValue!!.`val`[0], 2.0
                                ) +
                                        4 * Math.pow(
                                    measuredColor[1] - candidateColorTile.hsvCalibrateValue!!.`val`[1],
                                    2.0
                                ) +
                                        (2 + (255 - r) / 256.0) *
                                        Math.pow(
                                            measuredColor[2] - candidateColorTile.hsvCalibrateValue!!.`val`[2],
                                            2.0
                                        ))
                                error = Math.sqrt(error)
                            } else {
                                // Only examine U and V axis, and not luminous.
                                var downError =
                                    (measuredColor[0] - candidateColorTile.hsvMinValue.`val`[0]) * (measuredColor[0] - candidateColorTile.hsvMinValue.`val`[0]) +
                                            (measuredColor[1] - candidateColorTile.hsvMinValue.`val`[1]) * (measuredColor[1] - candidateColorTile.hsvMinValue.`val`[1]) +
                                            (measuredColor[2] - candidateColorTile.hsvMinValue.`val`[2]) * (measuredColor[2] - candidateColorTile.hsvMinValue.`val`[2])
                                downError = Math.sqrt(downError)
                                var upError =
                                    (measuredColor[0] - candidateColorTile.hsvMaxValue.`val`[0]) * (measuredColor[0] - candidateColorTile.hsvMaxValue.`val`[0]) +
                                            (measuredColor[1] - candidateColorTile.hsvMaxValue.`val`[1]) * (measuredColor[1] - candidateColorTile.hsvMaxValue.`val`[1]) +
                                            (measuredColor[2] - candidateColorTile.hsvMaxValue.`val`[2]) * (measuredColor[2] - candidateColorTile.hsvMaxValue.`val`[2])
                                upError = Math.sqrt(upError)

                                if (downError < upError) {
                                    error = Math.abs(downError)
                                } else {
                                    error = Math.abs(upError)
                                }
                            }

                            if (error < smallestError) {
                                tempTiles[n][m]!!.tileColor = candidateColorTile
                                if(rubikFace.ColorDetectionCount > 14){
                                    rubikFace.ColorDetectionCount--
                                }
                                rubikFace.averageColorArray[rubikFace.ColorDetectionCount][k] =
                                    candidateColorTile
                                smallestError = error

                                candidateColorTile.medianError =
                                    (candidateColorTile.medianError + error) / 2
                            }
                        }
                    }
                    k++
                }
            }
            if (rubikFace.faceRecognitionStatus != RubikFace.FaceRecognitionStatusEnum.SOLVED) {
                rubikFace.ColorDetectionCount++
            }
        }
        catch (ex : Exception){
            Log.e("ColorDetector", ex.message)
        }
        return tempTiles
    }
}