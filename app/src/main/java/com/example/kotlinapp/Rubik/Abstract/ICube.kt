package com.example.kotlinapp.Rubik.Abstract

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Cubie
import com.example.kotlinapp.Rubik.DirectionsControl
import com.example.kotlinapp.Rubik.Layer

interface ICube : ILayerRotatedCallback {
    var permutationAllowed : Boolean

    var directionsControl : DirectionsControl

    //for whole cube rotations
    var rotationAxis : Axis
    var rotationAngle : Float

    val permutationLock : Any

    fun resetLayerCubies()

    //region Layers Rotations

    fun performMoves(moves: String): String

    //rotate a cube side
    fun turn(turn: String)

    //endregion

    fun sortCubiesForLayer(iLayer: ILayer) : List<ICubie>?

    /**
     * return value when rotation process is complete
     */
    fun rotateCube(angle : Float, axis: Axis) : Boolean

    fun optimizeMoves(moves: String): String {
        var moves = moves
        var i = 0
        while (i < moves.length) {
            val move = moves.substring(i, i + 1)
            if (move != " " && move != "'" && move != "2") { //Only check if there is a meaningful turn/rotation
                if (i <= moves.length - 3) {
                    if (moves.substring(i + 1, i + 2).compareTo("2") == 0) { //Double turn
                        if (i <= moves.length - 4 && moves[i + 3] == moves[i]) {
                            if (i <= moves.length - 5) {
                                if (moves.substring(i + 4, i + 5).compareTo("2") == 0) {
                                    //Ex. "U2 U2" gets negated
                                    moves = moves.substring(0, i) + moves.substring(i + 5)
                                    i--
                                } else if (moves.substring(i + 4, i + 5).compareTo("'") == 0) {
                                    //Ex. "U2 U'" --> "U"
                                    moves = (moves.substring(0, i) + moves.substring(i, i + 1)
                                            + moves.substring(i + 5))
                                    i--
                                } else {
                                    //Ex. "U2 U" --> "U'"
                                    moves = (moves.substring(0, i) + moves.substring(i, i + 1) + "'"
                                            + moves.substring(i + 4))
                                    i--
                                }
                            } else {
                                //Ex. "U2 U" --> "U'"
                                moves = (moves.substring(0, i) + moves.substring(i, i + 1) + "'"
                                        + moves.substring(i + 4))
                                i--
                            }
                        }
                    } else if (moves.substring(i + 1, i + 2).compareTo("'") == 0) { //Clockwise turn
                        if (i <= moves.length - 4 && moves[i + 3] == moves[i]) {
                            if (i <= moves.length - 5) {
                                if (moves.substring(i + 4, i + 5).compareTo("2") == 0) {
                                    //Ex. "U' U2" --> "U"
                                    moves = (moves.substring(0, i) + moves.substring(i, i + 1)
                                            + moves.substring(i + 5))
                                    i--
                                } else if (moves.substring(i + 4, i + 5).compareTo("'") == 0) {
                                    //Ex. "U' U'" --> "U2"
                                    moves = (moves.substring(0, i) + moves.substring(i, i + 1) + "2"
                                            + moves.substring(i + 5))
                                    i--
                                } else {
                                    //Ex. "U' U" gets negated
                                    moves = moves.substring(0, i) + moves.substring(i + 4)
                                    i--
                                }
                            } else {
                                //Ex. "U' U" gets negated
                                moves = moves.substring(0, i) + moves.substring(i + 4)
                                i--
                            }
                        }
                    } else { //Clockwise turn
                        if (i <= moves.length - 3 && moves[i + 2] == moves[i]) {
                            if (i <= moves.length - 4) {
                                if (moves.substring(i + 3, i + 4).compareTo("2") == 0) {
                                    //Ex. "U U2" --> "U' "
                                    moves = (moves.substring(0, i) + moves.substring(i, i + 1) + "'"
                                            + moves.substring(i + 4))
                                    i--
                                } else if (moves.substring(i + 3, i + 4).compareTo("'") == 0) {
                                    //Ex. "U U'" gets negated
                                    moves = moves.substring(0, i) + moves.substring(i + 4)
                                    i--
                                } else {
                                    //Ex. "U U" --> "U2"
                                    moves =
                                        moves.substring(0, i) + moves.substring(i, i + 1) + "2" + moves.substring(i + 3)
                                    i--
                                }
                            } else {
                                //Ex. "U U" --> "U2"
                                moves = moves.substring(0, i) + moves.substring(i, i + 1) + "2" + moves.substring(i + 3)
                                i--
                            }
                        }

                    }
                }
            }
            i++
        }
        return moves
    }

    fun setPermutationAllowance(value : Boolean)

    fun getPermutationAllowance() : Boolean
}