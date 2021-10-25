package com.example.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.example.placebook.R
import com.example.placebook.databinding.ActivityBookmarkDetailBinding
import com.example.placebook.utils.ImageUtil
import com.example.placebook.viewmodel.BookmarkDetailViewModel
import java.io.File

class BookmarkDetail : AppCompatActivity() , PhotoOptionDialogFragment.photoOptionDialogListener{

    private lateinit var binding  : ActivityBookmarkDetailBinding
    private val bookmarkViewmodel by viewModels<BookmarkDetailViewModel>()
    private var bookmarkDetail : BookmarkDetailViewModel.BookemarkerView? = null
    private var photoFile  :File? = null

    companion object{
        private const val REQUEST_PHOTO_CAPTURE = 1
    }


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
        binding.imageViewPlace.setOnClickListener {
            replaceImage()
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

    override fun onCaptureClick() {
        Toast.makeText(this, "Camera Capture",
            Toast.LENGTH_SHORT).show()


        photoFile =null
        try {
            photoFile = ImageUtil.createUniqueImageName(this)
                    }catch (ex : java.io.IOException){
                        return
                    }
        photoFile?.let {
            val photouri = FileProvider.getUriForFile(this,
            "com.example.placebook.fileprovider", it)

            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT , photouri)


            val intentActivity = packageManager.queryIntentActivities(
                captureIntent , PackageManager.MATCH_DEFAULT_ONLY)

            intentActivity.map { it.activityInfo.packageName }
                .forEach{
                    grantUriPermission(it , photouri , Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
            startActivityForResult(captureIntent, REQUEST_PHOTO_CAPTURE)

        }

   
    }

    override fun onPickClick() {
        Toast.makeText(this, "Gallery Pick",
            Toast.LENGTH_SHORT).show()
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }


    private fun updateImage(image: Bitmap) {
        bookmarkDetail?.let {
            binding.imageViewPlace.setImageBitmap(image)
            it.setImage(this, image)
        }
    }
    private fun getImageWithPath(filePath: String) =
        ImageUtil.decodeFileToSize(
            filePath,
            resources.getDimensionPixelSize(R.dimen.default_image_width),
            resources.getDimensionPixelSize(R.dimen.default_image_hight)
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PHOTO_CAPTURE -> {

                    val photoFile = photoFile ?: return
                    val uri = FileProvider.getUriForFile(this,
                        "com.example.placebook.fileprovider",
                        photoFile)
                    revokeUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                    val image = getImageWithPath(photoFile.absolutePath)
                    val bitmap = ImageUtil.rotateImageIfRequired(this,
                        image , uri)
                    updateImage(bitmap)
                }
            }
        }
    }


}