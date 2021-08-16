package com.mobdeve.s11s13.group13.mp.vaccineph

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        initBirthdayCalendarDialog()
        initSexComboBox()
        initPriorityGroupComboBox()
    }

    private fun initBirthdayCalendarDialog() {
        val myCalendar = Calendar.getInstance()
        val updateLabel = fun(date: Date) {
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            etBirthday.setText(sdf.format(date));
        }

        val date =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel(myCalendar.time)
            }

        etBirthday.setOnClickListener {
            DatePickerDialog(
                this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun initSexComboBox() {
        val sexes = resources.getStringArray(R.array.sex_selection)
        val sexArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, sexes)
        actvSex.setAdapter(sexArrayAdapter)
    }

    private fun initPriorityGroupComboBox() {
        val priorityGroups = resources.getStringArray(R.array.priority_selection)
        val priorityGroupArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, priorityGroups)
        actvPriorityGroup.setAdapter(priorityGroupArrayAdapter)
    }
}