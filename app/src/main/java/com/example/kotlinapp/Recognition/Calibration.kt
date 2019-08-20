package com.example.kotlinapp.Recognition

import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Util.Constants
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import com.example.kotlinapp.Rubik.Enums.Color

class Calibration {
    internal lateinit var image: Mat
    internal lateinit var mainActivity: MainActivity

    fun Calibration(mainActivity: MainActivity){
        this.mainActivity = mainActivity
    }

    //go through all colors
    internal var colorNumber = 0

    fun getColor(image: Mat, rectangleList: List<Rectangle>): Scalar? {
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

    fun getColorByCoordinates(tileCenter: Point) {
        var measuredColorArray = DoubleArray(4)
        val mat = image.submat(
            (tileCenter.y - 10).toInt(),
            (tileCenter.y + 10).toInt(),
            (tileCenter.x - 10).toInt(),
            (tileCenter.x + 10).toInt()
        )
        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2HSV);
        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
        measuredColorArray = Core.mean(mat).`val`
        val color =
            Color.rgb(measuredColorArray[0].toFloat(), measuredColorArray[1].toFloat(), measuredColorArray[2].toFloat())
        when (colorNumber) {
            0 -> {
                Constants.ColorTileEnum.RED.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Red", Constants.ColorTileEnum.RED.hsvCalibrateValue)
                colorNumber++
            }
            1 -> {
                Constants.ColorTileEnum.GREEN.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Green", Constants.ColorTileEnum.GREEN.hsvCalibrateValue)
                colorNumber++
            }
            2 -> {
                Constants.ColorTileEnum.ORANGE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Orange", Constants.ColorTileEnum.ORANGE.hsvCalibrateValue)
                colorNumber++
            }
            3 -> {
                Constants.ColorTileEnum.YELLOW.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Yellow", Constants.ColorTileEnum.YELLOW.hsvCalibrateValue)
                colorNumber++
            }
            4 -> {
                Constants.ColorTileEnum.WHITE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("White", Constants.ColorTileEnum.WHITE.hsvCalibrateValue)
                colorNumber++
            }
            5 -> {
                Constants.ColorTileEnum.BLUE.hsvCalibrateValue =
                    Scalar(measuredColorArray[0], measuredColorArray[1], measuredColorArray[2], measuredColorArray[3])
                mainActivity.calibrateColorButton.setBackgroundColor(color)
                mainActivity.SaveSharedPreferences("Blue", Constants.ColorTileEnum.BLUE.hsvCalibrateValue)
                colorNumber++
            }
            else -> {
                MainActivity.IsCalibrationMode = false
                colorNumber = 0
                mainActivity.ResetFaces()
            }
        }
    }
}