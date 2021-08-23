package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText

interface ViewRefocuser {

   fun createKeyListenerForOTP(
        currentView: EditText,
        previousView: EditText?
    ): View.OnKeyListener {
        return object : View.OnKeyListener {
            override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event!!.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_DEL &&
                    currentView.text.isEmpty()
                ) {
                    //if current is empty then previous OTP digit will also be deleted
                    previousView?.text = null
                    previousView?.requestFocus()
                    return true
                }
                return false
            }
        }
    }

    fun createTextWatcherForOTP(nextView: View?): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.length == 1) //focus on nextView, only if the user has filled up the current OTP digit
                    nextView?.requestFocus()
            }
        }
    }
}