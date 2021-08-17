@file:Suppress("DEPRECATION")

package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.KeyEnum
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper.PhoneNumberFormatter
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        initSendOTPButton()
        etMobileNumberInput.addTextChangedListener(PhoneNumberFormatter)
    }

    private fun initSendOTPButton() {
        val loginToast = Toast.makeText(
            this,
            "Don't forget to fill your mobile number before proceeding.",
            Toast.LENGTH_SHORT
        )

        btnSendOTP.setOnClickListener {
            if (etMobileNumberInput.text.length != 12) {
                loginToast.show()
            } else {
                sendOTPAndLaunchNextActivity()
            }
        }
    }

    private fun sendOTPAndLaunchNextActivity() {
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
}
