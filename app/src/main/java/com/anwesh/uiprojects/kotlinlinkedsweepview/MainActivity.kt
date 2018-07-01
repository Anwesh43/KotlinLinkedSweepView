package com.anwesh.uiprojects.kotlinlinkedsweepview

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.anwesh.uiprojects.linkedsweepview.LinkedSweepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinkedSweepView.create(this)
        fullScreen()
    }
}

fun MainActivity.fullScreen() {
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    supportActionBar?.hide()
    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
}