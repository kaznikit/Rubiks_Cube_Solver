package com.example.kotlinapp

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.kotlinapp.Rubik.Cube

class TouchSurface : GLSurfaceView {

    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private val TRACKBALL_SCALE_FACTOR = 36.0f
    private var mRenderer : com.example.kotlinapp.Rubik.Renderer
    private var mPreviousX: Float = 0.0f
    private var mPreviousY: Float = 0.0f

    constructor(context: Context) : super(context) {
        var cube : Cube = Cube()
        mRenderer = com.example.kotlinapp.Rubik.Renderer(context, cube)
        setEGLContextClientVersion(2)
        setRenderer(mRenderer)
    }

    override fun onTrackballEvent(e: MotionEvent): Boolean {
        mRenderer.mAngleX += e.x * TRACKBALL_SCALE_FACTOR
        mRenderer.mAngleY += e.y * TRACKBALL_SCALE_FACTOR
        requestRender()
        return true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x
        val y = e.y
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - mPreviousX
                val dy = y - mPreviousY
                mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR
                mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y
        return true
    }
}