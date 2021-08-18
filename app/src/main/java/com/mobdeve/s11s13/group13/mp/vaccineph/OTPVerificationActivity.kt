package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.KeyEnum
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.ViewRefocuser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_otp_verification.*
import kotlinx.android.synthetic.main.activity_otp_verification.clMainContainer
import java.util.concurrent.TimeUnit

class OTPVerificationActivity : AppCompatActivity(), ViewRefocuser {
    private var listOfOTPDigits = mutableListOf<EditText>()
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        initOTPDigits()
        initResendOTPButton()
        initBtnVerifyOTP()
    }

    private fun initOTPDigits() {
        listOfOTPDigits.add(etOTPDigit1)
        listOfOTPDigits.add(etOTPDigit2)
        listOfOTPDigits.add(etOTPDigit3)
        listOfOTPDigits.add(etOTPDigit4)
        listOfOTPDigits.add(etOTPDigit5)
        listOfOTPDigits.add(etOTPDigit6)

        etOTPDigit1.addTextChangedListener(createTextWatcherForOTP(etOTPDigit2))
        etOTPDigit2.addTextChangedListener(createTextWatcherForOTP(etOTPDigit3))
        etOTPDigit3.addTextChangedListener(createTextWatcherForOTP(etOTPDigit4))
        etOTPDigit4.addTextChangedListener(createTextWatcherForOTP(etOTPDigit5))
        etOTPDigit5.addTextChangedListener(createTextWatcherForOTP(etOTPDigit6))
        etOTPDigit6.addTextChangedListener(createTextWatcherForOTP(null))

        etOTPDigit1.setOnKeyListener(createKeyListenerForOTP(etOTPDigit1, null))
        etOTPDigit2.setOnKeyListener(createKeyListenerForOTP(etOTPDigit2, etOTPDigit1))
        etOTPDigit3.setOnKeyListener(createKeyListenerForOTP(etOTPDigit3, etOTPDigit2))
        etOTPDigit4.setOnKeyListener(createKeyListenerForOTP(etOTPDigit4, etOTPDigit3))
        etOTPDigit5.setOnKeyListener(createKeyListenerForOTP(etOTPDigit5, etOTPDigit4))
        etOTPDigit6.setOnKeyListener(createKeyListenerForOTP(etOTPDigit6, etOTPDigit5))
    }

    private fun initBtnVerifyOTP() {
        val invalidToast = Toast.makeText(
            this,
            "InvalidCode",
            Toast.LENGTH_SHORT
        )

        verificationId = intent.getStringExtra(KeyEnum.KEY_OTP.name)!! // gets the otp that was sent
        btnVerifyOTP.setOnClickListener {
            if (!isOTPDigitsCompletelyFilled()) {
                invalidToast.show()
            } else {
                authenticate(invalidToast)
            }
        }
    }

    private fun initResendOTPButton() {
        tvResendOTP.setOnClickListener {
            val mobileNumber =
                intent.getStringExtra(KeyEnum.KEY_MOBILE_NUMBER.name)!! //get mobile number
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@OTPVerificationActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    newVerificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = newVerificationId
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

    private fun authenticate(errorToast: Toast) {
        val otp =
            etOTPDigit1.text.toString() +
                    etOTPDigit2.text.toString() +
                    etOTPDigit3.text.toString() +
                    etOTPDigit4.text.toString() +
                    etOTPDigit5.text.toString() +
                    etOTPDigit6.text.toString()

        val phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, otp)
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
            .addOnSuccessListener {
                //TODO: start home page activity
                startActivity(Intent(this, MapsActivity::class.java))
            }
            .addOnFailureListener {
                errorToast.show()
            }
    }

    private fun isOTPDigitsCompletelyFilled(): Boolean {
        listOfOTPDigits.forEach {
            if (it.text.isEmpty())
                return@isOTPDigitsCompletelyFilled false
        }
        return true
    }
}

