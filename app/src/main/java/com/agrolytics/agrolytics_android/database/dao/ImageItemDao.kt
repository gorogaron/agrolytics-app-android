package com.agrolytics.agrolytics_android.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.networking.model.ImageItem

@Dao
interface ImageItemDao {
    @Query("SELECT * from Images")
    fun getAllImage() : List<ImageItem>

    @Query("SELECT * from Images where id LIKE :id")
    fun getImageById(id: String) : ImageItem?

    @Query("SELECT * from Images where pushed_to_server LIKE :pushedToServer")
    fun getAllImage(pushedToServer: Boolean) : List<ImageItem>

    @Insert
    fun addImage(imageItem: ImageItem)

    @Delete
    fun deleteImage(imageItem: ImageItem)

    @Query("DELETE FROM Images")
    fun deleteAllImage()

}