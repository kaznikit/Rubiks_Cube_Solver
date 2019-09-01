package com.example.kotlinapp

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Solver

class TouchSurface : GLSurfaceView {

    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private val TRACKBALL_SCALE_FACTOR = 36.0f
    //private var mRenderer: com.example.kotlinapp.Rubik.Renderer
    private var mPreviousX: Float = 0.0f
    private var mPreviousY: Float = 0.0f
  //  var cube: Cube

    var k = 0

        //var solver : Solver

    constructor(context: Context) : super(context) {
      //  cube = Cube()
        //mRenderer = com.example.kotlinapp.Rubik.Renderer(context, cube)
       // solver = Solver(cube)
        setEGLContextClientVersion(2)

        //setRenderer(mRenderer)
    }

    override fun onTrackballEvent(e: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                //cube.randScramble()
                //solver.makeCube()

            }
        }
        return true
    }
}