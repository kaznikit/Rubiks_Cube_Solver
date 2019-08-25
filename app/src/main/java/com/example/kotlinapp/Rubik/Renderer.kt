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
    private var mArrowMatrix = FloatArray(16)
    private val mMatrixArrow = FloatArray(16)
private var rotation = 0f
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
            state.cube.resetLayerCubies()

            for (cubie in state.cube.cubies) {
                cubie.draw(mProjectionMatrix, mViewMatrix, programId)
            }

            if (isArrowDrawing) {
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
                renderCubeEdgeRotationArrow(mMVPMatrix, getRotationInDegrees())
            }
            else{
                rotation = 0f
                Matrix.setIdentityM(mArrowMatrix, 0)
            }
        }
    }

    fun drawArrow(isDrawing: Boolean) {
        isArrowDrawing = isDrawing
    }

    private fun getRotationInDegrees(): Int {
        val time = System.currentTimeMillis()

        val rate = 10
        return ((time - timeReference) / rate % 180).toInt()
    }

    private fun renderCubeEdgeRotationArrow(mvpMatrix: FloatArray, arrowRotationInDegrees: Int) {
        var direction: ArrowDirection? = null

        if(abs(rotation) >= 90f){
            rotation = 0f
        }
        // Rotate and Translate Arrow as required by Rubik Logic Solution algorithm.
        when (state.activeRubikFace.layerName) {
            LayerEnum.DOWN -> {
                Matrix.rotateM(mArrowMatrix, 0, -2f, 0.0f, 0.0f, -1.0f)
                rotation += 2f
                direction = ArrowDirection.NEGATIVE
            }
            LayerEnum.LEFT -> {
                Matrix.rotateM(mArrowMatrix, 0, -2f, 1.0f, 0.0f, 0.0f)
                direction = ArrowDirection.NEGATIVE
            }
            LayerEnum.FRONT -> {
                Matrix.rotateM(mArrowMatrix, 0, -2f, 0.0f, 0.0f, -1.0f)
                direction = ArrowDirection.NEGATIVE
            }
            LayerEnum.UP -> {
                Matrix.rotateM(mArrowMatrix, 0, -2f, 1.0f, 0.0f, 0.0f)
                direction = ArrowDirection.NEGATIVE
            }
            LayerEnum.RIGHT -> {
                Matrix.rotateM(mArrowMatrix, 0, -2f, 0f, 0.0f, -1f)
                direction = ArrowDirection.NEGATIVE
            }
            LayerEnum.BACK -> {
                direction = ArrowDirection.POSITIVE
                Matrix.rotateM(mArrowMatrix, 0, -2f, 1f, 0.0f, 0f)
            }
        }
        /*if (direction == ArrowDirection.NEGATIVE) {
            //Matrix.rotateM(mvpMatrix, 0, arrowRotationInDegrees.toFloat(), 0.0f, 0.0f, 1.0f)
            Matrix.rotateM(mArrowMatrix, 0, -90f, 0.0f, 0.0f, 1.0f)
            Matrix.rotateM(mArrowMatrix, 0, +180f, 0.0f, 1.0f, 0.0f)
        }*/ //else
          //  Matrix.rotateM(mArrowMatrix, 0, (-1 * arrowRotationInDegrees).toFloat(), 0.0f, 0.0f, 1.0f)

       // Matrix.transposeM(mArrowMatrix, 0, mArrowMatrix, 0)

        Matrix.multiplyMM(mMatrixArrow, 0, mViewMatrix, 0, mArrowMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mMatrixArrow, 0)

        if (direction == ArrowDirection.NEGATIVE) {
            Matrix.rotateM(mvpMatrix, 0, -2f, 0.0f, 0.0f, 1.0f)
            Matrix.rotateM(mvpMatrix, 0, -90f, 0.0f, 0.0f, 1.0f)
            Matrix.rotateM(mvpMatrix, 0, +180f, 0.0f, 1.0f, 0.0f)
        }
        else{
            Matrix.rotateM(mvpMatrix, 0, -2f, 0.0f, 0.0f, 1.0f)
        }

        Matrix.scaleM(mvpMatrix, 0, 4.5f, 4.5f, 3.5f)

        arrowHalfTurn?.draw(mvpMatrix, Color.WHITE.cvColor, programId)
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
        var ratio = 1f
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