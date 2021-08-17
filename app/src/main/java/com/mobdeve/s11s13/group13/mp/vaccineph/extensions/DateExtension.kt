package com.mobdeve.s11s13.group13.mp.vaccineph.extensions

import java.text.SimpleDateFormat
import java.util.*

val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)

fun Date.toFormattedString() : String{
    return sdf.format(this)
}