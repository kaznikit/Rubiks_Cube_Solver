package com.example.kotlinapp

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.kotlinapp.Rubik.Enums.Axis
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Solver

class TouchSurface : GLSurfaceView {

    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private val TRACKBALL_SCALE_FACTOR = 36.0f
    private var mRenderer: com.example.kotlinapp.Rubik.Renderer
    private var mPreviousX: Float = 0.0f
    private var mPreviousY: Float = 0.0f
    var cube: Cube

    var k = 0

    var solver : Solver

    constructor(context: Context) : super(context) {
        cube = Cube()
        mRenderer = com.example.kotlinapp.Rubik.Renderer(context, cube)
        solver = Solver(cube)
        setEGLContextClientVersion(2)
        setRenderer(mRenderer)
    }

    override fun onTrackballEvent(e: MotionEvent): Boolean {
        /*mRenderer.mAngleX += e.x * TRACKBALL_SCALE_FACTOR
        mRenderer.mAngleY += e.y * TRACKBALL_SCALE_FACTOR
        requestRender()*/
        return true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        /*val x = e.x
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
        mPreviousY = y*/

        /*for(layer in cube.layers) {
            layer.rotate(90.0f, 1.0f, 0.0f, 0.0f)
        }*/
        //cube.layers.get(0).rotate(90.0f, 1.0f, 0f, 0.0f)
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {

                //cube.randScramble()
                solver.makeCube()

                /*if (k == 0) {
                    cube.layers.get(0).rotate(90.0f)
                    k++
                } else if (k == 1) {
                    cube.layers.get(3).rotate(90.0f)
                    k++
                } else if (k == 2) {
                    cube.layers.get(6).rotate(90.0f)
                    k++
                } else if (k == 3) {
                    cube.layers.get(8).rotate(90.0f)
                    k++
                } else if (k == 4) {
                    cube.layers.get(8).rotate(-90.0f)
                    k++
                } else if (k == 5) {
                    cube.layers.get(5).rotate(-90.0f)
                    k++
                } else if (k == 6) {
                    cube.layers.get(2).rotate(-90.0f)
                    k++
                } else if (k == 7) {
                    cube.layers.get(1).rotate(-90.0f)
                    k++
                }*/
            }
        }
        return true
    }
}