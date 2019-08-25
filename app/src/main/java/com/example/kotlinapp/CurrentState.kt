package com.example.kotlinapp

import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.*
import com.example.kotlinapp.Rubik.Cube
import org.opencv.core.Mat
import java.util.*
import com.example.kotlinapp.Recognition.RubikTile
import com.example.kotlinapp.Util.Constants

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

    var IsCubeScannedAndReset = false

    var colorDetector: ColorDetector? = null
    var mainActivity : MainActivity

    var cube : Cube

    constructor(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
        cameraCalibration = Calibration(mainActivity)
        colorDetector = ColorDetector()
        cube = Cube()
        addNewFace(LayerEnum.DOWN, LayerEnum.FRONT)
    }

    fun calculateTilesForFace(rectangleList : LinkedList<Rectangle>, image : Mat){
        var faceColors = activeRubikFace.calculateTiles(rectangleList, image)

        if(faceColors != null) {
            if (isCubeSolved && adoptFaceCount > 6) {
                //if cube solved we should just check
                // which side of cube user holds the cube and rotate drawing cube
                if (!IsCubeScannedAndReset) {
                    checkCubeDownside(faceColors)
                } else if (checkActiveTileColors(faceColors) && adoptFaceCount > 7) {
                    //glRenderer.findActiveSideOfCube(faceColors)
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
                cube.fillFaceColors(activeRubikFace)
                activeRubikFace.ColorDetectionCount++
                activeRubikFace.faceRecognitionStatus = RubikFace.FaceRecognitionStatusEnum.SOLVED
                adoptFaceCount++
            } else if (activeRubikFace.ColorDetectionCount > 4) {
                //проверяем повернули ли сторону кубика
                if (!checkIfFaceExist(faceColors)) {
                    //кубик повернут другой стороной
                    mainActivity.glRenderer.drawArrow(false)
                    adopt(activeRubikFace)
                } else {
                    mainActivity.glRenderer.drawArrow(true)
                }
            }
            //processFinished = true
        }
    }

    fun addNewFace(activeFaceName: LayerEnum, frontFaceName: LayerEnum) {
        //create first face in front of user
        val activeFace = RubikFace(activeFaceName, this)
        activeRubikFace = activeFace
        //face shown to user
        frontFace = if (!rubikFaces.any{ x -> x.layerName == frontFaceName}) {
            val activeFrontFace = RubikFace(frontFaceName, this)
            activeFrontFace.createObservedTilesArray()
            activeFrontFace
        } else {
            rubikFaces.filter { x -> x.layerName == frontFaceName }.single()
        }
        activeFace.createObservedTilesArray()
        rubikFaces.add(activeFace)
    }

    fun adopt(rubikFace: RubikFace) {
        when (adoptFaceCount) {
            1 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.LEFT, LayerEnum.FRONT)
            }
            2 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(-90f, Axis.xAxis)
                addNewFace(LayerEnum.FRONT, LayerEnum.RIGHT)
            }
            3 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.UP, LayerEnum.RIGHT)
            }
            4 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(-90f, Axis.xAxis)
                addNewFace(LayerEnum.RIGHT, LayerEnum.DOWN)
            }
            5 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(-90f, Axis.zAxis)
                addNewFace(LayerEnum.BACK, LayerEnum.DOWN)
                isCubeSolved = true
            }
        }
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
   /*fun drawArchArrow() {
        when (adoptFaceCount) {
            1 -> MainActivity.glRenderer.drawArrow(true)
            2 -> MainActivity.glRenderer.drawArrow(true)
            3 -> MainActivity.glRenderer.drawArrow(true)
            4 -> MainActivity.glRenderer.drawArrow(true)
            5 -> MainActivity.glRenderer.drawArrow(true)
        }
    }*/
}