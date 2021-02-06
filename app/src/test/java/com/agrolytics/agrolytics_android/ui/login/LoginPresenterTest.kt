package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.mockito.MockitoAnnotations


@RunWith(AndroidJUnit4::class)
class LoginPresenterTest : KoinTest {

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun loginProcess() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)

        val loginPresenter = spyk(LoginPresenter(context))

        val email = "admin@admin.com"
        val password = "password"

        coEvery { loginPresenter.signInFirebaseUser(email, password) } returns ConfigInfo.LOGIN.SUCCESS
        coEvery { loginPresenter.getFirstLogin() } returns "2020-12-16"
        coEvery { loginPresenter.saveUser() } returns Unit
        coEvery { loginPresenter.initFirstLogin(any()) } returns Unit

        val loginReturnCode = loginPresenter.login(email, password)

        assertThat(loginReturnCode).isEqualTo(ConfigInfo.LOGIN.USER_EXPIRED)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
