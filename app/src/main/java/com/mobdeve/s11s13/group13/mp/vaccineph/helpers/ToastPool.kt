package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import android.content.Context
import android.widget.Toast

class ToastPool(context: Context) {

    //region Toasts for MainActivity
    val incompletePhoneNumMessage: Toast = Toast.makeText(
        context,
        "Don't forget to fill your mobile number before proceeding.",
        Toast.LENGTH_SHORT
    )
    //endregion

    //region Toasts for OTPScreenActivity
    val invalidOTPMessage: Toast = Toast.makeText(
        context,
        "Invalid Code",
        Toast.LENGTH_SHORT
    )
    //endregion

    //region Toasts for UserScreenActivity
    val incompleteInfoMessage: Toast =
        Toast.makeText(context, "Please enter information for all fields.", Toast.LENGTH_SHORT)
    val notOfAgeMessage: Toast =
        Toast.makeText(context, "You must be 18 and above to get vaccinated.", Toast.LENGTH_SHORT)
    val successfulSaveMessage: Toast =
        Toast.makeText(context, "Your profile has been saved.", Toast.LENGTH_SHORT)
    //endregion

    //region Toasts for AppointmentScreenActivity
    val saveDateMessage: Toast = Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT)
    val invalidDateMessage: Toast =
        Toast.makeText(context, "Appointment must be at least two day from now", Toast.LENGTH_SHORT)
    val appointmentFullMessage: Toast =
        Toast.makeText(
            context,
            "Appointment for this day is full already",
            Toast.LENGTH_LONG
        )
    val alreadyChosenMessage: Toast =
        Toast.makeText(
            context,
            "You have already booked this appointment date",
            Toast.LENGTH_LONG
        )

    val savingToDBMessage: Toast =
        Toast.makeText(
            context,
            "You appointment request is being processed. Please wait to select another appointment",
            Toast.LENGTH_SHORT
        )

    val networkUnavailable: Toast =
        Toast.makeText(
            context,
            "Network unavailable, try again later",
            Toast.LENGTH_SHORT
        )
    //endregion
}