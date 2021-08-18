package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdeve.s11s13.group13.mp.vaccineph.HomeScreenActivity
import com.mobdeve.s11s13.group13.mp.vaccineph.UserScreenActivity

object ViewLinker {

    fun linkViewsAndActivities(
        app: Activity,
        views: List<Pair<View, Class<*>>>
    ) {
        for (view in views) {
            linkViewAndActivity(app, view.first, view.second)
        }
    }

    private fun linkViewAndActivity(app: Activity, view: View, clazz: Class<*>) {
        view.setOnClickListener {
            val message = Toast.makeText(app, "Please give us ur data, before using the app :)", Toast.LENGTH_SHORT)
            //start activity only if we have user record
            isUserRegistered {
                if(it) {
                    app.startActivity(
                        Intent(app, clazz)
                    )
                    app.finish()
                } else {
                    message.show()
                }
            }
        }
    }

    private fun isUserRegistered(callback : (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance() //instance of database

        //check if user is in the database already
        db.collection("users")
            .whereEqualTo("mobile number", UserData.mobileNumber)
            .get()
            .addOnSuccessListener { query ->
                //user does not exist in the database yet
                var isUserRegistered = false
                for(document in query) {
                    if(document.contains("first name"))
                        isUserRegistered = true;
                }
                callback(isUserRegistered)
            }
            .addOnFailureListener { e ->
                println(e)
            }
    }
}