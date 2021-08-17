package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.mainactivityhelper

import android.text.Editable
import android.text.TextWatcher
import java.lang.StringBuilder

object PhoneNumberFormatter : TextWatcher {
    private var flag = true
    private var prevLength: Int? = 0

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
}