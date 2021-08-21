package com.mobdeve.s11s13.group13.mp.vaccineph

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.android.synthetic.main.activity_appointment_screen.btnSave
import kotlinx.android.synthetic.main.activity_appointment_screen.clMainContainer
import java.util.*
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UserData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.btnCalendar
import kotlinx.android.synthetic.main.activity_appointment_screen.btnHome
import kotlinx.android.synthetic.main.activity_appointment_screen.btnLocation
import kotlinx.android.synthetic.main.activity_appointment_screen.btnProfile

class AppointmentScreenActivity : AppCompatActivity() {

    private var max: Int = 0
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

    @SuppressLint("SetTextI18n")
    private fun updateCalendarView() {
       getSavedDate {
           println(it)
           if(it != null) {
               val date = it.toDateOrNull()
               if(date != null) {
                   cvCalendar.setDate(date.time, true, true)
                   tvAppointmentDate.text = it
               } else {
                   println("Invalid date string format")
               }
           } else {
               tvAppointmentDate.text = "No appointment date set"
               println("No appointment in database")
           }
       }
    }

    private fun init() {
        UIHider(this, clMainContainer)
        ViewLinker.linkViewsAndActivities(
            this,
            NavBarLinker.createNavBarLinkPairs(btnHome, btnProfile, btnLocation, btnCalendar)
        )
        initCalendar()
        initSaveButton()
        getMaxCap()
    }

    private fun initCalendar() {
        //changes the date when user chooses a new date
        cvCalendar.setOnDateChangeListener { _, year, month, date ->
            val selectedDate = Calendar().createDate(year, month, date)
            cvCalendar.setDate(selectedDate.time, true, true)
        }
    }

    private fun initSaveButton() {
        val dayAfterTomorrow = Calendar().getXDaysFromNow(2)
        var selectedDate = dayAfterTomorrow
        var prevDate = dayAfterTomorrow

        val saveDateMessage = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT)
        val invalidDateMessage =
            Toast.makeText(this, "Invalid, resetting date", Toast.LENGTH_SHORT)
        val appointmentFullMessage =
            Toast.makeText(this, "Appointment for this day is full already", Toast.LENGTH_LONG)

        btnSave.setOnClickListener {
            prevDate = if (isValidDate(selectedDate)) selectedDate else prevDate
            selectedDate = Date(cvCalendar.date)

            getSlotsTaken(selectedDate) {
                if (isValidDate(selectedDate) && isAvailable()) {
                    tvAppointmentDate.text = selectedDate.toFormattedString()
                    saveToDatabase()
                    saveDateMessage.show()
                } else {
                    cvCalendar.setDate(
                        prevDate.time,
                        true,
                        true
                    ) //set the date to the last selected date that was valid
                    if (!isValidDate(selectedDate))
                        invalidDateMessage.show()
                    else
                        appointmentFullMessage.show()
                }
            }
        }
    }

    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }

    // returns true if there are still slots remaining
    private fun isAvailable(): Boolean {
        return taken < max
    }

    // gets the maximum capacity of the vaccine
    // center
    private fun getMaxCap() {
        val location = UserData.location //use geolocation to determine the closest vaccine
        // center

        val db = FirebaseFirestore.getInstance()
        db.collection("vaccination centers")
            .whereEqualTo("name", location)
            .get()
            .addOnSuccessListener { query ->
                for (document in query) {
                    max = document.getLong("max capacity")!!.toInt()
                }
            }
    }

    // gets the number of slots taken on a certain day
    private fun getSlotsTaken(d: Date, callback : () -> Unit) {
        val date = d.toFormattedString()
        val db = FirebaseFirestore.getInstance()
        val location = UserData.location //use geolocation to determine the closest vaccine
        // center

        db.collection("appointments")
            .whereEqualTo("date", date)
            .whereEqualTo("location", location)
            .get()
            .addOnSuccessListener { query ->
                taken = query.size()
                println("take: $taken")
                println("max: $max")
                callback()
            }

    }

    // saves the user's appointment to the database
    private fun saveToDatabase() {
        val db = FirebaseFirestore.getInstance()
        val location = UserData.location //use geolocation to determine the closest vaccine
        // center

        //make a new appointment
        val newAppointment = hashMapOf(
            "date" to tvAppointmentDate.text,
            "location" to location,
            "mobile number" to UserData.mobileNumber,
        )

        db.collection("appointments")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                //if user made an appointment before
                if (query.size() > 0) {
                    val docId = query.documents[0].id
                    db.collection("appointments").document(docId)
                        .delete() //delete previous appointment
                }

                db.collection("appointments")
                    .add(newAppointment) //add new appointment to the database
            }
    }

    private fun getSavedDate(callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("appointments")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                var date : String? = null
                if(!query.isEmpty) {
                    date = query.documents[0].getString("date")
                }
                callback(date)
            }
    }
}