/**
 * This file is used to extend the functionality of Calendar.java
 * As such, the receiver of extension functions used in this file should only be for Calendar.java
 */

package com.mobdeve.s11s13.group13.mp.vaccineph.extensions

import java.util.*
import java.util.Calendar.*

fun Calendar(): Calendar {
    return Calendar.getInstance()
}

fun Calendar.getToday(): Date {
    val calendar = getInstance()
    return calendar.time
}

fun Calendar.getTomorrow(): Date {
    return getXDaysFromNow(1)
}

fun Calendar.getXDaysFromNow(numOfDays: Int): Date {
    val calendar = getInstance()
    calendar.set(DATE, calendar.get(DATE) + numOfDays)
    return calendar.time
}

fun Calendar.createDate(year: Int, month: Int, day: Int) : Date {
    val calendar = getInstance()
    calendar.set(year, month, day)
    return calendar.time
}

fun Calendar.todayInFormattedString() : String {
    return Calendar().time.toFormattedString()
}

fun Calendar.getYear() : Int {
    return this.get(YEAR)
}

fun Calendar.getMonth() : Int {
    return this.get(MONTH)
}

fun Calendar.getDate() : Int {
    return this.get(DATE)
}
