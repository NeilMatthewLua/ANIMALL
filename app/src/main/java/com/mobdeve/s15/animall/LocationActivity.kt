package com.mobdeve.s15.animall

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior


class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var bottomSheet: ConstraintLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var currentLocationButton: ImageButton
    private lateinit var locationSearchActv: AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var confirmLocationBtn: Button

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var gcd: Geocoder

    private var currentLocation: LatLng = LatLng(20.5, 78.9)
    private var address: String = "Manila, Metro Manila"
    private lateinit var cities: Array<String>

    companion object {
        const val PERMISSION_ID = 42

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout file as the content view.
        setContentView(R.layout.activity_location)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        bottomSheet = findViewById(R.id.bottom_sheet_layout)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        confirmLocationBtn = findViewById(R.id.confirmLocationBtn)

        // Get a handle to the fragment and register the callback.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        // Initializing fused location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Adding functionality to the button
        currentLocationButton = findViewById(R.id.currentLocationBtn)
        currentLocationButton.setOnClickListener {
            getLastLocation()
        }

        gcd = Geocoder(this)

        cities = CityDataHelper.initializeCityData()
        adapter = ArrayAdapter(this, android.R.layout.select_dialog_item, cities)

        locationSearchActv = findViewById(R.id.locationSearchActv)
        locationSearchActv.threshold = 1
        locationSearchActv.setAdapter(adapter)

        locationSearchActv.setOnItemClickListener { parent, _, position, _ ->
            address = adapter.getItem(position).toString()

            val list = gcd.getFromLocationName(address, 1)
            if (list.size > 0) {
                currentLocation = LatLng(list[0].latitude, list[0].longitude)
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(currentLocation))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))
            }

            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(parent.applicationWindowToken, 0)
        }

        confirmLocationBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.location_dialog_title)
            val str = "Your selected location is $address.\nPlease confirm your selection."
            builder.setMessage(str)
            builder.setIcon(android.R.drawable.ic_dialog_alert)

            builder.setPositiveButton("Confirm"){ _, _ ->
                val returnIntent = Intent()
                returnIntent.putExtra("PREF_LOC", address)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
            builder.setNeutralButton("Cancel"){ _, _ -> }

            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }

    // Get a handle to the GoogleMap object and display marker.
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(currentLocation))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))

            setAddressString(currentLocation)
        }

        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(14.5995, 120.9842))
                .title("Selected Location")
        )
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(14.5995, 120.9842), 16F))
        locationSearchActv.setText(address)
    }

    private fun setAddressString (currentLocation: LatLng) {
        val list = gcd.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)
        if (list.size > 0) {
            address = when {
                list[0].adminArea == "Metro Manila" -> {
                    "${list[0].locality}, ${list[0].adminArea}"
                }
                list[0].countryName == "Philippines" -> {
                    "${list[0].locality}, ${list[0].subAdminArea}"
                }
                else -> {
                    "Manila, Metro Manila"
                }
            }

            if (list[0].countryName != "Philippines") {
                Toast.makeText(this, "This service is only available for users in the Philippines.", Toast.LENGTH_SHORT).show()
            }

            locationSearchActv.setText(address, false)
        }
    }

    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.clear()
                        mMap.addMarker(MarkerOptions().position(currentLocation))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16F))

                        setAddressString(currentLocation)
                    }
                }
            } else {
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // Get current location, if shifted
    // from previous location
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    // If current location could not be located, use last location
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            currentLocation = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            setAddressString(currentLocation)
        }
    }

    // function to check if GPS is on
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    // Check if location permissions are
    // granted to the application
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    // Request permissions if not granted before
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }

    // What must happen when permission is granted
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }
}
