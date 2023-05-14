package com.picmewall.picmewall.ui

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.picmewall.picmewall.R
import com.picmewall.picmewall.models.Tile
import kotlinx.android.synthetic.main.list_item_image_tile.view.*


class ImageTileListAdapter internal constructor(
    private val onItemClickListener: (Tile, Int) -> Unit
) : RecyclerView.Adapter<ImageTileListAdapter.ItemViewHolder>() {

    companion object {
        var tileUniqueID = 1
    }

    private var imageList: ArrayList<Tile> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_image_tile,
            parent,
            false
        )
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun getTotalItemList(): ArrayList<Tile> {
        return imageList
    }

    fun submitList(imageList: ArrayList<Tile>) {
        this.imageList = imageList
        notifyDataSetChanged()
    }

    fun changeItemAt(tile: Tile, position: Int) {
        this.imageList[position] = tile
        notifyItemChanged(position)
    }

    fun removeItemAt(position: Int) {
        this.imageList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getTotalItemCount(): Int {
        var totalItem = 0
        this.imageList.forEach { tile ->
            totalItem += tile.quantity
        }
        return totalItem
    }

    fun changeTileDesign(design: Int) {
        val newFrame = if (design == R.drawable.bold_frame_tile) R.drawable.bold_frame else R.drawable.edge_frame
        for (i in 0 until this.imageList.size) {
            this.imageList[i].frame = newFrame
        }
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = imageList[position]

            itemView.quantity.text = item.quantity.toString()

            if (item.quantity > 1) {
                itemView.quantity.visibility = View.VISIBLE
            } else {
                itemView.quantity.visibility = View.GONE
            }

            val params: ViewGroup.MarginLayoutParams = itemView.image.layoutParams as ViewGroup.MarginLayoutParams
            if (item.frame == R.drawable.edge_frame) {
                params.topMargin = dpToPx(0)
                params.leftMargin = dpToPx(0)
                params.bottomMargin = dpToPx(13)
                params.rightMargin = dpToPx(5)
            } else {
                params.topMargin = dpToPx(6)
                params.leftMargin = dpToPx(6)
                params.bottomMargin = dpToPx(18)
                params.rightMargin = dpToPx(11)
            }

            itemView.frame.setImageResource(item.frame)

//            Picasso.get()
//                .load("file://" + item.resizedImagePath)
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
//                .into(itemView.image)

            Glide.with(itemView.context)
                .asBitmap()
                .load(item.resizedImagePath)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(itemView.image)
//            if (item.bitmap == null) {
//                Glide.with(itemView.context)
//                    .asBitmap()
//                    .load(item.imagePath)
//                    .apply(RequestOptions().centerCrop())
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(
//                            resource: Bitmap,
//                            transition: Transition<in Bitmap>?
//                        ) {
//                            item.bitmap = resource
//                            item.height = resource.height
//                            item.width = resource.width
//                            itemView.image.setImageBitmap(item.bitmap)
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {
//                        }
//                    })
//            } else {
//                itemView.image.setImageBitmap(item.bitmap)
//            }

            itemView.image.setOnClickListener {
                onItemClickListener(item, adapterPosition)
            }

            itemView.adjustPhotoButton.setOnClickListener {
                onItemClickListener(item, adapterPosition)
            }
        }

        private fun dpToPx(valueInDp: Int): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp.toFloat(), itemView.resources.displayMetrics).toInt()
    }
}
