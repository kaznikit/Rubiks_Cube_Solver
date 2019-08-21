package com.example.kotlinapp

import android.opengl.Matrix
import android.os.Environment
import android.util.Log
import com.example.kotlinapp.Recognition.RubikFace
import org.opencv.core.Scalar
import org.opencv.core.Size
import java.io.*
import java.util.HashMap

class CurrentState {
    // Rubik Face of latest processed frame: may or may not be any of the six state objects.
    //var lateinit activeRubikFace: RubikFace
   // var lateinit frontFace: RubikFace

    /*
     * This is "Rubik Cube State" or "Rubik Cube Model" in model-view-controller vernacular.
     * Map of above rubik face objects index by FaceNameEnum
     */
    //var nameRubikFaceMap = HashMap<Constants.FaceNameEnum, RubikFace>(6)

    /*
     * This is a hash map of OpenCV colors that are initialized to those specified by field
     * rubikColor of ColorTileEnum.   Function reevauateSelectTileColors() adjusts these
     * colors according to a Mean-Shift algorithm to correct for lumonosity.
     */
    //var mutableTileColors = HashMap<Constants.ColorTileEnum, Scalar>(6)


    var isCubeSolved = false

    // Result when Two Phase algorithm is ask to evaluate if cube in valid.  If valid, code is zero.
    var verificationResults: Int = 0

    // String notation on how to solve cube.
    var solutionResults: String? = null

    // Above, but broken into individual moves.
    var solutionResultsArray: Array<String>? = null

    // Index to above array as to which move we are on.
    var solutionResultIndex: Int = 0

    // We assume that faces will be explored in a particular sequence.
    var adoptFaceCount = 0

    // Additional Cube Rotation: initially set to Identity Rotation Matrix
    var additionalGLCubeRotation = FloatArray(16)

    // True if it is OK to render GL Pilot Cube
    var renderPilotCube = true

    // Intrinsic Camera Calibration Parameters from hardware.
    //var cameraCalibration: Calibration? = null

    var IsCubeScannedAndReset = false

    // For the purpose of running the Autocovariance Least Squares Method to object Kalman Filter Covariance Matrices
    //  public KalmanFilterALSM kalmanFilterALSM;

    // Display size of JavaCameraView and OpenCV InputFrame
    @Transient
    var openCVSize: Size? = null

    // Display size of OpenGL Rendering Surface
    @Transient
    var openGLSize: Size? = null

    //var ColorDetector: ColorDetector? = null

    // =+= DO
    // Objects Kalman filter and Cube Reconstructor should be member data of Image Recognizer
    // Position and Rotation should be stored as Cube Pose objects here.

    /**
     * Default State Model Constructor
     */
    constructor(){
        //reset();
    }


    /**
     * Adopt Face
     *
     * Adopt faces in a particular sequence dictated by the user directed instruction on
     * how to rotate the code during the exploration phase.  Also tile name is
     * specified at this time, and "transformedTileArray" is created which is a
     * rotated version of the observed tile array so that the face orientations
     * match the convention of a cut-out rubik cube layout.
     */
    /*fun adopt(rubikFace: RubikFace) {
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
        }//}
        //}
        //  }
        //  }
        // =+= log error ?

        if (adoptFaceCount < 6) {
            // Record Face by Name: i.e., UP, DOWN, LEFT, ...
            nameRubikFaceMap[rubikFace.faceNameEnum] = rubikFace
        }
    }

    fun drawArchArrow() {
        when (adoptFaceCount) {
            1 -> MainActivity.glRenderer.drawArrow(true)
            2 -> MainActivity.glRenderer.drawArrow(true)
            3 -> MainActivity.glRenderer.drawArrow(true)
            4 -> MainActivity.glRenderer.drawArrow(true)
            5 -> MainActivity.glRenderer.drawArrow(true)
        }//MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.LEFT, Constants.FaceNameEnum.FRONT);
        // MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.FRONT, Constants.FaceNameEnum.RIGHT);
        //MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.UP, Constants.FaceNameEnum.RIGHT);
        //MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.RIGHT, Constants.FaceNameEnum.DOWN);
        //MainActivity.glRenderer.rotateCubeDegrees(Constants.FaceNameEnum.BACK, Constants.FaceNameEnum.DOWN);
        // =+= log error ?
    }


    /**
     * Get Rubik Face by Name
     *
     * @param faceNameEnum
     * @return
     */
    fun getFaceByName(faceNameEnum: Constants.FaceNameEnum): RubikFace {
        return nameRubikFaceMap[faceNameEnum]
    }


    /**
     * Return the number of valid and adopted faces.  Maximum is of course six.
     *
     * @return
     */
    fun getNumObservedFaces(): Int {
        return nameRubikFaceMap.size
    }


    /**
     * Return true if all six faces have been observed and adopted.
     *
     * @return
     */
    fun isThereAfullSetOfFaces(): Boolean {
        return if (getNumObservedFaces() >= 6)
            true
        else
            false
    }


