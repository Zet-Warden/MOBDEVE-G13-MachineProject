package com.mobdeve.s11s13.group13.mp.vaccineph

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_user_screen.*
import kotlinx.android.synthetic.main.activity_user_screen.btnSave
import kotlinx.android.synthetic.main.activity_user_screen.clMainContainer
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.toDateOrNull
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UserData

class UserScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_screen)
        init()
    }

    override fun onResume() {
        super.onResume()

        initUserInfo()
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

        initUserInfo()
    }

    private fun initSaveButton() {
        val incompleteMessage =
            Toast.makeText(this, "Please enter information for all fields.", Toast.LENGTH_SHORT)
        val notOfAgeMessage =
            Toast.makeText(this, "You must be 18 and above to get vaccinated.", Toast.LENGTH_SHORT)
        val saveMessage =
            Toast.makeText(this, "Your profile has been saved.", Toast.LENGTH_SHORT)

        btnSave.setOnClickListener {
            if (!isEverythingFilledUp()) {
                incompleteMessage.show()
            } else if (!isOfLegalAge()) {
                notOfAgeMessage.show()
            } else {
                // add or update the database on save
                saveToDatabase()
                saveMessage.show()
            }
        }
    }

    private fun isOfLegalAge(): Boolean {
        val currentCalendar = Calendar.getInstance()
        val userCalendar = Calendar.getInstance()
        userCalendar.time = etBirthday.text.toString().toDateOrNull()!!

        if (currentCalendar.get(Calendar.YEAR) - userCalendar.get(Calendar.YEAR) < 18) return false
        if (currentCalendar.get(Calendar.YEAR) - userCalendar.get(Calendar.YEAR) > 18) return true

        if (userCalendar.get(Calendar.MONTH) > currentCalendar.get(Calendar.MONTH)) return false
        if (userCalendar.get(Calendar.MONTH) < currentCalendar.get(Calendar.MONTH)) return true

        return userCalendar.get(Calendar.DATE) <= currentCalendar.get(Calendar.DATE)
    }

    private fun initBirthdayCalendarDialog() {
        val myCalendar = Calendar.getInstance()
        val updateLabel = fun(date: Date) {
            val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            etBirthday.setText(sdf.format(date))
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

    private fun isEverythingFilledUp(): Boolean {
        return etFirstName.text.isNotBlank() &&
                etLastName.text.isNotBlank() &&
                etBirthday.text.isNotBlank() &&
                actvSex.text.isNotBlank() &&
                actvPriorityGroup.text.isNotBlank() &&
                etAddress.text.isNotBlank()
    }

    private fun saveToDatabase() {
        val db = FirebaseFirestore.getInstance()

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

    private fun initUserInfo() {
        val db = FirebaseFirestore.getInstance()

        //retrieve user's saved info from the database, if there is
        db.collection("users")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                if (query.size() > 0)
                    for (document in query)
                        if (document.contains("first name")) {
                            etFirstName.setText(document.getString("first name"))
                            etLastName.setText(document.getString("surname"))
                            etBirthday.setText(document.getString("birthday"))
                            actvSex.setText(document.getString("sex"))
                            actvPriorityGroup.setText(document.getString("priority group"))
                            etAddress.setText(document.getString("address"))
                        }
            }
    }
}