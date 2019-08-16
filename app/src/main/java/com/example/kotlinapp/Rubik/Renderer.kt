package com.example.kotlinapp.Rubik

import android.content.Context
import android.opengl.GLES10.glRotatef
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
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
    val mRotationMatrix = FloatArray(16)

    lateinit var mWorld: World
    private var mAngle: Float = 0.0f
    private lateinit var mCube: Cube
    var mAngleX: Float = 0.0f
    var mAngleY: Float = 0.0f

    var uMatrixLocation: Int = 0

    companion object {
        var intValue = 0;
    }

    constructor(context : Context, cube : Cube){
        this.context = context
        mCube = cube
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        glEnable(GLES20.GL_CULL_FACE)
        glClearColor(0f, 0f, 0f, 0f)
        glEnable(GL_DEPTH_TEST)
        val vertexShaderId = ShaderUtils.createShader(context, GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShaderId = ShaderUtils.createShader(context, GL_FRAGMENT_SHADER, R.raw.fragment_shader)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        glLinkProgram(programId)
        glUseProgram(programId)

        Matrix.setIdentityM(mCubeModelMatrix, 0)
        Matrix.setIdentityM(mRotationMatrix, 0)

        CreateViewMatrix()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        CreateProjectionMatrix(width, height)
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mCubeModelMatrix, 0)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        /*for (worldCubie in mCube.cubies) {
            glClearColor(0.5f, 0.5f, 0.5f, 1f)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            var layer = mCube.layers.getOrNull(intValue)
            if (layer != null) {
                for (cubie in layer.cubies) {
                    if (cubie.centerPoint.IsEqual(cubie.centerPoint, worldCubie.centerPoint)) {
                        //mCubeModelMatrix = worldCubie.animateTransform()
                        worldCubie.animateTransform(x, y, z)
                    }
                }
            }
            Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, worldCubie.mAnimationMatrix, 0)//mCubeModelMatrix, 0)

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
            glUniformMatrix4fv(uMatrixLocation, 1, false, mMVPMatrix, 0)
            worldCubie.draw(mMVPMatrix, programId)
        }*/

        mCube.resetLayerCubies()

        for(cubie in mCube.cubies){
            cubie.draw(mProjectionMatrix, mViewMatrix, programId)
        }
    }

    /*fun rotateCube() {
        mCubeModelMatrix = mCube.layers.get(0).rotate()
       // Matrix.rotateM(mRotationMatrix, 0, mAngleX, 90.0f, 0.0f, 0.0f)
       // Matrix.transposeM(mCubeModelMatrix, 0, mRotationMatrix, 0)
        //mAngleX += 2.0f
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mCubeModelMatrix, 0)
    }*/

    fun CreateViewMatrix(){
        val eyeX = -6.0f
        val eyeY = 4f
        val eyeZ = 7f

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ)
    }

    fun CreateProjectionMatrix(width : Int, height : Int){
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
}