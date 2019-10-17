package com.example.kotlinapp

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.opengl.Visibility
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.kotlinapp.Enums.SolvingPhaseEnum
import com.example.kotlinapp.Recognition.ImageRecognizer
import com.example.kotlinapp.Rubik.Renderer
import com.example.kotlinapp.Util.Constants
import com.example.kotlinapp.Util.InfoDisplayer
import com.example.kotlinapp.Util.SettingsMenu
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.*
import org.opencv.core.*
import java.lang.Exception

class MainActivity : FragmentActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    lateinit var calibrateColorButton: ImageButton
    lateinit var glRenderer: Renderer

    var processedMat: Mat? = null
    var isMatProcessed = false

    var isDispose = false
    var isStarting = true
    var currentImageTask: AsyncTask<Mat, Int, Mat>? = null

    lateinit var clockWiseArrow: Mat
    lateinit var counterclockwiseArrow: Mat

    internal lateinit var menu: SettingsMenu

    private var schemaFragment: SchemaFragment? = null

    //var imageRecognizer : ImageRecognizer? = null
    companion object {
        var IsCalibrationMode = false
    }

    lateinit var currentState: CurrentState

    private lateinit var glSurfaceView: GLSurfaceView

    private lateinit var firstMoveTextView: TextView
    private lateinit var movesTextView: TextView
    private lateinit var movesCountsTextView: TextView
    private lateinit var currentMoveTextView: TextView
    private lateinit var phasesTextView: TextView

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


        setContentView(R.layout.activity_main)
        //glSurfaceView = GLSurfaceView(this)
        glSurfaceView = findViewById(R.id.glSurface)

        currentState = CurrentState(this)

        //if calibration was selected
        if (intent.getBooleanExtra("isCalibration", false)) {
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
        //setContentView(glSurfaceView)


        //mOpenCvCameraView = JavaCameraView(this, 1)
        mOpenCvCameraView = findViewById(R.id.camera_view)
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView.setCameraIndex(1)
        mOpenCvCameraView.setCvCameraViewListener(this)
        //addContentView(mOpenCvCameraView, WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT))

        firstMoveTextView = findViewById(R.id.first_move_textview)
        movesTextView = findViewById(R.id.moves_textview)
        currentMoveTextView = findViewById(R.id.current_move_textview)
        phasesTextView = findViewById(R.id.phases_textview)
        movesCountsTextView = findViewById(R.id.moves_count_textview)
        //отображение счетчика действий
        /*countsTextview = TextView(this)
        var textParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        textParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        countsTextview.textSize = 23f
        countsTextview.setTextColor(Color.WHITE)
        countsTextview.background = null
        *//*textParams.leftMargin = 1550
        textParams.topMargin = 1000
        textParams.bottomMargin = 10*//*
            countsTextview.text = "Moves count: "
        addContentView(countsTextview, textParams)*/

        //отображение текущего действия
        /* currentMoveTextView = TextView(this)
         var currentMoveParams = RelativeLayout.LayoutParams(1000, 500)
         currentMoveTextView.textSize = 23f
         currentMoveTextView.setTextColor(Color.WHITE)
         currentMoveTextView.background = null
         currentMoveParams.leftMargin = 650
         currentMoveParams.topMargin = 150
         addContentView(currentMoveTextView, currentMoveParams)*/


        val b = ImageButton(this)
        val buttonParams = RelativeLayout.LayoutParams(400, 400)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        buttonParams.leftMargin = 1600
        buttonParams.topMargin = -20
        b.scaleType = ImageView.ScaleType.FIT_CENTER
        b.setImageResource(R.drawable.menu_icon)
        b.background = null
        val onClickListener = View.OnClickListener { v -> showPopup(v) }
        b.setOnClickListener(onClickListener)
        addContentView(b, buttonParams)

        val playButton = ImageButton(this)
        val playButtonParams = RelativeLayout.LayoutParams(270, 270)
        playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        playButtonParams.leftMargin = 1660
        playButtonParams.topMargin = 300
        playButton.scaleType = ImageView.ScaleType.FIT_CENTER
        playButton.setImageResource(R.drawable.play_icon)
        playButton.background = null
        val onPlayClickListener = View.OnClickListener { v -> onPlayButton() }
        playButton.setOnClickListener(onPlayClickListener)
        addContentView(playButton, playButtonParams)

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
                0 -> com.example.kotlinapp.Enums.Color.RED.hsvCalibrateValue =
                    GetScalarSharedValue("Red", sharedPref)
                1 -> com.example.kotlinapp.Enums.Color.GREEN.hsvCalibrateValue =
                    GetScalarSharedValue("Green", sharedPref)
                2 -> com.example.kotlinapp.Enums.Color.ORANGE.hsvCalibrateValue =
                    GetScalarSharedValue("Orange", sharedPref)
                3 -> com.example.kotlinapp.Enums.Color.YELLOW.hsvCalibrateValue =
                    GetScalarSharedValue("Yellow", sharedPref)
                4 -> com.example.kotlinapp.Enums.Color.WHITE.hsvCalibrateValue =
                    GetScalarSharedValue("White", sharedPref)
                5 -> com.example.kotlinapp.Enums.Color.BLUE.hsvCalibrateValue =
                    GetScalarSharedValue("Blue", sharedPref)
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
        if (currentImageTask != null) {
            currentImageTask!!.cancel(true)
        }
        mOpenCvCameraView.disableView()
        isDispose = false
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView()
            isMatProcessed = false
            processedMat = null
        }
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "opencv",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("opencv", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }


    /**
     * show info about calibration process
     */
    fun updateCalibrationText(mat: Mat?) {
        runOnUiThread {
            if (mat == null) {
                movesTextView.text = ""
            } else {
                movesTextView.text = currentState.cameraCalibration!!.getColor(mat)
            }
        }
    }

    /**
     * calls from current state to show necessary moves
     */
    fun showCurrentMoves() {
        runOnUiThread {
            if (currentState.CurrentMoves.size == 0 || currentState.CurrentMoves.size == currentState.MoveNumber) {
                movesTextView.text = ""
                firstMoveTextView.text = ""
            } else {
                if (currentState.IsWrongMove) {
                    movesTextView.setTextColor(Color.RED)
                    movesTextView.text = "Wrong move, return back!"
                } else {
                    currentMoveTextView.text = currentState.getCurrentMove()
                    movesCountsTextView.text = "Moves: " + (currentState.CurrentMoves.size - currentState.MoveNumber)
                    firstMoveTextView.text = currentState.CurrentMoves[currentState.MoveNumber]
                    movesTextView.text = currentState.CurrentMoves.drop(currentState.MoveNumber + 1)
                        .joinToString(separator = " ")
                }
            }
        }
    }

    /**
     * calls from current state to show phases
     */
    fun showCurrentPhase() {
        runOnUiThread {
            if (phasesTextView.visibility == GONE) {
                phasesTextView.visibility = VISIBLE
                WhiteLayer_phase.visibility = VISIBLE
                WhiteCross_phase.visibility = VISIBLE
                TwoLayers_phase.visibility = VISIBLE
                YellowCross_phase.visibility = VISIBLE
                YellowEdges_phase.visibility = VISIBLE
                YellowCornersOrient_phase.visibility = VISIBLE
                YellowCorners_phase.visibility = VISIBLE
                Finish_phase.visibility = VISIBLE
            }
            for (solvPhase in SolvingPhaseEnum.values()) {
                var color = Color.WHITE
                if (solvPhase == currentState.solver.currentPhase) {
                    color = Color.parseColor("#ff669900") //Color.RED
                }

                when (solvPhase) {
                    SolvingPhaseEnum.WhiteCross -> {
                        WhiteCross_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.WhiteLayer -> {
                        WhiteLayer_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.TwoLayers -> {
                        TwoLayers_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.YellowCross -> {
                        YellowCross_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.YellowEdges -> {
                        YellowEdges_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.YellowCornersOrient -> {
                        YellowCornersOrient_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.YellowCorners -> {
                        YellowCorners_phase.setTextColor(color)
                    }
                    SolvingPhaseEnum.Finish -> {
                        Finish_phase.setTextColor(color)
                    }
                }
            }
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        try {
            //if the calibration process, it's not necessary to process the frame
            if (IsCalibrationMode) {
                updateCalibrationText(inputFrame.rgba())
                return inputFrame.rgba()
            }

            var mat = inputFrame.rgba()//displayCurrentMoves(inputFrame.rgba())

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
                return mat//displayCurrentMoves(inputFrame.rgba())//InfoDisplayer.writeInfo(inputFrame.rgba(), com.example.kotlinapp.Enums.Color.WHITE.cvColor)//inputFrame.rgba()
            }
        } catch (ex: Exception) {
            return inputFrame.rgba()
        }
        /*if(imageRecognizer == null){
            imageRecognizer = ImageRecognizer(this)
        }
        return imageRecognizer!!.threesholdTestImage(inputFrame.rgba())*/
    }
    
    fun onMatProcessed(mat: Mat?) {
        if (mat != null) {
            processedMat = mat
        }
        isMatProcessed = true
        currentImageTask = null
    }

    /**
     * Show menu
     */
    private fun showPopup(view: View) {
        menu.showPopup(view)
    }

    /**
     * Pressing the play button
     */
    private fun onPlayButton() {
        if (currentState.CurrentMoves.size != 0) {
            currentState.startPlayMode()
        }
    }

    fun showSchema() {
//        schemaFragment.show(supportFragmentManager, "dialog")
        if (schemaFragment == null) {
            schemaFragment =
                SchemaFragment.newInstance()
        }
        if (schemaFragment!!.isAdded) {
            supportFragmentManager.fragments.clear()
        }
        schemaFragment?.show(supportFragmentManager, "schema")
    }
}
