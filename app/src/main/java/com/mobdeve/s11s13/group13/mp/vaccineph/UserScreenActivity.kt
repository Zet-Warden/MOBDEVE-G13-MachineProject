package com.mobdeve.s11s13.group13.mp.vaccineph

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.location.Location;
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.GeoPoint
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_user_screen.*
import kotlinx.android.synthetic.main.activity_user_screen.btnSave
import kotlinx.android.synthetic.main.activity_user_screen.clMainContainer
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.toDateOrNull
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.*
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import android.widget.Toast.makeText as makeText1


class UserScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_screen)
        init()

    }

    override fun onResume() {
        super.onResume()
        initSexComboBox()
        initPriorityGroupComboBox()
        initUserInfo()
    }

    /**
     * Initializes components
     */
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

    /**
     * Initializes the save button onClickListener
     *
     * Allow saving of information only if
     *   > everything is filled up
     *   > the User is of legal age
     */
    private fun initSaveButton() {
        val toast = ToastPool(this)

        btnSave.setOnClickListener {
            if (!isEverythingFilledUp()) {
                toast.incompleteInfoMessage.show()
            } else if (!isOfLegalAge()) {
                toast.notOfAgeMessage.show()
            } else {
                // add or update the database on save
                saveToDatabase()
                toast.successfulSaveMessage.show()
            }
        }
    }

    /**
     * Checks if the User is 18 years old or above
     * @return true if the User is at least 18 years old
     */
    private fun isOfLegalAge(): Boolean {
        val currentCalendar = Calendar()
        val userCalendar = Calendar()

        // assert that the text from etBirthday can always be parsed into a Data type
        // this is because a dialog box will be used when filling up the birthday field
        // to ensure proper format
        userCalendar.time = etBirthday.text.toString().toDateOrNull()!!

        if (currentCalendar.getYear() - userCalendar.getYear() < 18) return false
        if (currentCalendar.getYear() - userCalendar.getYear() > 18) return true

        if (userCalendar.getMonth() > currentCalendar.getMonth()) return false
        if (userCalendar.getMonth() < currentCalendar.getMonth()) return true

        return userCalendar.getDate() <= currentCalendar.getDate()
    }

    /**
     * Initializes the calendar dialog when filling up the birthday field
     * This ensures that all dates are in the same format
     * The format is defined in DateExtension.kt
     */
    private fun initBirthdayCalendarDialog() {
        val myCalendar = Calendar()

        // a date listener that sets the date of the birthday field to the chosen date of the user
        val dateListener =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val date = Calendar().createDate(year, monthOfYear, dayOfMonth)
                etBirthday.setText(date.toFormattedString())
            }

        etBirthday.setOnClickListener {
            DatePickerDialog(
                this, dateListener, myCalendar.getYear(), myCalendar.getMonth(),
                myCalendar.getDate()
            ).show()
        }
    }

    /**
     * Initialize comboBox for sex
     */
    private fun initSexComboBox() {
        val sexes = resources.getStringArray(R.array.sex_selection)
        val sexArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, sexes)
        actvSex.setAdapter(sexArrayAdapter)
    }

    /**
     * Initialize comboBox for priority group
     */
    private fun initPriorityGroupComboBox() {
        val priorityGroups = resources.getStringArray(R.array.priority_selection)
        val priorityGroupArrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, priorityGroups)
        actvPriorityGroup.setAdapter(priorityGroupArrayAdapter)
    }

    /**
     * Checks if every field is filled up
     * @return true if all fields are not blank (not composed entirely of the empty string or whitespaces)
     */
    private fun isEverythingFilledUp(): Boolean {
        return etFirstName.text.isNotBlank() &&
                etLastName.text.isNotBlank() &&
                etBirthday.text.isNotBlank() &&
                actvSex.text.isNotBlank() &&
                actvPriorityGroup.text.isNotBlank() &&
                etAddress.text.isNotBlank()
    }

    /**
     * Saves user information to the database
     * If save is successful, flag User as registered
     */
    private fun saveToDatabase() {
        val fields = hashMapOf(
            "firstName" to "${etFirstName.text}",
            "surname" to "${etLastName.text}",
            "birthday" to "${etBirthday.text}",
            "mobileNumber" to User.mobileNumber,
            "sex" to "${actvSex.text}",
            "priorityGroup" to "${actvPriorityGroup.text}",
            "address" to "${etAddress.text}",
        ) as HashMap<String, Any>

        val query = DB.createEqualToQuery("users", "mobileNumber" to User.mobileNumber)
        DB.readDocumentFromCollection(query) {
            if(it.isEmpty) {
                DB.createDocumentToCollection("users", fields) {
                    User.isRegistered = true
                    convertAddressAndAssign()
                }
            } else {
                DB.updateDocumentFromCollection(query, fields)
                convertAddressAndAssign()
            }
        }

    }

    /**
     * Initialize the User information upon going to this activity, if the User already has information in the database
     * This makes it easy for the User to edit their information
     */
    private fun initUserInfo() {
        val query = DB.createEqualToQuery("users", "mobileNumber" to User.mobileNumber)
        DB.readDocumentFromCollection(query) {
            if (!it.isEmpty) {
                val document = it.first()
                if (document.contains("mobileNumber")) {
                    etFirstName.setText(document.getString("firstName"))
                    etLastName.setText(document.getString("surname"))
                    etBirthday.setText(document.getString("birthday"))
                    actvSex.setText(document.getString("sex"), false)
                    actvPriorityGroup.setText(document.getString("priorityGroup"), false)
                    etAddress.setText(document.getString("address"))
                }
            }
        }
    }
    private fun convertAddressAndAssign() {
        val address = etAddress.text.toString()
        val location = GeoCodingLocation()
        location.getAddressFromLocation(address,applicationContext,GeoCoderHandler(this))
    }

    companion object {

        private class GeoCoderHandler(private val userScreenActivity: UserScreenActivity) : Handler() {

            override fun handleMessage(message: Message) {

                println("ASSIGNING LOCATION")
                val locationAddress: DoubleArray? = when (message.what) {
                    1 -> {
                        val bundle = message.data
                        bundle.getDoubleArray("address")
                    }
                    else -> null
                }
//                val loc = locationAddress!!
                val userAdd = Location("Cur_Loc")
                println(locationAddress)
                if ( locationAddress != null) {
                    userAdd.latitude = locationAddress?.get(0)!!
                    userAdd.longitude = locationAddress?.get(1)!!
                }
                else{
                    userAdd.latitude = 14.57249771
                    userAdd.longitude = 120.983662732
                }

                var vaccineCenters = ArrayList<Location>()
                val query = DB.createEqualToQueries("vaccination centers", mutableListOf())

                DB.readDocumentFromCollection(query) {
                    it.forEach { elem ->
                        var loc = Location(elem.getString("name"))
                        loc.longitude = elem.getGeoPoint("location")?.longitude!!.toDouble()
                        loc.latitude = elem.getGeoPoint("location")?.latitude!!.toDouble()
                        vaccineCenters.add(loc)

                    }

                    var distances = ArrayList<Float>()

                    vaccineCenters.forEach{elem ->
                        val diff = userAdd.distanceTo(elem)
                        distances.add(diff)
                    }

                    println(vaccineCenters[distances.indexOf(distances.minOrNull())].provider)
                    val assigned = vaccineCenters[distances.indexOf(distances.minOrNull())].provider
                    //TODO ADD ^ to current user as assigned center
                    val query = DB.createEqualToQuery("users","mobileNumber" to User.mobileNumber)
                    DB.readDocumentFromCollection(query){ ft ->
                        if (!ft.isEmpty) {
                            val document = ft.first();
                            if (document.contains("mobileNumber")) {
                                val firstName = document.getString("firstName")
                                val surname = document.getString("surName")
                                val birthday = document.getString("birthday")
                                val sex = document.getString("sex")
                                val priority = document.getString("priorityGroup")
                                val address = document.getString("address")
                                //etc etc save the info to local variables
                                val fields = hashMapOf(
                                    "firstName" to firstName,
                                    "surname" to surname,
                                    "birthday" to birthday,
                                    "mobileNumber" to User.mobileNumber,
                                    "sex" to sex,
                                    "priorityGroup" to priority,
                                    "address" to address,
                                    "assignedCenter" to assigned,
                                ) as HashMap<String, Any>
                                //create a hashmap here then
                                DB.updateDocumentFromCollection(query, fields)
                                println("saving")
                            }

                }

            }
        }
    }
        }
    }
}

