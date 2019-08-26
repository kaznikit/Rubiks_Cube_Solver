package com.example.kotlinapp.Util

class Tool {
    companion object {
        fun getYUVfromRGB(rgb: DoubleArray?): DoubleArray {
            if (rgb == null) {
                return doubleArrayOf(0.0, 0.0, 0.0, 0.0)
            }
            val yuv = DoubleArray(4)
            yuv[0] = 0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2]
            yuv[1] = -0.147 * rgb[0] + -0.289 * rgb[1] + 0.436 * rgb[2]
            yuv[2] = 0.615 * rgb[0] + -0.515 * rgb[1] + -0.100 * rgb[2]
            return yuv
        }
    }
}