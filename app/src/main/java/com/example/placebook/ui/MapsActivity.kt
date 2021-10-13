package com.example.placebook.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.example.placebook.R
import com.example.placebook.adapter.BookemarkInfoWindowAdapter
import com.example.placebook.model.Bookmark
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private lateinit var placesClient: PlacesClient

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
         const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
    }


    private val viewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setLocationClient()
        setupPlacesClient()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getCurrentLocation()
        setUpMapsListener()
        creatBookmarkerMarkerObserver()
    }

    private fun setUpMapsListener() {
        mMap.setInfoWindowAdapter(BookemarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener {
            //Toast.makeText(this , it.name , Toast.LENGTH_SHORT).show()
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClicked(it)
        }


    }

    private fun setLocationClient() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location Permission denied")
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latlong = LatLng(location.latitude, location.longitude)

                    val update = CameraUpdateFactory.newLatLngZoom(latlong, 16.0f)
                    mMap.moveCamera(update)
                } else {
                    Log.e(TAG, "no location found")
                }

            }


        }


    }


    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displatPoiGetPlaceStep(pointOfInterest)


    }

    private fun displatPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId

        val placeField = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        val request = FetchPlaceRequest.builder(placeId, placeField).build()


        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            Log.e(TAG, "${response.place.name}")
            displayPoiPhotoStep(response.place)
            /* Toast.makeText(this, "${response.place.name}, " +
                     "${response.place.phoneNumber}", Toast.LENGTH_LONG).show()*/
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val code = exception.statusCode
                Log.e(
                    TAG, "Place not found: " +
                            exception.message + ", " +
                            "statusCode: " + code
                )

            }
        }


    }


    private fun displayPoiPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas

        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata[0])
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_hight))
            .build()

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->

            val bitmap = fetchPhotoResponse.bitmap
            displayPoiDisplayStep(place, bitmap)


        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(
                    TAG,
                    "photo not found : " + "${exception.message}" + ", " + "statusCode" + "${statusCode}"
                )


            }
        }
    }


    class PlaceHolder(val place: Place? = null, val bitmap: Bitmap? = null)

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(place.latLng as LatLng)
                .title(place.name)
                .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceHolder(place, photo)
        marker?.showInfoWindow()


    }

    private fun handleInfoWindowClicked(marker: Marker) {
        when(marker.tag){
            is PlaceHolder ->{
                val placeHolder = (marker.tag as PlaceHolder)
                if (placeHolder.place != null && placeHolder.bitmap != null) {

                    GlobalScope.launch {
                        placeHolder.bitmap?.let {
                            viewModel.addBookmarkFromPlace(placeHolder.place, it)
                        }
                    }

                    marker.remove()
                }
            }

         is MapsViewModel.BookemarkerView ->{
             val bookmarkview = (marker.tag as MapsViewModel.BookemarkerView)
             marker.hideInfoWindow()
             bookmarkview.id?.let {
                 startBookDetail(it)
             }
         }

        }






    }


    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookemarkerView
    ): Marker? {
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE
                    )
                )
                .alpha(0.8f)
        )
        marker.tag = bookmark
        return marker
    }


    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookemarkerView>
    ) {
        bookmarks.forEach { addPlaceMarker(it) }
    }

    private fun creatBookmarkerMarkerObserver() {
        viewModel.getBookmarkMarkerViews()?.observe(
            this,
            {
                mMap.clear()
                it?.let {
                    displayAllBookmarks(it)
                }
            })
    }


    private fun startBookDetail(bookmarkId : Long){
        val intent = Intent(this, BookmarkDetail::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID ,bookmarkId )
        startActivity(intent)
    }


}