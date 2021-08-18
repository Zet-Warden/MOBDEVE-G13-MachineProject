package com.mobdeve.s11s13.group13.mp.vaccineph.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


var format: DateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)

fun String.toDateOrNull() : Date? {
    return format.parse(this)
}