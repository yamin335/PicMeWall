package com.picmewall.picmewall.models

data class TileInfoResponse(val code: Int?, val status: String?, val message: String?, val price: TilePrice?)

data class TilePrice(val tile_price: String?, val delivery_cost: String?, val bkashno: String?, val other_delivery_cost: String?)
