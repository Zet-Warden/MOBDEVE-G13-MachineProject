/**
 * This file is used to extend the functionality of String.java
 * As such, the receiver of extension functions used in this file should only be for String.java
 */

package com.mobdeve.s11s13.group13.mp.vaccineph.extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


var format: DateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)

fun String.toDateOrNull() : Date? {
    return format.parse(this)
}