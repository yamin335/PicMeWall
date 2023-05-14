package com.picmewall.picmewall.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

// For hiding keyboard in Fragment
fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

// For hiding keyboard in Activity
fun Activity.hideKeyboard() {
    if (currentFocus == null) {
        hideKeyboard(View(this))
    } else {
        hideKeyboard(currentFocus as View)
    }
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
}

fun AppCompatActivity.checkSelfPermissionCompat(permission: String) =
    ActivityCompat.checkSelfPermission(this, permission)

fun AppCompatActivity.shouldShowRequestPermissionRationaleCompat(permission: String) =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermissionsCompat(permissionsArray: Array<String>, requestCode: Int) =
    ActivityCompat.requestPermissions(this, permissionsArray, requestCode)

fun Bitmap.centerCrop(): Bitmap {
    if (this.width >= this.height) {
        return Bitmap.createBitmap(
            this,
            this.width /2 - this.height /2,
            0,
            this.height,
            this.height
        )
    } else {
        return Bitmap.createBitmap(
            this,
            0,
            this.height /2 - this.width /2,
            this.width,
            this.width
        )
    }
}

fun Bitmap.toFile(context: Context, posterior: String): File {
    //create a file to write bitmap customerMenu
    val f = File(context.cacheDir, "${UUID.randomUUID()}_$posterior.jpg")
    f.createNewFile()

    //Convert bitmap to byte array
    val os = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, os)
    val bitmapdata = os.toByteArray()

    //write the bytes in file
    val fos = FileOutputStream(f)
    fos.write(bitmapdata)
    fos.flush()
    fos.close()
    return f
}

fun File.toFilePart(name: String): MultipartBody.Part {
    val attachedFile = this.asRequestBody("multipart/form-data".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(name, this.name, attachedFile)
}

fun Double.toRounded(digit: Int): Double {
    return BigDecimal(this).setScale(digit, RoundingMode.HALF_UP).toDouble()
}