package com.example.kotlinapp.Rubik

import android.app.Activity
import android.content.Context
import android.opengl.GLES10.glRotatef
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.example.kotlinapp.CurrentState
import com.example.kotlinapp.Enums.Axis
import com.example.kotlinapp.R
import com.example.kotlinapp.Util.ShaderUtils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class Renderer : GLSurfaceView.Renderer {
    val context: Context
    var programId: Int = 0

    val mProjectionMatrix = FloatArray(16)
    val mViewMatrix = FloatArray(16)
    val mMVPMatrix = FloatArray(16)
    val mMatrix = FloatArray(16)
    var mCubeModelMatrix = FloatArray(16)

    var uMatrixLocation: Int = 0

    var view: View
    lateinit var state: CurrentState

    val displayMetrics = DisplayMetrics()

    private val SWIPE_MIN_DISTANCE = 120
    private val SWIPE_MAX_OFF_PATH = 250
    private val SWIPE_THRESHOLD_VELOCITY = 200
    var density: Float = 0.toFloat()

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
        state.cube.resetLayerCubies()

        for (cubie in state.cube.cubies) {
            cubie.draw(mProjectionMatrix, mViewMatrix, programId)
        }
    }

    fun CreateViewMatrix() {
        val eyeX = -6.0f
        val eyeY = 5f
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
            override fun onDown(motionEvent: MotionEvent): Boolean {
                /*if (MainActivity.IsCalibrationMode) {
                    MainActivity.currentState.cameraCalibration!!.getColorByCoordinates(
                        Point(motionEvent.getX().toDouble(), motionEvent.getY().toDouble())
                    )
                } else {
                }*/
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