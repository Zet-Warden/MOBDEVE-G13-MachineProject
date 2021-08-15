@file:Suppress("DEPRECATION")

package com.mobdeve.s11.sy.jacob.machineproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var hideUITimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UIHider(this, clMainContainer)



        btnSendOTP.setOnClickListener {
            startActivity(Intent(this, OTPVerification::class.java))
        }
    }


}
