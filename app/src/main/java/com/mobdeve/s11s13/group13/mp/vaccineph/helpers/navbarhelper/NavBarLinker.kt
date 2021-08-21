package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper

import android.view.View
import com.mobdeve.s11s13.group13.mp.vaccineph.AppointmentScreenActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.HomeScreenActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.UserScreenActivity

object NavBarLinker {

    fun createNavBarLinkPairs(
        btnHome: View,
        btnProfile: View,
        btnLocation: View,
        btnCalendar: View,
    ): List<Pair<View, Class<*>>> {
        val list = mutableListOf<Pair<View, Class<*>>>()

        list.add(Pair(btnHome, HomeScreenActivity::class.java))
        list.add(Pair(btnProfile, UserScreenActivity::class.java))
        list.add(Pair(btnLocation, UserScreenActivity::class.java))
        list.add(Pair(btnCalendar, AppointmentScreenActivity::class.java))

        return list
    }


}