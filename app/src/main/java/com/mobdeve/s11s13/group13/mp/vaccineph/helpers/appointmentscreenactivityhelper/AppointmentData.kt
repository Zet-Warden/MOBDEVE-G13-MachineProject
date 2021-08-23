package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.appointmentscreenactivityhelper

data class AppointmentData(
    val date : String = "",
    val location : String = "",
    val mobileNumbers : MutableList<String> = mutableListOf(),
    val count : Long = 0
)