package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Rubik.Enums.Color
import com.example.kotlinapp.Rubik.Enums.Direction
import com.example.kotlinapp.Rubik.Enums.LayerEnum

class Cube() {
    val spaceBetweenCubies: Float = 0.3f;

    val lowerBound: Float = -3.0f;
    val upperBound: Float = 3.0f;

    val sideLength: Float = Vertex.RoundFloat((upperBound - lowerBound - spaceBetweenCubies * 2) / 3.0f)

    val cubies = arrayListOf<Cubie>()
    val layers = arrayListOf<Layer>()

    val world: World = World()

    var permutationAllowed = true

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
                    cubies.add(Cubie(leftX, leftY, leftZ, sideLength, world, id, !isCorner, !isEdge))
                    id++
                    leftX += sideLength
                    leftX += spaceBetweenCubies

                    isCorner = !isCorner
                    isEdge = !isEdge
                }
                leftX = lowerBound
                leftZ += sideLength
                leftZ += spaceBetweenCubies
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
        /*var axis = 0
        for (i in 0 until 3) {
            var rotationAxis = RotationAxis.getRotationAxisByIndex(axis)
            for (cubie in cubies) {
                var coord = cubie.centerPoint.getCoordinateByAxis(rotationAxis)
                if (!layers.any { x -> x.layerName.rotationAxis == rotationAxis && x.centerPoint == coord }) {
                    var layerName =
                        LayerEnum.getLayerNameByCenterPoint(RotationAxis.getRotationAxisByIndex(axis), coord)
                    if (layerName != null) {
                        var layer = Layer(coord, layerName, layerName.direction, this)
                        for (cubie1 in cubies) {
                            if (cubie1.centerPoint.getCoordinateByAxis(rotationAxis) == coord) {
                                layer.addCubie(cubie1)
                            }
                        }
                        if (layer.layerName.direction != Direction.NON) {
                            layer.verifyTiles()
                        }
                        layers.add(layer)
                    }
                }
            }
            axis++
        }*/

        addLayer(LayerEnum.LEFT)
        addLayer(LayerEnum.MIDDLE)
        addLayer(LayerEnum.RIGHT)
        addLayer(LayerEnum.BACK)
        addLayer(LayerEnum.STANDING)
        addLayer(LayerEnum.FRONT)
        addLayer(LayerEnum.DOWN)
        addLayer(LayerEnum.EQUATOR)
        addLayer(LayerEnum.UP)
    }

    fun addLayer(layerName : LayerEnum) {
        var layer = Layer(layerName.centerPoint, layerName, layerName.direction, this)
        for (cubie1 in cubies) {
            if (cubie1.centerPoint.getCoordinateByAxis(layerName.rotationAxis) == layerName.centerPoint) {
                layer.addCubie(cubie1)
            }
        }
        if (layer.layerName.direction != Direction.NON) {
            layer.verifyTiles()
        }
        if (layer.layerName.direction != Direction.NON) {
            layer.verifyTiles()
        }
        layers.add(layer)
    }

    fun resetLayerCubies() {
        var count = cubies.count { x -> x.isRotated }
        if (count == 9) {
            for (layer in layers) {
                layer.cubies.clear()
                layer.cubiesIds.clear()
                for (cubie in cubies) {
                    if (cubie.centerPoint.getCoordinateByAxis(layer.layerName.rotationAxis) == layer.centerPoint) {
                        layer.addCubie(cubie)
                        cubie.isRotated = false
                    }
                }
            }
            permutationAllowed = true
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

    fun makeWhiteCross() : String {
        var moves = String()

        while (numWhiteEdgesOriented() !== 0) { //Turn sunflower into cross until no white edges remain in the U layer
            for (qb in cubies) {
                //take up face
                if (qb.isEdge && qb.centerPoint.y == 2.1f) {
                    if (qb.tiles.any() { x -> x.color == Color.WHITE }) {
                        for (k in 0..1) {
                            //Check for when the white edge is matched up with the respective face and turn it down
                            if (qb.tiles[k].color === Color.RED && qb.tiles[k].direction === Direction.LEFT ||
                                qb.tiles[k].color === Color.GREEN && qb.tiles[k].direction === Direction.FRONT ||
                                qb.tiles[k].color === Color.ORANGE && qb.tiles[k].direction === Direction.RIGHT ||
                                qb.tiles[k].color === Color.BLUE && qb.tiles[k].direction === Direction.BACK
                            ) {
                                moves += performMoves(qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y) + "2 ")
                            }
                        }
                    }
                }
            }
            //Turn U to try lining up edges that have not been turned down yet
            moves += performMoves("U ")
        }
        return optimizeMoves(moves)
    }

    fun scramble(scramble: String) {
        //Rotate the cube to get white on top, then return cube to original position at end of scramble
        performMoves("z2 $scramble z2")
    }

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
                Thread.sleep(10)
            }
        }
        return moves
    }

    fun turn(turn: String) {
        //See the first case (B) to understand how all cases work
        val preChange: CharArray //Directions prior to turning
        val postChange: CharArray //What the directions change to after the turn

        when (turn) {
            "B" -> {
                preChange = charArrayOf('B', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('B', 'L', 'U', 'R', 'D')
                //Rotate the matrix
                layers.filter { x -> x.direction == Direction.BACK }.single().rotate(90f)
            }

            "B'" -> {
                preChange = charArrayOf('B', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('B', 'R', 'D', 'L', 'U')
                layers.filter { x -> x.direction == Direction.BACK }.single().rotate(-90f)
            }

            "D" -> {
                preChange = charArrayOf('D', 'L', 'B', 'R', 'F')
                postChange = charArrayOf('D', 'F', 'L', 'B', 'R')
                layers.filter { x -> x.direction == Direction.DOWN }.single().rotate(90f)
            }

            "D'" -> {
                preChange = charArrayOf('D', 'F', 'L', 'B', 'R')
                postChange = charArrayOf('D', 'L', 'B', 'R', 'F')
                layers.filter { x -> x.direction == Direction.DOWN }.single().rotate(-90f)
            }

            "E" -> {
                preChange = charArrayOf('L', 'B', 'R', 'F')
                postChange = charArrayOf('F', 'L', 'B', 'R')

            }

            "E'" -> {
                preChange = charArrayOf('F', 'L', 'B', 'R')
                postChange = charArrayOf('L', 'B', 'R', 'F')

            }

            "F" -> {
                preChange = charArrayOf('F', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('F', 'R', 'D', 'L', 'U')
                layers.filter { x -> x.direction == Direction.FRONT }.single().rotate(90f)

            }

            "F'" -> {
                preChange = charArrayOf('F', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('F', 'L', 'U', 'R', 'D')
                layers.filter { x -> x.direction == Direction.FRONT }.single().rotate(-90f)

            }

            "L" -> {
                preChange = charArrayOf('L', 'B', 'D', 'F', 'U')
                postChange = charArrayOf('L', 'U', 'B', 'D', 'F')
                layers.filter { x -> x.direction == Direction.LEFT }.single().rotate(90f)

            }

            "L'" -> {
                preChange = charArrayOf('L', 'U', 'B', 'D', 'F')
                postChange = charArrayOf('L', 'B', 'D', 'F', 'U')
                layers.filter { x -> x.direction == Direction.LEFT }.single().rotate(-90f)

            }

            "M" -> {
                preChange = charArrayOf('B', 'D', 'F', 'U')
                postChange = charArrayOf('U', 'B', 'D', 'F')

            }

            "M'" -> {
                preChange = charArrayOf('U', 'B', 'D', 'F')
                postChange = charArrayOf('B', 'D', 'F', 'U')

            }

            "R" -> {
                preChange = charArrayOf('R', 'U', 'B', 'D', 'F')
                postChange = charArrayOf('R', 'B', 'D', 'F', 'U')
                layers.filter { x -> x.direction == Direction.RIGHT }.single().rotate(90f)

            }

            "R'" -> {
                preChange = charArrayOf('R', 'B', 'D', 'F', 'U')
                postChange = charArrayOf('R', 'U', 'B', 'D', 'F')
                layers.filter { x -> x.direction == Direction.RIGHT }.single().rotate(-90f)

            }

            "S" -> {
                preChange = charArrayOf('U', 'R', 'D', 'L')
                postChange = charArrayOf('R', 'D', 'L', 'U')

            }

            "S'" -> {
                preChange = charArrayOf('U', 'R', 'D', 'L')
                postChange = charArrayOf('L', 'U', 'R', 'D')

            }

            "U" -> {
                preChange = charArrayOf('U', 'F', 'L', 'B', 'R')
                postChange = charArrayOf('U', 'L', 'B', 'R', 'F')
                layers.filter { x -> x.direction == Direction.UP }.single().rotate(90f)

            }

            "U'" -> {
                preChange = charArrayOf('U', 'L', 'B', 'R', 'F')
                postChange = charArrayOf('U', 'F', 'L', 'B', 'R')
                layers.filter { x -> x.direction == Direction.BACK }.single().rotate(-90f)

            }

            "x" -> performMoves("R M' L'")

            "x'" -> performMoves("R' M L")

            "y" -> performMoves("U E' D'")

            "y'" -> performMoves("U' E D")

            "z" -> performMoves("F S B'")

            "z'" -> performMoves("F' S' B")
        }
    }

    fun numWhiteEdgesOriented(): Int {
        var numOriented = 0
        for (cubie in cubies) {
            if (cubie.isEdge) {
                if (cubie.getDirOfColor('W') == 'U') {
                    numOriented++
                }
            }
        }
        return numOriented
    }

    fun prepareSlot(id: Int, color: Char): String {
        var numUTurns = 0
        var tempColor = cubies.get(id).getColors()
        while (tempColor.any { x -> x.color.charNotation == color } && numUTurns < 5) {
            //Keep turning U until the position (x, y, z) is not occupied by a white edge
            performMoves("U")
            tempColor = cubies.get(id).getColors()
            numUTurns++
        }

        //Return appropriate amount of U turns
        return if (numUTurns == 0 || numUTurns == 4) {
            ""
        } else if (numUTurns == 1) {
            "U "
        } else if (numUTurns == 2) {
            "U2 "
        } else
            "U' "
    }

    fun makeSunflower(): String {
        var moves = String()

        //Brings up white edges in D Layer with white facing down
        if (numWhiteEdgesOriented() < 5) {
            var layer = layers.filter { x -> x.layerName == LayerEnum.DOWN }.single()
            for (qb in cubies) {
                if (layer.cubiesIds.contains(qb.id)) {
                    if (qb.getDirOfColor('W') === 'D') {
                        moves += prepareSlot(qb.id, 'W')
                        //Get the vertical plane in which the cubie lies
                        val turnToMake = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y)
                        moves += performMoves("" + turnToMake + "2 ")
                    }
                }
            }
        }

        //Orients white edges in D Layer with white NOT facing down
        if (numWhiteEdgesOriented() < 5) {
            for (qb in cubies) {
                if (qb.isEdge) {
                    if (qb.getDirOfColor('W') !== 'A' && qb.getDirOfColor('W') !== 'D') {
                        val vert = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y)
                        moves += prepareSlot(qb.id, 'W')
                        if (vert == 'F') {
                            moves += performMoves("F' U' R ")
                        } else if (vert == 'R') {
                            moves += performMoves("R' U' B ")
                        } else if (vert == 'B') {
                            moves += performMoves("B' U' L ")
                        } else if (vert == 'L') {
                            moves += performMoves("L' U' F ")
                        }
                    }
                }
            }
        }

        //Brings up white edges in E Layer
        //This one is filled with many if blocks because there are eight different possible orientations for
        //white edges in the E Layer, with none sharing a common move to bring it into the U layer.
        if (numWhiteEdgesOriented() < 5) {
            for (qb in cubies)
                if (qb.isEdge) {
                    val tempColors = qb.getColors()
                    for (k in 0..1) {
                        if (tempColors[k].color.charNotation === 'W') {
                            /* Depending on the position of the edge, one of the vertical planes it lies
								 * in must be cleared of white edges before bringing it up */
                            if (qb.centerPoint.x == -2.1f && qb.centerPoint.y == -2.1f) {
                                if (tempColors[k].direction.charName === 'L') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("F ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("L' ")
                                }
                            } else if (qb.centerPoint.x == 2.1f && qb.centerPoint.y == -2.1f) {
                                if (tempColors[k].direction.charName === 'F') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("R ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("F' ")
                                }
                            } else if (qb.centerPoint.x == 2.1f && qb.centerPoint.y == 2.1f) {
                                if (tempColors[k].direction.charName === 'B') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("R' ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("B ")
                                }
                            } else {
                                if (tempColors[k].direction.charName === 'B') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("L ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("B' ")
                                }
                            }
                        }
                    }
                }
        }

        //Fix any edges that are incorrectly oriented in the U Layer
        //For the sake of reducing movecount, I assigned a set of moves for each position,
        //but a solver may simply make U turns to bring the edge in front and perform "F U' R"
        if (numWhiteEdgesOriented() < 5) {
            for (qb in cubies) {
                if (qb.isEdge) {
                    if (qb.getDirOfColor('W') !== 'A' && qb.getDirOfColor('W') !== 'U') {
                        val vert = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y)
                        if (vert == 'F') {
                            moves += performMoves("F U' R ")
                        } else if (vert == 'R') {
                            moves += performMoves("R U' B ")
                        } else if (vert == 'B') {
                            moves += performMoves("B U' L ")
                        } else if (vert == 'L') {
                            moves += performMoves("L U' F ")
                        }
                    }
                }
            }
        }

        //If fewer than 4 white edges reached the top layer by the end of this, some white edge was missed
        //(This might happen, say, if bringing an edge up from the E Layer unintentionally brings down an incorrectly
        // oriented edge in the U Layer)
        //Recurse to oriented remaining white edges
        if (numWhiteEdgesOriented() < 4) {
            moves += makeSunflower()
        }

        return optimizeMoves(moves)
    }

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
}