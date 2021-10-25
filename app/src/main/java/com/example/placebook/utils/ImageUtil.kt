package com.example.placebook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtil {


    fun saveBitmapToFile(context: Context, bitmap: Bitmap,
                         filename: String) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        saveBytesToFile(context, bytes, filename)
    }
    private fun saveBytesToFile(context: Context, bytes:
    ByteArray, filename: String) {
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(filename,
                Context.MODE_PRIVATE)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadBitmapFromFile(context: Context, filename: String):
            Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        return BitmapFactory.decodeFile(filePath)
    }


    fun createUniqueImageName(context: Context) : File {
        val timeStamp = SimpleDateFormat("yyyyMMDDHHmmss").format(Date())
        val fiename = "PlaceBook_" + timeStamp +"_"
        val direction = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fiename , ".jpg" , direction)
    }






}