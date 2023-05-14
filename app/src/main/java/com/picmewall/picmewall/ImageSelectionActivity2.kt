package com.picmewall.picmewall

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.picmewall.picmewall.databinding.ImageSelectionActivityBinding2
import com.picmewall.picmewall.models.ImageSelection
import com.picmewall.picmewall.models.MediaStoreImage
import com.picmewall.picmewall.models.Tile
import com.picmewall.picmewall.ui.SelectedImageListAdapter
import kotlinx.android.synthetic.main.activity_image_selection2.*
import kotlinx.android.synthetic.main.bottom_sheet_checkout.*
import kotlinx.android.synthetic.main.content_image_selection2.*
import kotlinx.android.synthetic.main.content_image_selection2.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


const val OPEN_MEDIA_PICKER = 1;  // Request code
class ImageSelectionActivity2 : AppCompatActivity() {
    private val viewModel: ImageSelectionViewModel by viewModels()
    private lateinit var binding: ImageSelectionActivityBinding2
    private lateinit var selectedImageListAdapter: SelectedImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateStatusBarBackgroundColor("#00FFFFFF")

        selectedImageListAdapter = SelectedImageListAdapter()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_selection2)
        binding.contentImageSelection2.imageSelectionCount.text = "${viewModel.selectedImages.size} tiles selected"

        binding.contentImageSelection2.backButton.setOnClickListener {
            finish()
        }

        binding.contentImageSelection2.gallery.also { view ->
            view.layoutManager = GridLayoutManager(this, 3)
            view.adapter = selectedImageListAdapter
        }

        viewModel.images.observe(this, { images ->
            val sortedList = images as ArrayList<MediaStoreImage>
            sortedList.sortWith { firstImage, secondImage ->
                secondImage.dateAdded.compareTo(
                    firstImage.dateAdded
                )
            }

            val finalList = ArrayList<ImageSelection>()
            sortedList.forEach {
                finalList.add(ImageSelection(it.id, "", it))
            }
            selectedImageListAdapter.submitList(ArrayList())
        })

        binding.contentImageSelection2.nextButton.setOnClickListener {
            if (viewModel.selectedImages.size > 0 ) {
                BitmapImageLoaderAsyncTask().execute()
            } else {
                Toast.makeText(this, "Please select at least one image!", Toast.LENGTH_LONG).show()
            }
        }

        binding.contentImageSelection2.btnAddImage.setOnClickListener {

        }

        drawer_menu_icon.setOnClickListener {
            toggleDrawer()
        }

        navMenuFacebook.setOnClickListener {
            closeDrawer()
            goToFacebook("mixtiles")
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class BitmapImageLoaderAsyncTask: AsyncTask<String?, Int?, Boolean>() {

        override fun doInBackground(vararg params: String?): Boolean {
            AppGlobalValues.selectedImages.clear()
            val sortedList = ArrayList<ImageSelection>()
            viewModel.selectedImages.keys.forEach { key ->
                sortedList.add(viewModel.selectedImages[key]!!)
            }

            sortedList.sortWith { firstImage, secondImage ->
                secondImage.date.compareTo(
                    firstImage.date
                )
            }

            val tiles = ArrayList<Tile>()
//            sortedList.forEach { image ->
//                val originalBitmap = ImageUtils.getBitmapFromUri(
//                    this@ImageSelectionActivity2.contentResolver,
//                    image.image.contentUri
//                )
//                originalBitmap?.let { bitmap ->
////                val resizedBitmap = ImageUtils.getResizedBitmap(bitmap, 800)?.centerCrop()
//                    val resizedBitmap = bitmap.centerCrop()
//                    tiles.add(
//                        Tile(
//                            image.id,
//                            R.drawable.bold_frame,
//                            image.image,
//                            resizedBitmap,
//                            1,
//                            10.0
//                        )
//                    )
//                }
//            }

            AppGlobalValues.selectedImages = tiles
            return true
        }

        override fun onCancelled() {
            super.onCancelled()
            hideProgress()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgress()
        }

        override fun onPostExecute(isSuccessful: Boolean) {
            super.onPostExecute(isSuccessful)
            hideProgress()
            startActivity(Intent(this@ImageSelectionActivity2, ImageTilesActivity::class.java))
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

    private fun updateStatusBarBackgroundColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                window.statusBarColor = Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.contentImageSelection2.progressView.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.contentImageSelection2.progressView.visibility = View.GONE
        }
    }
}