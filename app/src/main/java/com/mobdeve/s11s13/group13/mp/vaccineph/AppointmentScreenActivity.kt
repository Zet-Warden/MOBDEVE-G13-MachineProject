package com.mobdeve.s11s13.group13.mp.vaccineph

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.DB
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.User
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.AppointmentData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.coroutines.*
import java.util.*

class AppointmentScreenActivity : AppCompatActivity() {

    private var max: Long = 0
    private var taken: Int = 0

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
        GlobalScope.launch(Dispatchers.IO) {
            max = getMaxCap()
            withContext(Dispatchers.Main) {
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
        }
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
    private fun setInitialAppointmentDateTextAndCalendarFocus(dateString : String?, date : Date?) {
        if (dateString == null || dateString == Calendar().time.toFormattedString()) {
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
            val selectedDate = Calendar().createDate(year, month, date)
            cvCalendar.setDate(selectedDate.time, true, true)
        }
    }

    private fun initSaveButton() {
        var selectedDate: Date
        var prevDate: Date

        val saveDateMessage = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT)
        val invalidDateMessage =
            Toast.makeText(this, "Invalid, resetting date", Toast.LENGTH_SHORT)
        val appointmentFullMessage =
            Toast.makeText(this, "Appointment for this day is full already", Toast.LENGTH_LONG)

        btnSave.setOnClickListener {
            selectedDate = Date(cvCalendar.date)
            GlobalScope.launch(Dispatchers.IO) {
                taken = getSlotsTaken(selectedDate)
                prevDate = getSavedDate()?.toDateOrNull()!!

                withContext(Dispatchers.Main) {
                    if (isValidDate(selectedDate) && isAvailable()) {
                        tvAppointmentDate.text = selectedDate.toFormattedString()
                        saveToDatabase()
                        saveDateMessage.show()
                    } else {
                        cvCalendar.setDate(
                            prevDate.time,
                            false,
                            false
                        ) //set the date to the last selected date that was valid
                        if (!isValidDate(selectedDate))
                            invalidDateMessage.show()
                        else
                            appointmentFullMessage.show()
                    }
                }
            }
        }
    }

    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }

    private fun isAvailable(): Boolean {
        return taken < max
    }

    // gets the maximum number of people that can be vaccinated on a given day and location
    private suspend fun getMaxCap(): Long {
        val query = DB.createEqualToQuery("vaccination centers", "name" to User.location)
        val document = DB.asyncReadDocumentFromCollection(query)

        return document.first().getLong("max capacity") ?: 0
    }

    // gets the number of slots taken on a certain day
    private suspend fun getSlotsTaken(d: Date): Int {
        val date = d.toFormattedString()
        val queryPairs = mutableListOf<Pair<String, Any?>>()

        queryPairs.add("date" to date)
        queryPairs.add("location" to User.location)

        val query = DB.createEqualToQueries("appointments", queryPairs)
        return DB.asyncReadDocumentFromCollection(query).size()
    }

    // saves the user's appointment to the database
    private suspend fun saveToDatabase() {
        var query = DB.createEqualToQuery("appointments", "location" to User.location)
        query = query.whereArrayContains("mobileNumbers",  User.mobileNumber)
        val querySnapshot= DB.asyncReadDocumentFromCollection(query)

        //delete previous appointments
        if(!querySnapshot.isEmpty) {
            val prevAppointment = querySnapshot.first().toObject(AppointmentData::class.java)
            println("here: $prevAppointment")
            prevAppointment.mobileNumbers.remove(User.mobileNumber)
            querySnapshot.first().reference.update(
                hashMapOf(
                    "mobileNumbers" to prevAppointment.mobileNumbers,
                ) as Map<String, Any>
            )
            querySnapshot.first().reference.update("count", FieldValue.increment(-1))
        }

        val documentTo = DB.asyncReadNamedDocumentFromCollection("appointments", "${tvAppointmentDate.text} - ${User.location}")
        if(!documentTo.exists()) {
            val newAppointment: MutableMap<String, Any> = hashMapOf(
                "date" to tvAppointmentDate.text,
                "location" to User.location,
                "mobileNumbers" to mutableListOf(User.mobileNumber),
                "count" to 0,
            )
            DB.createNamedDocumentToCollection("appointments", "${tvAppointmentDate.text} - ${User.location}", newAppointment)
        } else {
            val appointment = documentTo.toObject(AppointmentData::class.java)!!
            appointment.mobileNumbers.add(User.mobileNumber)
            DB.mergeDataToNamedDocument("appointments", "${tvAppointmentDate.text} - ${User.location}", appointment)
        }
        val newDoc = DB.asyncReadNamedDocumentFromCollection("appointments", "${tvAppointmentDate.text} - ${User.location}")
        newDoc.reference.update("count", FieldValue.increment(1))

        /*val _newAppointment: MutableMap<String, Any> = hashMapOf(
            "date" to tvAppointmentDate.text,
            "location" to User.location,
            "mobile_number" to User.mobileNumber,
        )


        val query = DB.createEqualToQuery("appointments", "date" to "${tvAppointmentDate.text}")
        DB.readDocumentFromCollection(query) {
            if (!it.isEmpty) {
                DB.updateDocumentFromCollection(query, newAppointment)
            } else {
                DB.createDocumentToCollection("appointments",newAppointment)
            }
        }*/
    }

    private suspend fun getSavedDate(): String? {
        //val query = DB.createEqualToQuery("appointments", "mobile_number" to User.mobileNumber)
        val query = DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)
        val document = DB.asyncReadDocumentFromCollection(query)

        if (document.isEmpty) return Calendar().time.toFormattedString()
        return document.first().getString("date")
    }
}