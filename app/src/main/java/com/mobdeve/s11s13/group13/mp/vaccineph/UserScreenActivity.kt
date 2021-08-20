package com.mobdeve.s11s13.group13.mp.vaccineph

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.location.Geocoder
import android.os.Bundle
import android.os.Message
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import kotlinx.android.synthetic.main.activity_user_screen.*
import java.text.SimpleDateFormat
import java.util.*

import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.GeoCodingLocation
import android.os.Handler as Handler


class UserScreenActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_screen)
        init()

    }

    private fun init() {
        UIHider(this, clMainContainer)
        initBirthdayCalendarDialog()
        initSexComboBox()
        initPriorityGroupComboBox()
        initSaveButton()
    }

    private fun initSaveButton() {
        val toast = Toast.makeText(this, "Please enter information for all fields.", Toast.LENGTH_SHORT)

        btnSave.setOnClickListener {
            if(isEverythingFilledUp()) {
                println(actvSex.text)
                println(actvPriorityGroup.text)
                // TODO add geocoder parser here
                convertAddress()
            } else {
                toast.show()
            }
        }
    }

    private fun convertAddress() {
        val address = etAddress.text.toString()
        val location = GeoCodingLocation()

        location.getAddressFromLocation(address,applicationContext,GeoCoderHandler(this))




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

    private fun isEverythingFilledUp() : Boolean {
        return etFirstName.text.isNotBlank() &&
                etLastName.text.isNotBlank() &&
                etBirthday.text.isNotBlank() &&
                actvSex.text.isNotBlank() &&
                actvPriorityGroup.text.isNotBlank() &&
                etAddress.text.isNotBlank()
    }
    companion object {
        private class GeoCoderHandler(private val userScreenActivity: UserScreenActivity) :
            Handler() {
            override fun handleMessage(message: Message) {
                val locationAddress: String? = when (message.what) {
                    1 -> {
                        val bundle = message.data
                        bundle.getString("address")
                    }
                    else -> null
                }
                println("ADDRESS: $locationAddress")
                //TODO insert to database
            }
        }
    }
}

