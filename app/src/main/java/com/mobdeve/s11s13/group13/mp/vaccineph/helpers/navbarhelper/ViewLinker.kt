package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.DB
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.User

object ViewLinker {

    fun linkViewsAndActivities(
        app: Activity,
        views: List<Pair<View, Class<*>>>
    ) {
        val message = Toast.makeText(
            app,
            "Please give us ur data, before using the app :)",
            Toast.LENGTH_SHORT
        )
        for (view in views) {
            linkViewAndActivity(app, view.first, view.second, message)
        }
    }

    private fun linkViewAndActivity(
        app: Activity,
        view: View,
        clazz: Class<*>,
        errorMessage: Toast
    ) {
        view.setOnClickListener {
            // return immediately if we are launching the same activity as the User is currently in
            if (app.javaClass == clazz) return@setOnClickListener

            //start activity only if User is flagged as registered
            if (User.isRegistered) {
                app.startActivity(Intent(app, clazz))
                app.finish()
            } else {
                errorMessage.show()
            }
        }
    }

}