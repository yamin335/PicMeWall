package com.picmewall.picmewall.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.picmewall.picmewall.R
import kotlinx.android.synthetic.main.list_item_tile_design.view.*

class TileDesignAdapter internal constructor(
     private val callback: (Int) -> Unit
) : RecyclerView.Adapter<TileDesignAdapter.ViewHolder>() {

    private var tileDesignList: ArrayList<Int> = ArrayList()

    private var checkedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileDesignAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tile_design, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: TileDesignAdapter.ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return tileDesignList.size
    }

    fun submitList(designList: ArrayList<Int>) {
        this.tileDesignList = designList
        notifyDataSetChanged()
    }

    inner class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val item = tileDesignList[position]
            when (checkedPosition) {
                -1 -> {
                    itemView.frame.visibility = View.INVISIBLE
                    itemView.designTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
                }
                adapterPosition -> {
                    itemView.frame.visibility = View.VISIBLE
                    itemView.designTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                }
                else -> {
                    itemView.frame.visibility = View.INVISIBLE
                    itemView.designTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorAccent))
                }
            }
            itemView.tile.setImageResource(item)
            itemView.designTitle.text = if (item == R.drawable.bold_frame_tile) "BOLD" else "EDGE"

            itemView.setOnClickListener {
                itemView.frame.visibility = View.VISIBLE
                if (checkedPosition != adapterPosition) {
                    notifyItemChanged(checkedPosition)
                    checkedPosition = adapterPosition
                }
                callback(tileDesignList[adapterPosition])
                notifyDataSetChanged()
            }
        }
    }
}
