package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.RubikFace
import kotlin.concurrent.thread

class Cube() {
    val spaceBetweenCubies: Float = 0.3f;

    val lowerBound: Float = -3.0f;
    val upperBound: Float = 3.0f;

    val sideLength: Float = Vertex.RoundFloat((upperBound - lowerBound - spaceBetweenCubies * 2) / 3.0f)

    val cubies = arrayListOf<Cubie>()
    val layers = arrayListOf<Layer>()
    var directionsControl = DirectionsControl()

    var permutationAllowed = true

    //for whole cube rotations
    var rotationAxis : Axis = Axis.xAxis
    var rotationAngle : Float = 0f

    //region Init

    init {
        CreateCubies()
    }

    fun resetVertexBuffer() {
        for (cubie in cubies) {
            cubie.resetVertexBuffer()
        }
    }

    fun CreateCubies() {
        var isCorner = false
        var isEdge = true

        var leftX = lowerBound
        var leftY = lowerBound
        var leftZ = lowerBound
        var id = 0
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                for (k in 0 until 3) {
                    cubies.add(Cubie(leftX, leftY, leftZ, sideLength, id, !isCorner, !isEdge))
                    id++
                    leftZ += sideLength
                    leftZ += spaceBetweenCubies

                    isCorner = !isCorner
                    isEdge = !isEdge
                }
                leftZ = lowerBound
                leftX += sideLength
                leftX += spaceBetweenCubies
            }
            leftX = lowerBound
            leftZ = lowerBound
            leftY += sideLength
            leftY += spaceBetweenCubies
        }
        CreateLayers()
        for (cubie in cubies) {
            for (tile in cubie.tiles) {
                if (!tile.isActive) {
                    tile.color = Color.BLACK
                }
            }
        }
        resetVertexBuffer()
    }

    fun CreateLayers() {
        var id = 0
        addLayer(LayerEnum.LEFT, directionsControl.getDirectionByCharName('L'), id)
        id++
        addLayer(LayerEnum.MIDDLE, directionsControl.getDirectionByCharName('N'), id)
        id++
        addLayer(LayerEnum.RIGHT, directionsControl.getDirectionByCharName('R'), id)
        id++
        addLayer(LayerEnum.BACK, directionsControl.getDirectionByCharName('B'), id)
        id++
        addLayer(LayerEnum.STANDING, directionsControl.getDirectionByCharName('N'), id)
        id++
        addLayer(LayerEnum.FRONT, directionsControl.getDirectionByCharName('F'), id)
        id++
        addLayer(LayerEnum.DOWN, directionsControl.getDirectionByCharName('D'), id)
        id++
        addLayer(LayerEnum.EQUATOR, directionsControl.getDirectionByCharName('N'), id)
        id++
        addLayer(LayerEnum.UP, directionsControl.getDirectionByCharName('U'), id)
        id++
    }

    fun addLayer(layerName: LayerEnum, direction: Direction, id : Int) {
        var layer = Layer(layerName.centerPoint, layerName, direction, this, id)
        for (cubie1 in cubies) {
            if (cubie1.centerPoint.getCoordinateByAxis(layer.layerName.rotationAxis) == layer.centerPoint) {
                layer.addCubie(cubie1)
            }
        }
        if (layer.direction != directionsControl.getDirectionByCharName('N')) {
            layer.verifyTiles()
        }
        layers.add(layer)
    }

    //endregion

    fun resetLayerCubies() {
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
            permutationAllowed = true
            Thread.sleep(200)
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
            permutationAllowed = true
            Thread.sleep(200)
        }
    }

    fun randScramble(): String {
        var scramble = String()
        val possMoves = charArrayOf('U', 'D', 'R', 'L', 'F', 'B') //The allowed set of moves
        var prevMove = possMoves[(Math.random() * 6).toInt()] //Pick random moves as prevMove and secondLastMove for now
        var secondLastMove = possMoves[(Math.random() * 6).toInt()]
        var numMoves = 0
        while (numMoves < 20) {
            val move = possMoves[(Math.random() * 6).toInt()] //Pick a random move
            //Only proceed if the random move is different from the last two
            if (move != prevMove && move != secondLastMove) {
                //Decide whether to add something onto the end of the move
                val rand = (Math.random() * 100).toInt()
                if (rand < 33) {
                    scramble += move + "2 "
                } else if (rand < 67) {
                    scramble += "$move' "
                } else {
                    scramble += "$move "
                }
                secondLastMove = prevMove
                prevMove = move
                numMoves++
            }
        }
        scramble(scramble) //perform the scramble on the cube
        return scramble
    }

    fun scramble(scramble: String) {
        //Rotate the cube to get white on top, then return cube to original position at end of scramble
        performMoves(scramble)
    }

    //region Solving Algorithms

    //make white cross on the up layer
    fun makeWhiteCross() : String {
        var moves = String()

        //find center white point and add it to the up layer
        for (qb in cubies) {
            if (!qb.isEdge && !qb.isCorner) {
                if (qb.tiles.any() { x -> x.color == Color.WHITE }) {
                    for (tile in qb.tiles) {
                        if (tile.color == Color.WHITE && tile.isActive) {
                            if (tile.direction == 'L') {
                                moves += performMoves("S")
                            } else if (tile.direction == 'R') {
                                moves += performMoves("S'")
                            } else if (tile.direction == 'D') {
                                moves += performMoves("M")
                                moves += performMoves("M")
                            } else if (tile.direction == 'B') {
                                moves += performMoves("M")
                            } else if (tile.direction == 'F') {
                                moves += performMoves("M'")
                            }
                        }
                    }
                }
            }
        }

        while (numWhiteEdgesOriented() < 4) {
            if(permutationAllowed) {
                var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any() { t -> t.color == Color.WHITE } }
                for (qb in tempCubies) {
                    //do while cubie is not on the right place
                    while (!qb.isCubieRightOriented()) {
                        if (permutationAllowed) {
                            //find all edges with white tiles
                            var tempTiles = qb.tiles.filter { x -> x.isActive }
                            var whiteTile = tempTiles.filter { x -> x.color == Color.WHITE }.single()
                            var anotherTile = tempTiles.filter { x -> x.color != Color.WHITE }.single()

                            //white tile on the side, another tile on down layer
                            if (whiteTile.direction != 'D' && anotherTile.direction == 'D') {
                                //if cubie is on the right layer
                                if (anotherTile.color.charNotation
                                    == directionsControl.getColorByDirection(whiteTile.direction).charNotation
                                ) {
                                    if (whiteTile.direction == 'R') {
                                        moves += performMoves("D S D' S'")
                                    } else if (whiteTile.direction == 'L') {
                                        moves += performMoves("D S' D' S")
                                    } else if (whiteTile.direction == 'F') {
                                        moves += performMoves("D M D' M'")
                                    } else if (whiteTile.direction == 'B') {
                                        moves += performMoves("D M' D' M")
                                    }
                                } else {
                                    //rotate down to check layout on the another iteration
                                    if (qb.getNormalVectorAfterRotation(whiteTile, 90f, 'D')
                                        == directionsControl.getDirectionByColor(anotherTile.color))
                                    {
                                        moves += performMoves("D")
                                    } else {
                                        moves += performMoves("D'")
                                    }

                                }
                            }
                            //white tile is on down
                            else if (whiteTile.direction == 'D') {
                                //check if cubie on the right layer
                                if (directionsControl.getColorByDirection(anotherTile.direction) != anotherTile.color) {
                                    if (qb.getNormalVectorAfterRotation(anotherTile, 90f, 'D')
                                        == directionsControl.getDirectionByColor(anotherTile.color)
                                    ) {
                                        moves += performMoves("D")
                                    } else {
                                        moves += performMoves("D'")
                                    }
                                } else {
                                    moves += performMoves(anotherTile.direction.toString())
                                    moves += performMoves(anotherTile.direction.toString())
                                }
                            } else if (anotherTile.direction == 'U') {
                                moves += performMoves(whiteTile.direction.toString())
                                moves += performMoves(whiteTile.direction.toString())
                            }
                            //tile is on the equator layer
                            else if(directionsControl.getDirectionByColor(anotherTile.color) == anotherTile.direction){
                                if(qb.getNormalVectorAfterRotation(whiteTile, 90f, anotherTile.direction) == 'U'){
                                    moves += performMoves(anotherTile.direction.toString())
                                }
                                else{
                                    moves += performMoves(anotherTile.direction + "'")
                                }
                            }
                            else {
                                moves += performMoves(whiteTile.direction + "'")//qb.getDirOfColor(anotherTile.color.charNotation) + "'")
                            }
                        }
                        Thread.sleep(50)
                    }
                }
            }
            Thread.sleep(50)
        }
        return optimizeMoves(moves)
    }

    //complete white side of the cube
    fun finishWhiteLayer(): String {
        var moves = String()

        //calculate white corners layout
        while(numWhiteCornersOriented() < 4){
            if(permutationAllowed) {
                var tempCubies = cubies.filter { x -> x.isCorner && x.tiles.any() { t -> t.color == Color.WHITE } }
                for (qb in tempCubies) {
                    while (!isCornerRightOriented(qb)) {
                        if (permutationAllowed) {
                            //find all edges with white tiles
                            var tempTiles = qb.tiles.filter { x -> x.isActive }
                            var whiteTile = tempTiles.filter { x -> x.color == Color.WHITE }.single()
                            var firstTile = tempTiles.filter { x -> x.color != Color.WHITE }[0]
                            var secondTile = tempTiles.filter { x -> x.color != Color.WHITE }[1]

                            //first case wrong layout on the UP layer
                            if (whiteTile.direction == 'U') {
                                var c = firstTile.direction
                                if (qb.getNormalVectorAfterRotation(secondTile, 90f, firstTile.direction) == 'D'
                                    || qb.getNormalVectorAfterRotation(firstTile, 90f, secondTile.direction) == 'D'
                                ) {
                                    moves += performMoves(c.toString())
                                    moves += performMoves("D")
                                    moves += performMoves(c + "'")
                                } else {
                                    moves += performMoves(c + "'")
                                    moves += performMoves("D'")
                                    moves += performMoves(c.toString())
                                }
                            }

                            //second case White color directs down
                            else if (whiteTile.direction == 'D') {
                                if (firstTile.color == directionsControl.getColorByDirection(secondTile.direction)
                                    || secondTile.color == directionsControl.getColorByDirection(firstTile.direction)
                                ) {
                                    var c = secondTile.direction
                                    if (qb.getNormalVectorAfterRotation(firstTile, -90f, secondTile.direction) == 'D') {
                                        moves += performMoves(c + "'")
                                        var whiteC = whiteTile.direction
                                        if (qb.getNormalVectorAfterRotation(secondTile, -90f, whiteC) == 'D') {
                                            moves += performMoves(whiteC + "'")
                                            moves += performMoves("D D")
                                            moves += performMoves(whiteC.toString())
                                            moves += performMoves(c.toString())
                                        } else {
                                            moves += performMoves(whiteC.toString())
                                            moves += performMoves("D' D'")
                                            moves += performMoves(whiteC + "'")
                                            moves += performMoves(c.toString())
                                        }
                                    } else {
                                        moves += performMoves(c.toString())
                                        var whiteC1 = whiteTile.direction
                                        if (qb.getNormalVectorAfterRotation(secondTile, 90f, whiteC1) == 'D') {
                                            moves += performMoves(whiteC1.toString())
                                            moves += performMoves("D D")
                                            moves += performMoves(whiteC1 + "'")
                                            moves += performMoves(c + "'")
                                        } else {
                                            moves += performMoves(whiteC1 + "'")
                                            moves += performMoves("D' D'")
                                            moves += performMoves(whiteC1.toString())
                                            moves += performMoves(c + "'")
                                        }
                                    }
                                } else if (secondTile.color == directionsControl.getColorByDirection(firstTile.direction)) {
                                    var c = firstTile.direction
                                    if (qb.getNormalVectorAfterRotation(secondTile, 90f, c) == 'D') {
                                        moves += performMoves(c + "' D D " + c + " D " + c + "' D' " + c)
                                    } else {
                                        moves += performMoves(c + " D' " + c + "' D' " + c + " D " + c + "'")
                                    }
                                } else {
                                    moves += performMoves("D")
                                }

                            }

                            //third case one of tiles is on UP layer
                            else if (firstTile.direction == 'U' || secondTile.direction == 'U') {
                                var c = whiteTile.direction
                                if (qb.getNormalVectorAfterRotation(firstTile, -90f, c) == 'D' ||
                                    qb.getNormalVectorAfterRotation(secondTile, -90f, c) == 'D'
                                ) {
                                    moves += performMoves(c + "'")
                                    moves += performMoves("D")
                                    moves += performMoves(c.toString())
                                } else {
                                    moves += performMoves(c.toString())
                                    moves += performMoves("D")
                                    moves += performMoves(c + "'")
                                }
                            }

                            //one of tiles has DOWN direction
                            else {
                                if (directionsControl.getColorByDirection(firstTile.direction) == firstTile.color
                                    && directionsControl.getColorByDirection(whiteTile.direction) == secondTile.color
                                ) {
                                    var c = directionsControl.getDirectionByColor(firstTile.color)
                                    moves += performMoves("D'")
                                    moves += performMoves(c + "'")
                                    moves += performMoves("D")
                                    moves += performMoves(c.toString())
                                } else if (directionsControl.getColorByDirection(secondTile.direction) == secondTile.color
                                    && directionsControl.getColorByDirection(whiteTile.direction) == firstTile.color
                                ) {

                                    if (qb.getNormalVectorAfterRotation(secondTile, -90f, whiteTile.direction)
                                        == directionsControl.getDirectionByColor(firstTile.color)
                                    ) {
                                        moves += performMoves("D'")
                                        moves += performMoves(directionsControl.getDirectionByColor(secondTile.color) + "'")
                                        moves += performMoves("D")
                                        moves += performMoves(directionsControl.getDirectionByColor(secondTile.color).toString())
                                    } else {
                                        moves += performMoves("D")
                                        moves += performMoves(directionsControl.getDirectionByColor(secondTile.color).toString())
                                        moves += performMoves("D'")
                                        moves += performMoves(directionsControl.getDirectionByColor(secondTile.color) + "'")
                                    }
                                } else {
                                    moves += performMoves("D")
                                }
                            }
                        }
                        Thread.sleep(50)
                    }
                }
            }
            Thread.sleep(50)
        }

        return optimizeMoves(moves)
    }

    fun finishTwoLayers() : String{
        var moves = String()

        // rotate the whole cube
        rotationAxis = Axis.zAxis
        rotationAngle = 180f
        rotateCube(180f, rotationAxis)

        //calculate white corners layout
        while(numEdgesOriented() < 4) {
            var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.all{t -> t.color != Color.WHITE && t.color != Color.YELLOW}}//.any() { t -> t.color == Color.WHITE } }
            for (qb in tempCubies) {
                while (!isEdgeRightOriented(qb)) {
                    if (permutationAllowed) {
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var firstTile = tempTiles[0]
                        var secondTile = tempTiles[1]

                        var trueFaceTile = qb.tiles.filter { x -> x.color == directionsControl.getColorByDirection(x.direction) }.singleOrNull()

                        //if cubie is on the side
                        if(firstTile.direction != 'U' && secondTile.direction != 'U'){
                            var c1 = secondTile.direction
                            var c2 = firstTile.direction
                            moves += performMoves("U")
                            moves += performMoves(c1.toString())
                            moves += performMoves("U'")
                            moves += performMoves(c1 + "'")
                            moves += performMoves("U'")
                            moves += performMoves(c2 + "'")
                            moves += performMoves("U")
                            moves += performMoves(c2.toString())
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
                            if(qb.getNormalVectorAfterRotation(firstTile, 90f, secondTile.direction) == 'U'){
                                moves += performMoves("U")
                                moves += performMoves(c1.toString())
                                moves += performMoves("U'")
                                moves += performMoves(c1 + "'")
                                moves += performMoves("U'")
                                moves += performMoves(c2 + "'")
                                moves += performMoves("U")
                                moves += performMoves(c2.toString())
                                moves += performMoves("U2")
                                moves += performMoves("U")
                                moves += performMoves(c1.toString())
                                moves += performMoves("U'")
                                moves += performMoves(c1 + "'")
                                moves += performMoves("U'")
                                moves += performMoves(c2 + "'")
                                moves += performMoves("U")
                                moves += performMoves(c2.toString())
                            }
                            else{
                                moves += performMoves("U'")
                                moves += performMoves(c1 + "'")
                                moves += performMoves("U")
                                moves += performMoves(c1.toString())
                                moves += performMoves("U")
                                moves += performMoves(c2.toString())
                                moves += performMoves("U'")
                                moves += performMoves(c2 + "'")
                                moves += performMoves("U2")
                                moves += performMoves("U'")
                                moves += performMoves(c1 + "'")
                                moves += performMoves("U")
                                moves += performMoves(c1.toString())
                                moves += performMoves("U")
                                moves += performMoves(c2.toString())
                                moves += performMoves("U'")
                                moves += performMoves(c2 + "'")
                            }
                        }

                        //first case one tile is on the right side
                        //second tile should be moved to the left side
                        else if(trueFaceTile != null){
                            var notTrueFaceTile = tempTiles.filter { x -> x.color != trueFaceTile.color }.single()

                            if(qb.getNormalVectorAfterRotation(trueFaceTile, 90f, notTrueFaceTile.direction)
                                == directionsControl.getDirectionByColor(notTrueFaceTile.color)){
                                moves += performMoves("U'")
                                moves += performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color) + "'")
                                moves += performMoves("U")
                                moves += performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color).toString())
                                moves += performMoves("U")
                                moves += performMoves(directionsControl.getDirectionByColor(trueFaceTile.color).toString())
                                moves += performMoves("U'")
                                moves += performMoves(directionsControl.getDirectionByColor(trueFaceTile.color) + "'")
                            }
                            else{
                                moves += performMoves("U")
                                moves += performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color).toString())
                                moves += performMoves("U'")
                                moves += performMoves(directionsControl.getDirectionByColor(notTrueFaceTile.color) + "'")
                                moves += performMoves("U'")
                                moves += performMoves(directionsControl.getDirectionByColor(trueFaceTile.color) + "'")
                                moves += performMoves("U")
                                moves += performMoves(directionsControl.getDirectionByColor(trueFaceTile.color).toString())
                            }
                        }
                        else {
                            moves += performMoves("U")
                        }
                    }
                }
            }
        }
        return moves
    }

    fun makeYellowCross() : String {
        var moves = String()

        //calculate white corners layout
        while (numYellowEdgesOriented() < 4) {
            if (permutationAllowed) {
                var tempCubies =
                    cubies.filter { x -> x.isEdge && x.tiles.any { t -> t.color == Color.YELLOW } }
                var tilesForRotationUP = 0

                for (qb in tempCubies) {
                    if (permutationAllowed) {
                        for (tile in qb.tiles) {
                            var tempTiles = qb.tiles.filter { x -> x.isActive }

                            if ((tempTiles[0].direction == 'U' && tempTiles[1].direction == 'L'
                                        || tempTiles[1].direction == 'U' && tempTiles[1].direction == 'L')
                                || (tempTiles[0].direction == 'U' && tempTiles[1].direction == 'F'
                                        || tempTiles[1].direction == 'U' && tempTiles[1].direction == 'F')
                            ) {
                                tilesForRotationUP++
                                break
                            }
                        }
                    }

                    if (tilesForRotationUP >= 2) {
                        rotationAxis = Axis.yAxis
                        rotationAngle = 180f
                        rotateCube(180f, rotationAxis)
                    }
                }
                moves += performMoves("L U F U' F' L'")
            }
            Thread.sleep(50)
        }

        if(directionsControl.getDirectionByColor(Color.GREEN) == 'F')
        {
            rotationAxis = Axis.yAxis
            rotationAngle = 180f
            rotateCube(180f, rotationAxis)
        }
        return moves
    }

    fun swapYellowEdgesTopLayer() : String {
        var moves = String()

        if(numYellowEdgeCubieOriented() < 4) {
            if(permutationAllowed) {
                var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any { y -> y.color == Color.YELLOW } }
                //find the right cubie
                var qb =
                    tempCubies.filter { qb -> qb.tiles.any { t -> t.isActive && directionsControl.getColorByDirection('R') == t.color } }
                        .single()
                var rightTile = qb.tiles.filter { x -> x.isActive && x.color == directionsControl.getColorByDirection('R') }.single()
                while (directionsControl.getDirectionByColor(rightTile.color) != rightTile.direction) {
                    if (permutationAllowed) {
                        moves += performMoves("U")
                    }
                }
                while (numYellowEdgeCubieOriented() < 4) {
                    if(permutationAllowed) {
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
                                    moves += performMoves("R U R' U R U U R' U")
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
                                    moves += performMoves("U B U B' U B U U B' U F U F' U F U U F' U")
                                }
                            }
                        } else {
                            //change back and left
                            moves += performMoves("F U F' U F U U F' U")
                        }
                    }
                    Thread.sleep(50)
                }
            }
        }

        return moves
    }

    //rotate cube to get right cubie in front of user
    fun findRightOrientedYellowCubie() {
        var moves = String()
        var tempCubies = cubies.filter { x -> x.isCorner && x.tiles.any{ y -> y.color == Color.YELLOW} }
        for(qb in tempCubies){
            while(!permutationAllowed){
                Thread.sleep(50)
            }
            var tempTiles = qb.tiles.filter { x -> x.isActive }
            var yellowTile = tempTiles.filter { x -> x.color == Color.YELLOW }.single()
            var firstTile = tempTiles.filter { x ->x.color != Color.YELLOW }[0]
            var secondTile = tempTiles.filter { x ->x.color != Color.YELLOW }[1]

            if(yellowTile.direction == 'U'){
                if(directionsControl.getDirectionByColor(firstTile.color) == secondTile.direction
                    || directionsControl.getDirectionByColor(firstTile.color) == firstTile.direction
                    || directionsControl.getDirectionByColor(firstTile.color) == directionsControl.getDirectionByColor(secondTile.color)){
                    if(firstTile.direction != 'F' || secondTile.direction != 'F'){
                        if(qb.getNormalVectorAfterRotation(firstTile, 180f, 'U') == 'F'
                            || qb.getNormalVectorAfterRotation(secondTile, 180f, 'U') == 'F'){
                            rotationAxis = Axis.yAxis
                            rotationAngle = 180f
                            rotateCube(180f, Axis.yAxis)
                        }
                        /*else if(qb.getNormalVectorAfterRotation(firstTile, 180f, 'U') == 'F'){

                        }*/
                    }
                }
            }
            else{
                if(yellowTile.direction == directionsControl.getDirectionByColor(firstTile.color)
                    || yellowTile.direction == directionsControl.getDirectionByColor(secondTile.color)){
                    moves += performMoves("U F U' B' U F' U' B")
                    break
                }
            }
        }
    }

    fun finishSolvingYellowCorners() : String{
        var moves = String()

        while(numYellowCornersOriented() < 4) {
            var num = numYellowCornersOriented()
            if (permutationAllowed) {
                var tempCubies = cubies.filter { qb -> qb.isCorner && !isYellowCornerOriented(qb) && qb.tiles.any{x -> x.color == Color.YELLOW}}

                if(num == 2){
                    if(tempCubies[0].tiles.any{x -> x.isActive && x.direction == 'L'} &&
                            tempCubies[1].tiles.any{x -> x.isActive && x.direction == 'L'}){
                        var seq = "F' D' F D"
                        var i = 4
                        var j = 2
                        if (tempCubies[0].tiles.any { x -> x.color == Color.YELLOW && x.direction == 'F' } ||
                            tempCubies[1].tiles.any { x -> x.color == Color.YELLOW && x.direction == 'F' }) {
                            i = 2
                            j = 4
                        }
                        for (o in 1..i) {
                            moves += performMoves(seq)
                        }
                        moves += performMoves("U'")
                        for (k in 1..j) {
                            moves += performMoves(seq)
                        }
                        moves += performMoves("U")
                    }
                    else{
                        while (!permutationAllowed){
                            Thread.sleep(50)
                        }
                        rotationAngle = 90f
                        rotationAxis = Axis.yAxis
                        rotateCube(rotationAngle, rotationAxis)
                    }
                }

                /*for(qb in tempCubies){
                    while(!isYellowCornerOriented(qb)){
                        if(permutationAllowed) {
                            var tempTiles = qb.tiles.filter { x -> x.isActive }
                            if (tempTiles[0].direction == getDirectionByColor(tempTiles[1].color) ||
                                tempTiles[0].direction == getDirectionByColor(tempTiles[2].color) &&
                                tempTiles[1].direction == getDirectionByColor(tempTiles[0].color) ||
                                tempTiles[1].direction == getDirectionByColor(tempTiles[2].color) &&
                                tempTiles.any { x -> x.direction == 'F' } && tempTiles.any { x -> x.direction == 'L' }
                            ) {

                                var seq = "F' D' F D"
                                var i = 4
                                var j = 2
                                if (tempTiles.any { x -> x.color == Color.YELLOW && x.direction == 'F' }) {
                                    i = 2
                                    j = 4
                                }
                                for (o in 0..i) {
                                    moves += performMoves(seq)
                                }
                                moves += performMoves("U'")
                                for (k in 0..j) {
                                    moves += performMoves(seq)
                                }
                                moves += performMoves("U")
                            } else {
                                rotationAngle = 90f
                                rotationAxis = Axis.yAxis
                                rotateCube(rotationAngle, rotationAxis)
                            }
                        }
                        Thread.sleep(50)
                    }
                }*/
            }
            Thread.sleep(50)
        }

        return moves
    }

    //endregion

    //region Cubies Orients

    //check if cubie on the right corner place
    fun isCornerRightOriented(cubie : Cubie) : Boolean {
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if (tempTiles.any(){x -> x.color == Color.WHITE && x.direction == 'U'}) {
            if(tempTiles.any(){x -> x.color == directionsControl.getColorByDirection(x.direction) && x.color != Color.WHITE}){
                return true
            }
        }
        return false
    }

    fun isEdgeRightOriented(cubie : Cubie) : Boolean{
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if(tempTiles[0].direction == directionsControl.getDirectionByColor(tempTiles[0].color)
            && tempTiles[1].direction == directionsControl.getDirectionByColor(tempTiles[1].color)){
            return true
        }
        return false
    }

    fun isYellowEdgeRightOriented(qb : Cubie) : Boolean{
        var tempTiles = qb.tiles.filter { x -> x.isActive }
        if(tempTiles[0].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
            || tempTiles[1].color == Color.YELLOW && directionsControl.getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction){
            return true
        }
        return false
    }

    fun isYellowCornerOriented(qb : Cubie) : Boolean{
        var tempTiles = qb.tiles.filter { x -> x.isActive }
        if(directionsControl.getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction
            && directionsControl.getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
            && directionsControl.getDirectionByColor(tempTiles[2].color) == tempTiles[2].direction){
            return true
        }
        return false
    }

    //endregion

    //region Layers Rotations

    fun performMoves(moves: String): String {
        var i = 0
        while (i < moves.length) {
            if (permutationAllowed) {
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
                Thread.sleep(100)
            }
        }
        return moves
    }

    //rotate a cube side
    fun turn(turn: String) {
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
        }
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
            if(qb.isEdge && qb.tiles.any{x -> x.color == Color.YELLOW && x.direction == 'U'}){
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

    /**
     * return value when rotation process is complete
     */
    fun rotateCube(angle : Float, axis: Axis) : Boolean {
        while(!permutationAllowed){
            Thread.sleep(50)
        }
        permutationAllowed = false
        for (layer in layers) {
            if (layer.layerName.rotationAxis.x == axis.x
                && layer.layerName.rotationAxis.y == axis.y
                && layer.layerName.rotationAxis.z == axis.z)
            {
                layer.rotate(angle)
            }
            else if(layer.layerName.rotationAxis.x == -axis.x
                && layer.layerName.rotationAxis.y == -axis.y
                && layer.layerName.rotationAxis.z == -axis.z)
            {
                layer.rotate(-angle)
            }
        }

        while(!permutationAllowed){
            Thread.sleep(50)
        }
        return true
    }

    //add rubik face colors to the necessary layer
    fun fillFaceColors(rubikFace: RubikFace){
        /*var layer = layers.filter { x -> x.layerName == rubikFace.layerName }.single()

        var k = 0
        for(rubikTileArray in rubikFace.transformedTileArray){
            for(rubikTile in rubikTileArray){
                var cubie = cubies.single { x -> x.id == layer.cubiesIds[k] }

                cubie.tiles.single { x -> x.isActive && x.direction == layer.direction.charName }
                    .setTileColor(rubikTile!!.tileColor)
                cubies[cubie.id] = cubie
                k++
            }
        }*/

        while(!permutationAllowed){
            Thread.sleep(20)
        }
        //always take down layer
        var layer = layers.single { x -> x.layerName == LayerEnum.DOWN }

        var k = 0

        for(i in 2 downTo 0){
            for(j in 2 downTo 0){
                var cubie = cubies.single { x -> x.id == layer.cubiesIds[k] }

                cubie.tiles.single { x -> x.isActive && x.direction == layer.direction.charName }
                    .setTileColor(rubikFace.transformedTileArray[i][j]!!.tileColor)
                cubies[cubie.id] = cubie
                k++
            }
        }


        /*for(rubikTileArray in rubikFace.transformedTileArray){
            for(rubikTile in rubikTileArray){
                var cubie = cubies.single { x -> x.id == layer.cubiesIds[k] }

                cubie.tiles.single { x -> x.isActive && x.direction == layer.direction.charName }
                    .setTileColor(rubikTile!!.tileColor)
                cubies[cubie.id] = cubie
                k++
            }
        }*/
    }
}