package com.picmewall.picmewall.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.picmewall.picmewall.ImageSelectionViewModel
import com.picmewall.picmewall.R
import com.picmewall.picmewall.models.ImageSelection
import kotlinx.android.synthetic.main.gallery_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllImageListAdapter internal constructor(
    private val rootView: View,
    private val viewModel: ImageSelectionViewModel,
    private val onItemClickListener: (ImageSelection) -> Unit
) : RecyclerView.Adapter<AllImageListAdapter.ItemViewHolder>() {

    private var imageList: ArrayList<ImageSelection> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun submitList(imageList: ArrayList<ImageSelection>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    inner class ItemViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = imageList[position]

            itemView.frame.visibility = if (viewModel.selectedImages.containsKey(item.id)) View.VISIBLE else View.GONE

            Glide.with(itemView.rootView.context)
                .load(item.image.contentUri)
                .thumbnail(0.33f)
                .centerCrop()
                .into(itemView.image)

            itemView.image.setOnClickListener {
                var height = 0
                var width = 0
                try {
                    height = item.image.height?.toInt() ?: 0
                    width = item.image.width?.toInt() ?: 0
                } catch (e: java.lang.Exception) {

                }

                if (height < 800 && width < 800) {
                    if (viewModel.selectedImages.containsKey(item.id))
                        viewModel.selectedImages.remove(item.id)
                    else {
                        val snackBar = Snackbar.make(rootView, "Low resolution image, want to select?", Snackbar.LENGTH_LONG)
                        snackBar.setAction("Yes") {
                            val temp = item
                            val selectionTime: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(
                                Date()
                            )
                            temp.date = selectionTime
                            viewModel.selectedImages[temp.id] = temp
                            itemView.frame.visibility = if (viewModel.selectedImages.containsKey(item.id)) View.VISIBLE else View.GONE
                            onItemClickListener(item)
                        }
                        snackBar.setActionTextColor(Color.YELLOW)
                        snackBar.show()
                    }
                } else {
                    if (viewModel.selectedImages.containsKey(item.id))
                        viewModel.selectedImages.remove(item.id)
                    else {
                        val temp = item
                        val selectionTime: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(
                            Date()
                        )
                        temp.date = selectionTime
                        viewModel.selectedImages[temp.id] = temp
                    }
                }
                itemView.frame.visibility = if (viewModel.selectedImages.containsKey(item.id)) View.VISIBLE else View.GONE
                onItemClickListener(item)
            }

//            itemView.packageName.text = item.packServiceName
//            itemView.packagePrice.text = "${item.packServicePrice?.toRounded(2)} BDT"
//            itemView.setOnClickListener {
//                itemView.check.visibility = View.VISIBLE
//                if (checkedPosition != adapterPosition) {
//                    notifyItemChanged(checkedPosition)
//                    checkedPosition = adapterPosition
//                }
//                listener.onItemClicked(packageList[adapterPosition])
//                notifyDataSetChanged()
//            }
        }
    }
}
