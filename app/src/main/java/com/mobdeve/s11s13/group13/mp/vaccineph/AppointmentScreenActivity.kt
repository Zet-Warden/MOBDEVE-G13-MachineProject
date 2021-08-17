package com.mobdeve.s11s13.group13.mp.vaccineph

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.android.synthetic.main.activity_appointment_screen.btnSave
import kotlinx.android.synthetic.main.activity_appointment_screen.clMainContainer
import java.util.*
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.btnCalendar
import kotlinx.android.synthetic.main.activity_appointment_screen.btnHome
import kotlinx.android.synthetic.main.activity_appointment_screen.btnLocation
import kotlinx.android.synthetic.main.activity_appointment_screen.btnProfile

class AppointmentScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_screen)
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        ViewLinker.linkViewsAndActivities(
            this,
            NavBarLinker.createNavBarLinkPairs(btnHome, btnProfile, btnLocation, btnCalendar)
        )
        initCalendar()
        initSaveButton()
    }

    private fun initCalendar() {
        //initialize the default appointment date
        val dayAfterTomorrow = Calendar().getXDaysFromNow(2)
        cvCalendar.setDate(dayAfterTomorrow.time, true, true)
        tvAppointmentDate.text = dayAfterTomorrow.toFormattedString()

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

        btnSave.setOnClickListener {
            prevDate = if (isValidDate(selectedDate)) selectedDate else prevDate
            selectedDate = Date(cvCalendar.date)

            if (isValidDate(selectedDate)) {
                tvAppointmentDate.text = selectedDate.toFormattedString()
            } else {
                cvCalendar.setDate(
                    prevDate.time,
                    true,
                    true
                ) //set the date to the last selected date that was valid
                Toast.makeText(this, "Invalid, resetting date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }
}