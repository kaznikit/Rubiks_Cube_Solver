package com.example.kotlinapp.Recognition

import com.example.kotlinapp.CurrentState
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.Enums.Color
import org.opencv.core.*
import org.opencv.core.CvType.CV_8UC1
import org.opencv.imgproc.Imgproc
import java.util.*

class ImageRecognizer {
    internal var grayscale_image: Mat
    internal var testImage: Mat
    internal var currentState: CurrentState

    constructor(currentState: CurrentState) {
        grayscale_image = Mat()
        testImage = Mat()
        this.currentState = currentState
    }

    private fun testColor(image: Mat, minColor: Scalar, maxColor: Scalar): List<Rectangle> {
        val skin = Mat()
        val global_dilate_image = Mat()
        val skinMask = Mat()
        val canny_image = Mat()
        val sampleImage = Mat()
        image.copyTo(sampleImage)

        val alpha = 3.0
        val beta = 30.0
        //skin.convertTo(skin, -1, alpha, beta);
        Imgproc.cvtColor(sampleImage, sampleImage, Imgproc.COLOR_RGB2HSV)

        Core.inRange(sampleImage, minColor, maxColor, skinMask)
        Core.bitwise_and(sampleImage, sampleImage, skin, skinMask)
        //Imgproc.cvtColor(skin, skin, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(skin, skin, Size(25.0, 25.0), 0.0, 0.0)
        Imgproc.Canny(skin, canny_image, 30.0, 200.0)

        Imgproc.dilate(
            canny_image,
            global_dilate_image,
            Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(7.0, 7.0))
        )

        Imgproc.GaussianBlur(global_dilate_image, global_dilate_image, Size(21.0, 21.0), 0.0, 0.0)

        val contours = LinkedList<MatOfPoint>()


        val heirarchy = Mat()
        Imgproc.findContours(global_dilate_image, contours, heirarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_NONE)

        /*       //bound rects
        Rect[] boundRect = new Rect[contours.size()];
        MatOfPoint2f[] contoursPoly  = new MatOfPoint2f[contours.size()];
        int i=0;*/

        //hull convex
        val hullRectList = LinkedList<Rectangle>()

