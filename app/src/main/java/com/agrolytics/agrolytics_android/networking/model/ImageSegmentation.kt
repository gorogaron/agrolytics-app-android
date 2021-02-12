package com.agrolytics.agrolytics_android.networking.model

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap

class ImageSegmentation {
    private val modelPath = ""
    private val imgSize = intArrayOf(640, 480)

    fun segment(bitmap: Bitmap) {
        var preprocessedData = preProcessData(bitmap)

        // Dummy
        val floatVector: FloatArray = floatArrayOf(122f, 119f, 149f, 135f, 154f, 140f, 140f, 132f)
        val floatMatrix: Array<FloatArray> = arrayOf(floatVector)
        var modelOutput = predict(floatMatrix)

        var maskImage = postProcessData()
    }

    private fun predict(floatMatrix: Array<FloatArray>) : Array<*> {
        var prediction: Array<*>
        OrtEnvironment.getEnvironment().use { env ->
            OrtSession.SessionOptions().use { opts ->
                opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                env.createSession(modelPath, opts).use { session ->
                    val inputName = session.inputNames.iterator().next()
                    OnnxTensor.createTensor(env, floatMatrix).use { imageTensor ->
                        val imageTensorMap = HashMap<String, OnnxTensor>()
                        imageTensorMap[inputName] = imageTensor
                        session.run(imageTensorMap).use { output ->
                            return output.get(0).value as Array<*>
                        }
                    }
                }
            }
        }
    }

    private fun preProcessData(bitmap: Bitmap) {}
    private fun postProcessData() {}
}