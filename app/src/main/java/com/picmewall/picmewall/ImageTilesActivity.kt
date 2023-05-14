package com.picmewall.picmewall

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.picmewall.picmewall.MainActivity.Companion.selectedImages
import com.picmewall.picmewall.api.*
import com.picmewall.picmewall.custom_components.CustomAlertDialog
import com.picmewall.picmewall.models.CheckoutResponse
import com.picmewall.picmewall.models.Tile
import com.picmewall.picmewall.models.TilePrice
import com.picmewall.picmewall.ui.ImageTileListAdapter
import com.picmewall.picmewall.ui.TileConfigureBottomModal
import com.picmewall.picmewall.ui.TileDesignAdapter
import com.picmewall.picmewall.utils.*
import kotlinx.android.synthetic.main.activity_image_tiles.*
import kotlinx.android.synthetic.main.activity_image_tiles.main_drawer
import kotlinx.android.synthetic.main.activity_image_tiles.navMenuAboutUs
import kotlinx.android.synthetic.main.activity_image_tiles.navMenuFacebook
import kotlinx.android.synthetic.main.activity_image_tiles.navMenuFaq
import kotlinx.android.synthetic.main.bottom_sheet_about_us.*
import kotlinx.android.synthetic.main.bottom_sheet_adjust_photo.*
import kotlinx.android.synthetic.main.bottom_sheet_adjust_photo.cancel
import kotlinx.android.synthetic.main.bottom_sheet_checkout.*
import kotlinx.android.synthetic.main.bottom_sheet_faq.*
import kotlinx.android.synthetic.main.content_image_tiles.*
import kotlinx.android.synthetic.main.content_image_tiles.drawer_menu_icon
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.list_item_image_tile.view.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

interface TileConfigureMenu {
    fun resizeImage(tile: Tile, position: Int)
    fun onQuantityChange(tile: Tile, position: Int)
    fun onRemoveTile(position: Int)
}

class ImageTilesActivity : AppCompatActivity(), TileConfigureMenu {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var checkoutBottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var aboutUsBottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var faqBottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private var tempTile: Tile? = null
    private var tempPosition: Int = -1
    private lateinit var imageTileAdapter: ImageTileListAdapter
    private lateinit var tileDesignAdapter: TileDesignAdapter
    private var unitPrice = 0
    var deliveryCharge = 0
    private var totalCost = 0
    private var totalBKashCharge = 0
    private var referenceNumber: String? = null
    private var invoiceID: String? = null
    private var grandTotalPrice = 0
    private var totalTilePrice = 0
    private var tilesCount = 0

