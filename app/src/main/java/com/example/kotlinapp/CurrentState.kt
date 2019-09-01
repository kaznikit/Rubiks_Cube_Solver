package com.example.kotlinapp

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.*
import com.example.kotlinapp.Rubik.Cube
import org.opencv.core.Mat
import java.util.*
import com.example.kotlinapp.Recognition.RubikTile
import com.example.kotlinapp.Rubik.Solver
import com.example.kotlinapp.Util.Constants
import java.lang.Exception

class CurrentState {
    lateinit var activeRubikFace: RubikFace
    lateinit var frontFace: RubikFace

    var rubikFaces  = arrayListOf<RubikFace>()

    var isCubeSolved = false

    // We assume that faces will be explored in a particular sequence.
    var adoptFaceCount = 0

    // Additional Cube Rotation: initially set to Identity Rotation Matrix
    var additionalGLCubeRotation = FloatArray(16)

    // True if it is OK to render GL Pilot Cube
    var renderPilotCube = true

    // Intrinsic Camera Calibration Parameters from hardware.
    var cameraCalibration: Calibration? = null

    /**
     * If cube is scanning
     */
    var IsCubeScannedAndReset = false

    /**
     * If cube is solving
     */
    var IsCubeSolving = false

    var colorDetector: ColorDetector? = null
    var mainActivity : MainActivity

    var cube : Cube
    var solver : Solver

    constructor(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
        cameraCalibration = Calibration(mainActivity)
        colorDetector = ColorDetector()
        cube = Cube()
        solver = Solver()
        addNewFace(LayerEnum.DOWN, LayerEnum.FRONT)
    }

    fun calculateTilesForFace(rectangleList : LinkedList<Rectangle>, image : Mat){
        //cube colors obtaining each frame
        var faceColors = activeRubikFace.calculateTiles(rectangleList, image)

        if(!IsCubeScannedAndReset){
            createCubeFromScannedTiles(faceColors)
        }
        else if(IsCubeSolving){
            guideSolvingCube(faceColors)
        }
    }

    /**
       Main method for scanning the cube and fill it's sides
     **/
    fun createCubeFromScannedTiles(faceColors : Array<Array<RubikTile?>>?){
        if(faceColors != null) {
            if (isCubeSolved && adoptFaceCount > 6) {
                //if cube solved we should just check
                // which side of cube user holds the cube and rotate drawing cube
                if (!IsCubeScannedAndReset) {
                    checkCubeDownside(faceColors)
                } else if (checkActiveTileColors(faceColors) && adoptFaceCount > 7) {
                    //findActiveSideOfCube(faceColors)

                    //solving cube
                    /*var whiteCross = solver.logicCube.makeWhiteCross()
                    cube.performMoves(whiteCross)*/
                    IsCubeSolving = true
                }
                return
            }

            //если прошел цикл определения
            if (activeRubikFace.ColorDetectionCount == 4) {
                for (j in 0..8) {
                    var colorTime = 0
                    var color2Time = 0
                    val temp = activeRubikFace.averageColorArray[0][j]
                    var temp2 = activeRubikFace.averageColorArray[1][j]
                    for (i in 2..3) {
                        if (activeRubikFace.averageColorArray[i][j]?.cvColor === temp?.cvColor) {
                            colorTime++
                        } else {
                            temp2 = activeRubikFace.averageColorArray[i][j]
                            color2Time++
                        }
                    }
                    if (colorTime > color2Time) {
                        activeRubikFace.observedTileArray[j / 3][j % 3]?.tileColor = temp!!
                    } else {
                        activeRubikFace.observedTileArray[j / 3][j % 3]?.tileColor = temp2!!
                    }
                }
                activeRubikFace.transformedTileArray = activeRubikFace.observedTileArray.clone()
                //cube.fillFaceColors(activeRubikFace)
                activeRubikFace.ColorDetectionCount++
                activeRubikFace.faceRecognitionStatus = RubikFace.FaceRecognitionStatusEnum.SOLVED
                adoptFaceCount++
            } else if (activeRubikFace.ColorDetectionCount > 4) {
                //проверяем повернули ли сторону кубика
                if (!checkIfFaceExist(faceColors) || adoptFaceCount > 5) {
                    //кубик повернут другой стороной
                    mainActivity.glRenderer.drawArrow(false, 'D')
                    adopt(activeRubikFace)
                } else {
                    mainActivity.glRenderer.drawArrow(true, getNextArrowRotation())
                }
            }
        }
    }

