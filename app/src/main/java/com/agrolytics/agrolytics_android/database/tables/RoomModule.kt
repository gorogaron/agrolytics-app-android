package com.agrolytics.agrolytics_android.database.tables

import android.content.Context
import androidx.room.Room
import com.agrolytics.agrolytics_android.database.database.ImageDatabase

class RoomModule(var context: Context) {

    var database: ImageDatabase? = null

    init {
        database = Room.databaseBuilder(context,
            ImageDatabase::class.java,
            "image-list.db").build()
    }

}