package com.example.placebook

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import java.lang.Exception
import java.util.jar.Manifest

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationRequest : LocationRequest? = null
    private lateinit var placesClient: PlacesClient
    companion object{
         private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setLocationClient()
       // setupPlacesClient()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getCurrentLocation()
        mMap.setOnPoiClickListener {
            //Toast.makeText(this , it.name , Toast.LENGTH_SHORT).show()
            //displayPoi(it)

        }


        /*// Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

    private fun setLocationClient(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermission(){
        ActivityCompat.requestPermissions(this ,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION) ,
             REQUEST_LOCATION)

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION){
            if(grantResults.size ==1  && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }else{
                Log.e(TAG , "Location Permission denied")
            }
        }
    }

    private fun getCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED){
            requestLocationPermission()
        }else{
        /*    if(locationRequest ==null){
                 locationRequest  = LocationRequest.create()
                locationRequest?.let{ locationRequest ->
                    locationRequest.priority=
                        LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 5000
                    locationRequest.fastestInterval = 1000

                    val locationCallback = object:  LocationCallback(){
                        override fun onLocationResult(p0: LocationResult) {
                            getCurrentLocation()
                        }
                    }

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest , locationCallback , null)
                }
            }*/
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if(location != null){
                    val latlong = LatLng(location.latitude , location.longitude)

                    val update = CameraUpdateFactory.newLatLngZoom(latlong , 16.0f)
                    mMap.moveCamera(update)
                }else{
                    Log.e(TAG , "no location found")
                }

            }


        }


    }




}