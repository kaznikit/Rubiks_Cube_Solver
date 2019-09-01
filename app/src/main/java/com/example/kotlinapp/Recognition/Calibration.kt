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
import com.example.kotlinapp.Util.InfoDisplayer
import org.opencv.core.Point

class Calibration(internal var mainActivity: MainActivity) {
    internal lateinit var image: Mat

    //go through all colors
    internal var colorNumber = 0

    fun getColor(image: Mat): Scalar? {
        this.image = image
        when (colorNumber) {
            0 -> InfoDisplayer.text = "Tap on the red color on the cube"
            1 -> InfoDisplayer.text = "Tap on the green color on the cube"
            2 -> InfoDisplayer.text = "Tap on the orange color on the cube"
            3 -> InfoDisplayer.text = "Tap on the yellow color on the cube"
            4 -> InfoDisplayer.text = "Tap on the white color on the cube"
            5 -> InfoDisplayer.text = "Tap on the blue color on the cube"
            else -> InfoDisplayer.text = "Calibration finished! Tap to continue."
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