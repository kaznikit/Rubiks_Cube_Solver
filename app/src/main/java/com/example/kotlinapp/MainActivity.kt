package com.example.kotlinapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.icu.text.IDNA
import android.opengl.GLSurfaceView
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.kotlinapp.Enums.SolvingPhaseEnum
import com.example.kotlinapp.Recognition.ImageRecognizer
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Renderer
import com.example.kotlinapp.Rubik.Solver
import com.example.kotlinapp.Util.Constants
import com.example.kotlinapp.Util.InfoDisplayer
import com.example.kotlinapp.Util.SettingsMenu
import org.opencv.android.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File.separator
import java.lang.Exception

class MainActivity : FragmentActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    lateinit var calibrateColorButton: ImageButton
    lateinit var glRenderer: Renderer

    var processedMat : Mat? = null
    var isMatProcessed = false

    var isDispose = false
    var isStarting = true
    var currentImageTask : AsyncTask<Mat, Int, Mat>? = null

    lateinit var clockWiseArrow : Mat
    lateinit var counterclockwiseArrow : Mat

    internal lateinit var menu: SettingsMenu

    private var schemaFragment : SchemaFragment? = null

    //var imageRecognizer : ImageRecognizer? = null
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

                    /*clockWiseArrow = Utils.loadResource(mAppContext, R.drawable.clockwise_png, CvType.CV_8UC4)
                    counterclockwiseArrow = Utils.loadResource(mAppContext, R.drawable.counterclockwise_png, CvType.CV_8UC4)*/
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


        glSurfaceView = GLSurfaceView(this)

        currentState = CurrentState(this)

        if(intent.getBooleanExtra("isCalibration", false)){
            //IsCalibrationMode = true
            TurnOnCalibration()
        }

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
        isDispose = true
        if(currentImageTask != null){
            currentImageTask!!.cancel(true)
        }
        mOpenCvCameraView.disableView()
        isDispose = false
    }

    override fun onPause() {
        super.onPause()
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView()
            isMatProcessed = false
            processedMat = null
        }
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
        try {
            var mat = displayCurrentMoves(inputFrame.rgba())

            //make processing 1 time per second
            if (System.currentTimeMillis() - start >= 50 && !isDispose && currentImageTask == null) {
                currentImageTask = ImageRecognizer(this).execute(mat)
                start = System.currentTimeMillis()
            }

            /*Imgproc.resize(clockWiseArrow, clockWiseArrow, Size (200.0, 100.0))
            //var submat = inputFrame.rgba().submat(0, 100, 0, 200)
            Imgproc.cvtColor(clockWiseArrow, clockWiseArrow, Imgproc.COLOR_RGB2BGR)
            clockWiseArrow.copyTo(inputFrame.rgba())*/

            if (isMatProcessed) {
                isMatProcessed = false
                return processedMat!!
            } else {
                return displayCurrentMoves(inputFrame.rgba())//InfoDisplayer.writeInfo(inputFrame.rgba(), com.example.kotlinapp.Enums.Color.WHITE.cvColor)//inputFrame.rgba()
            }
        }
        catch(ex : Exception){
            return inputFrame.rgba()
        }
        /*if(imageRecognizer == null){
            imageRecognizer = ImageRecognizer(this)
        }
        return imageRecognizer!!.threesholdTestImage(inputFrame.rgba())*/
    }

    fun displayCurrentMoves(mat1 : Mat) : Mat {
        if (currentState.IsCubeSolving) {
            var mat: Mat
            //write active solving phase
            mat = InfoDisplayer.writeInfoFromPlace(mat1, com.example.kotlinapp.Enums.Color.RED.cvColor,
                "Solving phases:", Constants.StartingPhasesTextPoint)

            var yMargin = 100.0
            for(solvPhase in SolvingPhaseEnum.values()){
                var color = com.example.kotlinapp.Enums.Color.WHITE
                if(solvPhase == currentState.solver.currentPhase){
                    color = com.example.kotlinapp.Enums.Color.RED
                }

                if(solvPhase.phaseName.contains("N")){
                    mat = InfoDisplayer.writeInfoFromPlace(mat, color.cvColor,
                        solvPhase.phaseName.substring(0, solvPhase.phaseName.indexOf("N")),
                        Point(Constants.StartingPhasesTextPoint.x, Constants.StartingPhasesTextPoint.y + yMargin))
                    yMargin += 80.0

                    mat = InfoDisplayer.writeInfoFromPlace(mat, color.cvColor,
                        solvPhase.phaseName.substring(solvPhase.phaseName.indexOf("N") + 2, solvPhase.phaseName.length),
                        Point(Constants.StartingPhasesTextPoint.x, Constants.StartingPhasesTextPoint.y + yMargin))
                }
                else {
                    mat = InfoDisplayer.writeInfoFromPlace(
                        mat, color.cvColor, solvPhase.phaseName,
                        Point(Constants.StartingPhasesTextPoint.x,
                            Constants.StartingPhasesTextPoint.y + yMargin))
                }
                yMargin += 80.0
            }

            if (currentState.CurrentMoves.size != 0) {
                //if wrong move
                if(currentState.IsWrongMove){
                    mat = InfoDisplayer.writeInfoFromPlace(
                        mat, com.example.kotlinapp.Enums.Color.RED.cvColor,
                        "Wrong move, return back!",
                        Point(Constants.StartingTextPoint.x, Constants.StartingTextPoint.y))
                }
                else if (currentState.MoveNumber != 0) {
                    InfoDisplayer.text = currentState.CurrentMoves[currentState.MoveNumber - 1]
                    mat = InfoDisplayer.writeInfo(
                        mat, com.example.kotlinapp.Enums.Color.WHITE.cvColor)

                    //if the active element not first or last
                    if (currentState.MoveNumber != currentState.CurrentMoves.size) {
                        mat = InfoDisplayer.writeInfoFromPlace(
                            mat, com.example.kotlinapp.Enums.Color.RED.cvColor,
                            currentState.CurrentMoves[currentState.MoveNumber],
                            Point(Constants.StartingTextPoint.x + 80.0, Constants.StartingTextPoint.y))

                        mat = InfoDisplayer.writeInfoFromPlace(
                            mat, com.example.kotlinapp.Enums.Color.WHITE.cvColor,
                            currentState.CurrentMoves.drop(currentState.MoveNumber + 1).joinToString(
                                separator = " "),
                            Point(Constants.StartingTextPoint.x + 140.0, Constants.StartingTextPoint.y))
                    }
                    return mat
                } else {
                    mat = InfoDisplayer.writeInfoFromPlace(
                        mat, com.example.kotlinapp.Enums.Color.RED.cvColor,
                        currentState.CurrentMoves[currentState.MoveNumber],
                        Point(Constants.StartingTextPoint.x, Constants.StartingTextPoint.y)
                    )

                    mat = InfoDisplayer.writeInfoFromPlace(
                        mat, com.example.kotlinapp.Enums.Color.WHITE.cvColor,
                        currentState.CurrentMoves.drop(1).joinToString(
                            separator = " "
                        ),
                        Point(Constants.StartingTextPoint.x + 90.0, Constants.StartingTextPoint.y)
                    )
                }
                return mat
            }
        }
        else if(currentState.IsCubeSolved){
            return InfoDisplayer.writeInfoFromPlace(
                mat1, com.example.kotlinapp.Enums.Color.RED.cvColor,
                "Congratulations, you are awesome!",
                Point(Constants.StartingTextPoint.x, Constants.StartingTextPoint.y))
        }
        return InfoDisplayer.writeInfo(mat1, com.example.kotlinapp.Enums.Color.WHITE.cvColor)
    }

    fun onMatProcessed(mat : Mat?){
        if(mat != null){
            processedMat = mat
        }
        isMatProcessed = true
        currentImageTask = null
    }

    fun showPopup(view: View) {
        menu.showPopup(view)
    }

    fun showSchema(){
//        schemaFragment.show(supportFragmentManager, "dialog")
        if(schemaFragment == null) {
            schemaFragment =
                SchemaFragment.newInstance()
        }
        if(schemaFragment!!.isAdded){
            supportFragmentManager.fragments.clear()
        }
        schemaFragment?.show(supportFragmentManager, "schema")
    }
}
