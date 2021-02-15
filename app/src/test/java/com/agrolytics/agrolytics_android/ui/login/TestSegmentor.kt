package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.data.DataClient
import com.google.firebase.FirebaseApp
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

    }
}