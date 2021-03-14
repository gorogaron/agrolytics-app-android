package com.agrolytics.agrolytics_android.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.google.firebase.firestore.GeoPoint
import java.nio.ByteBuffer

/**
 * Ez az osztály ahhoz kell, hogy a local database-ben tudjunk bitmap típusú objektumokat is kezelni.
 * */

class TypeConverter {

    @TypeConverter
    fun bitmapToByteArray(bitmap : Bitmap?): ByteArray? {
        return if (bitmap != null) ImageUtils.getBytes(bitmap) else null
    }

    @TypeConverter
    fun byteArrayToBitmap(byteArray: ByteArray?) : Bitmap? {
        return if (byteArray != null) BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) else null
    }

    @TypeConverter
    fun geoPointToByteArray(geoPoint: GeoPoint) : ByteArray {
        /**Két double változó 128 bit = 16 byte*/
        val byteBuffer = ByteBuffer.allocate(16)
            .putDouble(geoPoint.latitude)
            .putDouble(geoPoint.longitude)

        return  byteBuffer.array()
    }

    @TypeConverter
    fun byteArrayToGeoPoint(byteArray: ByteArray) : GeoPoint {
        val bytes = ByteBuffer.wrap(byteArray)
        return GeoPoint(
            bytes.getDouble(0),
            bytes.getDouble(8)
        )
    }
}