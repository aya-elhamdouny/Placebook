package com.example.placebook.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.placebook.R
import com.example.placebook.adapter.BookemarkInfoWindowAdapter
import com.example.placebook.adapter.BookmarkAdapter
import com.example.placebook.databinding.ActivityMapsBinding
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.common.GooglePlayServicesIncorrectManifestValueException
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
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
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var binding : ActivityMapsBinding
    private lateinit var  bookmarkAdapter: BookmarkAdapter
    private var markers = HashMap<Long , Marker>()

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
         const val EXTRA_BOOKMARK_ID = "BOOKMARK_ID"
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
    }


    private val viewModel by viewModels<MapsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setLocationClient()
        setupToolbar()
        setupNavigationDrawble()
        setupPlacesClient()
    }

    private fun setupToolbar(){
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout ,
            binding.mainMapView.toolbar, R.string.openDrawble,
            R.string.closeDrawble)
        toggle.syncState()
        setSupportActionBar(binding.mainMapView.toolbar)
    }

    private fun setupNavigationDrawble(){
        val layoutManager = LinearLayoutManager(this)
        binding.drawerViewMaps.bookmarkRv.layoutManager = layoutManager
        bookmarkAdapter = BookmarkAdapter(null , this)
        binding.drawerViewMaps.bookmarkRv.adapter = bookmarkAdapter

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
        mMap.setOnMapClickListener {
            newBookmark(it)
        }
        binding.mainMapView.fab.setOnClickListener {
            searchForCurrentlocation()
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
        bookmark.id?.let {
            markers.put(it , marker)
        }
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
                markers.clear()
                it?.let {
                    displayAllBookmarks(it)
                    bookmarkAdapter.setBookmarkData(it)
                }
            })
    }


    private fun startBookDetail(bookmarkId : Long){
        val intent = Intent(this, BookmarkDetail::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID ,bookmarkId )
        startActivity(intent)
    }




    //zoom in specific location
    private fun updateMapToLocation(location : Location){
        val latLng = LatLng(location.latitude , location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , 16.0f))
    }
    fun moveToBookmark(bookmark: MapsViewModel.BookemarkerView){
        binding.drawerLayout.closeDrawer(binding.drawerViewMaps.drwableView)

        val marker = markers[bookmark.id]
        marker?.showInfoWindow()
         val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchForCurrentlocation(){
        val placeFiled = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG
        )

        val bound = RectangularBounds.newInstance(mMap.projection.visibleRegion.latLngBounds)
        try{
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY , placeFiled
            ).setLocationBias(bound)
                .build(this)

            startActivityForResult(intent , AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException){
            Toast.makeText(this, "problems searching" , Toast.LENGTH_LONG).show()
        }catch (e: GooglePlayServicesNotAvailableException) {
            Toast.makeText(this, "Problems Searching. Google Play Not available", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            AUTOCOMPLETE_REQUEST_CODE->
                if (resultCode == Activity.RESULT_OK && data != null){
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?:0.0
                    location.longitude = place.latLng?.longitude ?:0.0
                    updateMapToLocation(location)
                    displayPoiPhotoStep(place)
                }
        }
    }

    private fun newBookmark(latLng: LatLng){
        GlobalScope.launch {
            val bookmarkID = viewModel.addBookmark(latLng)
            bookmarkID?.let {
                startBookDetail(it)
            }
        }
    }

}