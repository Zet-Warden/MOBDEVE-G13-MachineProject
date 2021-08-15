@file:Suppress("DEPRECATION")

package com.mobdeve.s11.sy.jacob.machineproject

import android.app.Activity
import android.os.CountDownTimer
import android.view.View

/**
 * Creates an object that hides System status and navigation bar
 * If the user opts to make the status and navigation bar appear again, it waits for a certain time
 * then hides the System status and navigation bar again
 *
 * Parameters:
 * app : AppCompatActivity - the activity that will hide the System UI
 * clMainContainer : View - the main container of the activity
 */
class UIHider(
    private val app: Activity,
    clMainContainer: View
) : CountDownTimer(1500, 1000) { //set wait time to 1.5s when user opts to make the status and navigation bar appear again

    init {
        hideSystemUI()

        //listen to the main container of the activity to check whether the status and navigation bar shows up
        clMainContainer.setOnClickListener {
            this.cancel()
            this.start()
        }
    }

    override fun onTick(millisUntilFinished: Long) {}

    /**
     * Hides the System UI when timer is finish
     */
    override fun onFinish() {
        hideSystemUI()
    }

    /**
     * Hides the System UI
     */
    private fun hideSystemUI() {
        app.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}