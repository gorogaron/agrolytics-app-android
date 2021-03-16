package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

object ImageSegmentation {

    var modelFileName = "deeplabv3_640_480.tflite"
    lateinit var model: Interpreter
    lateinit var assetManager: AssetManager

    fun init(iAssetManager : AssetManager) {
        assetManager = iAssetManager
        model = Interpreter(loadModelFile(modelFileName))
    }

    fun segment(
        inputImage: Bitmap
    ) : Pair<Bitmap, Int> {

        val preprocessedData = preProcessData(inputImage)
        val modelOutput = predict(preprocessedData)
        return postProcessData(inputImage, modelOutput)
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun predict(
        floatMatrix: Array<Array<Array<FloatArray>>>
    ) : Array<Array<Array<FloatArray>>> {

        val output = Array(1) { Array(480) { Array(640) { FloatArray(2) } } }

        model.run(floatMatrix, output)

        return output
    }

    private fun preProcessData(bitmap: Bitmap) : Array<Array<Array<FloatArray>>> {
        val preprocessedImage: Array<Array<Array<FloatArray>>> = Array(1) { Array(bitmap.height) {Array(bitmap.width) {FloatArray(3)}} }
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (i in 0 until bitmap.width){
            for (j in 0 until bitmap.height) {
                val pixelValue = intArray[j * bitmap.width + i]
                val red = pixelValue and 0xff0000 shr 16
                val green = pixelValue and 0x00ff00 shr 8
                val blue = pixelValue and 0x0000ff shr 0
                preprocessedImage[0][j][i][0] = red.toFloat() / 255f
                preprocessedImage[0][j][i][1] = green.toFloat() / 255f
                preprocessedImage[0][j][i][2] = blue.toFloat() / 255f
            }
        }
        return preprocessedImage
    }

    private fun postProcessData(
        inputImage: Bitmap,
        modelOutput: Array<Array<Array<FloatArray>>>
    ) : Pair<Bitmap, Int> {

        val woodIndex = 1
        val backgroundIndex = 0

        var numOfWoodPixels = 0
        val output: Bitmap = Bitmap.createBitmap(inputImage.width, inputImage.height, Bitmap.Config.ARGB_8888)
        for (i in 0 until inputImage.width) {
            for (j in 0 until inputImage.height) {

                if (modelOutput[0][j][i][woodIndex] > modelOutput[0][j][i][backgroundIndex]) {
                    output.setPixel(i,j, Color.WHITE)
                    numOfWoodPixels += 1
                }
                else{
                    output.setPixel(i,j,Color.BLACK)
                }
            }
        }
        return Pair(output, numOfWoodPixels)
    }
}