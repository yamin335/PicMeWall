package com.picmewall.picmewall.utils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object ImageUtils {
    fun getUriFromFilePath(filepath: String): Uri {
        return if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.N) {
            Uri.parse(filepath)
        } else {
            Uri.fromFile(File(filepath))
        }
    }

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } catch (e: IOException) {
            null
        }
    }

    // Use maxSize 500 to get medium quality image with faster conversion
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getBitmapFromFilePath(path: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val file = File(path)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(file), null, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    @Suppress("unused")
    @Throws(Exception::class)
    fun saveBitmapFileIntoExternalStorageWithTitle(
        bitmap: Bitmap,
        file: File
    ) {
        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: IOException) {
            throw e
        }
    }

    fun deleteFolderWithAllFilesFromExternalStorage(applicationContext: Context, folderName: String): Boolean {
        //If your app is used on a device that runs Android 4.3 (API level 18) or lower,
        // then the array contains just one element,
        // which represents the primary external storage volume
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(
            applicationContext,
            null
        )
        val primaryExternalStorage = externalStorageVolumes[0]
        //path = "$primaryExternalStorage/$realDocId"

        //val root: String = Environment.getExternalStorageDirectory().getAbsolutePath()
        val folder = File(primaryExternalStorage.absolutePath, folderName)
        if (folder.exists()) {
            if (!folder.deleteRecursively())
                return false
        }
        return true
    }

    fun makeEmptyFolderIntoExternalStorageWithTitle(applicationContext: Context, folderName: String): Boolean {
        //If your app is used on a device that runs Android 4.3 (API level 18) or lower,
        // then the array contains just one element,
        // which represents the primary external storage volume
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(
            applicationContext,
            null
        )
        val primaryExternalStorage = externalStorageVolumes[0]
        //path = "$primaryExternalStorage/$realDocId"

        //val root: String = Environment.getExternalStorageDirectory().getAbsolutePath()
        val folder = File(primaryExternalStorage.absolutePath, folderName)
        if (!folder.exists()) {
            if (!folder.mkdir())
                return false
        }
        return true
    }

    fun makeEmptyFileIntoExternalStorageWithTitle(applicationContext: Context, folderName: String, title: String): File {
        //If your app is used on a device that runs Android 4.3 (API level 18) or lower,
        // then the array contains just one element,
        // which represents the primary external storage volume
        val externalStorageVolumes: Array<out File> = ContextCompat.getExternalFilesDirs(
            applicationContext,
            null
        )
        val primaryExternalStorage = externalStorageVolumes[0]
        //path = "$primaryExternalStorage/$realDocId"

        //val root: String = Environment.getExternalStorageDirectory().getAbsolutePath()
        return File("${primaryExternalStorage.absolutePath}/$folderName", title)
    }

    fun createFileName(extension: String, name: String = ""): String {
        return if(name.isEmpty()) {
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + extension
        } else {
            name + extension
        }
    }
}