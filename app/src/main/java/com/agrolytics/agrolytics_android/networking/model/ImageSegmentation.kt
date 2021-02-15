package com.agrolytics.agrolytics_android.networking.model

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

class ImageSegmentation {
    private val modelPath = ""
    private val imgSize = intArrayOf(640, 480)

    fun segment(
        inputImage: Bitmap
    ) : Pair<Bitmap, Int> {
        val preprocessedData = preProcessData(inputImage)
//        Dummy
//        val floatVector: FloatArray = floatArrayOf(122f, 119f, 149f, 135f, 154f, 140f, 140f, 132f)
//        val floatMatrix: Array<FloatArray> = arrayOf(floatVector)
        val modelOutput = predict(preprocessedData)

        return postProcessData(inputImage, modelOutput)
    }

    private fun predict(
        floatMatrix: Array<FloatArray>
    ) : FloatArray {

        OrtEnvironment.getEnvironment().use { env ->
            OrtSession.SessionOptions().use { opts ->
                opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                env.createSession(modelPath, opts).use { session ->
                    val inputName = session.inputNames.iterator().next()
                    OnnxTensor.createTensor(env, floatMatrix).use { imageTensor ->
                        val imageTensorMap = HashMap<String, OnnxTensor>()
                        imageTensorMap[inputName] = imageTensor
                        session.run(imageTensorMap).use { output ->
                            return output.get(0).value as FloatArray
                        }
                    }
                }
            }
        }
    }

    private fun preProcessData(bitmap: Bitmap) : Array<FloatArray> {
        throw NotImplementedError()
    }

    private fun postProcessData(
        inputImage: Bitmap,
        modelOutput: FloatArray
    ) : Pair<Bitmap, Int> {

        var numOfWoodPixels = 0
        val mask = floatArrayToGrayscaleBitmap(modelOutput, 640, 480)
        val visualizedMask = inputImage.copy(Bitmap.Config.ARGB_8888 , true)
        val intValues = IntArray(inputImage.width * inputImage.height)
        inputImage.getPixels(intValues, 0, inputImage.width, 0, 0, inputImage.width, inputImage.height)
        for (i in 0 until inputImage.width){
            for (j in 0 until inputImage.height) {
                val pixelValue = intValues[j * inputImage.width + i]
                val R = pixelValue and 0xff0000 shr 16
                val G = pixelValue and 0x00ff00 shr 8
                val B = pixelValue and 0x0000ff shr 0
                if (Color.red(mask.getPixel(i, j)) > 0){
                    numOfWoodPixels += 1
                    val alpha = 0.5f
                    val newR = alpha * 255 + (1-alpha) * R
                    val newG = alpha * 0 + (1-alpha) * G
                    val newB = alpha * 0 + (1-alpha) * B
                    visualizedMask.setPixel(i, j, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
                }
            }
        }
        return Pair(visualizedMask, numOfWoodPixels)
    }

    private fun floatArrayToGrayscaleBitmap (
        floatArray: FloatArray,
        width: Int,
        height: Int,
        alpha :Byte = (255).toByte(),
        reverseScale :Boolean = false
    ) : Bitmap {

        // Create empty bitmap in RGBA format (even though it says ARGB but channels are RGBA)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val byteBuffer = ByteBuffer.allocate(width * height * 4)

        // mapping smallest value to 0 and largest value to 255
        val maxValue = floatArray.max() ?: 1.0f
        val minValue = floatArray.min() ?: 0.0f
        val delta = maxValue - minValue
        var tempValue: Byte

        // Define if float min..max will be mapped to 0..255 or 255..0
        val conversion = when (reverseScale) {
            false -> { v: Float -> ((v - minValue) / delta * 255).toInt().toByte() }
            true -> { v: Float -> (255 - (v - minValue) / delta * 255).toInt().toByte() }
        }

        // copy each value from float array to RGB channels and set alpha channel
        floatArray.forEachIndexed { i, value ->
            tempValue = conversion(value)
            byteBuffer.put(4 * i, tempValue)
            byteBuffer.put(4 * i + 1, tempValue)
            byteBuffer.put(4 * i + 2, tempValue)
            byteBuffer.put(4 * i + 3, alpha)
        }

        bmp.copyPixelsFromBuffer(byteBuffer)

        return bmp
    }
}