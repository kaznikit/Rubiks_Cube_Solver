package com.example.kotlinapp.Recognition

import android.os.AsyncTask
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Enums.Color
import org.opencv.core.*
import org.opencv.core.Core.split
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import java.lang.ref.WeakReference
import java.util.*

class ImageRecognizer internal constructor(mainActivity: MainActivity) : AsyncTask<Mat, Int, Mat>() {
    internal lateinit var grayscale_image: Mat
    internal lateinit var testImage: Mat
    private val activityReference: WeakReference<MainActivity> = WeakReference(mainActivity)
    val cl = Imgproc.createCLAHE(10.0)
    var lightened = Mat()
    val heirarchy = Mat()
    val contours = LinkedList<MatOfPoint>()
    val polygonList = LinkedList<Rectangle>()
    val rectangleList = LinkedList<Rectangle>()

    override fun doInBackground(vararg image: Mat): Mat {
        image[0].copyTo(lightened)
        Imgproc.cvtColor(lightened, lightened, Imgproc.COLOR_BGR2GRAY)
        lightened = correctGamma(lightened, 2.0)
        cl.apply(lightened, lightened)
        Imgproc.GaussianBlur(lightened, lightened, Size(9.0, 9.0), 0.0, 0.0)
        Imgproc.Canny(lightened, lightened, 30.0, 100.0)
        Imgproc.dilate(lightened, lightened,
            Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(11.0, 11.0)))

        Imgproc.findContours(lightened, contours, heirarchy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE)

        var i = 0
        for (contour in contours) {
            val contour2f = MatOfPoint2f()
            val polygone2f = MatOfPoint2f()
            val polygon = MatOfPoint()
            contour.convertTo(contour2f, CvType.CV_32FC2)
            Imgproc.approxPolyDP(contour2f, polygone2f, 15.0, true)
            polygone2f.convertTo(polygon, CvType.CV_32S)

            if (polygon.toArray().size == 4 && Imgproc.isContourConvex(polygon)) {
                if (Math.abs(Imgproc.contourArea(polygon)) > 100) {
                    var maxCosine = 0.0
                    for (j in 2..4) {
                        // find the maximum cosine of the angle between joint edges
                        val cosine = Math.abs(
                            angle(
                                polygon.toArray()[j % 4],
                                polygon.toArray()[j - 2],
                                polygon.toArray()[j - 1])
                        )
                        maxCosine = Math.max(maxCosine, cosine)
                    }
                    if (maxCosine < 0.4)
                        polygonList.add(Rectangle(polygon))
                }
            }
            i++
        }
        for (rectangle in polygonList) {
            rectangle.qualify()
            if (rectangle.status === Rectangle.StatusEnum.VALID) {
                rectangleList.add(rectangle)
            }
        }

        Rectangle.removedOutlierRhombi(rectangleList)
        for (rect in rectangleList)
            rect.draw(image[0], Color.YELLOW.cvColor)

        val activityReference = activityReference.get()
        //in calibration mode it's necessary to scan colors and write them
        if (!MainActivity.IsCalibrationMode) {
                activityReference!!.currentState.calculateTilesForFace(rectangleList, image[0])
        }
        /*else {
            //scan colors
            activityReference!!.currentState.cameraCalibration!!.getColor(image[0])
        }*/

        contours.clear()
        polygonList.clear()
        rectangleList.clear()

        return image[0]
    }

    override fun onPostExecute(result: Mat?) {
        val activity = activityReference.get() ?: return
        activity.onMatProcessed(result)
    }

    override fun onCancelled() {
        val activity = activityReference.get() ?: return
        activity.onMatProcessed(null)
    }

    fun threesholdTestImage(image: Mat): Mat {
        image.copyTo(lightened)
        Imgproc.cvtColor(lightened, lightened, Imgproc.COLOR_BGR2GRAY)
        lightened = correctGamma(lightened, 2.0)
        cl.apply(lightened, lightened)
        Imgproc.GaussianBlur(lightened, lightened, Size(9.0, 9.0), 0.0, 0.0)
        Imgproc.Canny(lightened, lightened, 30.0, 100.0)
        Imgproc.dilate(lightened, lightened,
            Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(11.0, 11.0)))

        /*Imgproc.findContours(lightened, contours, heirarchy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE)

        var i = 0
        for (contour in contours) {
            val contour2f = MatOfPoint2f()
            val polygone2f = MatOfPoint2f()
            val polygon = MatOfPoint()
            contour.convertTo(contour2f, CvType.CV_32FC2)
            Imgproc.approxPolyDP(contour2f, polygone2f, 15.0, true)
            polygone2f.convertTo(polygon, CvType.CV_32S)

            if (polygon.toArray().size == 4 && Imgproc.isContourConvex(polygon)) {
                if (Math.abs(Imgproc.contourArea(polygon)) > 100) {
                    var maxCosine = 0.0
                    for (j in 2..4) {
                        // find the maximum cosine of the angle between joint edges
                        val cosine = Math.abs(
                            angle(
                                polygon.toArray()[j % 4],
                                polygon.toArray()[j - 2],
                                polygon.toArray()[j - 1])
                        )
                        maxCosine = Math.max(maxCosine, cosine)
                    }
                    if (maxCosine < 0.4)
                        polygonList.add(Rectangle(polygon))
                }
            }
            i++
        }
        for (rectangle in polygonList) {
            rectangle.qualify()
            if (rectangle.status === Rectangle.StatusEnum.VALID) {
                rectangleList.add(rectangle)
            }
        }

        Rectangle.removedOutlierRhombi(rectangleList)
        for (rect in rectangleList)
            rect.draw(image, Color.YELLOW.cvColor)

        *//*val activityReference = activityReference.get()
        //in calibration mode it's necessary to scan colors and write them
        if (!MainActivity.IsCalibrationMode) {
            activityReference!!.currentState.calculateTilesForFace(rectangleList, image[0])
        } else {
            //scan colors
            activityReference!!.currentState.cameraCalibration!!.getColor(image[0])
        }*//*

        contours.clear()
        polygonList.clear()
        rectangleList.clear()*/

        return lightened
    }

    internal fun correctGamma(img: Mat, gamma: Double): Mat {
        val inverse_gamma = 1.0 / gamma
        val lut_matrix = Mat(1, 256, CV_8UC1)
        val ptr = ByteArray(256)
        for (i in 0..255)
            ptr[i] = (Math.pow(i.toDouble() / 255.0, inverse_gamma) * 255.0).toByte()

        lut_matrix.put(0, 0, ptr)
        val result: Mat
        Core.LUT(img, lut_matrix, img)
        return img
    }

    internal fun angle(pt1: Point, pt2: Point, pt0: Point): Double {
        val dx1 = pt1.x - pt0.x
        val dy1 = pt1.y - pt0.y
        val dx2 = pt2.x - pt0.x
        val dy2 = pt2.y - pt0.y
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }

    /*fun getBrightness(frame : Mat)
    {
        var brightness : Double
        var temp : Mat
        var color = ArrayList<Mat>()
        var lum : Double
        temp = frame

        split(temp, color)

        color[0] = color[0] * 0.299
        color[1] = color[1] * 0.587
        color[2] = color[2] * 0.114


        lum = color[0] + color [1] + color[2]

        Scalar summ = sum(lum)


        brightness = summ[0]/((pow(2,8)-1)*frame.rows * frame.cols) * 2; //-- percentage conversion factor
    }*/
}