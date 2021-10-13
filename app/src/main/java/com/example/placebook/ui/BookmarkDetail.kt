package com.example.placebook.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.placebook.R
import com.example.placebook.databinding.ActivityBookmarkDetailBinding
import com.example.placebook.viewmodel.BookmarkDetailViewModel

class BookmarkDetail : AppCompatActivity() {

    private lateinit var binding  : ActivityBookmarkDetailBinding
    private val bookmarkViewmodel by viewModels<BookmarkDetailViewModel>()

    private var bookmarkDetail : BookmarkDetailViewModel.BookemarkerView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bookmark_detail)
        setupToolbar()
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: Menu?) : Boolean {
        menuInflater.inflate(R.menu.menu , menu)
        return true
    }


    private fun setupToolbar(){
        setSupportActionBar(binding.toolbar)
    }


    private fun getIntentData(){
        val bbokId = intent.getLongExtra(
            MapsActivity.Companion.EXTRA_BOOKMARK_ID , 0 )

        bookmarkViewmodel.getBookmark(bbokId)?.observe(this,{
            it?.let {
                bookmarkDetail = it
                binding.bookmarkDetailView = it
                populateImageView()
            }
        })
    }

    private fun populateImageView(){
        bookmarkDetail?.let { bookmarkview ->
            val image = bookmarkview.getImage(this)
            image?.let {
                binding.imageViewPlace.setImageBitmap(image)
            }

        }
    }

    private fun saveChanges() {
        val name = binding.editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetail?.let { bookmarkView ->
            bookmarkView.name = binding.editTextName.text.toString()
            bookmarkView.notes = binding.editTextNotes.text.toString()
            bookmarkView.address = binding.editTextAddress.text.toString()
            bookmarkView.phone = binding.editTextPhone.text.toString()
            bookmarkViewmodel.updateBookmark(bookmarkView)
        }
        finish()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.save_btn -> {
                saveChanges()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}