package com.mobdeve.s11s13.group13.mp.vaccineph

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.extensions.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_appointment_screen.*
import kotlinx.android.synthetic.main.activity_appointment_screen.btnCalendar
import kotlinx.android.synthetic.main.activity_appointment_screen.btnHome
import kotlinx.android.synthetic.main.activity_appointment_screen.btnLocation
import kotlinx.android.synthetic.main.activity_appointment_screen.btnProfile
import kotlinx.android.synthetic.main.activity_appointment_screen.btnSave
import kotlinx.android.synthetic.main.activity_appointment_screen.clMainContainer
import kotlinx.android.synthetic.main.activity_user_screen.*
import kotlinx.android.synthetic.main.activity_user_screen.view.*
import kotlinx.android.synthetic.main.template_home_feed.*
import kotlinx.coroutines.*
import java.util.*

class AppointmentScreenActivity : AppCompatActivity() {

    // represents the User's chosen date
    // global variable bad, should be refactored into a local variable
    // and be passed into necessary functions
    // problem is at [saveToDatabase]

    //private var appointmentDate: Date = Calendar().time

    // get a toast pool to choose different toast messages
    private lateinit var toast: ToastPool

    private val activityResultLauncher = this.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if(it.resultCode == RESULT_OK) {
            //update the db to include the event id of the user's calendar event
            val id = getEventId()
            val query = DB.createEqualToQuery("users", "mobileNumber" to User.mobileNumber)

            val field = hashMapOf(
                "eventId" to id
            ) as HashMap<String, Any>

            DB.updateDocumentFromCollection(query, field)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_screen)
        init()
        toast = ToastPool(this)
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
            cvCalendar.setDate(date.time, false, false)
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
            val appointmentDate = Calendar().createDate(year, month, date)
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
        val appointmentDate = getAppointmentDate()
        if (isChosenDateValid(appointmentDate) && !isDateAlreadyChosen(appointmentDate)) {
            // check if saving to database is successful
            // it is successful only when the corresponding appointment document is not yet full
            val result = saveToDatabase(appointmentDate)
            if (result) {
                tvAppointmentDate.text = appointmentDate.toFormattedString()
                toast.saveDateMessage.show()

                addOrUpdateCalendar()
            } else {
                resetToPrevAppointmentDate()
                toast.appointmentFullMessage.show()
            }
        } else if (isDateAlreadyChosen(appointmentDate)) {
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
    private fun isDateAlreadyChosen(appointmentDate: Date): Boolean {
        return tvAppointmentDate.text == appointmentDate.toFormattedString()
    }

    /**
     * Checks if the chosen date is the day after tomorrow
     * @return true if the [appointmentDate] is the day after tomorrow
     */
    private fun isChosenDateValid(appointmentDate: Date): Boolean {
        return appointmentDate.after(Calendar().getTomorrow())
    }

    /**
     * Saves the User's appointment information to the database
     * @return true if User has successfully saved to the database
     */
    private suspend fun saveToDatabase(appointmentDate: Date): Boolean {
        startProgressBar()
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
            DB.createAppointmentTransaction(
                "${appointmentDate.toFormattedString()} - ${User.location}",
                locationId
            )

        // if transaction is successful, add the mobile number to the document
        // and delete previous appointment to avoid duplication
        if (result) {
            if (!querySnapShot.isEmpty) {
                //val querySnapShotId = querySnapShot.first().id
                querySnapShot.forEach {
                    DB.deleteAppointmentTransaction(it.id)
                }

            }
        }
        endProgressBar()
        return result
    }

    private fun startProgressBar() {
        btnSave.visibility = View.GONE
        pgProgressBar.visibility = View.VISIBLE
    }

    private fun endProgressBar() {
        pgProgressBar.visibility = View.GONE
        btnSave.visibility = View.VISIBLE
    }

    /**
     * @return the date chosen by the User in the calendar
     */
    private fun getAppointmentDate(): Date {
        return Date(cvCalendar.date)
    }

    /**
     * Gets the name of the document where User's location is
     * This is checked in the "vaccine centers" collections
     * @return the name of the document where User location is currently set to
     */
    private suspend fun getUserLocationId(): String {
        val locQuery = DB.createEqualToQuery("vaccination centers", "name" to User.location)
        val locationDocs = DB.asyncReadDocumentFromCollection(locQuery)
        var locationId = "none"
        if (!locationDocs.isEmpty)
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

    private fun addOrUpdateCalendar() {
        val query = DB.createEqualToQuery("users", "mobileNumber" to User.mobileNumber)
        DB.readDocumentFromCollection(query) {
            if (!it.isEmpty) {
                val document = it.first();
                if (document.contains("mobileNumber")) {
                    val firstName = document.getString("firstName")
                    val surname = document.getString("surname")
                    val eventId = document.getLong("eventId") ?: -1

                    val title = "Vaccination of $firstName $surname"
                    val location = User.location
                    val startDate = getAppointmentDate().time
                    val endDate = startDate + 1000 //1000 milliseconds after

                    addToCalendar(title, location, startDate, endDate, eventId)
                }
            }
        }
    }

    private fun addToCalendar(title: String, location: String, startDate: Long, endDate: Long, eventId: Long) {
        //if there is a previous event in the calendar, call the delete function
        if (eventId != -1L) {
            deleteCalendarEvent(eventId)
        }

        //add new calendar event
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            putExtra(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
        }

        if (intent.resolveActivity(packageManager) != null) {
            activityResultLauncher.launch(intent)
        }
    }

    private fun deleteCalendarEvent(id: Long) {
        val uri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
        contentResolver.delete(uri, null, null)
    }


    private fun getEventId() : Long {
        val cursor = this.contentResolver.query(CalendarContract.Events.CONTENT_URI, arrayOf("MAX(_id) as max_id"), null, null, "_id")

        if (cursor != null) {
            cursor.moveToFirst()
            return cursor.getLong(cursor.getColumnIndex("max_id"))
        }
        return -1
    }
}