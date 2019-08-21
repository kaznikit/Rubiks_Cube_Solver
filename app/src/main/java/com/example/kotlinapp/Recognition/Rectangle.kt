package com.example.kotlinapp.Recognition

import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import android.R.attr.x
import android.R.attr.y
import org.opencv.core.Scalar
import org.opencv.core.Mat
import com.example.kotlinapp.Recognition.Rectangle.StatusEnum
import java.util.*
import kotlin.Comparator


class Rectangle {
    // Various forms of storing the corner points.
    private var polygonMatrix: MatOfPoint
    private var polygonPointList: List<Point>   // =+= possibly eliminate
    private var polygonePointArray: Array<Point>    // =+= note order is adjusted

    // Possible states that this Rhombus can be identified.
    enum class StatusEnum {
        NOT_PROCESSED, NOT_4_POINTS, NOT_CONVEX, AREA, CLOCKWISE, OUTLIER, VALID
    };

    // Current Status
    var status = StatusEnum.NOT_PROCESSED

    // Center of Polygon
    var center = Point()

    // Area of Quadrilateral.
    var area: Double = 0.toDouble()

    // Smaller angle (in degrees: between 0 and 180) that two set parallelogram edges make to x-axis.
    var alphaAngle: Double = 0.toDouble()

    // Larger angle (in degrees: between 0 and 180) that two set parallelogram edges make to x-axis.
    var betaAngle: Double = 0.toDouble()

    // Best estimate (average) of parallelogram alpha side length.
    var alphaLength: Double = 0.toDouble()

    // Best estimate (average) of parallelogram beta side length.
    var betaLength: Double = 0.toDouble()

    // Ratio of beta to alpha length.
    var gammaRatio: Double = 0.toDouble()

    constructor(polygon: MatOfPoint) {
        polygonMatrix = polygon
        polygonPointList = polygon.toList()
        polygonePointArray = polygon.toArray()
    }

    /**
     * Determine is polygon is a value Rubik Face Parallelogram
     */
    fun qualify() {

        // Calculate center
        var x = 0.0
        var y = 0.0
        for (point in polygonPointList) {
            x += point.x
            y += point.y
        }
        center.x = x / polygonPointList.size
        center.y = y / polygonPointList.size

        // Check if has four sizes and endpoints.
        if (polygonPointList.size !== 4) {
            status = StatusEnum.NOT_4_POINTS
            return
        }

        // Check if convex
        // =+= I don't believe this is working.  result should be either true or
        // =+= false indicating clockwise or counter-clockwise depending if image
        // =+= is a "hole" or a "blob".
        if (Imgproc.isContourConvex(polygonMatrix) == false) {
            status = StatusEnum.NOT_CONVEX
            return
        }

        // Compute area; check if it is reasonable.
        area = areaOfConvexQuadrilateral(polygonePointArray)
        if (area < 1000 || area > 16000) {
            status = StatusEnum.AREA
            return
        }

        // Adjust vertices such that element 0 is at bottom and order is counter clockwise.
        // =+= return true here if points are counter-clockwise.
        // =+= sometimes both rotations are provided.
        if (adjustQuadrilaterVertices() === true) {
            status = StatusEnum.CLOCKWISE
            return
        }


        // =+= beta calculation is failing when close to horizontal.
        // =+= Can vertices be chooses so that we do not encounter the roll over problem at +180?
        // =+= Or can math be performed differently?

        /*
         * Calculate angles to X axis of Parallelogram sides.  Take average of both sides.
         * =+= To Do:
         *   1) Move to radians.
         *   2) Move to +/- PIE representation.
         */
        alphaAngle = 180.0 / Math.PI * Math.atan2(
            polygonePointArray[1].y - polygonePointArray[0].y + (polygonePointArray[2].y - polygonePointArray[3].y),
            polygonePointArray[1].x - polygonePointArray[0].x + (polygonePointArray[2].x - polygonePointArray[3].x)
        )

        betaAngle = 180.0 / Math.PI * Math.atan2(
            polygonePointArray[2].y - polygonePointArray[1].y + (polygonePointArray[3].y - polygonePointArray[0].y),
            polygonePointArray[2].x - polygonePointArray[1].x + (polygonePointArray[3].x - polygonePointArray[0].x)
        )

        alphaLength = (lineLength(polygonePointArray[0], polygonePointArray[1]) + lineLength(
            polygonePointArray[3],
            polygonePointArray[2]
        )) / 2
        betaLength = (lineLength(polygonePointArray[0], polygonePointArray[3]) + lineLength(
            polygonePointArray[1],
            polygonePointArray[2]
        )) / 2

        gammaRatio = betaLength / alphaLength


        status = StatusEnum.VALID
    }

