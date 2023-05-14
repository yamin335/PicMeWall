package com.picmewall.picmewall

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.picmewall.picmewall.databinding.ImageSelectionActivityBinding
import com.picmewall.picmewall.models.ImageSelection
import com.picmewall.picmewall.models.MediaStoreImage
import com.picmewall.picmewall.models.Tile
import com.picmewall.picmewall.ui.AllImageListAdapter
import com.picmewall.picmewall.utils.ImageUtils
import com.picmewall.picmewall.utils.centerCrop
import kotlinx.android.synthetic.main.activity_image_selection.*
import kotlinx.android.synthetic.main.bottom_sheet_checkout.*
import kotlinx.android.synthetic.main.content_image_selection.*
import kotlinx.android.synthetic.main.content_image_selection.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class ImageSelectionActivity : AppCompatActivity() {
    private val viewModel: ImageSelectionViewModel by viewModels()
    private lateinit var binding: ImageSelectionActivityBinding
    private val goToImageTileScreen: MutableLiveData<Boolean> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateStatusBarBackgroundColor("#00FFFFFF")

        goToImageTileScreen.observe(this, {
            it?.let {
                if (it) {
                    hideProgress()
                    startActivity(Intent(this, ImageTilesActivity::class.java))
                    goToImageTileScreen.postValue(null)
                }
            }
        })

        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_selection)
        binding.contentImageSelection.imageSelectionCount.text = "${viewModel.selectedImages.size} tiles selected"

//        viewModel.selectedImages.observe(this, {
//            it?.let { selectedItems ->
//
//            }
//        })

//        val galleryAdapter = GalleryAdapter { image ->
//            Toast.makeText(this, "Selected: ${image.displayName} -- ${image.id}", Toast.LENGTH_LONG).show()
//        }

        binding.contentImageSelection.backButton.setOnClickListener {
            finish()
        }
        val allImageListAdapter = AllImageListAdapter(binding.root, viewModel) {
            binding.contentImageSelection.imageSelectionCount.text = "${viewModel.selectedImages.size} tiles selected"
        }

        binding.contentImageSelection.gallery.also { view ->
            view.layoutManager = GridLayoutManager(this, 3)
            view.adapter = allImageListAdapter
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
            allImageListAdapter.submitList(finalList)
        })

        binding.contentImageSelection.nextButton.setOnClickListener {
            if (viewModel.selectedImages.size > 0 ) {
                showProgress()
                CoroutineScope(Dispatchers.Default).launch {
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
                    sortedList.forEach { image ->
                        val originalBitmap = ImageUtils.getBitmapFromUri(this@ImageSelectionActivity.contentResolver, image.image.contentUri)
                        originalBitmap?.let { bitmap ->
//                val resizedBitmap = ImageUtils.getResizedBitmap(bitmap, 800)?.centerCrop()
                            val resizedBitmap = bitmap.centerCrop()
                            //tiles.add(Tile(image.id, R.drawable.bold_frame, image.image, resizedBitmap, 1, 10.0))
                        }
                    }

                    AppGlobalValues.selectedImages = tiles
                    goToImageTileScreen.postValue(true)
                }
            } else {
                Toast.makeText(this, "Please select at least one image!", Toast.LENGTH_LONG).show()
            }
        }

        showImages()

        drawer_menu_icon.setOnClickListener {
            toggleDrawer()
        }

        navMenuFacebook.setOnClickListener {
            closeDrawer()
            goToFacebook("mixtiles")
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

    private fun showImages() {
        viewModel.loadImages()
    }

    fun updateStatusBarBackgroundColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                window.statusBarColor = Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * A [ListAdapter] for [MediaStoreImage]s.
     */
    private inner class GalleryAdapter(val onClick: (MediaStoreImage) -> Unit) :
        ListAdapter<MediaStoreImage, ImageViewHolder>(MediaStoreImage.DiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.gallery_layout, parent, false)
            return ImageViewHolder(view, onClick)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            //val mediaStoreImage = getItem(position)

            bind(holder, position)

            //holder.rootView.tag = mediaStoreImage

//            Glide.with(holder.imageView)
//                .load(mediaStoreImage.contentUri)
//                .thumbnail(0.33f)
//                .centerCrop()
//                .into(holder.imageView)
        }

        fun bind(holder: ImageViewHolder, position: Int) {
            val item = getItem(position)

            Glide.with(holder.imageView)
                .load(item.contentUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(holder.imageView)

//            holder.imageView.setOnClickListener {
//                //val image = rootView.tag as? MediaStoreImage ?: return@setOnClickListener
//                item.isChecked = !item.isChecked
//                holder.frame.visibility = if (item.isChecked) View.VISIBLE else View.GONE
//                onClick(item)
//            }
        }
    }

    private fun showProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.contentImageSelection.progressView.visibility = View.VISIBLE
        }
    }

    private fun hideProgress() {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            binding.contentImageSelection.progressView.visibility = View.GONE
        }
    }
}

/**
 * Basic [RecyclerView.ViewHolder] for our gallery.
 */
private class ImageViewHolder(view: View, onClick: (MediaStoreImage) -> Unit) :
    RecyclerView.ViewHolder(view) {
    val rootView = view
    val imageView: ImageView = view.findViewById(R.id.image)
    val frame: ConstraintLayout = view.findViewById(R.id.frame)

//    init {
//        imageView.setOnClickListener {
//            val image = rootView.tag as? MediaStoreImage ?: return@setOnClickListener
//            image.isChecked = !image.isChecked
//            frame.visibility = if (image.isChecked) View.VISIBLE else View.GONE
//            onClick(image)
//        }
//    }
}