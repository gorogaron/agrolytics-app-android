package com.agrolytics.agrolytics_android

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.login.LoginPresenter
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.auth.User
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.*
import org.awaitility.Awaitility.await
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import java.lang.Exception
import java.util.concurrent.Executor

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleUnitTest : KoinTest {

    @Mock
    lateinit var loginPresenter : LoginPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun loginProcess() = GlobalScope.launch {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
        loginPresenter = LoginPresenter(context)
        val email = "admin@admin.com"
        val password = "password"

        //TODO: Not even firebase API calls, but also mocks get deadlock when calling inside runBlocking...
        Mockito.`when`(loginPresenter.signInFirebaseUser(email, password)).thenReturn(ConfigInfo.LOGIN.SUCCESS)
        Mockito.`when`(loginPresenter.getFirstLogin()).thenReturn("2020-12-15")
        doNothing().`when`(loginPresenter).saveUser(Matchers.any(DocumentSnapshot::class.java))
        doNothing().`when`(loginPresenter).initFirstLogin(Matchers.any(FirebaseUser::class.java))

        var loginReturnCode = ConfigInfo.LOGIN.UNDEFINED
        runBlocking {
            withContext(Dispatchers.IO) {
                loginReturnCode = loginPresenter.login(email, password)
            }
        }

        assertThat(loginReturnCode).isEqualTo(ConfigInfo.LOGIN.SUCCESS)
    }
}
