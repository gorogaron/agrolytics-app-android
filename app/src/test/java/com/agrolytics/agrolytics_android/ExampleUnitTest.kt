package com.agrolytics.agrolytics_android

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.awaitility.Awaitility.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mock
import org.mockito.Mockito
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
    private lateinit var successTask: Task<AuthResult>
    private lateinit var failTask: Task<AuthResult>

    @Mock
    lateinit var mAuth: FirebaseAuth

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        successTask = object : Task<AuthResult>() {
            override fun isComplete(): Boolean = true

            override fun isSuccessful(): Boolean = true

            override fun addOnCompleteListener(p0: OnCompleteListener<AuthResult>): Task<AuthResult> {
                p0.onComplete(successTask)
                return successTask
            }

            override fun getException(): Exception? {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(p0: OnFailureListener): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun getResult(): AuthResult? {
                TODO("Not yet implemented")
            }

            override fun <X : Throwable?> getResult(p0: Class<X>): AuthResult? {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(p0: OnSuccessListener<in AuthResult>): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun isCanceled(): Boolean {
                TODO("Not yet implemented")
            }
        }
        failTask = object : Task<AuthResult>() {
            override fun isComplete(): Boolean = true

            override fun isSuccessful(): Boolean = false

            override fun addOnCompleteListener(p0: OnCompleteListener<AuthResult>): Task<AuthResult> {
                p0.onComplete(successTask)
                return successTask
            }

            override fun getException(): Exception? {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(p0: OnFailureListener): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Executor,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnFailureListener(
                p0: Activity,
                p1: OnFailureListener
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun getResult(): AuthResult? {
                TODO("Not yet implemented")
            }

            override fun <X : Throwable?> getResult(p0: Class<X>): AuthResult? {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(p0: OnSuccessListener<in AuthResult>): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Executor,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun addOnSuccessListener(
                p0: Activity,
                p1: OnSuccessListener<in AuthResult>
            ): Task<AuthResult> {
                TODO("Not yet implemented")
            }

            override fun isCanceled(): Boolean {
                TODO("Not yet implemented")
            }
        }
    }

    @Test
    fun addition_isCorrect() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        FirebaseApp.initializeApp(context)
        val email = "admin@admin.com"
        val password = "password"
        Mockito.`when`(mAuth.signInWithEmailAndPassword(email, password)).thenReturn(successTask)
        var success = false
        mAuth.signInWithEmailAndPassword("admin@admin.com", "password").addOnCompleteListener { task ->
            success = task.isSuccessful
        }
        assertThat(success).isEqualTo(true)

        /*var success = false
        mAuth?.signInWithEmailAndPassword("admin@admin.com", "password")?.addOnCompleteListener { task ->
            success = !task.isSuccessful
        }
        await().until({ success === true })

        var activity = Robolectric.setupActivity(LoginActivity::class.java)
        var injections  : ArrayList<Any> = arrayListOf(appServer, sessionManager!!, roomModule!!, fireStoreDB!!, auth!!)
        var loginPresenterTest = LoginPresenter(context)
        loginPresenterTest.addInjections(injections)
        loginPresenterTest.addView(activity)
        loginPresenterTest.login("admin@admin.com", "password")
        Thread.sleep(10000)

        assertThat(loginPresenterTest.sessionManager!!.userEmail).isEqualTo("admin@admin.com")*/
        //assertThat(success).isEqualTo(true)
    }
}