    /**
     * Area of Convex Quadrilateral
     *
     * @param quadrilateralPointArray
     * @return
     */
    private fun areaOfConvexQuadrilateral(quadrilateralPointArray: Array<Point>): Double {
        return areaOfaTriangle(
            lineLength(quadrilateralPointArray[0], quadrilateralPointArray[1]),
            lineLength(quadrilateralPointArray[1], quadrilateralPointArray[2]),
            lineLength(quadrilateralPointArray[2], quadrilateralPointArray[0]))
        + areaOfaTriangle(
            lineLength(quadrilateralPointArray[0], quadrilateralPointArray[3]),
            lineLength(quadrilateralPointArray[3], quadrilateralPointArray[2]),
            lineLength(quadrilateralPointArray[2], quadrilateralPointArray[0]))
    }

    /**
    * Area of a triangle specified by the three side lengths.
    */
    private fun areaOfaTriangle(a:Double, b:Double, c:Double) : Double {
        return (Math.sqrt((a + b - c) * (a - b + c) * (-a + b + c) * (a + b + c)) / 4.0)
    }

    /**
     * Line length between two points.
     */
    private fun lineLength(a: Point, b: Point): Double {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
    }

    /**
     * Adjust Quadrilater Vertices such that:
     * 1) Element 0 has the minimum y coordinate.
     * 2) Order draws a counter clockwise quadrilater.
     */
    private fun adjustQuadrilaterVertices(): Boolean {
        // Find minimum.
        var y_min = java.lang.Double.MAX_VALUE
        var index = 0
        for (i in 0 until polygonePointArray.size) {
            if (polygonePointArray[i].y < y_min) {
                y_min = polygonePointArray[i].y
                index = i
            }
        }

        // Rotate to get the minimum Y element ("index") as element 0.
        for (i in 0 until index) {
            val tmp = polygonePointArray[0]
            polygonePointArray[0] = polygonePointArray[1]
            polygonePointArray[1] = polygonePointArray[2]
            polygonePointArray[2] = polygonePointArray[3]
            polygonePointArray[3] = tmp
        }

        // Return true if points are as depicted above and in a clockwise manner.
        return polygonePointArray[1].x < polygonePointArray[3].x
    }

    /**
     * Remove Outlier Rhombi
     * For Alpha and Beta Angles:
     * 1) Find Median Value: i.e. value in which half are greater and half are less.
     * 2) Remove any that are > 10 degrees different
     */
    companion object {
        fun removedOutlierRhombi(rhombusList: MutableList<Rectangle>) {

            val angleOutlierTolerance = 10.0

            if (rhombusList.size < 3)
                return

            val midIndex = rhombusList.size / 2

            rhombusList.sortWith(Comparator { lhs, rhs -> (lhs.alphaAngle - rhs.alphaAngle) as Int })
            val medianAlphaAngle = rhombusList[midIndex].alphaAngle

            rhombusList.sortWith(Comparator { lhs, rhs -> (lhs.betaAngle - rhs.betaAngle) as Int })
            val medianBetaAngle = rhombusList[midIndex].betaAngle

            val rhombusItr = rhombusList.iterator()
            while (rhombusItr.hasNext()) {

                val rhombus = rhombusItr.next()

                if (Math.abs(rhombus.alphaAngle - medianAlphaAngle) > angleOutlierTolerance || Math.abs(
                        rhombus.betaAngle - medianBetaAngle
                    ) > angleOutlierTolerance
                ) {
                    rhombus.status = StatusEnum.OUTLIER
                    rhombusItr.remove()
                }
            }
        }
    }

    fun draw(rgba_gray_image: Mat, color: Scalar) {
        // Draw Polygone Edges
        val listOfPolygons = LinkedList<MatOfPoint>()
        listOfPolygons.add(polygonMatrix)
        Imgproc.polylines(rgba_gray_image, listOfPolygons, true, color, 3)
    }
}