        val polygons = LinkedList<Rectangle>()
        for (mat in contours) {
            val contour2f = MatOfPoint2f()
            val polygone2f = MatOfPoint2f()
            val polygon = MatOfPoint()
            mat.convertTo(contour2f, CvType.CV_32FC2)
            Imgproc.approxPolyDP(contour2f, polygone2f, 21.0, true)
            polygone2f.convertTo(polygon, CvType.CV_32S)

            //hull
            val hull = MatOfInt()
            Imgproc.convexHull(polygon, hull)
            val contourArray = polygon.toArray()
            val hullPoints = arrayOfNulls<Point>(hull.rows())
            val hullContourIdxList = hull.toList()
            for (k in hullContourIdxList.indices) {
                hullPoints[k] = contourArray[hullContourIdxList[k]]
            }
            if (Imgproc.contourArea(MatOfPoint(*hullPoints)) > 100) {
                if (MatOfPoint(*hullPoints).toArray().size == 4) {
                    var maxCosine = 0.0
                    for (j in 2..4) {
                        // find the maximum cosine of the angle between joint edges
                        val cosine = Math.abs(
                            angle(
                                MatOfPoint(*hullPoints).toArray()[j % 4],
                                MatOfPoint(*hullPoints).toArray()[j - 2],
                                MatOfPoint(*hullPoints).toArray()[j - 1]
                            )
                        )
                        maxCosine = Math.max(maxCosine, cosine)
                    }
                    if (maxCosine < 0.3)
                        hullRectList.add(Rectangle(MatOfPoint(*hullPoints)))
                }
            }

            if (polygon.toArray().size == 4) { //&& Imgproc.isContourConvex(polygon)) {
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

                    // if cosines of all angles are small
                    // (all angles are ~90 degree) then write quandrange
                    // vertices to resultant sequence
                    if (maxCosine < 0.3)
                        polygons.add(Rectangle(polygon))//new Rectangle(polygon));
                }
            }
            //  i++;
        }

        //bound
        /*      List<MatOfPoint> contoursPolyList = new ArrayList<>(contoursPoly.length);
        for (MatOfPoint2f poly : contoursPoly) {
            contoursPolyList.add(new MatOfPoint(poly.toArray()));
        }
        for (int j = 0; j < contours.size(); j++) {
            if(boundRect[j].area() > 300) {
                //  Imgproc.drawContours(image, contoursPolyList, j, Constants.ColorTileEnum.ORANGE.cvColor);
                //Imgproc.rectangle(image, boundRect[j].tl(), boundRect[j].br(), Constants.ColorTileEnum.ORANGE.cvColor, 2);
            }
        }*/

        //hull
        val hullRectangles = LinkedList<Rectangle>()
        for (rect in hullRectList) {
            rect.qualify()
            if (rect.status === Rectangle.StatusEnum.VALID) {
                //rect.draw(image, Constants.ColorTileEnum.GREEN.cvColor);
                hullRectangles.add(rect)
            }
        }


        val rectangleList = LinkedList<Rectangle>()
        var center = Point(0.0, 0.0)
        for (rectangle in polygons) {
            rectangle.qualify()
            if (rectangle.status === Rectangle.StatusEnum.VALID && rectangle.center.x > center.x + 5 && rectangle.center.y > center.y + 5) {
                rectangleList.add(rectangle)
                center = rectangle.center
            }
            //rectangleList.add(rectangle);
        }

        skin.release()
        skinMask.release()
        canny_image.release()
        sampleImage.release()
        global_dilate_image.release()
        return hullRectangles
    }

    fun testProcess(rgbaImage: Mat): Mat {
        val laplacian = Mat()
        val canny_image = Mat()
        val dilate_image = Mat()
        var lightenedImage = Mat()

        rgbaImage.copyTo(testImage)

        rgbaImage.copyTo(lightenedImage)

        lightenedImage = correctGamma(lightenedImage, 1.3)

        Imgproc.cvtColor(lightenedImage, grayscale_image, Imgproc.COLOR_BGR2GRAY)

        Imgproc.Canny(lightenedImage, canny_image, 30.0, 100.0)
        Imgproc.dilate(canny_image, dilate_image, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, Size(5.0, 5.0)))

        Imgproc.GaussianBlur(dilate_image, dilate_image, Size(17.0, 17.0), 0.0, 0.0)

        val contours = LinkedList<MatOfPoint>()
        val heirarchy = Mat()
        Imgproc.findContours(dilate_image, contours, heirarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE)
        heirarchy.release()
        //    Imgproc.drawContours(rgbaImage, contours, -1, Constants.ColorTileEnum.RED.cvColor);

        val polygonList = LinkedList<Rectangle>()
        var i = 0
        for (contour in contours) {
            val contour2f = MatOfPoint2f()
            val polygone2f = MatOfPoint2f()
            val polygon = MatOfPoint()

            // Make a Polygon out of a contour with provide Epsilon accuracy parameter.
            // It uses the Douglas-Peucker algorithm http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm
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
                    //Imgproc.drawContours(rgbaImage, contours, i, Constants.ColorTileEnum.YELLOW.cvColor, 5);
                    // if cosines of all angles are small
                    // (all angles are ~90 degree) then write quandrange
                    // vertices to resultant sequence
                    if (maxCosine < 0.3)
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

        ////////
        //red color tracking
        ////////
        /*for(Rectangle rect : redList){
            rectangleList.add(rect);
        }*/

        Rectangle.removedOutlierRhombi(rectangleList)
        for (rect in rectangleList)
            rect.draw(rgbaImage, Color.YELLOW.cvColor)


        //in calibration mode it's necessary to scan colors and write them
        /*if (!MainActivity.IsCalibrationMode) {
            currentState.activeRubikFace.calculateTiles(rectangleList, rgbaImage)
        } else {
            //scan colors
            currentState.cameraCalibration.getColor(rgbaImage, rectangleList)
        }*/
        canny_image.release()
        lightenedImage.release()
        dilate_image.release()
        return rgbaImage
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