    /**
     * Main method for solving the cube
     */
    fun guideSolvingCube(faceColors : Array<Array<RubikTile?>>?){
        
    }

    fun getNextArrowRotation() : Char{
        when (adoptFaceCount) {
            1 -> return 'F'
            2 -> return 'R'
            3 -> return 'F'
            4 -> return 'R'
            5 -> return 'F'
            6 -> return 'R'
        }
        return 'F'
    }

    fun addNewFace(activeFaceName: LayerEnum, frontFaceName: LayerEnum) {
        while (!cube.getPermutationAllowance()) {
            Thread.sleep(50)
        }

        //create first face in front of user
        val activeFace = RubikFace(activeFaceName, this)
        activeRubikFace = activeFace
        //face shown to user
        frontFace = if (!rubikFaces.any { x -> x.layerName == frontFaceName }) {
            val activeFrontFace = RubikFace(frontFaceName, this)
            activeFrontFace.createObservedTilesArray()
            activeFrontFace
        } else {
            if(rubikFaces.filter { x -> x.layerName == frontFaceName }.size > 1){
                rubikFaces.remove(rubikFaces.filter { x ->x.layerName == frontFaceName }[0])
            }
            rubikFaces.single { x -> x.layerName == frontFaceName }
        }
        activeFace.createObservedTilesArray()
        rubikFaces.add(activeFace)
    }

