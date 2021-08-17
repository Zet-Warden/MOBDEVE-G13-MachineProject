package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import android.app.Activity
import android.content.Intent
import android.view.View

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
            app.startActivity(
                Intent(app, clazz)
            )
            app.finish()
        }
    }
}