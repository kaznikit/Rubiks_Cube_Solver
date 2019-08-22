package com.example.kotlinapp.Recognition

import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

class RectanglesLayout {
    companion object {
        fun MakeLayout(
            _rectanglesList: List<Rectangle>, _rectanglesFaceArray: Array<Array<RubikTile?>>,
            _alphaAngle: Double, _betaAngle: Double): Boolean {
            var rectanglesList = _rectanglesList
            var alphaAngle = _alphaAngle
            var betaAngle = _betaAngle

            // Sort rectangles into three sets along alpha axis.
            val alphaListOfSets = createOptimizedListOfRhombiSets(
                object : Comparator<Rectangle> {
                    override fun compare(rect0: Rectangle, rect1: Rectangle): Int {
                        return getAlpha(rect1, alphaAngle) - getAlpha(rect0, alphaAngle)
                    }
                }, rectanglesList)

            // Sort rectangles into three sets along beta axis.
            val betaListOfSets = createOptimizedListOfRhombiSets(
                object : Comparator<Rectangle> {
                    override fun compare(rect0: Rectangle, rect1: Rectangle): Int {
                        return getBeta(rect1, betaAngle) - getBeta(rect0, betaAngle)
                    }
                }, rectanglesList)

            for (n in 2 downTo -1 + 1) {
                for (m in 2 downTo -1 + 1) {
                    // Get candidate rectangle that have the M and N indices.
                    val alphaSet = alphaListOfSets[n]
                    val betaSet = betaListOfSets[m]

                    // Find rectangle that have both the desired M and N indices.
                    val commonElements = findCommonElements(alphaSet, betaSet)

                    if (commonElements.size == 0)
                        _rectanglesFaceArray[n][m]!!.rectangle = null // No rectangles for this tile
                    else if (commonElements.size == 1) {
                        _rectanglesFaceArray[n][m]!!.rectangle = commonElements[0]  // Desired result
                    } else {
                        _rectanglesFaceArray[n][m]!!.rectangle = commonElements[0]
                    }
                }
            }

            // Check that there is at least on Rhombus in each row and column.
            if (_rectanglesFaceArray[0][0]!!.rectangle == null && _rectanglesFaceArray[0][1]!!.rectangle == null && _rectanglesFaceArray[0][2]!!.rectangle == null) return false
            if (_rectanglesFaceArray[1][0]!!.rectangle == null && _rectanglesFaceArray[1][1]!!.rectangle == null && _rectanglesFaceArray[1][2]!!.rectangle == null) return false
            if (_rectanglesFaceArray[2][0]!!.rectangle == null && _rectanglesFaceArray[2][1]!!.rectangle == null && _rectanglesFaceArray[2][2]!!.rectangle == null) return false
            if (_rectanglesFaceArray[0][0]!!.rectangle == null && _rectanglesFaceArray[1][0]!!.rectangle == null && _rectanglesFaceArray[2][0]!!.rectangle == null) return false
            if (_rectanglesFaceArray[0][1]!!.rectangle == null && _rectanglesFaceArray[1][1]!!.rectangle == null && _rectanglesFaceArray[2][1]!!.rectangle == null) return false
            return !(_rectanglesFaceArray[0][2]!!.rectangle == null && _rectanglesFaceArray[1][2]!!.rectangle == null && _rectanglesFaceArray[2][2]!!.rectangle == null)

        }

        private fun findCommonElements(alphaSet: Collection<Rectangle>, betaSet: Collection<Rectangle>):List<Rectangle> {
            val result = LinkedList(alphaSet)
            result.retainAll(betaSet)
            return result
        }

        private fun getAlpha(rhombus: Rectangle, alphaAngle : Double): Int {
            return (rhombus.center.x * cos(alphaAngle) + rhombus.center.y * sin(alphaAngle)).toInt()
        }

        private fun getBeta(rhombus: Rectangle, betaAngle : Double): Int {
            return (rhombus.center.x * cos(betaAngle) + rhombus.center.y * sin(betaAngle)).toInt()
        }

        private fun createOptimizedListOfRhombiSets(comparator: Comparator<Rectangle>, rectanglesList: List<Rectangle>): List<Collection<Rectangle>> {
            var best_error = java.lang.Double.POSITIVE_INFINITY
            var best_p = 0
            var best_q = 0

            val n = rectanglesList!!.size

            // First just perform a linear sort: smallest to largest.
            val sortedRhombusList = ArrayList(rectanglesList)
            Collections.sort(
                sortedRhombusList,
                comparator
            )

            // Next search overall all partition possibilities, and find that with the least error w.r.t. provided comparator.
            for (p in 1 until n - 1) {
                for (q in p + 1 until n) {
                    val error = calculateErrorAccordingToPartition_P_Q(
                        sortedRhombusList,
                        comparator,
                        p,
                        q
                    ).toDouble()

                    if (error < best_error) {
                        best_error = error
                        best_p = p
                        best_q = q
                    }
                }
            }

            val result = LinkedList<Collection<Rectangle>>()
            result.add(sortedRhombusList.subList(0, best_p))
            result.add(sortedRhombusList.subList(best_p, best_q))
            result.add(sortedRhombusList.subList(best_q, n))
            return result
        }

        private fun calculateErrorAccordingToPartition_P_Q(sortedRhombusList: ArrayList<Rectangle>, comparator: Comparator<Rectangle>, p: Int, q: Int): Int {
            val n = sortedRhombusList.size
            return calculateSumSquaredErrorOfSet(sortedRhombusList.subList(0, p), comparator) +
                    calculateSumSquaredErrorOfSet(sortedRhombusList.subList(p, q), comparator) +
                    calculateSumSquaredErrorOfSet(sortedRhombusList.subList(q, n), comparator)
        }

        private fun calculateSumSquaredErrorOfSet(subList: List<Rectangle>, comparator: Comparator<Rectangle>): Int {
            val n = subList.size
            var sumSquared = 0
            for (i in 0 until n - 1) {
                for (j in i + 1 until n) {
                    val cmp = comparator.compare(subList[i], subList[j])
                    sumSquared += cmp * cmp
                }
            }
            return sumSquared
        }
    }
}