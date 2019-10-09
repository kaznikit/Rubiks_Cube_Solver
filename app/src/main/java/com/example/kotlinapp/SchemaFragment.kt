package com.example.kotlinapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class SchemaFragment : DialogFragment() {

    companion object {
        var rootView: View? = null
        private var instance: SchemaFragment? = null

        fun newInstance() : SchemaFragment {
            if (instance == null) {
                instance = SchemaFragment()
            }
            return instance!!
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (rootView == null) {
            rootView = LayoutInflater.from(activity)
                .inflate(R.layout.schema_fragment, null, false)
        }

        return AlertDialog.Builder(activity)
            .setView(rootView)
            .setCancelable(true)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
/*
        var v = inflater.inflate(R.layout.schema_fragment, container, false)
        return v*/
        return rootView
    }

    private fun initViews() {
        if (rootView != null) {
            //var view = rootView?.findViewById<ImageView>(R.id.schema_view)
            var b = rootView?.findViewById<ImageView>(R.id.close_icon_view)
            b?.setOnClickListener { close() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        rootView = null
    }

    fun close(){
        dismiss()
    }
}