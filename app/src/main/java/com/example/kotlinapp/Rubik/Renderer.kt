package com.example.kotlinapp.Rubik

import android.app.Activity
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.example.kotlinapp.CurrentState
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.Enums.Color
import com.example.kotlinapp.Enums.Direction
import com.example.kotlinapp.Enums.LayerEnum
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.R
import com.example.kotlinapp.Util.ShaderUtils
import org.opencv.core.Point
import java.lang.Math.abs
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer : GLSurfaceView.Renderer {
    val context: Context
    var programId: Int = 0

    //region Arrows
    enum class ArrowRotation {
        CLOCKWISE, COUNTER_CLOCKWISE, ONE_HUNDRED_EIGHTY
    }
    enum class ArrowDirection {
        POSITIVE, NEGATIVE
    }

    private var arrowQuarterTurn: ArrowArch? = null
    private var arrowHalfTurn: ArrowArch? = null
    private var isArrowDrawing = false
    private var arrowDirection = "R"
    private var mArrowMatrix = FloatArray(16)
    private val mMatrixArrow = FloatArray(16)
    private var rotation = 0f
    private var isLayerArrow = false
    private var arrowDirectionValue = ArrowDirection.POSITIVE

    //endregion

    //region Matrices
    val mProjectionMatrix = FloatArray(16)
    val mViewMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)
    val mMatrix = FloatArray(16)
    var mCubeModelMatrix = FloatArray(16)