    /**
     * Get String Representation of Cube
     *
     * The "String Representation" is a per the two-phase rubik cube
     * logic solving algorithm requires.
     *
     * This should only be called if cube if colors are valid.
     *
     * @return
     */
    fun getStringRepresentationOfCube(): String {

        // Create a map of tile color to face name. The center tile of each face is used for this
        // definition.  This information is used by Rubik Cube Logic Solution.
        val colorTileToNameMap = HashMap<Constants.ColorTileEnum, Constants.FaceNameEnum>(6)
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.UP).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.UP
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.DOWN).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.DOWN
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.LEFT).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.LEFT
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.RIGHT).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.RIGHT
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.FRONT).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.FRONT
        colorTileToNameMap[getFaceByName(Constants.FaceNameEnum.BACK).transformedTileArray[1][1].tileColor] =
            Constants.FaceNameEnum.BACK


        val sb = StringBuffer()
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.UP)))
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.RIGHT)))
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.FRONT)))
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.DOWN)))
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.LEFT)))
        sb.append(getStringRepresentationOfFace(colorTileToNameMap, getFaceByName(Constants.FaceNameEnum.BACK)))
        return sb.toString()
    }


    /**
     * Get String Representing a particular Face.
     */
    private fun getStringRepresentationOfFace(
        colorTileToNameMap: HashMap<Constants.ColorTileEnum, Constants.FaceNameEnum>,
        rubikFace: RubikFace
    ): StringBuffer {

        val sb = StringBuffer()
        val virtualLogicalTileArray = rubikFace.transformedTileArray
        virtualLogicalTileArray.notify()
        for (m in 0..2)
            for (n in 0..2)
                sb.append(getCharacterRepresentingColor(colorTileToNameMap, virtualLogicalTileArray[n][m].tileColor))
        return sb
    }


    /**
     * Get Character Representing Color
     *
     * Return single character representing Face Name (i.e., Up, Down, etc...) of face
     * who's center tile is of the passed in arg.
     */
    private fun getCharacterRepresentingColor(
        colorTileToNameMap: HashMap<Constants.ColorTileEnum, Constants.FaceNameEnum>,
        colorEnum: Constants.ColorTileEnum
    ): Char {

        //		Log.e(Constants.TAG_COLOR, "colorEnum=" + colorEnum + " colorTileToNameMap=" + colorTileToNameMap);

        when (colorTileToNameMap[colorEnum]) {
            FRONT -> return 'F'
            BACK -> return 'B'
            DOWN -> return 'D'
            LEFT -> return 'L'
            RIGHT -> return 'R'
            UP -> return 'U'
            else -> return 0.toChar()   // Odd error message without this, but cannot get here by definition.  Hmm.
        }
    }


    /**
     * Save cube state to file.
     */
    fun saveState() {

        val rubikFaceArray = arrayOf<RubikFace>(
            getFaceByName(Constants.FaceNameEnum.UP),
            getFaceByName(Constants.FaceNameEnum.RIGHT),
            getFaceByName(Constants.FaceNameEnum.FRONT),
            getFaceByName(Constants.FaceNameEnum.DOWN),
            getFaceByName(Constants.FaceNameEnum.LEFT),
            getFaceByName(Constants.FaceNameEnum.BACK)
        )

        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val filename = "cube.ser"
            val file = File(path, filename)
            val out = ObjectOutputStream(FileOutputStream(file))
            out.writeObject(rubikFaceArray)
            out.flush()
            out.close()
            Log.i(Constants.TAG, "SUCCESS writing cube state to external storage:$filename")
        } catch (e: Exception) {
            print(e)
            Log.e(Constants.TAG, "Fail writing cube state to external storage: $e")
        }

    }


    /**
     * Recall cube state (i.e., the six faces) from file.
     */
    fun recallState() {

        var rubikFaceArray = arrayOfNulls<RubikFace>(6)

        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val filename = "cube.ser"
            val file = File(path, filename)
            val `in` = ObjectInputStream(FileInputStream(file))
            rubikFaceArray = `in`.readObject() as Array<RubikFace>
            `in`.close()
            Log.i(Constants.TAG, "SUCCESS reading cube state to external storage:$filename")
        } catch (e: Exception) {
            print(e)
            Log.e(Constants.TAG, "Fail reading cube to external storage: $e")
        }

        nameRubikFaceMap[Constants.FaceNameEnum.UP] = rubikFaceArray[0]
        nameRubikFaceMap[Constants.FaceNameEnum.RIGHT] = rubikFaceArray[1]
        nameRubikFaceMap[Constants.FaceNameEnum.FRONT] = rubikFaceArray[2]
        nameRubikFaceMap[Constants.FaceNameEnum.DOWN] = rubikFaceArray[3]
        nameRubikFaceMap[Constants.FaceNameEnum.LEFT] = rubikFaceArray[4]
        nameRubikFaceMap[Constants.FaceNameEnum.BACK] = rubikFaceArray[5]
    }


    /**
     * Reset
     *
     * Reset state to the initial values.
     */
    fun reset() {

        // Rubik Face of latest processed frame: may or may not be any of the six state objects.
        //activeRubikFace = null;

        // Array of above rubik face objects index by FaceNameEnum
        nameRubikFaceMap = HashMap<Constants.FaceNameEnum, RubikFace>(6)

        // Array of tile colors index by ColorTileEnum.
        mutableTileColors.clear()
        for (colorTile in Constants.ColorTileEnum.values())
            if (colorTile.isRubikColor === true)
                mutableTileColors[colorTile] = colorTile.rubikColor

        // Application State = null; see AppStateEnum.
        appState = Constants.AppStateEnum.START

        // Stable Face Recognizer State
        gestureRecogniztionState = Constants.GestureRecogniztionStateEnum.UNKNOWN

        // Result when Two Phase algorithm is ask to evaluate if cube in valid.  If valid, code is zero.
        verificationResults = 0

        // String notation on how to solve cube.
        solutionResults = null

        // Above, but broken into individual moves.
        solutionResultsArray = null

        // Index to above array as to which move we are on.
        solutionResultIndex = 0

        // We assume that faces will be explored in a particular sequence.
        adoptFaceCount = 0

        // True if we are to render GL Pilot Cube
        renderPilotCube = true

        // Set additional GL cube rotation to Identity Rotation Matrix
        Matrix.setIdentityM(additionalGLCubeRotation, 0)
    }*/
}