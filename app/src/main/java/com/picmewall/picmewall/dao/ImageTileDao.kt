package com.picmewall.picmewall.dao

import androidx.room.*
import com.picmewall.picmewall.models.ImageTile
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageTileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItemToImageTiles(item: ImageTile): Long

    @Delete
    suspend fun deleteImageTile(item: ImageTile)

    @Query("SELECT * FROM image_tiles")
    fun getImageTiles(): Flow<List<ImageTile>>

    @Query("SELECT COUNT(uri) FROM image_tiles")
    fun getHistoryItemsCount(): Flow<Int>

    @Query("SELECT COUNT(uri) FROM image_tiles WHERE uri = :uri")
    fun doesItemExistsInImageTileList(uri: String): Flow<Int>
}