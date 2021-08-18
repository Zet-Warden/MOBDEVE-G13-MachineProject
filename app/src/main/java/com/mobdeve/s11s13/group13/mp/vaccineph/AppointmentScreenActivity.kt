package com.mobdeve.s11s13.group13.mp.vaccineph

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.android.synthetic.main.activity_appointment_screen.btnSave
import kotlinx.android.synthetic.main.activity_appointment_screen.clMainContainer
import java.util.*
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UserData
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.btnCalendar
import kotlinx.android.synthetic.main.activity_appointment_screen.btnHome
import kotlinx.android.synthetic.main.activity_appointment_screen.btnLocation
import kotlinx.android.synthetic.main.activity_appointment_screen.btnProfile
import kotlinx.android.synthetic.main.activity_user_screen.*
import java.time.LocalTime

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

    private fun updateCalendarView() {
       getSavedDate {
           if(it != null) {
               val date = it.toDateOrNull()
               if(date != null) {
                   cvCalendar.setDate(date.time, true, true)
                   tvAppointmentDate.text = it
               } else {
                   tvAppointmentDate.text = "No appointment date set"
                   println("Invalid date string format")
               }
           } else {
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
        //getMaxCap() //get maximum capacity of vaxx center
        //getSlotsTaken(Calendar().getXDaysFromNow(2)) //idk if this should be here actually, but this checks how many slots take
    }

    private fun initCalendar() {
        //initialize the default appointment date
        
        /*val dayAfterTomorrow = Calendar().getXDaysFromNow(2)
        cvCalendar.setDate(dayAfterTomorrow.time, true, true)
        tvAppointmentDate.text = dayAfterTomorrow.toFormattedString()*/

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

        val saveDateMessage = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT);
        val invalidDateMessage =
            Toast.makeText(this, "Invalid, resetting date", Toast.LENGTH_SHORT);
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

    private fun amIKachow(myTime : Long, allTime : List<Long>) : Boolean {
        var fastestTime : Long = Long.MAX_VALUE

        for (time in allTime) {
            if(time < fastestTime)
                fastestTime = time
        }

        return myTime == fastestTime
    }


    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }

    // returns true if there are still slots remaining
    private fun isAvailable(): Boolean {
        return taken < max
    }

    // gets the maximum capacity of the vaxx center
    private fun getMaxCap() {
        val location = "dummy location" //use geolocation to determine the closest vaxx center

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
        val location = "dummy location" //use geolocation to determine the closest vaxx center

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
        val location = "dummy location" //use geolocation to determine the closest vaxx center

        //make a new appointment
        val newAppointment = hashMapOf(
            "date" to tvAppointmentDate.text,
            "location" to location,
            "mobile number" to UserData.mobileNumber,
            "time stamp" to System.currentTimeMillis()
        )

        db.collection("appointments")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                //if user made an appointment before
                if (query.size() > 0) {
                    var docId = query.documents[0].id
                    db.collection("appointments").document(docId)
                        .delete() //delete previous appointment
                }

                db.collection("appointments")
                    .add(newAppointment) //add new appointment to the database
                    .addOnSuccessListener {
                        //if(amIKachow(newAppointment.get("time stamp"), ))
                    }

            }
    }

    private fun getAllTimeStamps() {
        val db = FirebaseFirestore.getInstance()
        val location = "dummy location"
        val date : Date? = null

        db.collection("appointments")
            .whereEqualTo("mobile number", UserData.mobileNumber)
    }

    private fun getSavedDate(callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("appointments")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                if(!query.isEmpty) {
                    val date = query.documents[0].getString("date")
                    callback(date)
                } else {
                    //idk log smth?
                }
            }
    }
}