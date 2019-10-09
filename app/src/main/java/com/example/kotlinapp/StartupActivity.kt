package com.example.kotlinapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class StartupActivity : FragmentActivity() {
    private val REQUEST_CODE = 1
    private var schemaFragment : SchemaFragment? = null

    override fun onStart() {
        super.onStart()

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        //ask permissions for camera
        verifyPermissions()

        setContentView(R.layout.activity_startup)

        schemaFragment = SchemaFragment()

        findViewById<Button>(R.id.start_button).setOnClickListener { startMainActivity() }

        findViewById<Button>(R.id.calibration_button).setOnClickListener { startCalibrationProcess() }

        findViewById<Button>(R.id.schema_button).setOnClickListener { showSchema() }
    }

    fun startMainActivity(){
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun startCalibrationProcess(){
        var intent = Intent(this, MainActivity::class.java)
        intent.putExtra("isCalibration", true)
        startActivity(intent)
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

    //ask permission for camera using
    private fun verifyPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                permissions[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //ActivityCompat.requestPermissions(this@MainActivity, permissions, REQUEST_CODE)
            ActivityCompat.requestPermissions(this@StartupActivity, permissions, REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        verifyPermissions()
    }
}