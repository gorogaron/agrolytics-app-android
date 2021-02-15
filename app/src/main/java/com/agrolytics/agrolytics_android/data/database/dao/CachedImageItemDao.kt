package com.agrolytics.agrolytics_android.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem

@Dao
interface CachedImageItemDao {

    @Query("SELECT * FROM cached_images")
    fun getAll(): List<CachedImageItem>

    @Query("SELECT * FROM cached_images WHERE id LIKE :id")
    fun getById(id: String): CachedImageItem

    @Query("SELECT * FROM cached_images WHERE session_id LIKE :sessionId")
    fun getBySessionId(sessionId: String): List<CachedImageItem>

    @Insert
    fun add(cachedImageItem: CachedImageItem)

    @Insert
    fun addAll(cachedImageItems: List<CachedImageItem>)

    @Delete
    fun delete(cachedImageItem: CachedImageItem)

    @Query("DELETE FROM cached_images WHERE session_id LIKE :sessionId")
    fun deleteBySessionId(sessionId: String)

    @Query("DELETE FROM cached_images")
    fun deleteAll()
}