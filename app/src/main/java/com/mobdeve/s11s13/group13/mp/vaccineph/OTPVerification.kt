package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_otp_verification.*
import kotlinx.android.synthetic.main.activity_otp_verification.clMainContainer

class OTPVerification : AppCompatActivity() {
    private var listOfOTPDigits = mutableListOf<EditText>()
    private lateinit var toast : Toast

    private lateinit var verificationId : String

    //private var forceResendToken : PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)
        UIHider(this, clMainContainer)
        initOTPDigits()
        initResendOTP()

        verificationId =
            intent.getStringExtra(KeyEnum.KEY_OTP.name)!! // gets the otp that was sent to the mobile number, for verification purposes

        toast = Toast.makeText(
            this,
            "Don't forget to fill your OTP before verifying!",
            Toast.LENGTH_SHORT
        )

        btnVerifyOTP.setOnClickListener {
            if (!isOTPDigitsCompletelyFilled()) {
                toast.cancel()
                toast.show()
            } else {
                var otp =
                    etOTPDigit1.text.toString() +
                    etOTPDigit2.text.toString() +
                    etOTPDigit3.text.toString() +
                    etOTPDigit4.text.toString() +
                    etOTPDigit5.text.toString() +
                    etOTPDigit6.text.toString()

                var phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, otp)
                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                    .addOnSuccessListener {
                        //should send phone number here to the other activity?
                        startActivity(Intent(this, MainActivity::class.java)) //change destination activity
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@OTPVerification, "Invalid Code", Toast.LENGTH_SHORT).show()
                    }

            }
        }
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

    private fun isOTPDigitsCompletelyFilled() : Boolean {
        listOfOTPDigits.forEach {
            if(it.text.isEmpty())
                return@isOTPDigitsCompletelyFilled false
        }
        return true;
    }

    private fun createKeyListenerForOTP(
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

    private fun createTextWatcherForOTP(nextView: View?): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.length == 1) //focus on nextView, only if the user has filled up the current OTP digit
                    nextView?.requestFocus()
            }
        }
    }

    private fun initResendOTP() {
        tvResendOTP.setOnClickListener{
            TODO()
        }
    }

}

