package com.mobdeve.s11s13.group13.mp.vaccineph

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.DB
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.User
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.AppointmentData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class AppointmentScreenActivity : AppCompatActivity() {
    private var appointmentDate: Date = Calendar().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_screen)
        init()
    }

    override fun onResume() {
        super.onResume()
        updateCalendarView()
    }

    private fun init() {
        UIHider(this@AppointmentScreenActivity, clMainContainer)
        ViewLinker.linkViewsAndActivities(
            this@AppointmentScreenActivity,
            NavBarLinker.createNavBarLinkPairs(
                btnHome,
                btnProfile,
                btnLocation,
                btnCalendar
            )
        )
        initSaveButton()
        initCalendar()
    }

    private fun updateCalendarView() {
        GlobalScope.launch(Dispatchers.IO) {
            val dateString = getSavedDate()
            val date = dateString?.toDateOrNull()
            withContext(Dispatchers.Main) {
                setInitialAppointmentDateTextAndCalendarFocus(dateString, date)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setInitialAppointmentDateTextAndCalendarFocus(dateString: String?, date: Date?) {
        if (dateString == null || dateString == Calendar().todayInFormattedString()) {
            tvAppointmentDate.text = "No appointment date set"
            println("No appointment in database")
        } else {
            if (date != null) {
                cvCalendar.date = date.time
                tvAppointmentDate.text = dateString
            } else {
                println("Invalid date string format")
            }
        }
    }

    private fun initCalendar() {
        //changes the date when user clicks on new date
        cvCalendar.setOnDateChangeListener { _, year, month, date ->
            appointmentDate = Calendar().createDate(year, month, date)
            cvCalendar.setDate(appointmentDate.time, true, true)
        }
    }

    private fun initSaveButton() {
        val saveDateMessage = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT)
        val invalidDateMessage =
            Toast.makeText(this, "Invalid, resetting date", Toast.LENGTH_SHORT)
        val appointmentFullMessage =
            Toast.makeText(this, "Appointment for this day is full already", Toast.LENGTH_LONG)
        val alreadyChosenMessage =
            Toast.makeText(this, "You have already booked this appointment date", Toast.LENGTH_LONG)

        btnSave.setOnClickListener {
            appointmentDate = Date(cvCalendar.date)
            GlobalScope.launch(Dispatchers.IO) {
                val prevDate = getSavedDate()?.toDateOrNull()!!

                withContext(Dispatchers.Main) {
                    if (isValidDate(appointmentDate) && !isAlreadyChosenDate(appointmentDate)) {
                        println(appointmentDate.toFormattedString())
                        val result = saveToDatabase()
                        if (result) {
                            saveDateMessage.show()
                            tvAppointmentDate.text = appointmentDate.toFormattedString()
                        } else {
                            cvCalendar.setDate(
                                prevDate.time,
                                false,
                                false
                            ) //set the date to the last selected date that was valid
                            appointmentFullMessage.show()
                        }
                    } else if (isAlreadyChosenDate(appointmentDate)) {
                        alreadyChosenMessage.show()
                    } else {
                        invalidDateMessage.show()
                    }
                }
            }
        }
    }

    private fun isAlreadyChosenDate(date: Date): Boolean {
        return tvAppointmentDate.text == date.toFormattedString()
    }

    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }

    // saves the user's appointment to the database
    private suspend fun saveToDatabase(): Boolean {
        //cache user's previous appointment, so that it can be deleted if saveToDatabase is successful
        val query =
            DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)
        val querySnaphot = DB.asyncReadDocumentFromCollection(query)

        var document = DB.asyncReadNamedDocumentFromCollection(
            "appointments",
            "${appointmentDate.toFormattedString()} - ${User.location}"
        )

        if (!document.exists()) {
            //default document content upon creation
            val newAppointment: MutableMap<String, Any> = hashMapOf(
                "date" to appointmentDate.toFormattedString(),
                "location" to User.location,
                "mobileNumbers" to mutableListOf<String>(),
                "count" to 0,
            )
            //create a new document since it does not exist
            DB.asyncMergeDataToNamedDocument(
                "appointments",
                "${appointmentDate.toFormattedString()} - ${User.location}",
                newAppointment
            )
            //look for the document again after creating
            document = DB.asyncReadNamedDocumentFromCollection(
                "appointments",
                "${appointmentDate.toFormattedString()} - ${User.location}"
            )
        }

        //create a transaction request for appointment
        val result = DB.createAppointmentTransaction(
            "appointments",
            "${appointmentDate.toFormattedString()} - ${User.location}",
        )

        //if transaction is successful, add the mobile number to the document
        //and delete previous appointment to avoid duplicated
        if (result) {
            /*val appointment = document.toObject(AppointmentData::class.java)!!
            appointment.mobileNumbers.add(User.mobileNumber)
            document.reference.update(hashMapOf("mobileNumbers" to appointment.mobileNumbers) as Map<String, Any>)*/
            if(!querySnaphot.isEmpty) {
                val querySnapShotId = querySnaphot.first().id
                deleteAppointment(querySnapShotId)
            }
        }
        return result
    }

    private suspend fun deleteAppointment(querySnapShotId: String) {
       DB.deleteAppointmentTransaction("appointments", querySnapShotId)
    }

    private suspend fun getSavedDate(): String? {
        val query =
            DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)
        val document = DB.asyncReadDocumentFromCollection(query)

        if (document.isEmpty) return Calendar().todayInFormattedString() //default to today, if no previous saved date
        return document.first().getString("date")
    }
}