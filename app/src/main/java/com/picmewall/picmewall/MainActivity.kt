package com.picmewall.picmewall

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.anilokcun.uwmediapicker.UwMediaPicker
import com.anilokcun.uwmediapicker.model.UwMediaPickerMediaModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.picmewall.picmewall.api.ApiEmptyResponse
import com.picmewall.picmewall.api.ApiErrorResponse
import com.picmewall.picmewall.api.ApiResponse
import com.picmewall.picmewall.api.ApiService
import com.picmewall.picmewall.api.ApiSuccessResponse
import com.picmewall.picmewall.dao.UserDao
import com.picmewall.picmewall.models.TilePrice
import com.picmewall.picmewall.models.User
import com.picmewall.picmewall.utils.NetworkUtils
import com.picmewall.picmewall.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_about_us.*
import kotlinx.android.synthetic.main.bottom_sheet_faq.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.list_item_slider.view.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Inject

const val PERMISSION_REQUEST_READ_WRITE_STORAGE = 0x1045

// Generates an individual Hilt component for each Android class
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var indicatorAdapter: SliderIndicatorAdapter
    private lateinit var sliderPageChangeCallback: SliderPageChangeCallback

    @Inject lateinit var userDao: UserDao

    val user: MutableLiveData<User> = MutableLiveData()
    private val tileInfoResponse: MutableLiveData<TilePrice?> = MutableLiveData()

    private lateinit var aboutUsBottomSheetBehavior: BottomSheetBehavior<RelativeLayout>
    private lateinit var faqBottomSheetBehavior: BottomSheetBehavior<RelativeLayout>

    companion object {
        var selectedImages = ArrayList<UwMediaPickerMediaModel?>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateStatusBarBackgroundColor("#00FFFFFF")

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

        tileInfoResponse.observe(this) { tilePrice ->
            tilePrice?.let { validPrice ->
                validPrice.tile_price?.let {
                    val price = it.toDouble()/100
                    framePrice.text = "Each tile only $price$"
                }
            }
        }

        sliderPageChangeCallback = SliderPageChangeCallback {
            indicatorAdapter.setIndicatorAt(it)
        }

        slider_view.apply {
            // Set offscreen page limit to at least 1, so adjacent pages are always laid out
            offscreenPageLimit = 1
            val recyclerView = getChildAt(0) as RecyclerView
            recyclerView.apply {
                val padding = resources.getDimensionPixelOffset(R.dimen.halfPageMargin) +
                        resources.getDimensionPixelOffset(R.dimen.peekOffset)
                // setting padding on inner RecyclerView puts overscroll effect in the right place
                setPadding(padding, 0, padding, 0)
                clipToPadding = false
            }
            adapter = Adapter()
            registerOnPageChangeCallback(sliderPageChangeCallback)
        }

        indicatorAdapter = SliderIndicatorAdapter(8) { itemPosition ->
            slider_view.currentItem = itemPosition
        }
        indicatorView.adapter = indicatorAdapter
        indicatorAdapter.setIndicatorAt(slider_view.currentItem)

        letsGo.setOnClickListener {
            if (PermissionUtils.checkPermission(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                )) {
                selectImagesForTiles()
                //startActivity(Intent(this, ImageSelectionActivity2::class.java))
            } else {
                PermissionUtils.requestPermission(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    supportFragmentManager,
                    PERMISSION_REQUEST_READ_WRITE_STORAGE
                )
            }
        }

        user.observe(this) {
            val tt = it
        }

        lifecycleScope.launch(Dispatchers.Main.immediate) {
            userDao.getAllUsers().collect {
                it.forEach { client ->
                    print("User -> ${client.name}")
                    user.postValue(client)
                }
            }
        }

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
        getTilePrice()
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
                    selectedImages = imageList as ArrayList<UwMediaPickerMediaModel>
                    if (selectedImages.size > 0 ) {
                        startActivity(Intent(this@MainActivity, ImageTilesActivity::class.java))
                    } else {
                        Toast.makeText(this, "Please select at least one image!", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun getTilePrice() {
        if (!checkNetworkStatus(this))
            return
        showProgress()
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("http://backend.picmewall.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofitBuilder.create(ApiService::class.java)

        val handler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            hideProgress()
        }

        CoroutineScope(Dispatchers.IO).launch(handler) {

            when (val apiResponse = ApiResponse.create(
                apiService.getTileInfo()
            )) {
                is ApiSuccessResponse -> {
                    tileInfoResponse.postValue(apiResponse.body.price)
                    hideProgress()
                }
                is ApiEmptyResponse -> {
                    hideProgress()
                }
                is ApiErrorResponse -> {
                    hideProgress()
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

    private fun showProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            progressLoader.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            progressLoader.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        when {
            faqBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> faqBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            aboutUsBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED -> aboutUsBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            main_drawer.isDrawerOpen(GravityCompat.END) -> main_drawer.closeDrawer(GravityCompat.END)
            else -> {
                super.onBackPressed()
                finishAffinity()
                finish()
            }
        }
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
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/n/?$pageName")
                )
                // Verify the intent will resolve to at least one activity
                if (webIntent.resolveActivity(packageManager) != null) {
                    startActivity(webIntent)
                }
            } else {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/n/?$pageName")
                )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_WRITE_STORAGE) {
            // Check permission result
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start next operation.
//                startActivity(Intent(this, ImageSelectionActivity2::class.java))
                selectImagesForTiles()
            } else {
                // Permission request was denied.
                if (PermissionUtils.checkPermissionRationale(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    )) {
                    PermissionUtils.requestPermission(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        supportFragmentManager,
                        PERMISSION_REQUEST_READ_WRITE_STORAGE
                    )
                } else {
                    PermissionUtils.goToSettings(this, packageName)
                }
            }
        }
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.list_item_slider, parent, false)
    )

    class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val sliderImages = arrayOf(
            R.drawable.slide_1,
            R.drawable.slide_2,
            R.drawable.slide_3,
            R.drawable.slide_4,
            R.drawable.slide_5,
            R.drawable.slide_6,
            R.drawable.slide_7,
            R.drawable.slide_8
        )

        override fun getItemCount(): Int {
            return 8
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ViewHolder(parent)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            holder.itemView.tag = position
            holder.itemView.sliderImage.setImageResource(sliderImages[position])
        }
    }

    class SliderPageChangeCallback(private val listener: (Int) -> Unit) : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            listener.invoke(position)
        }
    }

    private fun updateStatusBarBackgroundColor(color: String) {
        try {
            window.statusBarColor = Color.parseColor(color)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}