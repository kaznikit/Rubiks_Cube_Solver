package com.example.kotlinapp

import android.app.Activity
import android.content.pm.ActivityInfo
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Renderer

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glSurfaceView = TouchSurface(this)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(glSurfaceView)
    }
}
