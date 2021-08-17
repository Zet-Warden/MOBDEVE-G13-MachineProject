package com.mobdeve.s11s13.group13.mp.vaccineph

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.ViewLinker
import kotlinx.android.synthetic.main.activity_user_screen.*
import kotlinx.android.synthetic.main.activity_user_screen.btnSave
import kotlinx.android.synthetic.main.activity_user_screen.clMainContainer
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UserData

class UserScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_screen)
        init()
    }

    private fun init() {
        UIHider(this, clMainContainer)
        ViewLinker.linkViewsAndActivities(
            this,
            NavBarLinker.createNavBarLinkPairs(btnHome, btnProfile, btnLocation, btnCalendar)
        )
        initBirthdayCalendarDialog()
        initSexComboBox()
        initPriorityGroupComboBox()
        initSaveButton()
    }

    private fun initSaveButton() {
        val toast = Toast.makeText(this, "Please enter information for all fields.", Toast.LENGTH_SHORT)

        btnSave.setOnClickListener {
            if(isEverythingFilledUp()) {
                // add or update the database on save
                saveToDatabase()

            } else {
                toast.show()
            }
        }
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

    private fun saveToDatabase() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()

        val fields = hashMapOf(
            "first name" to etFirstName.text.toString(),
            "surname" to etLastName.text.toString(),
            "birthday" to etBirthday.text.toString(),
            "sex" to actvSex.text.toString(),
            "priority group" to actvPriorityGroup.text.toString(),
            "address" to etAddress.text.toString()
        )

        db.collection("users").document(UserData.userDocumentId).set(fields, SetOptions.merge())
    }
}