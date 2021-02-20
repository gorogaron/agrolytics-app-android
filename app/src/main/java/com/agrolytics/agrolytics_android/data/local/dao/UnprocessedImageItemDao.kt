package com.agrolytics.agrolytics_android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem

@Dao
interface UnprocessedImageItemDao {

    @Query("SELECT * FROM unprocessed_images")
    fun getAll(): List<UnprocessedImageItem>

    @Query("SELECT * FROM unprocessed_images WHERE session_id LIKE :sessionId")
    fun getBySessionId(sessionId: String): List<UnprocessedImageItem>

    @Insert
    fun add(unprocessedImageItem: UnprocessedImageItem)

    @Insert
    fun addAll(unprocessedImageItems: List<UnprocessedImageItem>)

    @Delete
    fun delete(unprocessedImageItem: UnprocessedImageItem)

    @Query("DELETE FROM unprocessed_images WHERE session_id LIKE :sessionId")
    fun deleteBySessionId(sessionId: String)

    @Query("DELETE FROM unprocessed_images WHERE id LIKE :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM unprocessed_images")
    fun deleteAll()
}