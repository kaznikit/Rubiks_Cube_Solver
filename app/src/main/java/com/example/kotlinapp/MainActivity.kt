package com.example.kotlinapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.kotlinapp.Recognition.ColorDetector
import com.example.kotlinapp.Recognition.ImageRecognizer
import com.example.kotlinapp.Rubik.Cube
import com.example.kotlinapp.Rubik.Renderer
import com.example.kotlinapp.Util.SettingsMenu
import org.opencv.android.*
import org.opencv.core.Mat
import org.opencv.core.Scalar
import java.util.*

class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private val REQUEST_CODE = 1
    var calibrateColorButton: ImageButton
    var currentState: CurrentState
    var imageRecognizer: ImageRecognizer
    var glRenderer: Renderer
    var mCube: Cube

    internal var menu: SettingsMenu
    var IsCalibrationMode = false
    internal var glSurfaceView: GLSurfaceView
    //Get user movements
    private var mGesture: GestureDetector? = null
    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("camera", "OpenCV loaded successfully")
                    mOpenCvCameraView.enableView()
                    imageRecognizer = ImageRecognizer(currentState)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glSurfaceView = TouchSurface(this)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(glSurfaceView)
    }

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

        currentState = CurrentState()

        //mOpenCvCameraView.CAMERA_ID_FRONT
        // currentState.cameraCalibration = new Calibration(mOpenCvCameraView);
        currentState.ColorDetector = ColorDetector()
        currentState.cameraCalibration = Calibration(this)
        glSurfaceView = GLSurfaceView(this)

        glRenderer = OpenGLRenderer(glSurfaceView, this, currentState)
        mCube = Cube(currentState)

        addNewFace(Constants.FaceNameEnum.DOWN, Constants.FaceNameEnum.FRONT)

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

        addContentView(
            mOpenCvCameraView,
            WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        )

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

        val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val camList = manager.cameraIdList
            for (cameraID in camList) {

                val characteristics = manager.getCameraCharacteristics(cameraID)
                //currentState.cameraCalibration = new Calibration(characteristics);
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    fun addNewFace(activeFaceName: Constants.FaceNameEnum, frontFace: Constants.FaceNameEnum) {
        //create first face in front of user
        val activeFace = RubikFace()
        activeFace.faceNameEnum = activeFaceName
        //active face is down face
        // activeFace.faceNameEnum.axis = Constants.RotationAxis.yMinusAxis;
        activeFace.rotationAxis = Constants.RotationAxis.yMinusAxis
        currentState.activeRubikFace = activeFace

        //face shown to user
        if (!Arrays.asList(mCube.rubikFaces).contains(frontFace)) {
            val activeFrontFace = RubikFace()
            activeFrontFace.faceNameEnum = frontFace
            activeFrontFace.createObservedTilesArray()
            //activeFrontFace.faceNameEnum.axis = Constants.RotationAxis.zAxis;
            activeFrontFace.rotationAxis = Constants.RotationAxis.zAxis
            currentState.frontFace = activeFrontFace
        } else {
            val index = Arrays.asList(mCube.rubikFaces).lastIndexOf(frontFace)
            currentState.frontFace = mCube.rubikFaces[index]
        }

        activeFace.createObservedTilesArray()
        mCube.rubikFaces[currentState.adoptFaceCount] = activeFace
        glRenderer.CalculateRotationAxis()
    }

    fun ResetFaces() {
        currentState.adoptFaceCount = 0
        if (mCube.rubikFaces.length !== 0) {
            for (i in 0 until mCube.rubikFaces.length) {
                mCube.rubikFaces[i] = null
            }
        }
        addNewFace(Constants.FaceNameEnum.DOWN, Constants.FaceNameEnum.FRONT)
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
                0 -> Constants.ColorTileEnum.RED.hsvCalibrateValue = GetScalarSharedValue("Red", sharedPref)
                1 -> Constants.ColorTileEnum.GREEN.hsvCalibrateValue = GetScalarSharedValue("Green", sharedPref)
                2 -> Constants.ColorTileEnum.ORANGE.hsvCalibrateValue = GetScalarSharedValue("Orange", sharedPref)
                3 -> Constants.ColorTileEnum.YELLOW.hsvCalibrateValue = GetScalarSharedValue("Yellow", sharedPref)
                4 -> Constants.ColorTileEnum.WHITE.hsvCalibrateValue = GetScalarSharedValue("White", sharedPref)
                5 -> Constants.ColorTileEnum.BLUE.hsvCalibrateValue = GetScalarSharedValue("Blue", sharedPref)
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
        return mGesture.onTouchEvent(ev)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    public override fun onResume() {
        super.onResume()
        /*if(gLSurfaceView != null)
            gLSurfaceView.onResume();*/
        if (!OpenCVLoader.initDebug()) {
            Log.d("opencv", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d("opencv", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        // return imageRecognizer.testColorRecognition(inputFrame.rgba());
        return imageRecognizer.threesholdTestImage(inputFrame.rgba())//imageRecognizer.testProcess(inputFrame.rgba());//imageRecognizer.processFrame(inputFrame.rgba());//inputFrame.rgba();
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
