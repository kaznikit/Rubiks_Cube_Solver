package com.example.kotlinapp.Rubik

import android.text.style.TtsSpan
import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Enums.Color
import com.example.kotlinapp.Rubik.Enums.Direction
import com.example.kotlinapp.Rubik.Enums.LayerEnum
import java.time.Year
import kotlin.concurrent.thread
import kotlin.math.roundToInt

class Cube() {
    val spaceBetweenCubies: Float = 0.3f;

    val lowerBound: Float = -3.0f;
    val upperBound: Float = 3.0f;

    val sideLength: Float = Vertex.RoundFloat((upperBound - lowerBound - spaceBetweenCubies * 2) / 3.0f)

    val cubies = arrayListOf<Cubie>()
    val layers = arrayListOf<Layer>()
    val directions = arrayListOf<Direction>()

    val world: World = World()

    var permutationAllowed = true

    var rotationAxis : Axis = Axis.xAxis

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
        CreateDirections()
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
        addLayer(LayerEnum.LEFT, directions.filter { x -> x.charName == 'L' }.single())
        addLayer(LayerEnum.MIDDLE, directions.filter { x -> x.charName == 'N' }.single())
        addLayer(LayerEnum.RIGHT, directions.filter { x -> x.charName == 'R' }.single())
        addLayer(LayerEnum.BACK, directions.filter { x -> x.charName == 'B' }.single())
        addLayer(LayerEnum.STANDING, directions.filter { x -> x.charName == 'N' }.single())
        addLayer(LayerEnum.FRONT, directions.filter { x -> x.charName == 'F' }.single())
        addLayer(LayerEnum.DOWN, directions.filter { x -> x.charName == 'D' }.single())
        addLayer(LayerEnum.EQUATOR, directions.filter { x -> x.charName == 'N' }.single())
        addLayer(LayerEnum.UP, directions.filter { x -> x.charName == 'U' }.single())
    }

    fun CreateDirections() {
        directions.add(Direction('L', Color.ORANGE))
        directions.add(Direction('R', Color.RED))
        directions.add(Direction('U', Color.WHITE))
        directions.add(Direction('D', Color.YELLOW))
        directions.add(Direction('B', Color.BLUE))
        directions.add(Direction('F', Color.GREEN))
        directions.add(Direction('N', Color.BLACK))
    }

    fun addLayer(layerName: LayerEnum, direction: Direction) {
        var layer = Layer(layerName.centerPoint, layerName, direction, this)
        for (cubie1 in cubies) {
            if (cubie1.centerPoint.getCoordinateByAxis(layer.layerName.rotationAxis) == layer.centerPoint) {
                layer.addCubie(cubie1)
            }
        }
        if (layer.direction != directions.filter { x -> x.charName == 'N' }.single()) {
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
            updateDirectionsAfterRotation180Degrees(rotationAxis)
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

    //make white cross on the up layer
    fun makeWhiteCross() : String {
        var moves = String()

        for (qb in cubies) {
            //find center white point and add it to the up layer
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
            var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any() { t -> t.color == Color.WHITE } }
            for (qb in tempCubies) {
                //do while cubie is not on the right place
                while (!qb.isCubieRightOriented()) {
                    if (permutationAllowed) {
                        //find all edges with white tiles
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var whiteTile = tempTiles.filter { x -> x.color == Color.WHITE }.single()
                        var anotherTile = tempTiles.filter { x -> x.color != Color.WHITE }.single()

                        if (whiteTile.direction != 'D' && anotherTile.direction == 'D') {
                            //if cubie is on the right layer
                            if (anotherTile.color.charNotation
                                == getColorByDirection(whiteTile.direction).charNotation
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
                                if(qb.getNormalVectorAfterRotation(whiteTile, 90f, 'D')
                                    == getDirectionByColor(anotherTile.color)){
                                    moves += performMoves("D")
                                }
                                else{
                                    moves += performMoves("D'")
                                }

                            }
                        } else if (whiteTile.direction == 'D') {
                            //check if cubie on the right layer
                            if (getColorByDirection(anotherTile.direction) != anotherTile.color) {
                                if(qb.getNormalVectorAfterRotation(anotherTile, 90f, 'D')
                                == getDirectionByColor(anotherTile.color)){
                                    moves += performMoves("D")
                                }
                                else{
                                    moves += performMoves("D'")
                                }
                            } else {
                                moves += performMoves(anotherTile.direction.toString())
                                moves += performMoves(anotherTile.direction.toString())
                            }
                        } else if (anotherTile.direction == 'U') {
                            moves += performMoves(whiteTile.direction.toString())
                            moves += performMoves(whiteTile.direction.toString())
                        } else {
                            moves += performMoves(whiteTile.direction + "'")//qb.getDirOfColor(anotherTile.color.charNotation) + "'")
                        }
                    }
                    Thread.sleep(50)
                }
            }
        }
        return optimizeMoves(moves)
    }

    //complete white side of the cube
    fun finishWhiteLayer(): String {
        var moves = String()

        //calculate white corners layout
        while(numWhiteCornersOriented() < 4){
            var tempCubies = cubies.filter { x -> x.isCorner && x.tiles.any() { t -> t.color == Color.WHITE } }
            for (qb in tempCubies) {
                while (!isCornerRightOriented(qb)) {
                    if(permutationAllowed) {
                        //find all edges with white tiles
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var whiteTile = tempTiles.filter { x -> x.color == Color.WHITE }.single()
                        var firstTile = tempTiles.filter { x -> x.color != Color.WHITE }[0]
                        var secondTile = tempTiles.filter { x -> x.color != Color.WHITE }[1]

                        //first case wrong layout on the UP layer
                        if(whiteTile.direction == 'U'){
                            var c = firstTile.direction
                            if(qb.getNormalVectorAfterRotation(secondTile, 90f, firstTile.direction) == 'D'
                                || qb.getNormalVectorAfterRotation(firstTile, 90f, secondTile.direction) == 'D'){
                                moves += performMoves(c.toString())
                                moves += performMoves("D")
                                moves += performMoves(c + "'")
                            }
                            else{
                                moves += performMoves(c + "'")
                                moves += performMoves("D'")
                                moves += performMoves(c.toString())
                            }
                        }

                        //second case White color directs down
                        if(whiteTile.direction == 'D'){
                            if(firstTile.color == getColorByDirection(secondTile.direction) || secondTile.color == getColorByDirection(firstTile.direction)) {
                                var c = secondTile.direction
                                if (qb.getNormalVectorAfterRotation(firstTile, -90f, secondTile.direction) == 'D'){
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
                                    if (qb.getNormalVectorAfterRotation(secondTile, 90f, whiteC1) == 'D')
                                    {
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
                            }
                            else if(secondTile.color == getColorByDirection(firstTile.direction)){
                                var c = firstTile.direction
                                if(qb.getNormalVectorAfterRotation(secondTile, 90f, c) == 'D'){
                                    moves += performMoves(c + "' D D " + c + " D " + c + "' D' " + c)
                                }
                                else{
                                    moves += performMoves(c + " D' " + c + "' D' " + c + " D " + c + "'")
                                }
                            }
                            else{
                                moves += performMoves("D")
                            }

                        }

                        //third case one of tiles is on UP layer
                        else if(firstTile.direction == 'U' || secondTile.direction == 'U'){
                            var c = whiteTile.direction
                            if(qb.getNormalVectorAfterRotation(firstTile, -90f, c) == 'D' ||
                                    qb.getNormalVectorAfterRotation(secondTile, -90f, c) == 'D') {
                                moves += performMoves(c + "'")
                                moves += performMoves("D")
                                moves += performMoves(c.toString())
                            }
                            else{
                                moves += performMoves(c.toString())
                                moves += performMoves("D")
                                moves += performMoves(c + "'")
                            }
                        }

                        //one of tiles has DOWN direction
                        else{
                            if(getColorByDirection(firstTile.direction) == firstTile.color
                                && getColorByDirection(whiteTile.direction) == secondTile.color){
                                var c = getDirectionByColor(secondTile.color)
                                moves += performMoves(c + "'")
                                moves += performMoves("D'")
                                moves += performMoves(c.toString())
                            }
                            else if (getColorByDirection(secondTile.direction) == secondTile.color
                                    && getColorByDirection(whiteTile.direction) == firstTile.color) {

                                if(qb.getNormalVectorAfterRotation(secondTile, -90f, whiteTile.direction)
                                    == getDirectionByColor(firstTile.color)){
                                    moves += performMoves("D'")
                                    moves += performMoves(getDirectionByColor(secondTile.color) + "'")
                                    moves += performMoves("D")
                                    moves += performMoves(getDirectionByColor(secondTile.color).toString())
                                }
                                else{
                                    moves += performMoves("D")
                                    moves += performMoves(getDirectionByColor(secondTile.color).toString())
                                    moves += performMoves("D'")
                                    moves += performMoves(getDirectionByColor(secondTile.color) + "'")
                                }
                            }
                            else {
                                moves += performMoves("D")
                            }
                        }
                    }
                    Thread.sleep(50)
                }
            }
        }

        return optimizeMoves(moves)
    }

    fun finishTwoLayers() : String{
        var moves = String()

        // rotate the whole cube
        rotationAxis = Axis.zAxis
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

                        var trueFaceTile = qb.tiles.filter { x -> x.color == getColorByDirection(x.direction) }.singleOrNull()

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
                        else if(firstTile.color == getColorByDirection(secondTile.direction)
                            && secondTile.color == getColorByDirection(firstTile.direction))
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
                                == getDirectionByColor(notTrueFaceTile.color)){
                                moves += performMoves("U'")
                                moves += performMoves(getDirectionByColor(notTrueFaceTile.color) + "'")
                                moves += performMoves("U")
                                moves += performMoves(getDirectionByColor(notTrueFaceTile.color).toString())
                                moves += performMoves("U")
                                moves += performMoves(getDirectionByColor(trueFaceTile.color).toString())
                                moves += performMoves("U'")
                                moves += performMoves(getDirectionByColor(trueFaceTile.color) + "'")
                            }
                            else{
                                moves += performMoves("U")
                                moves += performMoves(getDirectionByColor(notTrueFaceTile.color).toString())
                                moves += performMoves("U'")
                                moves += performMoves(getDirectionByColor(notTrueFaceTile.color) + "'")
                                moves += performMoves("U'")
                                moves += performMoves(getDirectionByColor(trueFaceTile.color) + "'")
                                moves += performMoves("U")
                                moves += performMoves(getDirectionByColor(trueFaceTile.color).toString())
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
                var needToRotate180 = false
                var tilesForRotationUP = 0
                var tilesForRotationLF = 0

                for (qb in tempCubies) {
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
                    rotateCube(180f, rotationAxis)
                }
                moves += performMoves("L U F U' F' L'")
            }
        }

        if(getDirectionByColor(Color.GREEN) == 'F')
        {
            rotationAxis = Axis.yAxis
            rotateCube(180f, rotationAxis)
        }
        return moves
    }

    fun swapYellowEdgesTopLayer() : String {
        var moves = String()

        while (numYellowEdgeCubieOriented() < 4) {
            if (permutationAllowed) {
                var tempCubies = cubies.filter { x -> x.isEdge && x.tiles.any { y -> y.color == Color.YELLOW } }

                for (qb in tempCubies) {
                    while (!isYellowEdgeRightOriented(qb)) {
                        var tempTiles = qb.tiles.filter { x -> x.isActive }
                        var yellowTile = tempTiles.filter { x -> x.color == Color.YELLOW }.single()
                        var anotherTile = tempTiles.filter { x -> x.color != Color.YELLOW }.single()
                        if(qb.getNormalVectorAfterRotation(anotherTile, 90f, 'U')
                            == getDirectionByColor(anotherTile.color)){
                            //rotate layer opposite to desire
                            var c = qb.getNormalVectorAfterRotation(anotherTile, -90f, 'U')
                            moves += performMoves(c.toString())
                            moves += performMoves("U")
                            moves += performMoves(c + "'")
                            moves += performMoves("U")
                            moves += performMoves(c.toString())
                            moves += performMoves("U U")
                            moves += performMoves(c + "'")
                            moves += performMoves("U")
                        }
                        else if(qb.getNormalVectorAfterRotation(anotherTile, -90f, 'U')
                            == getDirectionByColor(anotherTile.color)){
                            var c = qb.getNormalVectorAfterRotation(anotherTile, 90f, 'U')
                            moves += performMoves(c.toString())
                            moves += performMoves("U")
                            moves += performMoves(c + "'")
                            moves += performMoves("U")
                            moves += performMoves(c.toString())
                            moves += performMoves("U U")
                            moves += performMoves(c + "'")
                            moves += performMoves("U")
                        }
                        else{
                            moves += performMoves("U")
                        }
                    }
                }
            }
        }
        return moves
    }

    //check if cubie on the right corner place
    fun isCornerRightOriented(cubie : Cubie) : Boolean {
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if (tempTiles.any(){x -> x.color == Color.WHITE && x.direction == 'U'}) {
            if(tempTiles.any(){x -> x.color == getColorByDirection(x.direction) && x.color != Color.WHITE}){
                return true
            }
        }
        return false
    }

    fun isEdgeRightOriented(cubie : Cubie) : Boolean{
        var tempTiles = cubie.tiles.filter { x -> x.isActive }
        if(tempTiles[0].direction == getDirectionByColor(tempTiles[0].color)
            && tempTiles[1].direction == getDirectionByColor(tempTiles[1].color)){
            return true
        }
        return false
    }

    fun isYellowEdgeRightOriented(qb : Cubie) : Boolean{
        var tempTiles = qb.tiles.filter { x -> x.isActive }
        if(tempTiles[0].color == Color.YELLOW && getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
            || tempTiles[1].color == Color.YELLOW && getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction){
            return true
        }
        return false
    }

    fun scramble(scramble: String) {
        //Rotate the cube to get white on top, then return cube to original position at end of scramble
        performMoves(scramble)
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
                Thread.sleep(100)
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
                layers.filter { x -> x.layerName == LayerEnum.BACK }.single().rotate(90f)
            }

            "B'" -> {
                preChange = charArrayOf('B', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('B', 'R', 'D', 'L', 'U')
                layers.filter { x -> x.layerName == LayerEnum.BACK }.single().rotate(-90f)
            }

            "D" -> {
                preChange = charArrayOf('D', 'L', 'B', 'R', 'F')
                postChange = charArrayOf('D', 'F', 'L', 'B', 'R')
                layers.filter { x -> x.layerName == LayerEnum.DOWN }.single().rotate(90f)
            }

            "D'" -> {
                preChange = charArrayOf('D', 'F', 'L', 'B', 'R')
                postChange = charArrayOf('D', 'L', 'B', 'R', 'F')
                layers.filter { x -> x.layerName == LayerEnum.DOWN }.single().rotate(-90f)
            }

            "E" -> {
                preChange = charArrayOf('L', 'B', 'R', 'F')
                postChange = charArrayOf('F', 'L', 'B', 'R')
                layers.filter { x -> x.layerName == LayerEnum.EQUATOR }.single().rotate(90f)

            }

            "E'" -> {
                preChange = charArrayOf('F', 'L', 'B', 'R')
                postChange = charArrayOf('L', 'B', 'R', 'F')
                layers.filter { x -> x.layerName == LayerEnum.EQUATOR }.single().rotate(-90f)

            }

            "F" -> {
                preChange = charArrayOf('F', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('F', 'R', 'D', 'L', 'U')
                layers.filter { x -> x.layerName == LayerEnum.FRONT }.single().rotate(90f)

            }

            "F'" -> {
                preChange = charArrayOf('F', 'U', 'R', 'D', 'L')
                postChange = charArrayOf('F', 'L', 'U', 'R', 'D')
                layers.filter { x -> x.layerName == LayerEnum.FRONT }.single().rotate(-90f)

            }

            "L" -> {
                preChange = charArrayOf('L', 'B', 'D', 'F', 'U')
                postChange = charArrayOf('L', 'U', 'B', 'D', 'F')
                layers.filter { x -> x.layerName == LayerEnum.LEFT }.single().rotate(90f)

            }

            "L'" -> {
                preChange = charArrayOf('L', 'U', 'B', 'D', 'F')
                postChange = charArrayOf('L', 'B', 'D', 'F', 'U')
                layers.filter { x -> x.layerName == LayerEnum.LEFT }.single().rotate(-90f)

            }

            "M" -> {
                preChange = charArrayOf('B', 'D', 'F', 'U')
                postChange = charArrayOf('U', 'B', 'D', 'F')
                layers.filter { x -> x.layerName == LayerEnum.MIDDLE }.single().rotate(-90f)

            }

            "M'" -> {
                preChange = charArrayOf('U', 'B', 'D', 'F')
                postChange = charArrayOf('B', 'D', 'F', 'U')
                layers.filter { x -> x.layerName == LayerEnum.MIDDLE }.single().rotate(90f)
            }

            "R" -> {
                preChange = charArrayOf('R', 'U', 'B', 'D', 'F')
                postChange = charArrayOf('R', 'B', 'D', 'F', 'U')
                layers.filter { x -> x.layerName == LayerEnum.RIGHT }.single().rotate(90f)

            }

            "R'" -> {
                preChange = charArrayOf('R', 'B', 'D', 'F', 'U')
                postChange = charArrayOf('R', 'U', 'B', 'D', 'F')
                layers.filter { x -> x.layerName == LayerEnum.RIGHT }.single().rotate(-90f)

            }

            "S" -> {
                preChange = charArrayOf('U', 'R', 'D', 'L')
                postChange = charArrayOf('R', 'D', 'L', 'U')
                layers.filter { x -> x.layerName == LayerEnum.STANDING }.single().rotate(90f)

            }

            "S'" -> {
                preChange = charArrayOf('U', 'R', 'D', 'L')
                postChange = charArrayOf('L', 'U', 'R', 'D')
                layers.filter { x -> x.layerName == LayerEnum.STANDING }.single().rotate(-90f)

            }

            "U" -> {
                preChange = charArrayOf('U', 'F', 'L', 'B', 'R')
                postChange = charArrayOf('U', 'L', 'B', 'R', 'F')
                layers.filter { x -> x.layerName == LayerEnum.UP }.single().rotate(90f)

            }

            "U'" -> {
                preChange = charArrayOf('U', 'L', 'B', 'R', 'F')
                postChange = charArrayOf('U', 'F', 'L', 'B', 'R')
                layers.filter { x -> x.layerName == LayerEnum.UP }.single().rotate(-90f)

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
                    if(tile.color == getColorByDirection(tile.direction)){
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
                if(tempTiles[0].color == Color.YELLOW && getDirectionByColor(tempTiles[1].color) == tempTiles[1].direction
                    || tempTiles[1].color == Color.YELLOW && getDirectionByColor(tempTiles[0].color) == tempTiles[0].direction){
                    num++
                }
            }
        }
        return num
    }

    fun getDirectionByColor(color: Color) : Char{
        for(direction in directions){
            if(direction.color == color){
                return direction.charName
            }
        }
        return 'N'
    }

    fun getColorByDirection(charName : Char) : Color{
        for(direction in directions){
            if(charName == direction.charName){
                return direction.color
            }
        }
        return Color.BLACK
    }

    //how many cubies are right oriented for two layers
    fun numEdgesOriented() : Int{
        var num = 0
        for(qb in cubies) {
            if (qb.isEdge) {
                if (qb.tiles.all { x -> x.color != Color.WHITE && x.color != Color.YELLOW }) {
                    var tempTiles = qb.tiles.filter { x -> x.isActive }
                    if (tempTiles[0].color == getColorByDirection(tempTiles[0].direction)
                        && tempTiles[1].color == getColorByDirection(tempTiles[1].direction))
                    {
                        num++
                        continue
                    }
                }
            }
        }
        return num
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
                    if (qb.tiles.any{x -> x.color == Color.WHITE && x.direction == 'D'}) {
                        moves += prepareSlot(qb.id, 'W')
                        //Get the vertical plane in which the cubie lies
                        val turnToMake = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y, qb.centerPoint.z)
                        moves += performMoves("" + turnToMake + "2 ")
                    }
                }
            }
        }

        //Orients white edges in D Layer with white NOT facing down
        if (numWhiteEdgesOriented() < 5) {
            for (qb in cubies) {
                if (qb.isEdge) {
                    if (qb.tiles.any{x -> x.color == Color.WHITE && x.direction != 'A'}
                        && qb.tiles.any{x -> x.color == Color.WHITE && x.direction != 'D'}) {
                        val vert = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y, qb.centerPoint.z)
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
                                if (tempColors[k].direction === 'L') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("F ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("L' ")
                                }
                            } else if (qb.centerPoint.x == 2.1f && qb.centerPoint.y == -2.1f) {
                                if (tempColors[k].direction === 'F') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("R ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("F' ")
                                }
                            } else if (qb.centerPoint.x == 2.1f && qb.centerPoint.y == 2.1f) {
                                if (tempColors[k].direction === 'B') {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("R' ")
                                } else {
                                    moves += prepareSlot(qb.id, 'W') + performMoves("B ")
                                }
                            } else {
                                if (tempColors[k].direction === 'B') {
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
        /*if (numWhiteEdgesOriented() < 5) {
            for (qb in cubies) {
                if (qb.isEdge) {
                    if (qb.tiles.any{getDirOfColor('W') !== 'A' && qb.getDirOfColor('W') !== 'U') {
                        val vert = qb.verticalFace(qb.centerPoint.x, qb.centerPoint.y, qb.centerPoint.z)
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
        }*/

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

    fun rotateCube(angle : Float, axis: Axis) {
        for (layer in layers) {
            if (Math.abs(layer.layerName.rotationAxis.x) == Math.abs(axis.x)
                && Math.abs(layer.layerName.rotationAxis.y) == Math.abs(axis.y)
                && Math.abs(layer.layerName.rotationAxis.z) == Math.abs(axis.z)
            ) {
                layer.rotate(angle)
            }
        }
    }

    fun updateDirectionsAfterRotation180Degrees(axis: Axis) {
        if (axis == Axis.zAxis || axis == Axis.zMinusAxis) {
            for (direction in directions) {
                if (direction.charName == 'L') {
                    if (direction.color == Color.ORANGE) {
                        direction.changeColor(Color.RED)
                    } else {
                        direction.changeColor(Color.ORANGE)
                    }
                } else if (direction.charName == 'R') {
                    if (direction.color == Color.RED) {
                        direction.changeColor(Color.ORANGE)
                    } else {
                        direction.changeColor(Color.RED)
                    }
                } else if (direction.charName == 'U') {
                    if (direction.color == Color.WHITE) {
                        direction.changeColor(Color.YELLOW)
                    } else {
                        direction.changeColor(Color.WHITE)
                    }
                } else if (direction.charName == 'D') {
                    if (direction.color == Color.YELLOW) {
                        direction.changeColor(Color.WHITE)
                    } else {
                        direction.changeColor(Color.YELLOW)
                    }
                }
            }
        }
        if (axis == Axis.yAxis || axis == Axis.yMinusAxis) {
            for (direction in directions) {
                if (direction.charName == 'L') {
                    if (direction.color == Color.ORANGE) {
                        direction.changeColor(Color.RED)
                    } else {
                        direction.changeColor(Color.ORANGE)
                    }
                } else if (direction.charName == 'R') {
                    if (direction.color == Color.RED) {
                        direction.changeColor(Color.ORANGE)
                    } else {
                        direction.changeColor(Color.RED)
                    }
                } else if (direction.charName == 'B') {
                    if (direction.color == Color.BLUE) {
                        direction.changeColor(Color.GREEN)
                    } else {
                        direction.changeColor(Color.BLUE)
                    }
                } else if (direction.charName == 'F') {
                    if (direction.color == Color.GREEN) {
                        direction.changeColor(Color.BLUE)
                    } else {
                        direction.changeColor(Color.GREEN)
                    }
                }
            }
        }
    }
}