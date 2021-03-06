package com.mobdeve.s11s13.group13.mp.vaccineph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.DB
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.User
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.activity_home_screen.btnCalendar
import kotlinx.android.synthetic.main.activity_home_screen.btnHome
import kotlinx.android.synthetic.main.activity_home_screen.btnLocation
import kotlinx.android.synthetic.main.activity_home_screen.btnProfile
import kotlinx.android.synthetic.main.activity_home_screen.clMainContainer
import kotlinx.android.synthetic.main.activity_maps.*

class MapsScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        init();
    }
    /**
     * Initialize components of the activity
     */
    private fun init() {
        UIHider(this, clMainContainer)
        ViewLinker.linkViewsAndActivities(
            this,
            NavBarLinker.createNavBarLinkPairs(btnHome, btnProfile, btnLocation, btnCalendar)
        )

        tvLocationName.visibility = View.GONE
        val query =
            DB.createArrayContainsQuery("appointments", "mobileNumbers" to User.mobileNumber)

        DB.readDocumentFromCollection(query){
            //no user appointment
            var vaccineCenter : String?
            if(it.isEmpty) {
                //since user has no appointment, display the assigned user center
                val query = DB.createEqualToQuery("users","mobileNumber" to User.mobileNumber)
                DB.readDocumentFromCollection(query) { otherIt ->
                    if (otherIt.first().contains("assignedCenter")){
                        vaccineCenter = otherIt.first().getString("assignedCenter")
                        tvLocationName.text = vaccineCenter
                        tvLocationName.visibility = View.VISIBLE
                    }
                }
            } else {
                //since user has an appointment already set, display the center registered in that appointment
                //the assigned center gets updated when the user reschedules their appointment
                vaccineCenter = it.first().getString("location")
                tvLocationName.text = vaccineCenter
                tvLocationName.visibility = View.VISIBLE
            }

        }
    }
}