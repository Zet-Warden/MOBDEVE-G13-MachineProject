package com.mobdeve.s11s13.group13.mp.vaccineph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.UIHider
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.NavBarLinker
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper.ViewLinker
import kotlinx.android.synthetic.main.activity_home_screen.*

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
    }
}