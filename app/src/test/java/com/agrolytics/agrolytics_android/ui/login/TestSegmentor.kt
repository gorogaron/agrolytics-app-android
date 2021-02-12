package com.agrolytics.agrolytics_android.ui.login

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.agrolytics.agrolytics_android.database.DataClient
import io.mockk.InternalPlatformDsl.toArray
import org.junit.Test


class TestSegmentor {
    @Test
    fun testModel() {
        val modelPath = "C:\\Users\\kenderak\\Projects\\Agrolytics\\agrolytics-app-android\\app\\src\\main\\assets\\xgboost.onnx"

        val floatVector: FloatArray = floatArrayOf(122f, 119f, 149f, 135f, 154f, 140f, 140f, 132f)
        val floatMatrix: Array<FloatArray> = arrayOf(floatVector)

        val dataClient = DataClient()


        var prediction: Array<*>
        OrtEnvironment.getEnvironment().use { env ->
            OrtSession.SessionOptions().use { opts ->
                opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
                env.createSession(modelPath, opts).use { session ->
                    val inputName = session.inputNames.iterator().next()
                    OnnxTensor.createTensor(env, floatMatrix).use { testTensor ->
                        val testTensorMap = HashMap<String, OnnxTensor>()
                        testTensorMap[inputName] = testTensor
                        session.run(testTensorMap).use { output ->
                            prediction = output.get(0).value.toArray()
                        }
                    }
                }
            }
        }
        println(0)
    }
}