package com.agrolytics.agrolytics_android.utils

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.HashMap
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth



object Detector {

    private var assetManager: AssetManager? = null;

    private var model: Interpreter? = null;
    private var input_width = 640;
    private var input_height = 480;

    private var imgData: ByteBuffer? = null
    private var intValues: IntArray? = null
    private var output: Array<Array<Array<FloatArray>>>? = null

    object Result {
        var mask: Bitmap? = null
        var input: Bitmap? = null
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        var fileDescriptor: AssetFileDescriptor = assetManager!!.openFd(filename)
        var inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        var fileChannel = inputStream.channel
        var startOffset: Long = fileDescriptor.startOffset
        var declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun init(assetManager: AssetManager){
        this.assetManager = assetManager
        this.model = Interpreter(loadModelFile("deeplabv3_640_480.tflite"))

        this.imgData = ByteBuffer.allocateDirect(1*input_height*input_width*4*3)
        this.imgData?.order(ByteOrder.nativeOrder())
        this.intValues = IntArray(input_height* input_width)
        this.output = Array(1, { Array(input_width) { Array(input_height) { FloatArray(2) } } })
    }

    fun segmentOffline(inputImage: Bitmap){
        val resizedInput = Bitmap.createScaledBitmap(inputImage, input_width, input_height, false)
        val rotatedBitmap = rotateBitmap(resizedInput, 90f)

        rotatedBitmap.getPixels(this.intValues, 0, rotatedBitmap.width, 0, 0, rotatedBitmap.width, rotatedBitmap.height)
        imgData?.rewind()

        try {
            for (i in 0 until rotatedBitmap.width){
                for (j in 0 until rotatedBitmap.height){
                    val pixelValue = intValues!![j * rotatedBitmap.width + i]
                    imgData!!.putFloat((pixelValue shr 16 and 0xFF) / 255f)
                    imgData!!.putFloat((pixelValue shr 8 and 0xFF) / 255f)
                    imgData!!.putFloat((pixelValue and 0xFF) / 255f)
                }
            }
        } catch (e: Exception){
            val a = 2
        }


        var outputSegment =
            Array(1) { Array(input_height) { Array(input_width) { FloatArray(2) } } }

        val inputArray = arrayOf<ByteBuffer?>(imgData)
        val outputMap = HashMap<Int, Any>()
        outputMap.put(0,outputSegment)

        this.model!!.runForMultipleInputsOutputs(inputArray, outputMap)

        val output: Bitmap = Bitmap.createBitmap(rotatedBitmap.width, rotatedBitmap.height, Bitmap.Config.ARGB_8888)
        for (i in 0 until rotatedBitmap.width) {
            for (j in 0 until rotatedBitmap.height) {
                val WOOD_IDX = 1
                val BG_IDX = 0
                if (outputSegment[0][i][j][WOOD_IDX] > outputSegment[0][i][j][BG_IDX]) {
                    output.setPixel(i,j, Color.WHITE)
                }
                else{
                    output.setPixel(i,j,Color.BLACK)
                }
            }
        }

        Result.input = resizedInput
        Result.mask = rotateBitmap(output, -90f)
    }

    private fun rotateBitmap(img: Bitmap, deg: Float): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(deg)
        val rotatedBitmap = Bitmap.createBitmap(
            img,
            0,
            0,
            img.getWidth(),
            img.getHeight(),
            matrix,
            true
        )
        return rotatedBitmap
    }
}