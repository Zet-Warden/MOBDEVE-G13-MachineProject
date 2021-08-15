package com.mobdeve.s11.sy.jacob.machineproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var hideUITimer: CountDownTimer
    private lateinit var container: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemUI()

        container = cl_main

        //hides status and nav bar after a certain time
        hideUITimer = object : CountDownTimer(1500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                hideSystemUI()
            }
        }

        //start timer
        container.setOnClickListener {
            hideUITimer.cancel()
            hideUITimer.start()
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
