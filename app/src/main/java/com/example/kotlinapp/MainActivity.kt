package com.example.kotlinapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.kotlinapp.Recognition.ImageRecognizer
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Renderer
import com.example.kotlinapp.Rubik.Solver
import com.example.kotlinapp.Util.InfoDisplayer
import com.example.kotlinapp.Util.SettingsMenu
import org.opencv.android.*
import org.opencv.core.Mat
import org.opencv.core.Scalar

class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private val REQUEST_CODE = 1
    lateinit var calibrateColorButton: ImageButton
    lateinit var glRenderer: Renderer

    var processedMat : Mat? = null
    var isMatProcessed = false

    internal lateinit var menu: SettingsMenu

    companion object {
        var IsCalibrationMode = false
    }

    lateinit var currentState: CurrentState

    internal lateinit var glSurfaceView: GLSurfaceView

    //Get user movements
    private var mGesture: GestureDetector? = null
    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("camera", "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                    //imageRecognizer = ImageRecognizer(currentState)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    var start = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        //ask permissions for camera
        verifyPermissions()

        glSurfaceView = GLSurfaceView(this)

        currentState = CurrentState(this)

        glRenderer = Renderer(glSurfaceView, this, currentState)

        mGesture = GestureDetector(this, glRenderer.mOnGesture)

        glSurfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setZOrderOnTop(true)
        glSurfaceView.setRenderer(glRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        setContentView(glSurfaceView)

        mOpenCvCameraView = JavaCameraView(this, 1)
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView.setCameraIndex(1)
        mOpenCvCameraView.setCvCameraViewListener(this)

        addContentView(mOpenCvCameraView, WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT))

        val b = ImageButton(this)
        val buttonParams = RelativeLayout.LayoutParams(400, 400)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        buttonParams.leftMargin = 1600
        buttonParams.topMargin = 0
        b.scaleType = ImageView.ScaleType.FIT_CENTER
        b.setImageResource(R.drawable.menu_icon)
        b.background = null
        val onClickListener = View.OnClickListener { v -> showPopup(v) }
        b.setOnClickListener(onClickListener)
        addContentView(b, buttonParams)

        menu = SettingsMenu(this, glSurfaceView, this)

        ReadSharedPreferences()
        start = System.currentTimeMillis()

    }

    fun TurnOnCalibration() {
        IsCalibrationMode = true
        AddTextInfo()
    }

    //add plain to write info text about calibration
    fun AddTextInfo() {
        val textView = TextView(this)
        val textParams = RelativeLayout.LayoutParams(400, 400)
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        textParams.leftMargin = 0
        textParams.topMargin = 0
        textView.setBackgroundColor(50)
        // textView.setText("test");

        calibrateColorButton = ImageButton(this)
        val buttonParams = RelativeLayout.LayoutParams(150, 150)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        buttonParams.leftMargin = 100
        buttonParams.topMargin = 200
        calibrateColorButton.scaleType = ImageView.ScaleType.FIT_CENTER
        calibrateColorButton.setBackgroundColor(Color.RED)
        val onClickListener = View.OnClickListener { v -> showPopup(v) }
        calibrateColorButton.setOnClickListener(onClickListener)
        //addContentView(calibrateColorButton, buttonParams);
    }

    fun SaveSharedPreferences(colorName: String, values: Scalar) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        for (i in 0..3) {
            editor.remove(colorName + "_" + i)
            editor.putString(colorName + "_" + i, java.lang.Double.toString(values.`val`[i]))
        }
        editor.commit()
    }

    private fun ReadSharedPreferences() {
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        for (j in 0..5) {
            when (j) {
                0 -> com.example.kotlinapp.Enums.Color.RED.hsvCalibrateValue = GetScalarSharedValue("Red", sharedPref)
                1 -> com.example.kotlinapp.Enums.Color.GREEN.hsvCalibrateValue = GetScalarSharedValue("Green", sharedPref)
                2 -> com.example.kotlinapp.Enums.Color.ORANGE.hsvCalibrateValue = GetScalarSharedValue("Orange", sharedPref)
                3 -> com.example.kotlinapp.Enums.Color.YELLOW.hsvCalibrateValue = GetScalarSharedValue("Yellow", sharedPref)
                4 -> com.example.kotlinapp.Enums.Color.WHITE.hsvCalibrateValue = GetScalarSharedValue("White", sharedPref)
                5 -> com.example.kotlinapp.Enums.Color.BLUE.hsvCalibrateValue = GetScalarSharedValue("Blue", sharedPref)
            }
        }
    }

    private fun GetScalarSharedValue(colorName: String, sharedPref: SharedPreferences): Scalar {
        val value = Scalar(0.0, 0.0, 0.0, 0.0)
        for (i in 0..3) {
            val `val` = sharedPref.getString(colorName + "_" + i, null)
            if (`val` != null) {
                value.`val`[i] = java.lang.Double.parseDouble(`val`)
            }
        }
        return value
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        super.dispatchTouchEvent(ev)
        return mGesture!!.onTouchEvent(ev)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("opencv", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        //make processing 1 time per second
        if (System.currentTimeMillis() - start >= 50) {
            ImageRecognizer(this).execute(inputFrame.rgba())
            start = System.currentTimeMillis()
        }
        if (isMatProcessed) {
            isMatProcessed = false
            return processedMat!!
        } else {
            return InfoDisplayer.writeInfo(inputFrame.rgba(), com.example.kotlinapp.Enums.Color.WHITE.cvColor)//inputFrame.rgba()
        }
    }

    fun onMatProcessed(mat : Mat?){
        processedMat = mat
        isMatProcessed = true
    }

    //ask permission for camera using
    private fun verifyPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                permissions[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        verifyPermissions()
    }

    fun showPopup(view: View) {
        menu.showPopup(view)
    }
}
