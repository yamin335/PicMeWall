package com.picmewall.picmewall.models

data class Tile(var id: Int, var frame: Int, var resizedImagePath: String?, var height: Int = 0, var width: Int = 0, var quantity: Int, val imagePath: String)