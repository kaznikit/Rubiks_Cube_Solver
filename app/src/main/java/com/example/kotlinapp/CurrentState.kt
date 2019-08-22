package com.example.kotlinapp

import android.opengl.Matrix
import android.os.Environment
import android.util.Log
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.Recognition.Calibration
import com.example.kotlinapp.Recognition.ColorDetector
import com.example.kotlinapp.Recognition.RubikFace
import com.example.kotlinapp.Rubik.Cube
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
        addNewFace(LayerEnum.DOWN, LayerEnum.FRONT)
    }

    fun addNewFace(activeFaceName: LayerEnum, frontFace: LayerEnum) {
        //create first face in front of user
        //val activeFace = RubikFace()
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

   /* fun adopt(rubikFace: RubikFace) {
        when (adoptFaceCount) {
            1 -> {
                MainActivity.glRenderer.fillFaceColors(rubikFace.faceNameEnum, rubikFace.transformedTileArray)
                MainActivity.glRenderer.isCubeRotation = true
                MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.LEFT.axis)
                //if(MainActivity.glRenderer.CalculateRotationAxis()) {
                MainActivity.addNewFace(Constants.FaceNameEnum.LEFT, Constants.FaceNameEnum.FRONT)
            }
            2 -> {
                MainActivity.glRenderer.fillFaceColors(rubikFace.faceNameEnum, rubikFace.transformedTileArray)
                MainActivity.glRenderer.isCubeRotation = true
                MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.FRONT.axis)
                //if(MainActivity.glRenderer.CalculateRotationAxis()) {
                MainActivity.addNewFace(Constants.FaceNameEnum.FRONT, Constants.FaceNameEnum.RIGHT)
            }
            3 -> {
                MainActivity.glRenderer.fillFaceColors(rubikFace.faceNameEnum, rubikFace.transformedTileArray)
                MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.UP.axis)
                MainActivity.glRenderer.isCubeRotation = true
                // if(MainActivity.glRenderer.CalculateRotationAxis()) {
                MainActivity.addNewFace(Constants.FaceNameEnum.UP, Constants.FaceNameEnum.RIGHT)
            }
            4 -> {
                MainActivity.glRenderer.fillFaceColors(rubikFace.faceNameEnum, rubikFace.transformedTileArray)
                MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.RIGHT.axis)
                MainActivity.glRenderer.isCubeRotation = true
                //  if(MainActivity.glRenderer.CalculateRotationAxis()) {
                MainActivity.addNewFace(Constants.FaceNameEnum.RIGHT, Constants.FaceNameEnum.DOWN)
            }
            5 -> {
                MainActivity.glRenderer.fillFaceColors(rubikFace.faceNameEnum, rubikFace.transformedTileArray)
                MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.BACK.axis)
                MainActivity.glRenderer.isCubeRotation = true
                //  if(MainActivity.glRenderer.CalculateRotationAxis()) {
                MainActivity.addNewFace(Constants.FaceNameEnum.BACK, Constants.FaceNameEnum.DOWN)
                //  }
                isCubeSolved = true
            }
        }
        if (adoptFaceCount < 6) {
            // Record Face by Name: i.e., UP, DOWN, LEFT, ...
            nameRubikFaceMap[rubikFace.faceNameEnum] = rubikFace
        }
    }*/

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