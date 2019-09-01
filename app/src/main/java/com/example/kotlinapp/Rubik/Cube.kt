package com.example.kotlinapp.Rubik

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.RubikFace
import com.example.kotlinapp.Rubik.Abstract.ICube
import com.example.kotlinapp.Rubik.Abstract.ICubie
import com.example.kotlinapp.Rubik.Abstract.ILayer
import java.lang.Exception

class Cube : ICube {
    override val permutationLock = Any()

    val spaceBetweenCubies: Float = 0.3f

    val lowerBound: Float = -3.0f
    val upperBound: Float = 3.0f

    val sideLength: Float = Vertex.RoundFloat((upperBound - lowerBound - spaceBetweenCubies * 2) / 3.0f)

    var cubies = arrayListOf<Cubie>()
    var layers = arrayListOf<Layer>()

    override var directionsControl = DirectionsControl()

    override var permutationAllowed = true

    //for whole cube rotations
    override var rotationAxis = Axis.xAxis
    override var rotationAngle = 0f

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
            //Thread.sleep(200)
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
            }
            Thread.sleep(50)
        }
        return moves
    }

    //add rubik face colors to the necessary layer
    fun fillFaceColors(rubikFace: RubikFace) : Boolean{
        while(!getPermutationAllowance()){
            Thread.sleep(50)
        }
        var layer = layers.filter { x -> x.layerName == LayerEnum.DOWN }.single()

        var tempCubies: List<ICubie>? = sortCubiesForLayer(layer) ?: return false

        var k = 0
        for(rubikTileArray in rubikFace.transformedTileArray){
            for(rubikTile in rubikTileArray){
                var cubie = cubies.single { x -> x.id == tempCubies!![k]!!.id }

                cubie.tiles.single { x -> x.isActive && x.direction == layer.direction.charName }
                    .setTileColor(rubikTile!!.tileColor)
                cubies[cubie.id] = cubie
                k++
            }
        }

        while(!getPermutationAllowance()){
            Thread.sleep(20)
        }
        return true
    }

    //sort cubies for layer to go on X axis
    override fun sortCubiesForLayer(iLayer: ILayer) : List<ICubie>? {
        try {
            var layer = iLayer as Layer
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
            while(!getPermutationAllowance()){
                Thread.sleep(50)
            }
            return null
        }
    }

    //get layers on the sides of cube
    fun getSideLayers() : List<Layer>{
        return layers.filter { x -> x.layerName != LayerEnum.MIDDLE && x.layerName != LayerEnum.EQUATOR && x.layerName != LayerEnum.STANDING }
    }

    override fun turn(turn: String) {
        while(!getPermutationAllowance()){
            Thread.sleep(50)
        }
        setPermutationAllowance(false)
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
            else -> {
                setPermutationAllowance(true)
            }
        }
    }

    override fun rotateCube(angle: Float, axis: Axis): Boolean {
        while(!getPermutationAllowance()){
            Thread.sleep(50)
        }
        setPermutationAllowance(false)
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

        while(!getPermutationAllowance()){
            Thread.sleep(50)
        }
        return true
    }

    override fun setPermutationAllowance(value : Boolean){
        synchronized(permutationLock, block = {permutationAllowed = value})
    }

    override fun getPermutationAllowance() : Boolean{
        synchronized(permutationLock, block = {return permutationAllowed})
    }
}