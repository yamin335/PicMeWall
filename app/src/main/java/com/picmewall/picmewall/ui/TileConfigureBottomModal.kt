package com.picmewall.picmewall.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.picmewall.picmewall.R
import com.picmewall.picmewall.TileConfigureMenu
import com.picmewall.picmewall.models.Tile
import kotlinx.android.synthetic.main.tile_configure_bottom_modal.*

class TileConfigureBottomModal constructor(private val tile: Tile, private val position: Int) : BottomSheetDialogFragment() {

    private var callback: TileConfigureMenu? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TileConfigureMenu) {
            callback = context
        } else {
            throw RuntimeException("$context must implement TileConfigureMenu")
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.tile_configure_bottom_modal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        quantity.text = tile.quantity.toString()

        adjustPhoto.setOnClickListener {
            dismiss()
            callback?.resizeImage(tile, position)
        }

        plus.setOnClickListener {
            tile.quantity++
            callback?.onQuantityChange(tile, position)
            quantity.text = tile.quantity.toString()
        }

        minus.setOnClickListener {
            if (tile.quantity > 1) {
                tile.quantity--
                callback?.onQuantityChange(tile, position)
                quantity.text = tile.quantity.toString()
            }
        }

        dismissMenu.setOnClickListener {
            dismiss()
        }

        removeTile.setOnClickListener {
            callback?.onRemoveTile(position)
            dismiss()
        }

    }
}