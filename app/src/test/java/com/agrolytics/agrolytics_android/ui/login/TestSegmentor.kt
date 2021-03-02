package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageSegmentation
import com.google.firebase.FirebaseApp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TestSegmentor {

    private lateinit var context: Context

    @Before
    fun setupTest() {
        context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
    }

    @Test
    fun testModel() {
        val model = ImageSegmentation()

        val conf = Bitmap.Config.ARGB_8888 // see other conf types
        val bmp = Bitmap.createBitmap(640, 480, conf) // this creates a MUTABLE bitmap


        val testOutput = model.segment(bmp)

        print("OK")
    }
}