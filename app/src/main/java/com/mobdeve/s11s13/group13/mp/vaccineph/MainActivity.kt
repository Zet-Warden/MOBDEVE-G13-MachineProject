@file:Suppress("DEPRECATION")

package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var toast : Toast

    private var resendToken : PhoneAuthProvider.ForceResendingToken?= null
    private var mCallbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId : String?= null
    private lateinit var firebaseAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UIHider(this, clMainContainer)

       toast = Toast.makeText(
            this,
            "Don't forget to fill your mobile number before proceeding!",
            Toast.LENGTH_SHORT
        )

        btnSendOTP.setOnClickListener {
            if (etMobileNumberInput.text.length != 12) {
                toast.cancel()
                toast.show()
            } else {

                val mobileNumber = "+63" + etMobileNumberInput.text.toString() //adding country code to mobile number

                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        //do nothing
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        val intent = Intent(this@MainActivity, OTPVerification::class.java).apply {
                            putExtra(KeyEnum.KEY_MOBILE_NUMBER.name, mobileNumber) //mobile number
                            putExtra(KeyEnum.KEY_OTP.name, verificationId) //otp code
                        }

                        startActivity(intent) // go to verify otp activity
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

        etMobileNumberInput.addTextChangedListener(object : TextWatcher {
            var flag = true
            var prevLength: Int? = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (flag && !isDeleting(prevLength, s?.length)) {
                    flag = false
                    s?.replace( //this triggers afterTextChanged event, flag is necessary to avoid infinite recursions
                        0,
                        s.length,
                        formatString(s)
                    )
                }
                prevLength = s?.length
                flag = true
            }

            private fun formatString(s: Editable?): String {
                val sb = StringBuilder()
                s?.forEachIndexed { index, c ->
                    if (c != ' ') { //skip spaces, treat s as a continuous char of digits
                        sb.append(c)
                        if (index == 2 || index == 6)
                            sb.append(' ')
                    }
                }
                return sb.toString()
            }

            private fun isDeleting(prevLength: Int?, currLength: Int?): Boolean {
                if (prevLength != null && currLength != null)
                    return prevLength > currLength
                return false
            }

        })
    }
}
