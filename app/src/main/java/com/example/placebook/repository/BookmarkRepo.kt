package com.example.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.placebook.db.BookmarkDao
import com.example.placebook.db.PlaceBookDatabase
import com.example.placebook.model.Bookmark

class BookmarkRepo(context: Context) {

    private val db = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao : BookmarkDao = db.bookMarkDao()


    fun addBookmark(bookmark: Bookmark) : Long?{
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark() : Bookmark{
       return Bookmark()
    }


    val allBookmark : LiveData<List<Bookmark>>
        get() {
        return bookmarkDao.loadAll()       }


    fun getLiveBookmark( bookmarkId : Long) : LiveData<Bookmark> =
        bookmarkDao.loadLiveBookmark(bookmarkId)



    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }
    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }


}