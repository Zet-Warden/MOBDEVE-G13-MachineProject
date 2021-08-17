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
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.KeyEnum
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper.ViewRefocuser
import kotlinx.android.synthetic.main.activity_otp_screen.*
import java.util.concurrent.TimeUnit

class OTPScreenActivity : AppCompatActivity(), ViewRefocuser {
    private var listOfOTPDigits = mutableListOf<EditText>()
    private lateinit var verificationId: String

    // these are for the user's information
    private var givenName = ""
    private var surname = ""
    private var birthdate = ""
    private var sex = ""
    private var priorityGroup = ""
    private var address = ""
    private var appointmentDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_screen)
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
                    Toast.makeText(this@OTPScreenActivity, "${e.message}", Toast.LENGTH_SHORT).show()
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

                loginOrAddUser()

                val homeIntent = Intent(this@OTPScreenActivity, HomeScreenActivity::class.java).apply {
                    putExtras(intent)
                    //if user has already filled up the personal information form
                    if (givenName != "") {
                        putExtra(KeyEnum.KEY_GIVEN_NAME.name, givenName)
                        putExtra(KeyEnum.KEY_SURNAME.name, surname)
                        putExtra(KeyEnum.KEY_BIRTHDATE.name, birthdate)
                        putExtra(KeyEnum.KEY_SEX.name, sex)
                        putExtra(KeyEnum.KEY_PRIORITY_GROUP.name, priorityGroup)
                        putExtra(KeyEnum.KEY_ADDRESS.name, address)
                        putExtra(KeyEnum.KEY_APPOINTMENT_DATE.name, appointmentDate)
                    }
                }

                startActivity(homeIntent) // go to home screen activity
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

    private fun loginOrAddUser() {
        val db = FirebaseFirestore.getInstance() //instance of database
        val mobileNumber = intent.getStringExtra(KeyEnum.KEY_MOBILE_NUMBER.name)

        //check if user is in the database already
        db.collection("users")
            .whereEqualTo("mobile number", mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                //user exists already
                if (query.size() > 0) {
                    // read the document and add extra to intent later
                    for (document in query)
                        // if user has already filled up the personal information form
                        if (document.contains("given name")) {
                            //getting all the data
                            givenName = document.getString("given name")!!
                            surname = document.getString("surname")!!
                            birthdate = document.getString("birthdate")!!
                            sex = document.getString("sex")!!
                            priorityGroup = document.getString("priority group")!!
                            address = document.getString("address")!!
                            appointmentDate = document.getString("appointment date")!!
                        }
                }
                //new user
                else {

                    //create new document
                    val newUser = hashMapOf(
                        "mobile number" to mobileNumber
                    )
                    db.collection("users").add(newUser)
                        .addOnSuccessListener {
                            Toast.makeText(this@OTPScreenActivity, "New User Created", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@OTPScreenActivity, "rip", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@OTPScreenActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

