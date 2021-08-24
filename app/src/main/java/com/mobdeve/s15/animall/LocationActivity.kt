package com.mobdeve.s15.animall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout file as the content view.
        setContentView(R.layout.activity_location)

        bottomSheet = findViewById(R.id.bottom_sheet_layout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    // Get a handle to the GoogleMap object and display marker.
    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(0.0, 0.0))
                .title("Marker")
        )
    }
}
