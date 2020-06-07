package com.agrolytics.agrolytics_android.networking.model

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Detector
import kotlin.math.pow

class MeasurementResult(mask_b64 : String?, input_bmp : Bitmap?, rod_length : Double, rod_length_pixel : Int): Parcelable {
    private var mask = BitmapUtils.getImage(mask_b64)
    private var input =  input_bmp
    private var maskedInput = visualizeMask(input!!, mask)
    private var volume = 0.toDouble()
    private var numOfWoodPixels = 0

    init {
        volume = rod_length.pow(2) / rod_length_pixel.toFloat().pow(2) * numOfWoodPixels
    }

    constructor(parcel: Parcel) : this(null, null, 0.toDouble(), 0) {
        mask = parcel.readParcelable(Bitmap::class.java.classLoader)!!
        input = parcel.readParcelable(Bitmap::class.java.classLoader)
        maskedInput = parcel.readParcelable(Bitmap::class.java.classLoader)!!
        volume = parcel.readDouble()
        numOfWoodPixels = parcel.readInt()
    }

    private fun visualizeMask(input: Bitmap, mask: Bitmap): Bitmap{
        var visualizedMask = input
        val intValues = IntArray(input.width * input.height)
        input.getPixels(intValues, 0, input.width, 0, 0, input.width, input.height)
        for (i in 0 until input.width){
            for (j in 0 until input.height) {
                val pixelValue = intValues[j * input.width + i]
                val R = pixelValue and 0xff0000 shr 16
                val G = pixelValue and 0x00ff00 shr 8
                val B = pixelValue and 0x0000ff shr 0
                if (Color.red(mask.getPixel(i, j)) > 0){
                    numOfWoodPixels = numOfWoodPixels + 1
                    val alpha = 0.5f
                    val newR = alpha * 255 + (1-alpha) * R
                    val newG = alpha * 0 + (1-alpha) * G
                    val newB = alpha * 0 + (1-alpha) * B
                    visualizedMask.setPixel(i, j, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
                }
            }
        }
        return visualizedMask
    }

    fun getMask(): Bitmap{
        return mask
    }

    fun getInput(): Bitmap{
        return input!!
    }

    fun getVolume(): Double{
        return volume
    }

    fun getMaskedInput(): Bitmap{
        return maskedInput
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(mask)
        parcel.writeValue(input)
        parcel.writeValue(maskedInput)
        parcel.writeValue(volume)
        parcel.writeValue(numOfWoodPixels)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MeasurementResult> {
        override fun createFromParcel(parcel: Parcel): MeasurementResult {
            return MeasurementResult(parcel)
        }

        override fun newArray(size: Int): Array<MeasurementResult?> {
            return arrayOfNulls(size)
        }
    }
}