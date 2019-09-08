package com.example.kotlinapp.Rubik.LogicSolving

import android.util.Log
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import com.example.kotlinapp.Rubik.DirectionsControl
import com.example.kotlinapp.Rubik.Layer
import com.example.kotlinapp.Rubik.Solver
import java.lang.Exception

//solving cube without tiles rotation
class LogicCube : ICube {
    override var permutationAllowed = true
    override var directionsControl = DirectionsControl()
    var cubies = arrayListOf<LogicCubie>()
    var layers = arrayListOf<LogicLayer>()
    override var rotationAxis = Axis.xAxis
    override var rotationAngle = 0f
    override val permutationLock = Any()

    var currentTurn = ""

    //region Solving Algorithms

    /**
     * Make white cross on the up layer
     */
    fun makeWhiteCross() : ArrayList<String> {
        var moves = ArrayList<String>() //String()

        //find center white point and add it to the up layer
        for (qb in cubies) {
            if (!qb.isEdge && !qb.isCorner) {
                if (qb.tiles.any { x -> x.color == Color.WHITE }) {
                    for (tile in qb.tiles) {
                        if (tile.color == Color.WHITE && tile.isActive) {
                            if (tile.direction == 'L') {
                                moves.add(performMoves("S"))
                            } else if (tile.direction == 'R') {
                                moves.add(performMoves("S'"))
                            } else if (tile.direction == 'D') {
                                moves.add(performMoves("M"))
                                moves.add(performMoves("M"))
                            } else if (tile.direction == 'B') {
                                moves.add(performMoves("M"))
                            } else if (tile.direction == 'F') {
                                moves.add(performMoves("M'"))
                            }
                        }
                    }
                }
            }
        }

        while (numWhiteEdgesOriented() < 4) {
            if(getPermutationAllowance()) {
                var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any { t -> t.color == Color.WHITE } }
                for (qb in tempCubies) {
                    //do while cubie is not on the right place
                    while (!qb.isCubieRightOriented()) {
                        if (getPermutationAllowance()) {
                            //find all edges with white tiles
                            var tempTiles = qb.tiles.filter { x -> x.isActive }
                            var whiteTile = tempTiles.filter { x -> x.color == Color.WHITE }.single()
                            var anotherTile = tempTiles.filter { x -> x.color != Color.WHITE }.single()

                            //white tile on the side, another tile on down layer
                            if (whiteTile.direction != 'D' && anotherTile.direction == 'D') {
                                //if cubie is on the right layer
                                var whiteDir : Char
                                var downMove = "D'"
                                if(whiteTile.direction == directionsControl.getDirectionByColor(anotherTile.color)){
                                    whiteDir = whiteTile.direction
                                    moves.add(performMoves("D"))
                                }else {
                                    whiteDir = qb.getNormalVectorAfterRotation(whiteTile, 90f, 'D')
                                    downMove = "D"
                                }
                                if (whiteDir == directionsControl.getDirectionByColor(anotherTile.color))
                                {
                                    if (whiteDir == 'R') {
                                        moves.add(performMoves("S"))
                                        moves.add(performMoves(downMove))
                                        moves.add(performMoves("S'"))
                                    } else if (whiteDir == 'L') {
                                        moves.add(performMoves("S'"))
                                        moves.add(performMoves(downMove))
                                        moves.add(performMoves("S"))
                                    } else if (whiteDir == 'F') {
                                        moves.add(performMoves("M"))
                                        moves.add(performMoves(downMove))
                                        moves.add(performMoves("M'"))
                                    } else if (whiteDir == 'B') {
                                        moves.add(performMoves("M'"))
                                        moves.add(performMoves(downMove))
                                        moves.add(performMoves("M"))
                                    }
                                } else {
                                    moves.add(performMoves("D"))
                                }
                            }
                            //white tile is on down
                            else if (whiteTile.direction == 'D') {
                                //check if cubie on the right layer
                                if (directionsControl.getColorByDirection(anotherTile.direction) != anotherTile.color) {
                                    if (qb.getNormalVectorAfterRotation(anotherTile, 90f, 'D')
                                        == directionsControl.getDirectionByColor(anotherTile.color)
                                    ) {
                                        moves.add(performMoves("D"))
                                    } else {
                                        moves.add(performMoves("D'"))
                                    }
                                } else {
                                    moves.add(performMoves(anotherTile.direction.toString()))
                                    moves.add(performMoves(anotherTile.direction.toString()))
                                }
                            } else if (anotherTile.direction == 'U') {
                                moves.add(performMoves(whiteTile.direction.toString()))
                                moves.add(performMoves(whiteTile.direction.toString()))
                            }
                            //tile is on the equator layer
                            else if(directionsControl.getDirectionByColor(anotherTile.color) == anotherTile.direction){
                                if(qb.getNormalVectorAfterRotation(whiteTile, 90f, anotherTile.direction) == 'U'){
                                    moves.add(performMoves(anotherTile.direction.toString()))
                                }
                                else{
                                    moves.add(performMoves(anotherTile.direction + "'"))
                                }
                            }
                            else{
                                moves.add(performMoves(whiteTile.direction + "'"))
                            }
                        }
                        Thread.sleep(10)
                    }
                }
            }
            Thread.sleep(10)
        }
        return moves
    }

    /**
     * Complete white side of the cube
     */
    fun finishWhiteLayer(): ArrayList<String> {
        var moves = ArrayList<String>()

        //calculate white corners layout
        while (numWhiteCornersOriented() < 4) {
            if (getPermutationAllowance()) {
                var tempCubies =
                    cubies.filter { x -> x.isCorner && x.tiles.any { t -> t.color == Color.WHITE } }
                for (qb in tempCubies) {
                    while (!isCornerRightOriented(qb)) {
                        if (getPermutationAllowance()) {
                            //find all edges with white tiles
                            var tempTiles = qb.tiles.filter { x -> x.isActive }
                            var whiteTile =
                                tempTiles.filter { x -> x.color == Color.WHITE }.single()
                            var firstTile = tempTiles.filter { x -> x.color != Color.WHITE }[0]
                            var secondTile = tempTiles.filter { x -> x.color != Color.WHITE }[1]

                            Log.i("info", ""+firstTile.color +" dir = " + firstTile.direction + "  " + secondTile.color + " dir = " + secondTile.direction + "  White dir = " + whiteTile.direction)

                            //WHITE is UP
                            if (whiteTile.direction == 'U') {
                                var c = firstTile.direction
                                if (qb.getNormalVectorAfterRotation(
                                        secondTile,
                                        90f,
                                        firstTile.direction
                                    ) == 'D'
                                //|| qb.getNormalVectorAfterRotation(firstTile, 90f, secondTile.direction) == 'D'
                                ) {
                                    moves.add(performMoves(c.toString()))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(c + "'"))
                                } else {
                                    moves.add(performMoves(c + "'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves(c.toString()))
                                }
                            }

                            //WHITE is DOWN
                            else if (whiteTile.direction == 'D') {
                                //if one of tiles is on the right side of another
                                if (firstTile.color == directionsControl.getColorByDirection(
                                        secondTile.direction
                                    )
                                    || secondTile.color == directionsControl.getColorByDirection(
                                        firstTile.direction
                                    )
                                ) {
                                    var c = secondTile.direction
                                    if (qb.getNormalVectorAfterRotation(
                                            firstTile,
                                            -90f,
                                            secondTile.direction
                                        ) == 'D'
                                    ) {
                                        moves.add(performMoves(c + "'"))
                                        var whiteC = whiteTile.direction
                                        if (qb.getNormalVectorAfterRotation(
                                                secondTile,
                                                -90f,
                                                whiteC
                                            ) == 'D'
                                        ) {
                                            moves.add(performMoves(whiteC + "'"))
                                            moves.add(performMoves("D"))
                                            moves.add(performMoves("D"))
                                            moves.add(performMoves(whiteC.toString()))
                                            moves.add(performMoves(c.toString()))
                                        } else {
                                            moves.add(performMoves(whiteC.toString()))
                                            moves.add(performMoves("D'"))
                                            moves.add(performMoves("D'"))
                                            moves.add(performMoves(whiteC + "'"))
                                            moves.add(performMoves(c.toString()))
                                        }
                                    } else {
                                        moves.add(performMoves(c.toString()))
                                        var whiteC1 = whiteTile.direction
                                        if (qb.getNormalVectorAfterRotation(
                                                secondTile,
                                                90f,
                                                whiteC1
                                            ) == 'D'
                                        ) {
                                            moves.add(performMoves(whiteC1.toString()))
                                            moves.add(performMoves("D"))
                                            moves.add(performMoves("D"))
                                            moves.add(performMoves(whiteC1 + "'"))
                                            moves.add(performMoves(c + "'"))
                                        } else {
                                            moves.add(performMoves(whiteC1 + "'"))
                                            moves.add(performMoves("D'"))
                                            moves.add(performMoves("D'"))
                                            moves.add(performMoves(whiteC1.toString()))
                                            moves.add(performMoves(c + "'"))
                                        }
                                    }
                                } else if (secondTile.color == directionsControl.getColorByDirection(
                                        firstTile.direction
                                    )
                                ) {
                                    var c = firstTile.direction
                                    if (qb.getNormalVectorAfterRotation(
                                            secondTile,
                                            90f,
                                            c
                                        ) == 'D'
                                    ) {
                                        moves.add(performMoves(c + "'"))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves(c.toString()))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves(c + "'"))
                                        moves.add(performMoves("D'"))
                                        moves.add(performMoves(c.toString()))
                                    } else {
                                        //moves += performMoves(c + " D' " + c + "' D' " + c + " D " + c + "'")
                                        moves.add(performMoves(c.toString()))
                                        moves.add(performMoves("D'"))
                                        moves.add(performMoves(c + "'"))
                                        moves.add(performMoves("D'"))
                                        moves.add(performMoves(c.toString()))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves(c + "'"))
                                    }
                                } else {
                                    if (qb.getNormalVectorAfterRotation(firstTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                        || qb.getNormalVectorAfterRotation(firstTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)

                                        || qb.getNormalVectorAfterRotation(secondTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)
                                        || qb.getNormalVectorAfterRotation(secondTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                    ) {
                                        moves.add(performMoves("D'"))
                                    } else {
                                        moves.add(performMoves("D"))
                                    }

                                }
                                //check
                                /*else {
                                    moves.add(performMoves("D"))
                                }*/
                            }

                            //TILE is UP
                            else if (firstTile.direction == 'U' || secondTile.direction == 'U') {
                                var c = whiteTile.direction
                                if (qb.getNormalVectorAfterRotation(firstTile, -90f, c) == 'D' ||
                                    qb.getNormalVectorAfterRotation(secondTile, -90f, c) == 'D'
                                ) {
                                    moves.add(performMoves(c + "'"))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(c.toString()))
                                } else {
                                    moves.add(performMoves(c.toString()))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(c + "'"))
                                }
                            }

                            //TILE is DOWN
                            else {
                                if (directionsControl.getColorByDirection(firstTile.direction) == firstTile.color
                                    && directionsControl.getColorByDirection(whiteTile.direction) == secondTile.color
                                ) {
                                    var c = directionsControl.getDirectionByColor(firstTile.color)
                                    var downMove = "D"
                                    if (qb.getNormalVectorAfterRotation(firstTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)
                                    ) {
                                        downMove = "D'"
                                    }
                                    //change 04.09.2019

                                    /*moves.add(performMoves("D'"))
                                    moves.add(performMoves(c + "'"))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(c.toString()))*/

                                    moves.add(performMoves(downMove))
                                    if (downMove == "D") {
                                        moves.add(performMoves(c.toString()))
                                        moves.add(performMoves("D'"))
                                        moves.add(performMoves(c + "'"))
                                    } else {
                                        moves.add(performMoves(c + "'"))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves(c.toString()))
                                    }

                                } else if (directionsControl.getColorByDirection(secondTile.direction) == secondTile.color
                                    && directionsControl.getColorByDirection(whiteTile.direction) == firstTile.color
                                ) {
                                    //change 04.09.2019
                                    if (qb.getNormalVectorAfterRotation(
                                            secondTile,
                                            -90f,
                                            'D'
                                        )//whiteTile.direction)
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                    ) {
                                        moves.add(performMoves("D'"))
                                        moves.add(performMoves(directionsControl.getDirectionByColor(secondTile.color) + "'"))
                                        moves.add(performMoves("D"))
                                        moves.add(performMoves(directionsControl.getDirectionByColor(secondTile.color).toString()))
                                    } else {
                                        moves.add(performMoves("D"))
                                        moves.add(
                                            performMoves(
                                                directionsControl.getDirectionByColor(
                                                    secondTile.color
                                                ).toString()
                                            )
                                        )
                                        moves.add(performMoves("D'"))
                                        moves.add(
                                            performMoves(
                                                directionsControl.getDirectionByColor(
                                                    secondTile.color
                                                ) + "'"
                                            )
                                        )
                                    }
                                }
                                //04.09.2019
                                //if one of tiles is down
                                /*else if(qb.getNormalVectorAfterRotation(whiteTile, 90f, 'D') == directionsControl.getDirectionByColor(firstTile.color)
                                    && qb.getNormalVectorAfterRotation(secondTile, 90f, 'D') == directionsControl.getDirectionByColor(secondTile.color)){
                                    moves.add(performMoves(directionsControl.getDirectionByColor(secondTile.color) + "'"))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(directionsControl.getDirectionByColor(secondTile.color).toString()))
                                }
                                else if(qb.getNormalVectorAfterRotation(whiteTile, 90f, 'D') == directionsControl.getDirectionByColor(secondTile.color)
                                    && qb.getNormalVectorAfterRotation(firstTile, 90f, 'D') == directionsControl.getDirectionByColor(firstTile.color)){
                                    moves.add(performMoves(directionsControl.getDirectionByColor(firstTile.color) + "'"))
                                    moves.add(performMoves("D"))
                                    moves.add(performMoves(directionsControl.getDirectionByColor(firstTile.color).toString()))
                                }*/
                                else {
                                    if (qb.getNormalVectorAfterRotation(firstTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                        || qb.getNormalVectorAfterRotation(firstTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)

                                        || qb.getNormalVectorAfterRotation(secondTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)
                                        || qb.getNormalVectorAfterRotation(secondTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(firstTile.color)

                                        || qb.getNormalVectorAfterRotation(whiteTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                        || qb.getNormalVectorAfterRotation(whiteTile, -90f, 'D')
                                        == directionsControl.getDirectionByColor(secondTile.color)
                                    ) {
                                        moves.add(performMoves("D'"))
                                    } else {
                                        moves.add(performMoves("D"))
                                    }
                                }
                            }
                        }
                        Thread.sleep(10)
                    }
                }
            }
            Thread.sleep(10)
        }

        var greenCenterCubie = cubies.single { x -> !x.isCorner && !x.isEdge && x.tiles.any { t -> t.color == Color.GREEN } }
        if(greenCenterCubie.tiles.single { x -> x.isActive }.direction != 'F'){
            var tile = greenCenterCubie.tiles.single { x -> x.isActive }
            if(tile.direction == 'L'){
                moves.add(performMoves("E'"))
            }
            else if(tile.direction == 'R'){
                moves.add(performMoves("E"))
            }
            else{
                moves.add(performMoves("E"))
                moves.add(performMoves("E"))
            }
        }

        return moves
    }

    fun finishTwoLayers() : ArrayList<String>{
        var moves = ArrayList<String>()

        // rotate the whole cube
        /*rotationAxis = Axis.zAxis
        rotationAngle = 90f
        //rotateCube(180f, rotationAxis)
        moves.add(performMoves("Z"))
        moves.add(performMoves("Z"))*/

        //calculate white corners layout
        while(numEdgesOriented() < 4) {
            var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.all{t -> t.color != Color.WHITE && t.color != Color.YELLOW}}
            for (qb in tempCubies) {
                while (!isEdgeRightOriented(qb)) {
                    if (getPermutationAllowance()) {
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var firstTile = tempTiles[0]
                        var secondTile = tempTiles[1]

                        var trueFaceTile = qb.tiles.filter { x -> x.color == directionsControl.getColorByDirection(x.direction) }.singleOrNull()

                        //if cubie is on the side
                        if(firstTile.direction != 'D' && secondTile.direction != 'D'){
                            var c1 = secondTile.direction
                            var c2 = firstTile.direction
                            moves.add(performMoves("D"))
                            moves.add(performMoves(c1.toString()))
                            moves.add(performMoves("D'"))
                            moves.add(performMoves(c1 + "'"))
                            moves.add(performMoves("D'"))
                            moves.add(performMoves(c2 + "'"))
                            moves.add(performMoves("D"))
                            moves.add(performMoves(c2.toString()))
                            /*moves += performMoves("U U U")
                            moves += performMoves(c2.toString())
                            moves += performMoves("U'")
                            moves += performMoves(c2 + "'")
                            moves += performMoves("U'")
                            moves += performMoves(c1 + "'")
                            moves += performMoves("U")
                            moves += performMoves(c1.toString())*/
                        }

                        //cubie should be rotated
                        else if(firstTile.color == directionsControl.getColorByDirection(secondTile.direction)
                            && secondTile.color == directionsControl.getColorByDirection(firstTile.direction))
                        {
                            var c1 = secondTile.direction
                            var c2 = firstTile.direction
                            if(qb.getNormalVectorAfterRotation(firstTile, 90f, secondTile.direction) == 'D'){
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c1.toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c1 + "'"))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c2 + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c2.toString()))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c1.toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c1 + "'"))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c2 + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c2.toString()))
                            }
                            else{
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c1 + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c1.toString()))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c2.toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c2 + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c1 + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c1.toString()))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(c2.toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(c2 + "'"))
                            }
                        }

                        //first case one tile is on the right side
                        //second tile should be moved to the left side
                        else if(trueFaceTile != null){
                            var notTrueFaceTile = tempTiles.filter { x -> x.color != trueFaceTile.color }.single()

                            if(qb.getNormalVectorAfterRotation(trueFaceTile, 90f, notTrueFaceTile.direction)
                                == directionsControl.getDirectionByColor(notTrueFaceTile.color)){
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color) + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color).toString()))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(trueFaceTile.color).toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(trueFaceTile.color) + "'"))
                            }
                            else{
                                moves.add(performMoves("D"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color).toString()))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color) + "'"))
                                moves.add(performMoves("D'"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(trueFaceTile.color) + "'"))
                                moves.add(performMoves("D"))
                                moves.add(performMoves(directionsControl.getDirectionByColor(trueFaceTile.color).toString()))
                            }
                        }
                        else {
                            moves.add(performMoves("D"))
                        }
                    }
                }
            }
        }
        return moves
    }

    fun makeYellowCross() : ArrayList<String> {
        var moves = ArrayList<String>()

        //calculate yellow edges layout
        while (numYellowEdgesOriented() < 4) {
            if (getPermutationAllowance()) {
                var tempCubies =
                    cubies.filter { x -> x.isEdge && x.tiles.any { t -> t.color == Color.YELLOW } }
                var tilesForRotationUP = 0

                for (qb in tempCubies) {
                    if (getPermutationAllowance()) {
                        var tempTiles = qb.tiles.filter { x -> x.isActive }

                        //check if yellow tile is down and on the left or front layer
                        if (tempTiles.single { t -> t.color == Color.YELLOW }.direction == 'D') {
                            if (tempTiles.single { t -> t.color != Color.YELLOW }.direction == 'L' ||
                                tempTiles.single { t -> t.color != Color.YELLOW }.direction == 'F'
                            ) {
                                tilesForRotationUP++
                                continue
                            }
                        }
                    }
                }

                if (tilesForRotationUP >= 2) {
                    rotationAxis = Axis.yAxis
                    rotationAngle = 90f//180f
                    moves.add(performMoves("Y"))
                    moves.add(performMoves("Y"))
                }
                else if(tilesForRotationUP == 1){
                    rotationAxis = Axis.yAxis
                    rotationAngle = -90f
                    moves.add(performMoves("Y'"))
                }
                moves.add(performMoves("L'"))
                moves.add(performMoves("D'"))
                moves.add(performMoves("F'"))
                moves.add(performMoves("D"))
                moves.add(performMoves("F"))
                moves.add(performMoves("L"))
            }
            Thread.sleep(10)
        }

        if (directionsControl.getDirectionByColor(Color.GREEN) == 'B') {
            rotationAxis = Axis.yAxis
            rotationAngle = 90f//180f
            moves.add(performMoves("Y"))
            moves.add(performMoves("Y"))
        }
        return moves
    }

    fun swapYellowEdgesTopLayer() : ArrayList<String> {
        var moves = ArrayList<String>()

        if(numYellowEdgeCubieOriented() < 4) {
            if(getPermutationAllowance()) {
                var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any { y -> y.color == Color.YELLOW } }
                //find the right cubie
                /*var qb =
                    tempCubies.filter { qb -> qb.tiles.any { t -> t.isActive && directionsControl.getColorByDirection('R') == t.color } }
                        .single()
                var rightTile = qb.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('R') }.single()
                while (directionsControl.getDirectionByColor(rightTile.color) != rightTile.direction) {
                    if (getPermutationAllowance()) {
                        moves.add(performMoves("D"))
                    }
                }*/
                while (numYellowEdgeCubieOriented() < 4) {
                    if(getPermutationAllowance()) {
                        var frontCubie =
                            tempCubies.filter { qb -> qb.tiles.any { t -> t.isActive && directionsControl.getColorByDirection('F') == t.color } }
                                .single()
                        var leftCubie =
                            tempCubies.filter { qb -> qb.tiles.any { t -> t.isActive && directionsControl.getColorByDirection('L') == t.color } }
                                .single()
                        var backCubie =
                            tempCubies.filter { qb -> qb.tiles.any { t -> t.isActive && directionsControl.getColorByDirection('B') == t.color } }
                                .single()

                        if (!isYellowEdgeRightOriented(frontCubie)) {
                            if (!isYellowEdgeRightOriented(leftCubie)) {
                                //if it's need to change front and left
                                var frontTile =
                                    frontCubie.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('F') }
                                        .single()
                                var leftTile =
                                    leftCubie.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('L') }
                                        .single()
                                if (frontTile.direction == directionsControl.getDirectionByColor(leftTile.color)
                                    && leftTile.direction == directionsControl.getDirectionByColor(frontTile.color)
                                ) {
                                    //moves += performMoves("R U R' U R U U R' U")
                                    moves.add(performMoves("R'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("R"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("R'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("R"))
                                    moves.add(performMoves("D'"))
                                }else{
                                    rotationAxis = Axis.yAxis
                                    rotationAngle = 90f
                                    moves.add(performMoves("Y"))
                                }
                            } else if (!isYellowEdgeRightOriented(backCubie)) {
                                var frontTile =
                                    frontCubie.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('F') }
                                        .single()
                                var backTile =
                                    backCubie.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('B') }
                                        .single()

                                if (frontTile.direction == directionsControl.getDirectionByColor(backTile.color)
                                    && backTile.direction == directionsControl.getDirectionByColor(frontTile.color)
                                ) {
                                    //moves += performMoves("U B U B' U B U U B' U F U F' U F U U F' U")
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("B'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("B"))

                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("B'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("D'"))

                                    moves.add(performMoves("B"))
                                    moves.add(performMoves("D'"))


                                    rotationAngle = 90f
                                    rotationAxis = Axis.yAxis
                                    moves.add(performMoves("Y"))
                                    moves.add(performMoves("Y"))

                                    moves.add(performMoves("B'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("B"))
                                    moves.add(performMoves("D'"))

                                    moves.add(performMoves("B'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("B"))
                                    moves.add(performMoves("D'"))

                                    //07.09.19
                                    /*moves.add(performMoves("F"))
                                    moves.add(performMoves("D'"))

                                    moves.add(performMoves("F'"))
                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("F"))
                                    moves.add(performMoves("D'"))

                                    moves.add(performMoves("D'"))
                                    moves.add(performMoves("F'"))
                                    moves.add(performMoves("D'"))*/
                                }
                                else{
                                    rotationAxis = Axis.yAxis
                                    rotationAngle = 90f
                                    moves.add(performMoves("Y"))
                                }
                            }
                            else{
                                rotationAxis = Axis.yAxis
                                rotationAngle = 90f
                                moves.add(performMoves("Y"))
                            }
                        }
                        else if(isYellowEdgeRightOriented(frontCubie) && isYellowEdgeRightOriented(leftCubie)){
                            rotationAxis = Axis.yAxis
                            rotationAngle = 90f
                            moves.add(performMoves("Y"))
                            moves.add(performMoves("Y"))
                        }
                        else {
                            //change back and left
                            //moves += performMoves("F U F' U F U U F' U")
                            moves.add(performMoves("F'"))
                            moves.add(performMoves("D'"))
                            moves.add(performMoves("F"))
                            moves.add(performMoves("D'"))

                            moves.add(performMoves("F'"))
                            moves.add(performMoves("D'"))
                            moves.add(performMoves("D'"))
                            moves.add(performMoves("F"))
                            moves.add(performMoves("D'"))
                        }
                    }
                    Thread.sleep(10)
                }
            }
        }

        return moves
    }

    //rotate cube to get right cubie in front of user
    fun findRightOrientedYellowCubie() : ArrayList<String> {
        var moves = ArrayList<String>()
        while(numYellowCornersOnPlace() != 4) {
            var num = numYellowCornersOnPlace()
            var tempCubies =
                cubies.filter { x -> x.isCorner && x.tiles.any { y -> y.color == Color.YELLOW } }

            //first case one cubie is on the right place
            if(num != 0) {
                //move the cube to make the right cubie in front
                for (qb in tempCubies) {
                    if (isYellowCornerOnRightPlace(qb)) {
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var yellowTile = tempTiles.single { x -> x.color == Color.YELLOW }
                        var firstTile = tempTiles.filter { x -> x.color != Color.YELLOW }[0]
                        var secondTile = tempTiles.filter { x -> x.color != Color.YELLOW }[1]

                        if ((yellowTile.direction == 'F' || firstTile.direction == 'F' || secondTile.direction == 'F')
                            && (yellowTile.direction == 'L' || firstTile.direction == 'L' || secondTile.direction == 'L')
                        ) {
                        } else {
                            if ((qb.getNormalVectorAfterRotation(firstTile, 180f, 'D') == 'F'
                                        || qb.getNormalVectorAfterRotation(
                                    secondTile,
                                    180f,
                                    'D'
                                ) == 'F'
                                        || qb.getNormalVectorAfterRotation(
                                    yellowTile,
                                    180f,
                                    'D'
                                ) == 'F') &&
                                (qb.getNormalVectorAfterRotation(firstTile, 180f, 'D') == 'L'
                                        || qb.getNormalVectorAfterRotation(
                                    secondTile,
                                    180f,
                                    'D'
                                ) == 'L'
                                        || qb.getNormalVectorAfterRotation(
                                    yellowTile,
                                    180f,
                                    'D'
                                ) == 'L')
                            ) {
                                rotationAxis = Axis.yAxis
                                rotationAngle = 90f
                                moves.add(performMoves("Y"))
                                moves.add(performMoves("Y"))
                            } else if ((qb.getNormalVectorAfterRotation(firstTile, 90f, 'D') == 'F'
                                        || qb.getNormalVectorAfterRotation(
                                    secondTile,
                                    90f,
                                    'D'
                                ) == 'F'
                                        || qb.getNormalVectorAfterRotation(
                                    yellowTile,
                                    90f,
                                    'D'
                                ) == 'F') &&
                                (qb.getNormalVectorAfterRotation(firstTile, 90f, 'D') == 'L'
                                        || qb.getNormalVectorAfterRotation(
                                    secondTile,
                                    90f,
                                    'D'
                                ) == 'L'
                                        || qb.getNormalVectorAfterRotation(
                                    yellowTile,
                                    90f,
                                    'D'
                                ) == 'L')
                            ) {
                                rotationAxis = Axis.yAxis
                                rotationAngle = 90f
                                moves.add(performMoves("Y"))
                            } else {
                                rotationAxis = Axis.yAxis
                                rotationAngle = -90f
                                moves.add(performMoves("Y'"))
                            }
                        }
                        break
                    }
                }
            }


            /*for (qb in tempCubies) {
                while (!getPermutationAllowance()) {
                    Thread.sleep(10)
                }
                var tempTiles = qb.tiles.filter { x -> x.isActive }
                var yellowTile = tempTiles.single { x -> x.color == Color.YELLOW }
                var firstTile = tempTiles.filter { x -> x.color != Color.YELLOW }[0]
                var secondTile = tempTiles.filter { x -> x.color != Color.YELLOW }[1]


                if (yellowTile.direction == directionsControl.getDirectionByColor(firstTile.color) ||
                    yellowTile.direction == directionsControl.getDirectionByColor(secondTile.color) ||
                    yellowTile.direction == 'D' &&
                    (directionsControl.getDirectionByColor(firstTile.color) == secondTile.direction
                            || directionsControl.getDirectionByColor(firstTile.color) == firstTile.direction
                            || directionsControl.getDirectionByColor(firstTile.color) ==
                            directionsControl.getDirectionByColor(secondTile.color))
                ) {
                    if (firstTile.direction != 'F' || secondTile.direction != 'F' || yellowTile.direction != 'F') {
                        if (qb.getNormalVectorAfterRotation(firstTile, 180f, 'D') == 'F'
                            || qb.getNormalVectorAfterRotation(secondTile, 180f, 'D') == 'F'
                            || qb.getNormalVectorAfterRotation(yellowTile, 180f, 'D') == 'F'
                        ) {
                            rotationAxis = Axis.yAxis
                            rotationAngle = 90f
                            moves.add(performMoves("Y"))
                            moves.add(performMoves("Y"))
                        } else if (qb.getNormalVectorAfterRotation(firstTile, 90f, 'D') == 'F'
                            || qb.getNormalVectorAfterRotation(secondTile, 90f, 'D') == 'F'
                            || qb.getNormalVectorAfterRotation(yellowTile, 90f, 'D') == 'F'
                        ) {
                            rotationAxis = Axis.yAxis
                            rotationAngle = 90f
                            moves.add(performMoves("Y"))
                        } else {
                            rotationAxis = Axis.yAxis
                            rotationAngle = -90f
                            moves.add(performMoves("Y'"))
                        }
                    }
                    //break
                }
            }*/

            // moves += performMoves("U F U' B' U F' U' B")
            moves.add(performMoves("D'"))
            moves.add(performMoves("F'"))
            moves.add(performMoves("D"))
            moves.add(performMoves("B"))
            moves.add(performMoves("D'"))
            moves.add(performMoves("F"))
            moves.add(performMoves("D"))
            moves.add(performMoves("B'"))
        }
        return moves
    }

    fun finishSolvingYellowCorners() : ArrayList<String>{
        var moves = ArrayList<String>()

        while(numYellowCornersOriented() < 4) {
            var num = numYellowCornersOriented()
            if (getPermutationAllowance()) {
                var tempCubies = cubies.filter { qb -> qb.isCorner && !isYellowCornerOriented(qb) && qb.tiles.any{x -> x.color == Color.YELLOW}}

                if(tempCubies.size != 0) {
                    for (cubie in tempCubies) {
                        var cubieTiles = cubie.tiles.filter { x -> x.isActive }
                        while (!isYellowCornerOriented(cubie)) {
                            if (cubieTiles.any { t -> t.direction == 'F' } && cubieTiles.any { t -> t.direction == 'L' }) {
                                moves = performCornerCubieRotation(moves, 2)
                            } else if ((cubie.getNormalVectorAfterRotation(
                                    cubieTiles[0],
                                    90f,
                                    'D'
                                ) == 'F'
                                        || cubie.getNormalVectorAfterRotation(
                                    cubieTiles[0],
                                    90f,
                                    'D'
                                ) == 'L')
                                && (cubie.getNormalVectorAfterRotation(
                                    cubieTiles[1],
                                    90f,
                                    'D'
                                ) == 'L' ||
                                        cubie.getNormalVectorAfterRotation(
                                            cubieTiles[1],
                                            90f,
                                            'D'
                                        ) == 'F')
                            ) {
                                moves.add(performMoves("D"))
                            } else {
                                moves.add(performMoves("D'"))
                            }
                        }
                    }
                }
                else{
                    moves.add(performMoves("D"))
                }
            }
            Thread.sleep(10)
        }

        return moves
    }

    fun performCornerCubieRotation(moves : ArrayList<String>, count : Int) : ArrayList<String>{
        for(k in 0 until count){
            moves.add(performMoves("F"))
            moves.add(performMoves("U"))
            moves.add(performMoves("F'"))
            moves.add(performMoves("U'"))
        }
        return moves
    }

    //endregion

    //region Cubies Orients

    //check if cubie on the right corner place
    fun isCornerRightOriented(cubie : ICubie) : Boolean {
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if (tempTiles.any{x -> x.color == Color.WHITE && x.direction == 'U'}) {
            if(tempTiles.any{x -> x.color == directionsControl.getColorByDirection(x.direction) && x.color != Color.WHITE}){
                return true
            }
        }
        return false
    }

    fun isEdgeRightOriented(cubie : ICubie) : Boolean{
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if(tempTiles[0].direction == directionsControl.getDirectionByColor(tempTiles[0].color)
            && tempTiles[1].direction == directionsControl.getDirectionByColor(tempTiles[1].color)){
            return true
        }
        return false
    }

    fun isYellowEdgeRightOriented(qb : ICubie) : Boolean{
        var tempTiles = qb.tiles.filter { x -> x.isActive }
        if(tempTiles[0].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
            || tempTiles[1].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction){
            return true
        }
        return false
    }

    fun isYellowCornerOriented(qb : LogicCubie) : Boolean {
        /*var tempTiles = qb.tiles.filter { x -> x.isActive }
        if(directionsControl.getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction
            && directionsControl.getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
            && directionsControl.getDirectionByColor(tempTiles[2].color) == tempTiles[2].direction){
            return true
        }
        return false*/

        if(qb.tiles.any { x -> x.color == Color.YELLOW }) {
            var yellowTile = qb.tiles.single { x -> x.color == Color.YELLOW }
            if (yellowTile.direction == 'D') {
                return true
            }
        }
        return false
    }

    fun isYellowCornerOnRightPlace(qb : LogicCubie) : Boolean{
        var tempTiles = qb.tiles.filter { x -> x.isActive }
        var yellowTile = tempTiles.single { x -> x.color == Color.YELLOW }
        var firstTile = tempTiles.filter { x -> x.color != Color.YELLOW }[0]
        var secondTile = tempTiles.filter { x -> x.color != Color.YELLOW }[1]

        if (yellowTile.direction == directionsControl.getDirectionByColor(firstTile.color) ||
            yellowTile.direction == directionsControl.getDirectionByColor(secondTile.color) ||
            yellowTile.direction == 'D' &&
            (directionsControl.getDirectionByColor(firstTile.color) == secondTile.direction
                    || directionsControl.getDirectionByColor(firstTile.color) == firstTile.direction
                    || directionsControl.getDirectionByColor(secondTile.color) == firstTile.direction ||
                    directionsControl.getDirectionByColor(secondTile.color) == secondTile.direction))
        {
            return true
        }
        return false
    }

    //endregion

    //region CubieNums
    fun numWhiteEdgesOriented(): Int {
        var numOriented = 0
        for (cubie in cubies) {
            if (cubie.isEdge) {
                if (cubie.tiles.any { x -> x.isActive && x.color == Color.WHITE && x.direction == 'U'}) {
                    numOriented++
                }
            }
        }
        return numOriented
    }

    fun numWhiteCornersOriented() : Int{
        var num = 0
        for(qb in cubies){
            if(qb.isCorner && qb.tiles.any{x -> x.color == Color.WHITE && x.direction == 'U'}){
                var tempTiles = qb.tiles.filter { x -> x.color != Color.WHITE }
                for(tile in tempTiles){
                    if(tile.color == directionsControl.getColorByDirection(tile.direction)){
                        num++
                        break
                    }
                }
            }
        }
        return num
    }

    fun numYellowEdgesOriented() : Int{
        var num = 0
        for(qb in cubies){
            if(qb.isEdge && qb.tiles.any{x -> x.color == Color.YELLOW && x.direction == 'D'}){
                num++
            }
        }
        return num
    }

    fun numYellowEdgeCubieOriented() : Int{
        var num = 0
        for(qb in cubies){
            if(qb.isEdge && qb.tiles.any{x -> x.color == Color.YELLOW}){
                var tempTiles = qb.tiles.filter { x -> x.isActive }
                if(tempTiles[0].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
                    || tempTiles[1].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction){
                    num++
                }
            }
        }
        return num
    }

    fun numYellowCornersOnPlace() : Int{
        var num = 0
        for(qb in cubies){
            if(qb.isCorner && qb.tiles.any{x -> x.color == Color.YELLOW}){
                var tempTiles = qb.tiles.filter { x -> x.isActive }
                var yellowTile = tempTiles.single { t -> t.color == Color.YELLOW}
                var firstTile = tempTiles.filter{t -> t.color != Color.YELLOW}[0]
                var secondTile = tempTiles.single {t -> t.color != Color.YELLOW && t.color != firstTile.color}

                if(yellowTile.direction == 'D') {
                    if (directionsControl.getDirectionByColor(firstTile.color) == firstTile.direction &&
                        directionsControl.getDirectionByColor(secondTile.color) == secondTile.direction
                    ) {
                        num++
                    }
                }
                else if((directionsControl.getDirectionByColor(firstTile.color) == secondTile.direction ||
                    directionsControl.getDirectionByColor(firstTile.color) == firstTile.direction ||
                    directionsControl.getDirectionByColor(firstTile.color) == yellowTile.direction) &&
                    (directionsControl.getDirectionByColor(secondTile.color) == secondTile.direction ||
                            directionsControl.getDirectionByColor(secondTile.color) == firstTile.direction ||
                            directionsControl.getDirectionByColor(secondTile.color) == yellowTile.direction))
                {
                    num++
                }
            }
        }
        return num
    }

    fun numYellowCornersOriented() : Int{
        var num = 0
        for(qb in cubies){
            if(qb.isCorner && qb.tiles.any{x -> x.color == Color.YELLOW}){
                var tempTiles = qb.tiles.filter { x -> x.isActive }
                if(tempTiles.all { x -> x.direction == directionsControl.getDirectionByColor(x.color) }){
                    num++
                }
            }
        }
        return num
    }

    //how many cubies are right oriented for two layers
    fun numEdgesOriented() : Int{
        var num = 0
        for(qb in cubies) {
            if (qb.isEdge) {
                if (qb.tiles.all { x -> x.color != Color.WHITE && x.color != Color.YELLOW }) {
                    var tempTiles = qb.tiles.filter { x -> x.isActive }
                    if (tempTiles[0].color == directionsControl.getColorByDirection(tempTiles[0].direction)
                        && tempTiles[1].color == directionsControl.getColorByDirection(tempTiles[1].direction))
                    {
                        num++
                        continue
                    }
                }
            }
        }
        return num
    }

    //endregion

    override fun performMoves(moves: String): String {
        var i = 0
        while (i < moves.length) {
            if (getPermutationAllowance()) {
                if (moves.substring(i, i + 1) !== " ") { //Only check if there is a meaningful character
                    if (i != moves.length - 1) {
                        if (moves.substring(i + 1, i + 2).compareTo("2") == 0) {
                            //Turning twice ex. U2
                            turn(moves.substring(i, i + 1))
                            turn(moves.substring(i, i + 1))
                            i++ //Skip the "2" for the next iteration
                        } else if (moves.substring(i + 1, i + 2).compareTo("'") == 0) {
                            //Making a counterclockwise turn ex. U'
                            turn(moves.substring(i, i + 2))
                            i++ //Skip the apostrophe for the next iteration
                        } else {
                            //Regular clockwise turning
                            turn(moves.substring(i, i + 1))
                        }
                    } else {
                        //Nothing is after the turn letter, so just perform the turn
                        turn(moves.substring(i, i + 1))
                    }
                }
                i++
            } else {
                Thread.sleep(10)
            }
        }
        return moves
    }

    override fun resetLayerCubies() {
        var count = cubies.count { x -> x.isRotated }
        if (count == 9) {
            for (layer in layers) {
                layer.cubies.clear()
                layer.cubiesIds.clear()
                for (cubie in cubies) {
                    if (cubie.centerPoint.getCoordinateByAxis(LayerEnum.getRotationAxisByLayerName(layer.layerName)) == layer.centerPoint) {
                        layer.addCubie(cubie)
                        cubie.isRotated = false
                    }
                }
            }
            setPermutationAllowance(true)
            Thread.sleep(10)
        }
        //cube rotated 180 degrees
        else if (count == 27) {
            directionsControl.updateDirectionsAfterRotation(rotationAngle, rotationAxis)
            for (layer in layers) {
                layer.cubies.clear()
                layer.cubiesIds.clear()
                for (cubie in cubies) {
                    if (cubie.centerPoint.getCoordinateByAxis(LayerEnum.getRotationAxisByLayerName(layer.layerName)) == layer.centerPoint) {
                        layer.addCubie(cubie)
                        cubie.isRotated = false
                    }
                }
            }
            setPermutationAllowance(true)
            Thread.sleep(10)
        }
        else{
            //problem with rotation
            //it's necessary to return cubies back and try to rotate again
            var rotatedCubies = cubies.filter { x -> x.isRotated }
            var angle: Float
            var curr = ""
            if(currentTurn.contains("'")){
                curr = currentTurn.substring(0, 1)
                angle = 90f
            }
            else{
                angle = -90f
            }
            var layer = LayerEnum.values().single{x -> x.charName == curr[0] }
            for(qb in rotatedCubies){
                qb.rotate(angle, LayerEnum.getRotationAxisByLayerName(layer))
            }
            Thread.sleep(10)
            turn(currentTurn)
            Thread.sleep(10)
        }

        var downLayer = layers.single{x -> x.layerName == LayerEnum.DOWN}
        var tempLayer = LogicLayer.CloneLayer(downLayer, this)
        var cubies = sortCubiesForLayer(tempLayer) as List<LogicCubie>
        Solver.addPhaseLayer(cubies)
    }

    override fun turn(turn: String) {
        when (turn) {
            "B" -> {
                layers.filter { x -> x.layerName == LayerEnum.BACK }.single().rotate(90f)
            }
            "B'" -> {
                layers.filter { x -> x.layerName == LayerEnum.BACK }.single().rotate(-90f)
            }
            "D" -> {
                layers.filter { x -> x.layerName == LayerEnum.DOWN }.single().rotate(90f)
            }
            "D'" -> {
                layers.filter { x -> x.layerName == LayerEnum.DOWN }.single().rotate(-90f)
            }
            "E" -> {
                layers.filter { x -> x.layerName == LayerEnum.EQUATOR }.single().rotate(90f)
            }
            "E'" -> {
                layers.filter { x -> x.layerName == LayerEnum.EQUATOR }.single().rotate(-90f)
            }
            "F" -> {
                layers.filter { x -> x.layerName == LayerEnum.FRONT }.single().rotate(90f)
            }
            "F'" -> {
                layers.filter { x -> x.layerName == LayerEnum.FRONT }.single().rotate(-90f)
            }
            "L" -> {
                layers.filter { x -> x.layerName == LayerEnum.LEFT }.single().rotate(90f)
            }
            "L'" -> {
                layers.filter { x -> x.layerName == LayerEnum.LEFT }.single().rotate(-90f)
            }
            "M" -> {
                layers.filter { x -> x.layerName == LayerEnum.MIDDLE }.single().rotate(-90f)
            }
            "M'" -> {
                layers.filter { x -> x.layerName == LayerEnum.MIDDLE }.single().rotate(90f)
            }
            "R" -> {
                layers.filter { x -> x.layerName == LayerEnum.RIGHT }.single().rotate(90f)
            }
            "R'" -> {
                layers.filter { x -> x.layerName == LayerEnum.RIGHT }.single().rotate(-90f)
            }
            "S" -> {
                layers.filter { x -> x.layerName == LayerEnum.STANDING }.single().rotate(90f)
            }
            "S'" -> {
                layers.filter { x -> x.layerName == LayerEnum.STANDING }.single().rotate(-90f)
            }
            "U" -> {
                layers.filter { x -> x.layerName == LayerEnum.UP }.single().rotate(90f)
            }
            "U'" -> {
                layers.filter { x -> x.layerName == LayerEnum.UP }.single().rotate(-90f)
            }
            "X", "X'", "Y", "Y'", "Z", "Z'" -> {
                rotateCube(rotationAngle, rotationAxis)
            }
        }
        currentTurn = turn
        resetLayerCubies()
    }

    override fun rotateCube(angle: Float, axis: Axis): Boolean {
        while (!getPermutationAllowance()) {
            Thread.sleep(10)
        }
        setPermutationAllowance(false)
        for (layer in layers) {
            if (layer.layerName.rotationAxis.x == axis.x
                && layer.layerName.rotationAxis.y == axis.y
                && layer.layerName.rotationAxis.z == axis.z
            ) {
                layer.rotate(angle)
            } else if (layer.layerName.rotationAxis.x == -axis.x
                && layer.layerName.rotationAxis.y == -axis.y
                && layer.layerName.rotationAxis.z == -axis.z
            ) {
                layer.rotate(-angle)
            }
        }

        /*while (!permutationAllowed) {
            Thread.sleep(10)
        }
        resetLayerCubies()*/
        return true
    }

    override fun sortCubiesForLayer(iLayer: ILayer) : List<ICubie>? {
        try {
            var layer = iLayer as LogicLayer
            var tempCubies = layer.cubies.clone() as ArrayList<ICubie>

            var sortedList = arrayListOf<ICubie>()

            //find cubie close to the user
            //go on minus z
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'F' && t.isActive } && x.tiles.any { t -> t.direction == 'L' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'L' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.isEdge })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'L' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.tiles.any { t -> t.direction == 'B' && t.isActive } })

            //increase x and go on minus z
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'F' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.isEdge })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'D' && t.isActive } && !x.isEdge && !x.isCorner })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.tiles.any { t -> t.direction == 'B' && t.isActive } && x.isEdge })

            //increase x and go on minus z
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'F' && t.isActive } && x.tiles.any { t -> t.direction == 'R' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'R' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.isEdge })
            sortedList.add(tempCubies.single { x -> x.tiles.any { t -> t.direction == 'R' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.tiles.any { t -> t.direction == 'B' && t.isActive } })

            return sortedList
        } catch (ex: Exception) {
            resetLayerCubies()
            return null
        }
    }

    override fun setPermutationAllowance(value : Boolean){
        synchronized(permutationLock, block = {permutationAllowed = value})
    }

    override fun getPermutationAllowance() : Boolean{
        synchronized(permutationLock, block = {return permutationAllowed})
    }
}