    private val checkoutResponse: MutableLiveData<CheckoutResponse> = MutableLiveData()
    private val tileInfoResponse: MutableLiveData<TilePrice> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_tiles)

        InitialBitmapImageLoaderAsyncTask().execute()
        updateStatusBarBackgroundColor("#00FFFFFF")

        tileInfoResponse.observe(this) { tilePrice ->
            tilePrice?.let { validPrice ->
                try {
                    validPrice.tile_price?.let {
                        unitPrice = it.toInt()
                    }
                    validPrice.delivery_cost?.let {
                        deliveryCharge = it.toInt()
                    }
                    validPrice.bkashno?.let {
                        bkashNumber.text = it
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        backButton.setOnClickListener {
            selectImagesForTiles()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = false

        checkoutBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetCheckout)
        checkoutBottomSheetBehavior.isDraggable = false

        aboutUsBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetAboutUs)
        aboutUsBottomSheetBehavior.isDraggable = false

        faqBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetFaq)
        faqBottomSheetBehavior.isDraggable = false

        closeAboutUs.setOnClickListener {
            aboutUsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        closeFaq.setOnClickListener {
            faqBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        navMenuFaq.setOnClickListener {
            closeDrawer()
            faqBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        navMenuAboutUs.setOnClickListener {
            closeDrawer()
            aboutUsBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        imageTileAdapter = ImageTileListAdapter { tile, position ->
            val tileConfigureBottomModal = TileConfigureBottomModal(tile, position)
            tileConfigureBottomModal.show(supportFragmentManager, "#Tile_Configure_Bottom_Modal")
        }
        tileRecycler.adapter = imageTileAdapter

        tileDesignAdapter = TileDesignAdapter { design ->
            imageTileAdapter.changeTileDesign(design)
        }
        tileDesignRecycler.adapter = tileDesignAdapter

        rotateLeft.setOnClickListener {
            cropImageView.rotateImage(-90)
        }

        rotateRight.setOnClickListener {
            cropImageView.rotateImage(90)
        }

        cancel.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        cancelCheckout.setOnClickListener {
            checkoutBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioBkash -> {
                    bkashChecked()
                }

                R.id.radioCash -> {
                    cashOnChecked()
                }
            }
        }

//        checkoutBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                when(newState) {
//
//                }
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//            }
//        })

        checkout.setOnClickListener {
            val tileList = imageTileAdapter.getTotalItemList()
            if (tileList.size <= 0) {
                Toast.makeText(this@ImageTilesActivity, "Please prepare at least 3 pictures!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            var hasLowQuality = false
            tileList.forEach {
                if (it.height < 800 && it.width < 800) {
                    hasLowQuality = true
                }
            }

            if (hasLowQuality) {
                val alertDialog = CustomAlertDialog(onYesCallback = {
                    proceedToCheckout()
                }, onNoCallback = {

                }, "Attention Please!",
                    "Some image has low resolution, want to proceed next?",
                    "Yes", "No")
                alertDialog.isCancelable = false
                alertDialog.show(supportFragmentManager, "#LOW_QUALITY_WARNING_DIALOG")
            } else {
                proceedToCheckout()
            }
        }

        apply.setOnClickListener {
            if (tempTile != null && tempPosition != -1) {
                val croppedBitmap = cropImageView.croppedImage
                try {
                    showTileProgress()
                    val resizeImagePath = tempTile?.resizedImagePath ?: ""
                    val file = File(resizeImagePath)
                    if (file.exists()) file.delete()
                    ImageUtils.saveBitmapFileIntoExternalStorageWithTitle(croppedBitmap, file)
                } catch (e: IOException) {
                    Toast.makeText(this@ImageTilesActivity, "Operation failed! please try again", Toast.LENGTH_LONG).show()
                    hideTileProgress()
                    return@setOnClickListener
                }
                tempTile?.height = croppedBitmap.height
                tempTile?.width = croppedBitmap.width
                imageTileAdapter.changeItemAt(tempTile!!, tempPosition)
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            tempTile = null
            tempPosition = -1
            hideTileProgress()
        }

        val tileDesigns = ArrayList<Int>()
        tileDesigns.add(R.drawable.bold_frame_tile)
        tileDesigns.add(R.drawable.edge_frame_tile)
        tileDesignAdapter.submitList(tileDesigns)

        drawer_menu_icon.setOnClickListener {
            toggleDrawer()
        }

        navMenuFacebook.setOnClickListener {
            closeDrawer()
            goToFacebook("Picmewall")
        }

        navMenuInstagram.setOnClickListener {
            closeDrawer()
            goToInstagram("https://www.instagram.com/picmewall/")
        }

        checkoutResponse.observe(this) {
            //checkoutBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            CoroutineScope(Dispatchers.IO).launch {
                deleteAllPreviousCreatedTiles()
            }
            AppGlobalValues.selectedImages.clear()
            Toast.makeText(this, "Order submitted successfully!", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        getTilePrice()

        districtField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    deliveryCharge = if (s.toString().toLowerCase(Locale.ROOT) == "dhaka") {
                        tileInfoResponse.value?.delivery_cost?.toInt() ?: 0
                    } else {
                        tileInfoResponse.value?.other_delivery_cost?.toInt() ?: 0
                    }

                    if (radioBkash.isChecked)
                        bkashChecked()
                    else
                        cashOnChecked()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        orderPlaceButton.setOnClickListener {
            if (nameField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter your name!", Toast.LENGTH_LONG).show()
                nameField.requestFocus()
                return@setOnClickListener
            }

            if (mobileField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter mobile number!", Toast.LENGTH_LONG).show()
                mobileField.requestFocus()
                return@setOnClickListener
            }

            val mobileNumber = mobileField.text.toString()
            if (!mobileNumber.startsWith("01") && mobileNumber.length != 11) {
                Toast.makeText(this, "Please enter valid mobile number!", Toast.LENGTH_LONG).show()
                mobileField.requestFocus()
                return@setOnClickListener
            }

            if (addressField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter your address!", Toast.LENGTH_LONG).show()
                addressField.requestFocus()
                return@setOnClickListener
            }

            if (districtField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter your district name!", Toast.LENGTH_LONG).show()
                districtField.requestFocus()
                return@setOnClickListener
            }

            if (thanaField.text.isNullOrBlank()) {
                Toast.makeText(this, "Please enter your thana name!", Toast.LENGTH_LONG).show()
                thanaField.requestFocus()
                return@setOnClickListener
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                Date()
            )

            hideKeyboard()

            val reference = if (!radioBkash.isChecked) "" else referenceNumber ?: generateReference()

            if (grandTotalPrice <= 0 || deliveryCharge <= 0) {
                Toast.makeText(this, "Something wrong! Please try again later.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            placeOrder(this, thanaField.text.toString(), districtField.text.toString(),
                nameField.text.toString(), mobileField.text.toString(), invoiceID ?: generateInvoiceID(),
                reference, addressField.text.toString(), today, grandTotalPrice.toString())
        }
        showTileProgress()
    }

    private fun goToInstagram(url: String) {
        try {
            val applicationInfo = packageManager.getApplicationInfo("com.instagram.android", 0)
            if (applicationInfo.enabled) {
                val uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.instagram.android")
                startActivity(intent)
            } else {
                throw PackageManager.NameNotFoundException()
            }
        } catch (exception: Exception) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun selectImagesForTiles() {
        UwMediaPicker
            .with(this)
            .setGalleryMode(UwMediaPicker.GalleryMode.ImageGallery)
            .setGridColumnCount(3)
            .setLightStatusBar(true)
            .launch { selectedMediaList->
                selectedMediaList?.let { imageList ->
                    if (imageList.isEmpty()) return@launch
                    selectedImages = imageList as ArrayList<UwMediaPickerMediaModel?>
                    if (selectedImages.size > 0 ) {
                        BitmapImageLoaderAsyncTask().execute()
                    }
                }
            }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class InitialBitmapImageLoaderAsyncTask: AsyncTask<String?, Int?, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean {
            if (!ImageUtils.makeEmptyFolderIntoExternalStorageWithTitle(this@ImageTilesActivity,
                    AppGlobalValues.TILES_FOLDER_NAME
                ))
                return false
            selectedImages.forEachIndexed { _, image ->
                //val uri = ImageUtils.getUriFromFilePath(image.mediaPath)
                val bitmap = ImageUtils.getBitmapFromFilePath(image.mediaPath)
                val resizedBitmap = bitmap?.centerCrop() ?: return@forEachIndexed
                val height = bitmap.height
                val width = bitmap.width
                val file = ImageUtils.makeEmptyFileIntoExternalStorageWithTitle(this@ImageTilesActivity,
                    AppGlobalValues.TILES_FOLDER_NAME, ImageUtils.createFileName(".jpg"))
                if (file.exists()) file.delete()

                try {
                    ImageUtils.saveBitmapFileIntoExternalStorageWithTitle(resizedBitmap, file)
                } catch (e: IOException) {
                    return@forEachIndexed
                }

                AppGlobalValues.selectedImages.add(0, Tile(ImageTileListAdapter.tileUniqueID++, R.drawable.bold_frame, file.path, height, width, 1, image.mediaPath))
            }
            return true
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showTileProgress()
        }

        override fun onCancelled() {
            super.onCancelled()
            showTileProgress()
        }

        override fun onPostExecute(isSuccessful: Boolean) {
            super.onPostExecute(isSuccessful)

            if (isSuccessful) {
                selectedImages.clear()
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    imageTileAdapter.submitList(AppGlobalValues.selectedImages)
                    hideTileProgress()
                }
            } else {
                Toast.makeText(this@ImageTilesActivity, "Operation failed! please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BitmapImageLoaderAsyncTask: AsyncTask<String?, Int?, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean {
            selectedImages.forEachIndexed { _, image ->
                //val uri = ImageUtils.getUriFromFilePath(image.mediaPath)
                val bitmap = ImageUtils.getBitmapFromFilePath(image.mediaPath)
                val resizedBitmap = bitmap?.centerCrop() ?: return@forEachIndexed
                val height = bitmap.height
                val width = bitmap.width
                val file = ImageUtils.makeEmptyFileIntoExternalStorageWithTitle(this@ImageTilesActivity,
                    AppGlobalValues.TILES_FOLDER_NAME, ImageUtils.createFileName(".jpg"))
                if (file.exists()) file.delete()

                try {
                    ImageUtils.saveBitmapFileIntoExternalStorageWithTitle(resizedBitmap, file)
                } catch (e: IOException) {
                    return@forEachIndexed
                }

                AppGlobalValues.selectedImages.add(0, Tile(ImageTileListAdapter.tileUniqueID++, R.drawable.bold_frame, file.path, height, width, 1, image?.mediaPath!!))
            }
            return true
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showTileProgress()
        }

        override fun onCancelled() {
            super.onCancelled()
            showTileProgress()
        }

        override fun onPostExecute(isSuccessful: Boolean) {
            super.onPostExecute(isSuccessful)

            if (isSuccessful) {
                selectedImages.clear()
                CoroutineScope(Dispatchers.Main.immediate).launch {
                    imageTileAdapter.submitList(AppGlobalValues.selectedImages)
                    hideTileProgress()
                }
            } else {
                Toast.makeText(this@ImageTilesActivity, "Operation failed! please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun proceedToCheckout() {
        tilesCount = imageTileAdapter.getTotalItemCount()

        if (tilesCount < 3) {
            Toast.makeText(this, "Please prepare at least 3 pictures!", Toast.LENGTH_LONG).show()
            return
        }

        if (radioBkash.isChecked)
            bkashChecked()
        else
            cashOnChecked()

        invoiceID = generateInvoiceID()

        checkoutBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun toggleDrawer() {
        if (main_drawer.isDrawerOpen(GravityCompat.END)) {
            main_drawer.closeDrawer(GravityCompat.END)
        } else main_drawer.openDrawer(GravityCompat.END)
    }

    private fun closeDrawer() {
        if (main_drawer.isDrawerOpen(GravityCompat.END)) {
            main_drawer.closeDrawer(GravityCompat.END)
        }
    }

    private fun goToFacebook(pageName: String) {
        try {
            val applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/n/?$pageName"))
                // Verify the intent will resolve to at least one activity
                if (webIntent.resolveActivity(packageManager) != null) {
                    startActivity(webIntent)
                }
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/n/?$pageName"))
                val chooser = Intent.createChooser(webIntent, "View Facebook Page Using")
                // Verify the intent will resolve to at least one activity
                if (webIntent.resolveActivity(packageManager) != null) {
                    startActivity(chooser)
                }
            }
        } catch (exception: PackageManager.NameNotFoundException) {
            exception.printStackTrace()
        }
    }

    private fun generateInvoiceID(): String {
        val random1 = "${1 + SecureRandom().nextInt(9999)}"
        val random2 = "${1 + SecureRandom().nextInt(9999)}"
        return "${random1}${random2}"
    }

    private fun generateReference(): String {
        val random = "${1 + SecureRandom().nextInt(99999)}"
        referenceNumber = random
        return random
    }

    private fun setTotalPrice() {
        totalTiles.text = tilesCount.toString()

        totalTilePrice = unitPrice * tilesCount
        val vTotalPrice = "$tilesCount X $unitPrice৳ = $totalTilePrice৳"
        totalPrice.text = vTotalPrice

        val vDeliveryFee = "$deliveryCharge৳"
        deliveryFee.text = vDeliveryFee

        totalCost = totalTilePrice + deliveryCharge
    }

    private fun bkashChecked() {
        setTotalPrice()
        val temp = referenceNumber ?: generateReference()
        val temp1 = "Please use this code '$temp' as payment reference"
        reference.text = temp1
        bkashChargeLinear.visibility = View.VISIBLE
        linear1.visibility = View.VISIBLE
        val bkashRatio = 20.0 / 1000.0
        totalBKashCharge = (bkashRatio * totalCost).toInt()
        bkashCharge.text = "${totalBKashCharge}৳"

        grandTotalPrice = totalCost + totalBKashCharge
        val vTotal = "Total Price:  ${grandTotalPrice}৳"
        total.text = vTotal
    }

    private fun cashOnChecked() {
        setTotalPrice()
        bkashChargeLinear.visibility = View.GONE
        linear1.visibility = View.GONE
        totalBKashCharge = 0

        grandTotalPrice = totalCost + totalBKashCharge
        val vTotal = "Total Price:  ${grandTotalPrice}৳"
        total.text = vTotal
    }

    override fun onBackPressed() {
        when {
            bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            checkoutBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> checkoutBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            faqBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> faqBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            aboutUsBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> aboutUsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            main_drawer.isDrawerOpen(GravityCompat.END) -> main_drawer.closeDrawer(GravityCompat.END)
            else -> super.onBackPressed()
        }
    }

    override fun resizeImage(tile: Tile, position: Int) {
        showTileProgress()
        tempTile = tile
        tempPosition = position
        Glide.with(this)
            .asBitmap()
            .load(tile.imagePath)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    cropImageView.setImageBitmap(resource)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    hideTileProgress()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
//        val originalBitmap = ImageUtils.getBitmapFromUri(this.contentResolver, tile.image.contentUri)
//        originalBitmap?.let {
////            val resizedBitmap = ImageUtils.getResizedBitmap(originalBitmap, 800)
////            cropImageView.setImageBitmap(resizedBitmap)
//            cropImageView.setImageBitmap(it)
//        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onQuantityChange(tile: Tile, position: Int) {
        imageTileAdapter.changeItemAt(tile, position)
    }

    override fun onRemoveTile(position: Int) {
        if (position >= 0)
            imageTileAdapter.removeItemAt(position)
        else
            Toast.makeText(this, "No image found to select!", Toast.LENGTH_LONG).show()
    }

    private fun updateStatusBarBackgroundColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                window.statusBarColor = Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTilePrice() {
        if (!checkNetworkStatus(this))
            return
        //showTileProgress()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("http://backend.picmewall.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofitBuilder.create(ApiService::class.java)

        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            //hideTileProgress()
        }

        CoroutineScope(Dispatchers.IO).launch(handler) {

            when (val apiResponse = ApiResponse.create(
                apiService.getTileInfo()
            )) {
                is ApiSuccessResponse -> {
                    tileInfoResponse.postValue(apiResponse.body.price)
                    //hideTileProgress()
                }
                is ApiEmptyResponse -> {
                    //hideTileProgress()
                }
                is ApiErrorResponse -> {
                    //hideTileProgress()
                }
            }
        }
    }

    private fun placeOrder(context: Context, upazila: String, city: String, customerName: String,
                           mobile: String, invoiceID: String, payemtReferenceID: String,
                           address: String, date: String, grand_total: String) {

        if (!checkNetworkStatus(context))
            return
        showNetworkProgress()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("http://backend.picmewall.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofitBuilder.create(ApiService::class.java)
        val tileList = imageTileAdapter.getTotalItemList()
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
            addFormDataPart("upazila", upazila)
            addFormDataPart("city", city)
            addFormDataPart("customerName", customerName)
            addFormDataPart("mobile", mobile)
            addFormDataPart("invoiceID", invoiceID)
            addFormDataPart("payemtReferenceID", payemtReferenceID)
            addFormDataPart("address", address)
            addFormDataPart("date", date)
            addFormDataPart("grand_total", grand_total)
            tileList.forEachIndexed { index, tile ->
                val posterior = if (tile.frame == R.drawable.bold_frame) "bold" else "edge"
                val imageFile = File(tile.resizedImagePath ?: "")
                val fileRequestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull()) ?: return@forEachIndexed
                addFormDataPart("files[]", "${index}_$posterior.jpg", fileRequestBody)
//                val fileRequestBody = tile.bitmap?.toFile(context, posterior)?.asRequestBody("multipart/form-data".toMediaTypeOrNull()) ?: return@forEachIndexed
//                addFormDataPart("files[]", "$index.jpg", fileRequestBody)
            }
        }.build()

//        val files = ArrayList<MultipartBody.Part>()
//
//        tileList.forEachIndexed { index, tile ->
//            val filePart = tile.bitmap?.toFile(context)?.toFilePart("files${index + 1}") ?: return@forEachIndexed
//            files.add(filePart)
//        }

//        val handler = CoroutineExceptionHandler { _, exception ->
//            exception.printStackTrace()
//            //Toast.makeText(context, "Please check your internet connection!", Toast.LENGTH_LONG).show()
//        }

//        CoroutineScope(Dispatchers.IO).launch(handler) {
//
//            when (val apiResponse = ApiResponse.create(apiService.requestOrder(files,
//                upazila.toRequestBody("text/plain".toMediaTypeOrNull()),
//                city.toRequestBody("text/plain".toMediaTypeOrNull()),
//                customerName.toRequestBody("text/plain".toMediaTypeOrNull()),
//                mobile.toRequestBody("text/plain".toMediaTypeOrNull()),
//                invoiceID.toRequestBody("text/plain".toMediaTypeOrNull()),
//                payemtReferenceID.toRequestBody("text/plain".toMediaTypeOrNull()),
//                address.toRequestBody("text/plain".toMediaTypeOrNull()),
//                date.toRequestBody("text/plain".toMediaTypeOrNull()),
//                grand_total.toRequestBody("text/plain".toMediaTypeOrNull())))) {
//                is ApiSuccessResponse -> {
//                    checkoutResponse.postValue(apiResponse.body)
//                    //Toast.makeText(context, "Successfully order submitted!", Toast.LENGTH_LONG).show()
//                }
//                is ApiEmptyResponse -> {
//                    Log.e("CHECKOUT", "Order placement is not successful!")
//                    //Toast.makeText(context, "Order placement is not successful!", Toast.LENGTH_LONG).show()
//                }
//                is ApiErrorResponse -> {
//                    Log.e("CHECKOUT", "Order placement failed! please try again later.")
//                    //Toast.makeText(context, "Order placement failed! please try again later.", Toast.LENGTH_LONG).show()
//                }
//            }
//        }


//        val map: HashMap<String, RequestBody> = HashMap()
//        map.put("upazila", upazila.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("city", city.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("customerName", customerName.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("mobile", mobile.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("invoiceID", invoiceID.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("payemtReferenceID", payemtReferenceID.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("address", address.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("date", date.toRequestBody("text/plain".toMediaTypeOrNull()))
//        map.put("grand_total", grand_total.toRequestBody("text/plain".toMediaTypeOrNull()))

        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            hideNetworkProgress()
            //Toast.makeText(context, "Please check your internet connection!", Toast.LENGTH_LONG).show()
        }

        CoroutineScope(Dispatchers.IO).launch(handler) {

            when (val apiResponse = ApiResponse.create(
                apiService.requestOrder(requestBody)
            )) {
                is ApiSuccessResponse -> {
                    checkoutResponse.postValue(apiResponse.body)
                    hideNetworkProgress()
                    //Toast.makeText(context, "Successfully order submitted!", Toast.LENGTH_LONG).show()
                }
                is ApiEmptyResponse -> {
                    hideNetworkProgress()
                    //Toast.makeText(context, "Order placement is not successful!", Toast.LENGTH_LONG).show()
                }
                is ApiErrorResponse -> {
                    hideNetworkProgress()
                    //Toast.makeText(context, "Order placement failed! please try again later.", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun checkNetworkStatus(context: Context) = if (NetworkUtils.isNetworkConnected(context)) {
        true
    } else {
        Toast.makeText(context, "Please check your internet connection!", Toast.LENGTH_LONG).show()
        false
    }

    private fun showTileProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            tileProgress.visibility = View.VISIBLE
        }
    }

    private fun hideTileProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            tileProgress.visibility = View.GONE
        }
    }

    private fun showNetworkProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            networkProgress.visibility = View.VISIBLE
        }
    }

    private fun hideNetworkProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            networkProgress.visibility = View.GONE
        }
    }

    private suspend fun deleteAllPreviousCreatedTiles() {
        withContext(Dispatchers.IO) {
            ImageUtils.deleteFolderWithAllFilesFromExternalStorage(this@ImageTilesActivity,
                AppGlobalValues.TILES_FOLDER_NAME
            )
        }
    }
}