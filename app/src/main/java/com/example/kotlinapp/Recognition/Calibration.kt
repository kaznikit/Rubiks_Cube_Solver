package com.example.kotlinapp.Recognition

import android.os.Build
import android.support.annotation.RequiresApi
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Util.Constants
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import com.example.kotlinapp.Enums.Color
import org.opencv.core.Point

class Calibration(internal var mainActivity: MainActivity) {
    internal lateinit var image: Mat

    //go through all colors
    internal var colorNumber = 0

    fun getColor(image: Mat): Scalar? {
        this.image = image
        when (colorNumber) {
            0 -> Imgproc.putText(
                image,
                "Tap on the red color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            1 -> Imgproc.putText(
                image,
                "Tap on the green color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            2 -> Imgproc.putText(
                image,
                "Tap on the orange color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            3 -> Imgproc.putText(
                image,
                "Tap on the yellow color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            4 -> Imgproc.putText(
                image,
                "Tap on the white color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            5 -> Imgproc.putText(
                image,
                "Tap on the blue color on the cube",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
            else -> Imgproc.putText(
                image,
                "Calibration finished! Tap to continue.",
                Point(50.0, 100.0),
                Constants.FontFace,
                5.0,
                Color.WHITE.cvColor,
                4
            )
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getColorByCoordinates(tileCenter: Point) {
        var measuredColorArray = DoubleArray(4)
        val mat = image.submat(
            (tileCenter.y - 10).toInt(),
            (tileCenter.y + 10).toInt(),
            (tileCenter.x - 10).toInt(),
            (tileCenter.x + 10).toInt()
        )
        measuredColorArray = Core.mean(mat).`val`
        val color =
            android.graphics.Color.rgb(measuredColorArray[0].toFloat(), measuredColorArray[1].toFloat(), measuredColorArray[2].toFloat())
        when (colorNumber) {
            0 -> {
                Color.RED.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                    mainActivity.calibrateColorButton.setBackgroundColor(color)
                    mainActivity.SaveSharedPreferences("Red", Color.RED.hsvCalibrateValue!!)
                    colorNumber++
            }
            1 -> {
                Color.GREEN.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Green", Color.GREEN.hsvCalibrateValue!!)
                colorNumber++
            }
            2 -> {
                Color.ORANGE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Orange", Color.ORANGE.hsvCalibrateValue!!)
                colorNumber++
            }
            3 -> {
                Color.YELLOW.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Yellow", Color.YELLOW.hsvCalibrateValue!!)
                colorNumber++
            }
            4 -> {
                Color.WHITE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("White", Color.WHITE.hsvCalibrateValue!!)
                colorNumber++
            }
            5 -> {
                Color.BLUE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Blue", Color.BLUE.hsvCalibrateValue!!)
                colorNumber++
            }
            else -> {
                MainActivity.IsCalibrationMode = false
                colorNumber = 0
                mainActivity.currentState.resetFaces()
            }
        }
    }
}