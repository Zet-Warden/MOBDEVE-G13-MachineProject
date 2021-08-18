package com.mobdeve.s11s13.group13.mp.vaccineph

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory


import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.firebase.firestore.FirebaseFirestore


class MapsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "vaccination centers"
    private var lat = 15.6037
    private var long = 120.9737



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

//        readDbfromCoords("2401 Taft Ave, Malate, Manila, 1004 Metro Manila")
        //Get handle and register callback


    }


}