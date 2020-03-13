package com.agrolytics.agrolytics_android.utils

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.HashMap

object Detector {

    private var assetManager: AssetManager? = null;

    private var model: Interpreter? = null;
    private var input_width = 512;
    private var input_height = 512;

    private var imgData: ByteBuffer? = null
    private var intValues: IntArray? = null
    private var output: Array<Array<Array<FloatArray>>>? = null

    object Result {
        var mask: Bitmap? = null
        var input: Bitmap? = null
        var maskedInput: Bitmap? = null
        var numOfWoodPixels: Int = 0
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
        this.model = Interpreter(loadModelFile("deeplabv3_512.tflite"))

        this.imgData = ByteBuffer.allocateDirect(1*input_height*input_width*4*3)
        this.imgData?.order(ByteOrder.nativeOrder())
        this.intValues = IntArray(input_height* input_width)
        this.output = Array(1, { Array(input_width) { Array(input_height) { FloatArray(2) } } })
    }

    fun segmentOffline(inputImage: Bitmap): Bitmap{
        val resizedInput = Bitmap.createScaledBitmap(inputImage, input_width, input_height, false)
        resizedInput.getPixels(this.intValues, 0, resizedInput.width, 0, 0, resizedInput.width, resizedInput.height)
        imgData?.rewind()

        try {
            for (i in 0 until input_width){
                for (j in 0 until input_height){
                    val pixelValue = intValues!![j * input_width + i]
                    imgData!!.putFloat((pixelValue shr 16 and 0xFF) / 255f)
                    imgData!!.putFloat((pixelValue shr 8 and 0xFF) / 255f)
                    imgData!!.putFloat((pixelValue and 0xFF) / 255f)
                }
            }
        } catch (e: Exception){
            val a = 2
        }


        var outputSegment =
            Array(1) { Array(input_width) { Array(input_height) { FloatArray(2) } } }

        val inputArray = arrayOf<ByteBuffer?>(imgData)
        val outputMap = HashMap<Int, Any>()
        outputMap.put(0,outputSegment)

        this.model!!.runForMultipleInputsOutputs(inputArray, outputMap)

        val output: Bitmap = Bitmap.createBitmap(input_width, input_height, Bitmap.Config.ARGB_8888)
        for (i in 0 until input_width) {
            for (j in 0 until input_height) {
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
        Result.mask = output
        Result.maskedInput = visualizeMask()
        //Result.numOfWoodPixels = countWoodPixels(Result.mask)
        return Result.maskedInput!!
    }

    private fun visualizeMask(): Bitmap{

        var maskedInput = Result.input
        for (i in 0 until input_width){
            for (j in 0 until input_height) {
                val pixelValue = intValues!![j * input_width + i]
                val R = pixelValue and 0xff0000 shr 16
                val G = pixelValue and 0x00ff00 shr 8
                val B = pixelValue and 0x0000ff shr 0
                if (Result.mask!!.getPixel(i, j) == Color.WHITE){
                    Result.numOfWoodPixels = Result.numOfWoodPixels + 1
                    val alpha = 0.5f
                    val newR = alpha * 255 + (1-alpha) * R
                    val newG = alpha * 0 + (1-alpha) * G
                    val newB = alpha * 0 + (1-alpha) * B
                    maskedInput!!.setPixel(i, j, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
                }
            }
        }

        return maskedInput!!
    }
}