package com.mobdeve.s11s13.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper.ViewRefocuser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_otp_screen.*
import kotlinx.android.synthetic.main.activity_otp_screen.clMainContainer
import kotlinx.android.synthetic.main.activity_otp_screen.pgProgressBar
import java.util.concurrent.TimeUnit

class  OTPScreenActivity : AppCompatActivity(), ViewRefocuser {
    private var listOfOTPDigits = mutableListOf<EditText>()
    private lateinit var verificationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_screen)
        init()
    }

    /**
     * Initializes components
     */
    private fun init() {
        UIHider(this, clMainContainer)
        initOTPDigits()
        initResendOTPButton()
        initBtnVerifyOTP()
    }

    /**
     * Initializes the OTP digits' behaviour as well as add them to [listOfOTPDigits]
     */
    private fun initOTPDigits() {
        listOfOTPDigits.add(etOTPDigit1)
        listOfOTPDigits.add(etOTPDigit2)
        listOfOTPDigits.add(etOTPDigit3)
        listOfOTPDigits.add(etOTPDigit4)
        listOfOTPDigits.add(etOTPDigit5)
        listOfOTPDigits.add(etOTPDigit6)

        // automatically go to the next OTP digit once the current has been filled up
        etOTPDigit1.addTextChangedListener(createTextWatcherForOTP(etOTPDigit2))
        etOTPDigit2.addTextChangedListener(createTextWatcherForOTP(etOTPDigit3))
        etOTPDigit3.addTextChangedListener(createTextWatcherForOTP(etOTPDigit4))
        etOTPDigit4.addTextChangedListener(createTextWatcherForOTP(etOTPDigit5))
        etOTPDigit5.addTextChangedListener(createTextWatcherForOTP(etOTPDigit6))
        etOTPDigit6.addTextChangedListener(createTextWatcherForOTP(null))

        // automatically go to the previous OTP digit once the current has been deleted
        etOTPDigit1.setOnKeyListener(createKeyListenerForOTP(etOTPDigit1, null))
        etOTPDigit2.setOnKeyListener(createKeyListenerForOTP(etOTPDigit2, etOTPDigit1))
        etOTPDigit3.setOnKeyListener(createKeyListenerForOTP(etOTPDigit3, etOTPDigit2))
        etOTPDigit4.setOnKeyListener(createKeyListenerForOTP(etOTPDigit4, etOTPDigit3))
        etOTPDigit5.setOnKeyListener(createKeyListenerForOTP(etOTPDigit5, etOTPDigit4))
        etOTPDigit6.setOnKeyListener(createKeyListenerForOTP(etOTPDigit6, etOTPDigit5))
    }

    private fun initBtnVerifyOTP() {
        val toast = ToastPool(this)

        // gets the otp that was sent, pass an empty string if key is not present
        requireNotNull(intent.getStringExtra(KeyEnum.KEY_OTP.name)) {
            """KeyEnum.KEY_OTP.name is null, ensure all KeyEnums are defined in KeyEnum.kt
                |Or that the proper key was put when passing the intent
            """.trimMargin()
        }
        verificationId = intent.getStringExtra(KeyEnum.KEY_OTP.name)!!

        btnVerifyOTP.setOnClickListener {
            if (!isOTPDigitsCompletelyFilled()) {
                toast.invalidOTPMessage.show()
            } else {
                authenticate(toast.invalidOTPMessage)
            }
        }
    }

    /**
     * Resends the OTP code
     */
    private fun initResendOTPButton() {
        tvResendOTP.setOnClickListener {
            val mobileNumber =
                intent.getStringExtra(KeyEnum.KEY_MOBILE_NUMBER.name)!! //get mobile number
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(this@OTPScreenActivity, "${e.message}", Toast.LENGTH_SHORT)
                        .show()
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
        startProgressBar()
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
                //assigns the mobile number of the user to a "static" variable in the UserData class for easy access
                requireNotNull(intent.getStringExtra(KeyEnum.KEY_OTP.name)) {
                    """KeyEnum.KEY_OTP.name is null, ensure all KeyEnums are defined in KeyEnum.kt
                        |Or that the proper key was put when passing the intent
                    """.trimMargin()
                }
                User.mobileNumber =
                    intent.getStringExtra(KeyEnum.KEY_MOBILE_NUMBER.name)!!

                //adds the user's mobile number to the database
                launchNextActivity()
                object : CountDownTimer(1000, 3000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        endProgressBar()
                    }
                }.start()
            }
            .addOnFailureListener {
                errorToast.show()
                object : CountDownTimer(1000, 3000) {
                    override fun onTick(millisUntilFinished: Long) {
                    }

                    override fun onFinish() {
                        endProgressBar()
                    }
                }.start()
            }
    }

    private fun startProgressBar() {
        btnVerifyOTP.visibility = View.GONE
        pgProgressBar.visibility = View.VISIBLE
    }

    private fun endProgressBar() {
        pgProgressBar.visibility = View.GONE
        btnVerifyOTP.visibility = View.VISIBLE
    }

    /**
     * Checks if all the OTP digits are not blank (strings composed entirely of the empty string or whitespaces)
     */
    private fun isOTPDigitsCompletelyFilled(): Boolean {
        listOfOTPDigits.forEach {
            if (it.text.isBlank())
                return@isOTPDigitsCompletelyFilled false
        }
        return true
    }

    /**
     * Launches the next activity depending whether the User is registered in the database or not
     *
     * If the User is registered
     *  > Launch the HomeScreenActivity
     * If not
     *  > Launch the UserScreenActivity
     */
    private fun launchNextActivity() {
        val query = DB.createEqualToQuery("users", "mobileNumber" to User.mobileNumber)

        DB.readDocumentFromCollection(query) {
            // check if the User is seen in the database
            if (it.isEmpty) {
                startActivity(Intent(this, UserScreenActivity::class.java))
            } else {
                //flag that the User is registered
                User.isRegistered = true
                startActivity(Intent(this, HomeScreenActivity::class.java))
            }
        }.addOnFailureListener {
            println(it.message)
        }
    }
}