    fun adopt(rubikFace: RubikFace) {
        while(!cube.getPermutationAllowance()){
            Thread.sleep(50)
        }
        when (adoptFaceCount) {
            1 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.LEFT, LayerEnum.FRONT)
            }
            2 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.xAxis)
                addNewFace(LayerEnum.FRONT, LayerEnum.RIGHT)
            }
            3 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.UP, LayerEnum.RIGHT)
            }
            4 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.xAxis)
                addNewFace(LayerEnum.RIGHT, LayerEnum.DOWN)
            }
            5 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.BACK, LayerEnum.DOWN)
            }
            6 -> {
                if(!cube.fillFaceColors(rubikFace)){
                    return
                }
                var response = cube.rotateCube(-90f, Axis.xAxis)
                isCubeSolved = true
                adoptFaceCount++
                createSolver()
            }
        }
    }

    fun createSolver(){
        solver.copyCube(cube)
    }

    //check if user turned cube to the start position
    fun checkCubeDownside(tiles: Array<Array<RubikTile?>>) {
        if (tiles != null) {
            //how many tiles have the same colors
            for (face in rubikFaces) {
                if (face!!.layerName == LayerEnum.DOWN) {
                    var coincide = 0
                    for (i in 0..2) {
                        for (j in 0..2) {
                            if (tiles!![i][j]?.tileColor == Color.BLACK || face == null || face!!.transformedTileArray == null) {
                                continue
                            }
                            if (face!!.transformedTileArray[i][j]?.tileColor == tiles!![i][j]?.tileColor) {
                                coincide++
                            }
                        }
                    }
                    if (coincide > 5) {
                        IsCubeScannedAndReset = true
                        adoptFaceCount++
                    }
                }
            }
        }
    }

    fun checkActiveTileColors(tiles: Array<Array<RubikTile?>>): Boolean {
        if (tiles != null && tiles.size == 0) {
            return false
        }
        //how many tiles have the same colors
        var coincide = 0
        for (k in 0..2) {
            for (l in 0..2) {
                for (i in 0..2) {
                    for (j in 0..2) {
                        if (activeRubikFace.transformedTileArray[i][j]?.tileColor == tiles!![k][l]?.tileColor
                            && tiles!![k][l]?.tileState != Constants.TileState.PROCESSED) {
                            tiles!![k][l]?.tileState = Constants.TileState.PROCESSED
                            coincide++
                        }
                    }
                }
                if (coincide > 5) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * check by two tiles
     * @param tiles
     * @return true if face exist, false if not
     */
    fun checkIfFaceExist(tiles: Array<Array<RubikTile?>>): Boolean {
        if (tiles != null && tiles.isEmpty()) {
            return false
        }
        //check the center tile
        for (face in rubikFaces) {
            if (face == null || face!!.transformedTileArray == null) {
                continue
            }
            var sameTiles = 0
            if (face!!.transformedTileArray[1][1]?.tileColor === tiles!![1][1]?.tileColor) {
                //if we found a face with such center tile
                sameTiles++
            }
            //check the tile close to center
            if (face!!.transformedTileArray[2][1]?.tileColor === tiles!![2][1]?.tileColor) {
                sameTiles++
            }
            //if we found the same two tiles, need to scan more
            if (sameTiles > 1) {
                return true
            }
        }
        return false
    }

    fun resetFaces() {
        adoptFaceCount = 0
        if (rubikFaces.size != 0) {
            for (i in 0 until rubikFaces.size - 1) {
                rubikFaces.remove(rubikFaces[i])
            }
        }
        addNewFace(LayerEnum.DOWN, LayerEnum.FRONT)
    }

    //find which side of scanned cube is active
    fun findActiveSideOfCube(tiles: Array<Array<RubikTile?>>) {
        //take down layer
        val activeLayer = cube.layers.single{x -> x.direction == cube.directionsControl.getDirectionByCharName ('D')}

        if (activeLayer != null) {
            //center tile is not active ==> find active layer
            if (!activeLayer.cubies.any { c -> c.tiles.any { t -> t.color == tiles[1][1]?.tileColor && t.isActive } && !c.isEdge && !c.isCorner }) {
                for (layer in cube.getSideLayers()) {
                    try {
                        //firstly check the center tile
                        var centerCubie = layer.cubies.single { x -> !x.isCorner && !x.isEdge }
                        if (centerCubie.tiles.any { t -> t.isActive && t.color == tiles[1][1]?.tileColor }) {
                            //check if this face is active
                            var activeLayerCenterCubie =
                                activeLayer.cubies.single { x -> !x.isCorner && !x.isEdge }
                            if (layer.layerName != activeLayer.layerName
                                && !activeLayerCenterCubie.tiles.any { t -> t.isActive && t.color == centerCubie.tiles.single { x -> x.isActive }.color }
                            ) {
                                //if it's not active, then firstly rotate cube by active side
                                if (layer.layerName == LayerEnum.FRONT || layer.layerName == LayerEnum.UP) {
                                    cube.rotateCube(-90f, Axis.xAxis)
                                } else if (layer.layerName == LayerEnum.LEFT) {
                                    cube.rotateCube(-90f, Axis.zAxis)
                                } else if (layer.layerName == LayerEnum.RIGHT) {
                                    cube.rotateCube(90f, Axis.zAxis)
                                } else {
                                    cube.rotateCube(90f, Axis.xAxis)
                                }
                            }
                        }
                    }
                    catch(ex : Exception){
                        return
                    }
                }
            }

            while (!cube.getPermutationAllowance()){
                Thread.sleep(50)
            }

            try {
                //take front left tile
                var frontDownCubie =
                    activeLayer.cubies.single { x -> x.tiles.any { t -> t.direction == 'F' && t.isActive } && x.tiles.any { t -> t.direction == 'L' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } }

                //also compare back left tile
                var backLeftCubie =
                    activeLayer.cubies.single { x -> x.tiles.any { t -> t.direction == 'L' && t.isActive } && x.tiles.any { t -> t.direction == 'D' && t.isActive } && x.tiles.any { t -> t.direction == 'B' && t.isActive } }

                if (!frontDownCubie.tiles.any { x -> x.isActive && x.direction == 'D' && x.color == tiles[0][0]?.tileColor }
                    && !backLeftCubie.tiles.any { x -> x.isActive && x.direction == 'D' && x.color == tiles[2][0]?.tileColor }) {
                    cube.rotateCube(-90f, Axis.yAxis)
                }
            }
            catch(ex : Exception){

            }
        }
    }
}