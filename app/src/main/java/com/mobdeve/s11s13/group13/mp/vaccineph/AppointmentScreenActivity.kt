package com.mobdeve.s11s13.group13.mp.vaccineph

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.android.synthetic.main.activity_appointment_screen.btnSave
import kotlinx.android.synthetic.main.activity_appointment_screen.clMainContainer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.*
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*


class AppointmentScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_screen)
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        initCalendar()
        initSaveButton()
    }

    private fun initCalendar() {
        val calendar = Calendar.getInstance()
        calendar.set(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DATE) + 2
        )
        val selectedDate = calendar.time
        cvCalendar.setDate(selectedDate.time, true, true)

        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        tvAppointmentDate.text = sdf.format(selectedDate)

        cvCalendar.setOnDateChangeListener { _, year, month, date ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, date)
            println("$year $month $date")
            println(calendar.time.time)
            cvCalendar.setDate(calendar.time.time, true, true)
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
                cvCalendar.setDate(prevDate.time, true, true)
                Toast.makeText(this, "Invalid date, resetting date", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidDate(date: Date): Boolean {
        return date.after(Calendar().getTomorrow())
    }
}