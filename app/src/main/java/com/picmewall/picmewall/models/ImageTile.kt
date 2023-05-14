package com.picmewall.picmewall.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_tiles")
data class ImageTile(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "image_name") val imageName: String?,
    @ColumnInfo(name = "image_size") val imageSize: Double?,
    @ColumnInfo(name = "image_resolution") val imageResolution: String?
)