//endregion

    var uMatrixLocation: Int = 0

    var view: View
    lateinit var state: CurrentState

    val displayMetrics = DisplayMetrics()

    private val SWIPE_MIN_DISTANCE = 120
    private val SWIPE_THRESHOLD_VELOCITY = 200
    var density: Float = 0.toFloat()

    private val timeReference : Long = 0

    constructor(view: View, context: Context, state: CurrentState) {
        this.view = view
        this.context = context
        this.state = state
        val main = context as Activity
        main.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        density = displayMetrics.density;
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GLES20.GL_CULL_FACE)
        glClearColor(0f, 0f, 0f, 0f)
        glEnable(GL_DEPTH_TEST)
        val vertexShaderId =
            ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId =
            ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        glLinkProgram(programId)
        glUseProgram(programId)

        Matrix.setIdentityM(mCubeModelMatrix, 0)
        Matrix.setIdentityM(mArrowMatrix, 0)

        arrowHalfTurn = ArrowArch(ArrowArch.Amount.HALF_TURN)

        CreateViewMatrix()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        CreateProjectionMatrix(width, height)
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mCubeModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        if(!MainActivity.IsCalibrationMode) {
            if(state.cube.cubies.size != 0) {
                state.cube.resetLayerCubies()

                for (cubie in state.cube.cubies) {
                    cubie.draw(mProjectionMatrix, mViewMatrix, programId)
                }

                if (isArrowDrawing) {
                    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
                    if (!isLayerArrow) {
                        renderCubeEdgeRotationArrow(90f, arrowDirection)
                    } else {
                        if (arrowDirectionValue == ArrowDirection.NEGATIVE) {
                            renderCubeLayerRotationArrow(90f, arrowDirection)
                        } else {
                            renderCubeLayerRotationArrow(-90f, arrowDirection)
                        }
                    }
                } else {
                    rotation = 0f
                    Matrix.setIdentityM(mArrowMatrix, 0)
                }
            }
        }
    }

    fun drawArrow(isDrawing: Boolean, direction: String, isLayer : Boolean) {
        isArrowDrawing = isDrawing
        arrowDirection = direction
        if (arrowDirection.contains("'")) {
            arrowDirectionValue = ArrowDirection.NEGATIVE
        }
        else{
            arrowDirectionValue = ArrowDirection.POSITIVE
        }
        isLayerArrow = isLayer
    }

    private fun renderCubeEdgeRotationArrow(degrees : Float, direction : String) {
        if(abs(rotation) >= 90f){
            rotation = 0f
        }
        var vec = LayerEnum.getVectorByDirection(direction.first())
        if(degrees > 0) {
            Matrix.rotateM(mArrowMatrix, 0, 2f, vec[0], vec[1], vec[2])
            rotation += 2
        }
        else{
            Matrix.rotateM(mArrowMatrix, 0, -2f, vec[0], vec[1], vec[2])
            rotation -= 2
        }
        Matrix.multiplyMM(mMatrixArrow, 0, mViewMatrix, 0, mArrowMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMatrixArrow, 0)

        //to the user (front), left for the back
        if(direction == "R") {
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 1f, 0f)
        }
        //to the left, back to the right
        else if(direction == "F"){
            Matrix.rotateM(mMVPMatrix, 0, +180f, 0.0f, 1.0f, 0.0f)
        }
        //counterclockwise, down for clockwise
        else if(direction == "U"){
            Matrix.rotateM(mMVPMatrix, 0, 90f, 1f, 0f, 0f)
        }

        Matrix.scaleM(mMVPMatrix, 0, 4.5f, 4.5f, 3.5f)

        arrowHalfTurn?.draw(mMVPMatrix, Color.WHITE.cvColor, programId)
    }

    private fun renderCubeLayerRotationArrow(degrees: Float, direction: String) {
        var color = Color.WHITE.cvColor
        if (abs(rotation) >= 90f) {
            rotation = 0f
        }
        var vec = FloatArray(4)
        if(direction.first() == 'M'){
            vec[0] = -1f
            vec[1] = 0f
            vec[2] = 0f
            vec[3] = 0f
        }
        else if(direction.first() == 'E'){
            vec[0] = 0f
            vec[1] = -1f
            vec[2] = 0f
            vec[3] = 0f
        }
        else {
            vec = LayerEnum.getVectorByDirection(direction.first())
        }
        if (degrees > 0) {
            Matrix.rotateM(mArrowMatrix, 0, 2f, vec[0], vec[1], vec[2])
            rotation += 2
        } else {
            Matrix.rotateM(mArrowMatrix, 0, -2f, vec[0], vec[1], vec[2])
            rotation -= 2
        }
        Matrix.multiplyMM(mMatrixArrow, 0, mViewMatrix, 0, mArrowMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMatrixArrow, 0)

        var usualScaling = true

        //to the user (front), left for the back
        if (direction == "R") {
            Matrix.translateM(mMVPMatrix, 0, 4f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 0f, 1f, 0f)
            color = Color.RED.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        } else if (direction == "R'") {
            Matrix.translateM(mMVPMatrix, 0, 4f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 1f, 0f)
            color = Color.RED.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        }
        //to the left, back to the right
        else if (direction == "F") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 4f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0.0f, 1.0f, 0.0f)
            color = Color.GREEN.cvColor
        } else if (direction == "F'") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 4f)
            Matrix.rotateM(mMVPMatrix, 0, +180f, 0.0f, 1.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 0f, 1f)
            color = Color.GREEN.cvColor
        }
        else if (direction == "L") {
            Matrix.translateM(mMVPMatrix, 0, -4f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 1f, 0f)
            color = Color.ORANGE.cvColor
        } else if (direction == "L'") {
            Matrix.translateM(mMVPMatrix, 0, -4f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 0f, 1f, 0f)
            color = Color.ORANGE.cvColor
        }
        else if (direction == "B") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -4f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            color = Color.BLUE.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        } else if (direction == "B'") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -4f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 1f, 0f)
            color = Color.BLUE.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        }
        //counterclockwise, down for clockwise
        else if (direction == "U") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 4f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 1f, 0f, 0f)
            color = Color.WHITE.cvColor
        }else if(direction == "U'"){
            Matrix.translateM(mMVPMatrix, 0, 0f, 4f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 1f, 0f, 0f)
            color = Color.WHITE.cvColor
        }
        else if(direction == "D"){
            Matrix.translateM(mMVPMatrix, 0, 0f, -4f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 1f, 0f, 0f)
            color = Color.YELLOW.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        } else if(direction == "D'"){
            Matrix.translateM(mMVPMatrix, 0, 0f, -4f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 1f, 0f, 0f)
            color = Color.YELLOW.cvColor
            Matrix.scaleM(mMVPMatrix, 0, 4.0f, 4.0f, 1.5f)
            usualScaling = false
        }
        else if (direction == "S") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0.0f, 1.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 0f, 0f, 1f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }else if(direction == "S'"){
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }
        else if (direction == "M") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 1f, 0f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }else if(direction == "M'"){
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 0f, 1f, 0f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }
        else if (direction == "E") {
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, 90f, 1f, 0f, 0f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }else if(direction == "E'"){
            Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 1.0f, 0.0f, 0.0f)
            Matrix.rotateM(mMVPMatrix, 0, 180f, 0f, 0f, 1f)
            Matrix.rotateM(mMVPMatrix, 0, -90f, 1f, 0f, 0f)
            Matrix.scaleM(mMVPMatrix, 0, 4.3f, 4.3f, 1.5f)
            usualScaling = false
        }

        if(usualScaling) {
            Matrix.scaleM(mMVPMatrix, 0, 3f, 3f, 1.5f)
        }
        arrowHalfTurn?.draw(mMVPMatrix, color, programId)
    }

    fun CreateViewMatrix() {
        val eyeX = -6.0f
        val eyeY = 4.5f
        val eyeZ = 5f

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        Matrix.setLookAtM(
            mViewMatrix,
            0,
            eyeX,
            eyeY,
            eyeZ,
            centerX,
            centerY,
            centerZ,
            upX,
            upY,
            upZ
        )
    }

    fun CreateProjectionMatrix(width: Int, height: Int) {
        var ratio : Float
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 0.9f
        val far = 15f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    var mOnGesture: GestureDetector.OnGestureListener =
        object : GestureDetector.SimpleOnGestureListener() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDown(motionEvent: MotionEvent): Boolean {
                if (MainActivity.IsCalibrationMode) run {
                    state.cameraCalibration?.getColorByCoordinates(
                        Point(motionEvent.x.toDouble(), motionEvent.y.toDouble())
                    )
                }
                return false
            }

            override fun onScroll(
                motionEvent: MotionEvent,
                motionEvent1: MotionEvent,
                v: Float,
                v1: Float
            ): Boolean {
                return true
            }

            override fun onLongPress(motionEvent: MotionEvent) {}

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                try {
                    // right to left swipe
                    if (e1.x - e2.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        state.cube.rotateCube(90f, Axis.yAxis)
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        state.cube.rotateCube(-90f, Axis.yAxis)
                    }// left to right swipe
                    //down to up
                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        state.cube.rotateCube(90f, Axis.zAxis)
                    } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        state.cube.rotateCube(-90f, Axis.zAxis)
                    }//up to down
                } catch (e: Exception) {

                }

                return false
            }
        }
}