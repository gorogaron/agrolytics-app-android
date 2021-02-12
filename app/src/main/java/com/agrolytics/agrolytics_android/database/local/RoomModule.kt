package com.agrolytics.agrolytics_android.database.local

import android.content.Context
import androidx.room.Room

class RoomModule(var context: Context) {

    var database: ImageDatabase? = null

    init {
        database = Room.databaseBuilder(context,
            ImageDatabase::class.java,
            "image-list.db").build()
    }

}