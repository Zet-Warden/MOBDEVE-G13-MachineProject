package com.mobdeve.s11s13.group13.mp.vaccineph

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class MapsFragment : Fragment() {


    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "vaccination centers"
    private var lat = 0.0
    private var long = 0.0
    private lateinit var map : GoogleMap




    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        googleMap.setMinZoomPreference(15.0f)
        googleMap.setMaxZoomPreference(20.0f)

        //TODO Implement GetAssignedCenter ( gets which vacc center user is assigned to)

        getAssignedCenter()
        readDbfromCoords("Robinsons Place Manila")






    }

    private fun getAssignedCenter() {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }



    private fun readDbfromCoords(address : String){

        db.collection(collectionName)
            .whereEqualTo("name", address)
            .get()
            .addOnSuccessListener { query ->
                if (query.size() > 0)
                    for (document in query)
                        if (document.contains("location")) {
                            lat = document.getGeoPoint("location")?.latitude!!
                            long = document.getGeoPoint("location")?.longitude!!

                            changecamera()
                        }

            }

    }

    private fun changecamera() {
        map.animateCamera(CameraUpdateFactory.zoomTo(20.0f))
        val coords = LatLng(lat,long)
        map.addMarker(MarkerOptions().position(coords).title("Vaccine Center"))
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    lat,
                    long
                ), 18.0f
            )
        )
    }
}