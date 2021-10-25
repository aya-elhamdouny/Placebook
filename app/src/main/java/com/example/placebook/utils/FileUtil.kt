package com.example.placebook.utils

import android.content.Context
import java.io.File

object FileUtil {


    fun deleteFile(context: Context, filename: String) {
        val dir = context.filesDir
        val file = File(dir, filename)
        file.delete()
    }
}