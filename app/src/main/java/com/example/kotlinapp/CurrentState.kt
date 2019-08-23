package com.example.kotlinapp

import android.opengl.Matrix
import android.os.Environment
import android.util.Log
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.*
import com.example.kotlinapp.Rubik.Cube
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import java.io.*
import java.util.*

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
        colorDetector = ColorDetector()
        cube = Cube()
        activeRubikFace = RubikFace(LayerEnum.DOWN, this)
        activeRubikFace.transformedTileArray[0][0] = RubikTile(null, Color.RED)
        activeRubikFace.transformedTileArray[0][1] = RubikTile(null, Color.BLUE)
        activeRubikFace.transformedTileArray[0][2] = RubikTile(null, Color.GREEN)
        activeRubikFace.transformedTileArray[1][0] = RubikTile(null, Color.ORANGE)
        activeRubikFace.transformedTileArray[1][1] = RubikTile(null, Color.WHITE)
        activeRubikFace.transformedTileArray[1][2] = RubikTile(null, Color.RED)
        activeRubikFace.transformedTileArray[2][0] = RubikTile(null, Color.YELLOW)
        activeRubikFace.transformedTileArray[2][1] = RubikTile(null, Color.BLUE)
        activeRubikFace.transformedTileArray[2][2] = RubikTile(null, Color.ORANGE)

        cube.fillFaceColors(activeRubikFace)

        addNewFace(LayerEnum.DOWN, LayerEnum.FRONT)
    }

    fun calculateTilesForFace(rectangleList : LinkedList<Rectangle>, image : Mat){
        activeRubikFace.calculateTiles(rectangleList, image)
    }

    fun addNewFace(activeFaceName: LayerEnum, frontFace: LayerEnum) {
        //create first face in front of user
        //val activeFace = RubikFace(this)
        //activeFace.faceNameEnum = activeFaceName

        /*activeFace.rotationAxis = Axis.yMinusAxis
        activeRubikFace = activeFace

        //face shown to user
        if (!Arrays.asList(cube.rubikFaces).contains(frontFace)) {
            val activeFrontFace = RubikFace()
            activeFrontFace.faceNameEnum = frontFace
            activeFrontFace.createObservedTilesArray()
            //activeFrontFace.faceNameEnum.axis = Constants.RotationAxis.zAxis;
            activeFrontFace.rotationAxis = Axis.zAxis
            MainActivity.currentState.frontFace = activeFrontFace
        } else {
            val index = Arrays.asList(mCube.rubikFaces).lastIndexOf(frontFace)
            MainActivity.currentState.frontFace = mCube.rubikFaces[index]
        }

        activeFace.createObservedTilesArray()
        mCube.rubikFaces[MainActivity.currentState.adoptFaceCount] = activeFace
        glRenderer.CalculateRotationAxis()*/
    }

    fun adopt(rubikFace: RubikFace) {
        when (adoptFaceCount) {
            1 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(90f, Axis.zAxis)
                addNewFace(LayerEnum.LEFT, LayerEnum.FRONT)
            }
            2 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(90f, Axis.xAxis)
                addNewFace(LayerEnum.FRONT, LayerEnum.RIGHT)
            }
            3 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(90f, Axis.zAxis)
                addNewFace(LayerEnum.UP, LayerEnum.RIGHT)
            }
            4 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(90f, Axis.xAxis)
                addNewFace(LayerEnum.RIGHT, LayerEnum.DOWN)
            }
            5 -> {
                cube.fillFaceColors(rubikFace)
                cube.rotateCube(90f, Axis.zAxis)
                addNewFace(LayerEnum.BACK, LayerEnum.DOWN)
                isCubeSolved = true
            }
        }
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