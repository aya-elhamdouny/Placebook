package com.example.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.service.autofill.Transformation
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.placebook.model.Bookmark
import com.example.placebook.repository.BookmarkRepo
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class MapsViewModel(application: Application) : AndroidViewModel(application) {

   private val TAG = "MapsViewModel"
   private val bookmarkRepo : BookmarkRepo = BookmarkRepo(getApplication())
    private var bookmarks : LiveData<List<BookemarkerView>>? = null

    fun addBookmarkFromPlace(place: Place , image : Bitmap){
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()

        val newId = bookmarkRepo.addBookmark(bookmark)
        image?.let { bookmark.setImage(it, getApplication()) }
        Log.i(TAG, "New bookmark $newId added to the database.")

    }

    private fun bookMarkToMarkerView(bookmark: Bookmark) :BookemarkerView {
     return BookemarkerView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude)
        )
    }

    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepo.allBookmark) { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookMarkToMarkerView(bookmark)
            }
        }
    }

    data class BookemarkerView(
        var id : Long? = null,
        var location : LatLng = LatLng(0.0,0.0)
    )

    fun getBookmarkMarkerViews() :
            LiveData<List<BookemarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

}