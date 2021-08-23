package com.mobdeve.s11s13.group13.mp.vaccineph

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.coroutines.*
import java.util.*

class AppointmentScreenActivity : AppCompatActivity() {

    // represents the User's chosen date
    private var appointmentDate: Date = Calendar().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_screen)
        init()
    }

    override fun onResume() {
        super.onResume()
        updateCalendarView()
    }

    /**
     * Initializes all the components for this activity
     */
    private fun init() {
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

    /**
     * Initializes the appointment date text to the chosen appointment date in the GUI
     * Initializes the focus of the calendar to the chosen appointment date
     */
    private fun updateCalendarView() {
        GlobalScope.launch(Dispatchers.IO) {
            //get the chosen appointment date as String
            val dateString = getSavedDate()
            //get the chosen appointment date as Date
            val date = dateString?.toDateOrNull()
            withContext(Dispatchers.Main) {
                setInitialAppointmentDateTextAndCalendarFocus(dateString, date)
            }
        }
    }

    /**
     * Sets the proper label and focus for appointment date text and calendar in the GUI, respectively
     * To be called upon by [updateCalendarView]
     */
    @SuppressLint("SetTextI18n")
    private fun setInitialAppointmentDateTextAndCalendarFocus(dateString: String?, date: Date?) {
        if (dateString != null) {
            //assert date != null, improper date string format (should not happen)
            requireNotNull(date) { "date should not be null if dateString is not null, as all dates should be properly formatted" }
            cvCalendar.date = date.time
            tvAppointmentDate.text = dateString
        } else {
            //no appointment date found in database
            tvAppointmentDate.text = "No appointment date set"
        }
    }

    /**
     * Initializes the Calendar of the GUI
     * Sets an onDateChangeListener to change [appointmentDate]
     * and [cvCalendar] whenever the User clicks on a date
     */
    private fun initCalendar() {
        cvCalendar.setOnDateChangeListener { _, year, month, date ->
            //changes the date (both in GUI and logic) when user clicks on new date
            appointmentDate = Calendar().createDate(year, month, date)
            cvCalendar.setDate(appointmentDate.time, true, true)
        }
    }

    /**
     * Initializes the onClickListener of the save button
     * Its actions are delegated to [saveButtonAction]
     */
    private fun initSaveButton() {
        btnSave.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    saveButtonAction()
                }
            }
        }
    }

    /**
     * Saves the current [appointmentDate] to the database as String
     *
     * Only saves to the database when:
     *  > the [appointmentDate] is valid, as defined by [isChosenDateValid]
     *  > the [appointmentDate] is not yet chosen
     *  > the corresponding appointment document under the "appointments" collection is not yet at max capacity
     *      max capacity is defined by the corresponding location document under the "vaccine centers" collection
     *
     *  Resets to the previously chosen valid appointment date when these conditions are not met
     *  If the User has yet to choose an appointment date, set the calendar focus to Today
     *  However, no appointment is made in the database, this is reflected in [tvAppointmentDate]
     */
    private suspend fun saveButtonAction() {
        // get a toast pool to choose different toast messages
        val toast = ToastPool(this)
        if (isChosenDateValid() && !isDateAlreadyChosen()) {
            // check if saving to database is successful
            // it is successful only when the corresponding appointment document is not yet full
            val result = saveToDatabase()
            if (result) {
                tvAppointmentDate.text = appointmentDate.toFormattedString()
                toast.saveDateMessage.show()
            } else {
                resetToPrevAppointmentDate()
                toast.appointmentFullMessage.show()
            }
        } else if (isDateAlreadyChosen()) {
            toast.alreadyChosenMessage.show()
        } else {
            resetToPrevAppointmentDate()
            toast.invalidDateMessage.show()
        }
    }

    /**
     * Resets calendar focus to the previously chosen appointment date
     * If no appointment date has been chosen, set the focus to today
     */
    private suspend fun resetToPrevAppointmentDate() {
        val prevDate = getSavedDate()?.toDateOrNull() ?: Calendar().getToday()
        //set the date to the last selected date that was valid
        cvCalendar.setDate(
            prevDate.time,
            false,
            false
        )
    }

    /**
     * Checks if the chosen date is matched with [tvAppointmentDate]
     * [tvAppointmentDate] reflects the date successfully chosen by the User
     *
     * @return true if [tvAppointmentDate] is equal to [appointmentDate], as formatted in String
     */
    private fun isDateAlreadyChosen(): Boolean {
        return tvAppointmentDate.text == appointmentDate.toFormattedString()
    }

    /**
     * Checks if the chosen date is the day after tomorrow
     * @return true if the [appointmentDate] is the day after tomorrow
     */
    private fun isChosenDateValid(): Boolean {
        return appointmentDate.after(Calendar().getTomorrow())
    }

    /**
     * Saves the User's appointment information to the database
     * @return true if User has successfully saved to the database
     */
    private suspend fun saveToDatabase(): Boolean {
        // cache user's previous appointment
        // so that it can be deleted at a later time
        val query =
            DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)
        val querySnapShot = DB.asyncReadDocumentFromCollection(query)

        // get the document pertaining to the chosen appointment date
        // the name of the document is characterized by the appointment date
        // as well as the location of the vaccine center
        val document = DB.asyncReadNamedDocumentFromCollection(
            "appointments",
            "${appointmentDate.toFormattedString()} - ${User.location}"
        )

        // create document if it does not exist
        if (!document.exists()) {
            //default document content upon creation
            val newAppointment: MutableMap<String, Any> = hashMapOf(
                "date" to appointmentDate.toFormattedString(),
                "location" to User.location,
                "mobileNumbers" to mutableListOf<String>(),
                "count" to 0,
            )
            // create a new document since it does not exist
            DB.asyncMergeDataToNamedDocument(
                "appointments",
                "${appointmentDate.toFormattedString()} - ${User.location}",
                newAppointment
            )
        }

        val locationId = getUserLocationId()
        // create a transaction request for appointment
        // caching from earlier is necessary as this masks the previous appointment
        // store the result if the request, true if successful
        val result =
            DB.createAppointmentTransaction("${appointmentDate.toFormattedString()} - ${User.location}", locationId)

        // if transaction is successful, add the mobile number to the document
        // and delete previous appointment to avoid duplication
        if (result) {
            if (!querySnapShot.isEmpty) {
                val querySnapShotId = querySnapShot.first().id
                DB.deleteAppointmentTransaction(querySnapShotId)
            }
        }
        return result
    }

    /**
     * Gets the name of the document where User's location is
     * This is checked in the "vaccine centers" collections
     * @return the name of the document where User location is currently set to
     */
    private suspend fun getUserLocationId() : String {
        val locQuery = DB.createEqualToQuery("vaccination centers", "name" to User.location)
        val locationDocs = DB.asyncReadDocumentFromCollection(locQuery)
        var locationId = "none"
        if(!locationDocs.isEmpty)
            locationId = locationDocs.first().id
        return locationId
    }

    /**
     * Gets the chosen appointment date of the User as recorded in the database
     * @return the appointment date of the User in the database, null if it does not exist
     */
    private suspend fun getSavedDate(): String? {
        val query =
            DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)
        val document = DB.asyncReadDocumentFromCollection(query)

        if (document.isEmpty) return null
        return document.first().getString("date")
    }
}