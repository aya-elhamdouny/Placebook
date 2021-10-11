package com.example.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.example.placebook.databinding.ContentBookmarkInfoBinding
import com.example.placebook.ui.MapsActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookemarkInfoWindowAdapter(context : Activity) : GoogleMap.InfoWindowAdapter {


    val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)


    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        binding.placeTitle.text = marker.title ?: ""
        binding.placeNumber.text = marker.snippet ?: ""
        val imageView = binding.placePhoto
        imageView.setImageBitmap((marker.tag as MapsActivity.PlaceHolder).bitmap)
        // imageView.setImageBitmap((marker.tag as Bitmap))
        return binding.root
    }
}