package com.picmewall.picmewall.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.picmewall.picmewall.R
import com.picmewall.picmewall.models.Tile
import kotlinx.android.synthetic.main.list_item_selected_image.view.*
import kotlin.collections.ArrayList

class SelectedImageListAdapter: RecyclerView.Adapter<SelectedImageListAdapter.ItemViewHolder>() {

    private var imageList: ArrayList<Tile> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_selected_image, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun submitList(imageList: ArrayList<Tile>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    fun getImageList(): ArrayList<Tile> {
        return imageList
    }

    inner class ItemViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = imageList[position]

            val height = item.height ?: 0
            val width = item.width ?: 0

            if (height < 800 && width < 800) {
                itemView.tvLowQuality.visibility = View.VISIBLE
            } else {
                itemView.tvLowQuality.visibility = View.GONE
            }

//            Glide.with(itemView.rootView.context)
//                .load(item.bitmap)
//                .thumbnail(0.33f)
//                .centerCrop()
//                .into(itemView.selectedImage)

            itemView.removeImage.setOnClickListener {
                imageList.remove(item)
                notifyItemRemoved(position)
            }
        }
    }
}
