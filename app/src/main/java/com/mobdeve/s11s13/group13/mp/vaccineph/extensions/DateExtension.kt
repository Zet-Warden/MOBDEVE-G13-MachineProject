/**
 * This file is used to extend the functionality of Date.java
 * As such, the receiver of extension functions used in this file should only be for Date.java
 */

package com.mobdeve.s11s13.group13.mp.vaccineph.extensions

import java.text.SimpleDateFormat
import java.util.*

val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)

fun Date.toFormattedString() : String{
    return sdf.format(this)
}