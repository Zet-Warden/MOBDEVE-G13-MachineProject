@file:Suppress("DEPRECATION")

package com.mobdeve.s11.group13.mp.vaccineph

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {
    private lateinit var toast : Toast

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
                startActivity(Intent(this, OTPVerification::class.java))
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
