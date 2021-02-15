package com.agrolytics.agrolytics_android.database.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.database.local.ImageItem

@Dao
interface ImageItemDao {
    @Query("SELECT * FROM Images")
    fun getAllImage() : List<ImageItem>

    @Query("SELECT * FROM Images WHERE id LIKE :id")
    fun getImageById(id: String) : ImageItem

    @Query("SELECT * FROM Images WHERE session_id LIKE :sessionId")
    fun getImagesBySessionId(sessionId: String) : List<ImageItem>

    @Query("SELECT * from Images where pushed_to_server LIKE :pushedToServer")
    fun getAllImage(pushedToServer: Boolean) : List<ImageItem>

    @Query("SELECT session_id from Images where id LIKE :id")
    fun getSessionIdForImageId(id: String) : String

    @Insert
    fun addImage(imageItem: ImageItem)

    @Delete
    fun deleteImage(imageItem: ImageItem)

    @Query("DELETE FROM Images WHERE id LIKE :id")
    fun deleteImageById(id: String)

    @Query("DELETE FROM Images WHERE session_id LIKE :sessionId")
    fun deleteImagesBySessionId(sessionId: String)

    @Query("DELETE FROM Images")
    fun deleteAllImage()

}