package com.example.placebook.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import com.example.placebook.databinding.ContentBookmarkInfoBinding
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class BookemarkInfoWindowAdapter(val context : Activity) : GoogleMap.InfoWindowAdapter {


    val binding = ContentBookmarkInfoBinding.inflate(context.layoutInflater)


    override fun getInfoWindow(p0: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View? {
        binding.placeTitle.text = marker.title ?: ""
        binding.placeNumber.text = marker.snippet ?: ""
        val imageView = binding.placePhoto

        when(marker.tag){
            is MapsActivity.PlaceHolder -> {
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceHolder).bitmap)
            }

            is MapsViewModel.BookemarkerView ->{
                val bookmarkView = marker.tag as
                        MapsViewModel.BookemarkerView
                imageView.setImageBitmap(bookmarkView.getImage(context))


            }

        }
        return binding.root
    }
}