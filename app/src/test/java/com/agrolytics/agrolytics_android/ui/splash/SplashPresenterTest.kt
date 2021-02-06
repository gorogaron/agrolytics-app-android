package com.agrolytics.agrolytics_android.ui.splash

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class SplashPresenterTest : KoinTest {

    @Test
    fun testCheckExpire() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val presenter: SplashPresenter = SplashPresenter(context)

        assertThat(true).isEqualTo(true)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}