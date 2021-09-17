package com.mobdeve.s11s13.group13.mp.vaccineph.helpers
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import java.util.Locale;


class GeoCodingLocation {
    private val TAG = "GeoCodeLocation"


    fun getAddressFromLocation(locationAddress : String,
                               context: Context,
                               handler: Handler)
    {
        val thread = object : Thread()
        {
            override fun run() {
                val geocoder = Geocoder(context)

                var result : DoubleArray? = null

                println("Present: " +Geocoder.isPresent())
                try {
                    val addressList = geocoder.getFromLocationName(locationAddress, 1)
                    if (addressList != null && addressList.size > 0) {
                        val address = addressList[0] as Address

                        var coords = DoubleArray(2)
                        coords[0] = address.latitude
                        coords[1] = address.longitude
                        result = coords
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Unable to connect to GeoCoder", e)
                } finally {
                    val message = Message.obtain()
                    message.target = handler
                    message.what = 1
                    val bundle = Bundle()
//                    result = ("Address: $locationAddress" +
//                            "\n\nLatitude and Longitude: \n" + result)
                    bundle.putDoubleArray("address", result)

                    message.data = bundle
                    message.sendToTarget()
                }
            }
        }
        thread.start()
    }
}