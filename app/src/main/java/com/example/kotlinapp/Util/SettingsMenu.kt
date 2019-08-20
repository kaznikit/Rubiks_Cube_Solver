package com.example.kotlinapp.Util

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.example.kotlinapp.MainActivity
import com.example.kotlinapp.R

class SettingsMenu(context: Context?, anchor: View?) : PopupMenu(context, anchor) {
    internal lateinit var mainContext: Context
    internal lateinit var mainView: View
    internal lateinit var mainActivity : MainActivity

    constructor(context: Context, anchor: View, mainActivity: MainActivity) : this(context, anchor) {
        this.mainContext = context
        this.mainView = anchor
        this.mainActivity = mainActivity
    }

    fun showPopup(v: View) {
        val popup = PopupMenu(mainContext, v)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.settings_menu, popup.menu)
        val onMenuItemClickListener = OnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.calibration -> {
                    mainActivity.TurnOnCalibration()
                    true
                }
                else -> false
            }
        }
        popup.setOnMenuItemClickListener(onMenuItemClickListener)
        popup.show()
    }
}