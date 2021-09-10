@file:Suppress("DEPRECATION")

package com.mobdeve.s11s13.group13.mp.vaccineph

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.KeyEnum
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.ToastPool
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper.PhoneNumberFormatter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val requestPermissionLauncher =
            this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    init()
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    finish()
                }
            }
        //requestPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
        requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)


    }

    private fun init() {
        UIHider(this, clMainContainer)
        initSendOTPButton()
        etMobileNumberInput.addTextChangedListener(PhoneNumberFormatter)
    }

    private fun initSendOTPButton() {
        val toast = ToastPool(this)

        btnSendOTP.setOnClickListener {
            if (etMobileNumberInput.text.length != 12) {
                toast.incompletePhoneNumMessage.show()
            } else {
                sendOTPAndLaunchNextActivity()
            }
        }
    }

    private fun sendOTPAndLaunchNextActivity() {
        startProgressBar()

        val mobileNumber =
            "+63" + etMobileNumberInput.text.toString() //adding country code to mobile number
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                val loginIntent = Intent(this@MainActivity, OTPScreenActivity::class.java).apply {
                    putExtra(KeyEnum.KEY_MOBILE_NUMBER.name, mobileNumber) //mobile number
                    putExtra(KeyEnum.KEY_OTP.name, verificationId) //otp code
                }

                startActivity(loginIntent) // go to verify otp activity

                object : CountDownTimer(1000, 3000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        endProgressBar()
                    }
                }.start()
            }
        }

        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(mobileNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun startProgressBar() {
        btnSendOTP.visibility = View.GONE
        pgProgressBar.visibility = View.VISIBLE
    }

    private fun endProgressBar() {
        pgProgressBar.visibility = View.GONE
        btnSendOTP.visibility = View.VISIBLE
    }
}
