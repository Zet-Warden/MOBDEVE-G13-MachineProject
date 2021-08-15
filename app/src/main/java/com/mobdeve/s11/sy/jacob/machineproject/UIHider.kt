package com.mobdeve.s11.sy.jacob.machineproject

import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*

class UIHider(
    private val app: AppCompatActivity,
    private val clMainContainer: ConstraintLayout
) : CountDownTimer(1500, 1000) {

    init {
        hideSystemUI()
        clMainContainer.setOnClickListener {
            this.cancel()
            this.start()
        }
    }

    override fun onTick(millisUntilFinished: Long) {}

    override fun onFinish() {
        hideSystemUI()
    }

    private fun hideSystemUI() {
        app.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}