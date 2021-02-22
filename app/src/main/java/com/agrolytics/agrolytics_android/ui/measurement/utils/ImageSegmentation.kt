package com.agrolytics.agrolytics_android.ui.measurement.utils

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Color

object ImageSegmentation {

    lateinit var assetManager: AssetManager

    fun init(iAssetManager : AssetManager) {
        assetManager = iAssetManager
    }

    fun segment(
        inputImage: Bitmap
    ) : Pair<Bitmap, Int> {

        val preprocessedData = preProcessData(inputImage)
        val modelOutput = predict(preprocessedData)
        return postProcessData(inputImage, modelOutput)
    }

    private fun predict(
        floatMatrix: Array<Array<Array<FloatArray>>>
    ) : Array<Array<Array<FloatArray>>> {

        OrtEnvironment.getEnvironment().use { env ->
            OrtSession.SessionOptions().use { opts ->
                opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                val modelInputStream = assetManager.open("deeplab.onnx")
                env.createSession(modelInputStream.readBytes(), opts).use { session ->
                    val inputName = session.inputNames.iterator().next()
                    OnnxTensor.createTensor(env, floatMatrix).use { imageTensor ->
                        val imageTensorMap = HashMap<String, OnnxTensor>()
                        imageTensorMap[inputName] = imageTensor
                        session.run(imageTensorMap).use { output ->
                            return output.get(0).value as Array<Array<Array<FloatArray>>>
                        }
                    }
                }
            }
        }
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