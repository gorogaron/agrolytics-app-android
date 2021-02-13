package com.agrolytics.agrolytics_android.ui.login

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.database.DataClient
import com.agrolytics.agrolytics_android.database.local.ImageItem
import com.agrolytics.agrolytics_android.database.local.ImageItemDao_Impl
import com.agrolytics.agrolytics_android.database.local.RoomModule
import com.google.firebase.FirebaseApp
import io.mockk.InternalPlatformDsl.toArray
import okhttp3.internal.wait
import org.jetbrains.anko.doAsync
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TestSegmentor {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
    }

    @Test
    fun testModel() {
        val modelPath = "C:\\Users\\kenderak\\Projects\\Agrolytics\\agrolytics-app-android\\app\\src\\main\\assets\\xgboost.onnx"

        val floatVector: FloatArray = floatArrayOf(122f, 119f, 149f, 135f, 154f, 140f, 140f, 132f)
        val floatMatrix: Array<FloatArray> = arrayOf(floatVector)


        val dataClient = DataClient(context)

        val roomModule: RoomModule = RoomModule(context)
        val image: ImageItem = ImageItem("0", "0", "./", false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        dataClient.local.addImage(image)
        var images = dataClient.local.getImageById("0")
        var images2: List<ImageItem>
        doAsync {
            roomModule.database?.imageItemDao()?.addImage(image)
            roomModule.database?.imageItemDao()?.getAllImage().let {
                if (it != null) {
                    images2 = it
                }
            }
        }

//        var testImage = images2.get(0)

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