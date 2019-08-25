package com.example.kotlinapp.Recognition

import android.os.AsyncTask
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Enums.Color
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import java.lang.ref.WeakReference
import java.util.*

class ImageRecognizer internal constructor(mainActivity: MainActivity) : AsyncTask<Mat, Int, Mat>() {
    internal lateinit var grayscale_image: Mat
    internal lateinit var testImage: Mat
    private val activityReference: WeakReference<MainActivity> = WeakReference(mainActivity)

    override fun doInBackground(vararg image: Mat): Mat {
        val lightened = Mat()
        image[0].copyTo(lightened)
        //lightened = correctGamma(lightened, 1.0);

        Imgproc.cvtColor(lightened, lightened, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(lightened, lightened, Size(9.0, 9.0), 0.0, 0.0)
        Imgproc.adaptiveThreshold(
            lightened,
            lightened,
            180.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            5,
            2.0
        )

        //added
        Imgproc.Laplacian(lightened, lightened, lightened.depth(), 7)//, 0, Core.BORDER_DEFAULT);

        Imgproc.threshold(lightened, lightened, 1.0, 180.0, Imgproc.THRESH_BINARY)

        Imgproc.dilate(lightened, lightened, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(15.0, 15.0)))

        val contours = LinkedList<MatOfPoint>()
        val heirarchy = Mat()
        Imgproc.findContours(lightened, contours, heirarchy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE)

        val polygonList = LinkedList<Rectangle>()
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
                                polygon.toArray()[j - 1]
                            )
                        )
                        maxCosine = Math.max(maxCosine, cosine)
                    }
                    if (maxCosine < 0.4)
                        polygonList.add(Rectangle(polygon))
                }
            }
            i++
        }
        val rectangleList = LinkedList<Rectangle>()
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
            //if (activityReference!!.currentState.activeRubikFace.processFinished) {
                activityReference!!.currentState.calculateTilesForFace(rectangleList, image[0])
            //}
        } else {
            //scan colors
            activityReference!!.currentState.cameraCalibration!!.getColor(image[0])
        }

        return image[0]
    }

    override fun onPostExecute(result: Mat?) {
        val activity = activityReference.get() ?: return
        activity.onMatProcessed(result)
    }

    fun threesholdTestImage(image: Mat): Mat {
        val lightened = Mat()
        image.copyTo(lightened)
        //lightened = correctGamma(lightened, 1.0);

        Imgproc.cvtColor(lightened, lightened, Imgproc.COLOR_BGR2GRAY)
        Imgproc.GaussianBlur(lightened, lightened, Size(9.0, 9.0), 0.0, 0.0)
        Imgproc.adaptiveThreshold(
            lightened,
            lightened,
            180.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            5,
            2.0
        )
        //Imgproc.GaussianBlur(lightened, lightened, new Size(9, 9), 0, 0);
        //Imgproc.dilate(lightened, lightened, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(9, 9)));

        //added
        Imgproc.Laplacian(lightened, lightened, lightened.depth(), 7)//, 0, Core.BORDER_DEFAULT);

        Imgproc.threshold(lightened, lightened, 1.0, 180.0, Imgproc.THRESH_BINARY)

        Imgproc.dilate(lightened, lightened, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(15.0, 15.0)))

        val contours = LinkedList<MatOfPoint>()
        val heirarchy = Mat()
        Imgproc.findContours(lightened, contours, heirarchy, Imgproc.CHAIN_APPROX_NONE, Imgproc.CHAIN_APPROX_SIMPLE)

        val polygonList = LinkedList<Rectangle>()
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
                                polygon.toArray()[j - 1]
                            )
                        )
                        maxCosine = Math.max(maxCosine, cosine)
                    }
                    if (maxCosine < 0.4)
                        polygonList.add(Rectangle(polygon))
                }
            }
            i++
        }
        val rectangleList = LinkedList<Rectangle>()
        for (rectangle in polygonList) {
            rectangle.qualify()
            if (rectangle.status === Rectangle.StatusEnum.VALID) {
                rectangleList.add(rectangle)
            }
        }

        Rectangle.removedOutlierRhombi(rectangleList)
        for (rect in rectangleList)
            rect.draw(image, Color.YELLOW.cvColor)


        //in calibration mode it's necessary to scan colors and write them
        /*if (!MainActivity.IsCalibrationMode) {
            if (currentState.activeRubikFace.processFinished) {
                currentState.activeRubikFace.calculateTiles(rectangleList, image)
            }
        } else {
            //scan colors
            currentState.cameraCalibration.getColor(image, rectangleList)
        }*/

        return image
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
}