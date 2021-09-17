package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import android.content.Context
import android.net.ConnectivityManager

object NetworkChecker {
    fun isNetworkAvailable(ct: Context): Boolean {
        val connectivityManager =
            ct.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}