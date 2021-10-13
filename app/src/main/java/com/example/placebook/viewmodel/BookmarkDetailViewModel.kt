package com.example.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.location.Address
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.placebook.model.Bookmark
import com.example.placebook.repository.BookmarkRepo
import com.example.placebook.utils.ImageUtil
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val bookmarkrepo = BookmarkRepo(getApplication())

    private var bookmarkdetail  : LiveData<BookemarkerView> ? =null


    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkrepo.getLiveBookmark(bookmarkId)
        bookmarkdetail = Transformations.map(bookmark)
        { repoBookmark ->
            bookMarkToMarkerView(repoBookmark)
        }
    }

    fun getBookmark(bookmarkId: Long):
            LiveData<BookemarkerView>? {
        if (bookmarkdetail == null) {
            mapBookmarkToBookmarkView(bookmarkId)
        }
        return bookmarkdetail
    }


    private fun bookMarkToMarkerView(bookmark: Bookmark) : BookemarkerView {
        return BookemarkerView(
            bookmark.id,
            bookmark.name,
            bookmark.phone,
            bookmark.address,
            bookmark.notes
        )
    }

    data class BookemarkerView(
        var id : Long? = null,
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var notes : String =""
    ) {
        fun getImage(context: Context) = id?.let {
            ImageUtil.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
    }
    private fun bookmarkViewToBookmark(bookmarkView :  BookemarkerView): Bookmark? {
        val bookmark = bookmarkView.id?.let {
            bookmarkrepo.getBookmark(it)
        }
        if (bookmark != null) {
            bookmark.id = bookmarkView.id
            bookmark.name = bookmarkView.name
            bookmark.phone = bookmarkView.phone
            bookmark.address = bookmarkView.address
            bookmark.notes = bookmarkView.notes
        }
        return bookmark
    }


    fun updateBookmark(bookmarkView: BookemarkerView) {
        GlobalScope.launch {
            val bookmark = bookmarkViewToBookmark(bookmarkView)
            bookmark?.let { bookmarkrepo.updateBookmark(it) }
        }
    }

}