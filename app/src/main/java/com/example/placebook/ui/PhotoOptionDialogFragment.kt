package com.example.placebook.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment
import dagger.BindsInstance
import java.util.ArrayList

class PhotoOptionDialogFragment : DialogFragment() {


    interface photoOptionDialogListener{
        fun onCaptureClick()
        fun onPickClick()
    }

    private lateinit var photoListener : photoOptionDialogListener

    override fun onCreateDialog(savedInstance: Bundle?) : Dialog{
        photoListener = activity as photoOptionDialogListener
        var captureSelectIdx = -1
        var pickSelectedIdx = -1
        val  option = ArrayList<String>()
        val context = activity as Context

        if(canCapture(context)){
            option.add("Camera")
            captureSelectIdx =0
        }

        if(canPick(context)){
            option.add("Gallery")
             pickSelectedIdx = if(captureSelectIdx ==0 ) 1 else 0
        }
        return AlertDialog.Builder(context)
            .setTitle("photo option")
            .setItems(option.toTypedArray<CharSequence>()){
                _, which ->
                if (which == captureSelectIdx){
                    photoListener.onCaptureClick()
                } else if (which == pickSelectedIdx){
                    photoListener.onPickClick()
                }
            }
            .setNegativeButton("cancel" , null)
            .create()

    }



    companion object{
        private fun canPick(context: Context): Boolean {
            val pickIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            return (pickIntent.resolveActivity(
                context.packageManager) != null)

        }

        private fun canCapture(context: Context): Boolean {
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE)
            return (captureIntent.resolveActivity(
                context.packageManager) != null)
        }


        fun newInstance(context: Context) =
            if (canPick(context) || canCapture(context)) {
                PhotoOptionDialogFragment()
            } else {
                null
            }
    }

}