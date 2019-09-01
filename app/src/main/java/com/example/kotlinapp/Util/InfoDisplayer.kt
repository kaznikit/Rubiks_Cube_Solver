package com.example.kotlinapp.Util

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class InfoDisplayer {
    companion object{
        var text = ""
        fun writeInfo(image : Mat, color : Scalar) : Mat{
            Imgproc.putText(image, text, Constants.StartingTextPoint, Constants.FontFace, Constants.FontSize, color, Constants.TextThickness)
            return image
        }
    }
}