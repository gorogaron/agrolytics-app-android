package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.util.Log
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import java.lang.Exception
import java.lang.NullPointerException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.*
import kotlin.math.sign

class LoginPresenter(val context: Context) : BasePresenter<LoginScreen>() {

    val TAG = "LoginPresenter"

    suspend fun login(email: String?, password: String?) : Int {
        var signInResult = ConfigInfo.LOGIN.UNDEFINED
        try {
            if (Util.isNetworkAvailable(context)) {
                if (checkInputFields(email, password)) {
                    signInResult = signInFirebaseUser(email!!, password!!)

                    if (signInResult == ConfigInfo.LOGIN.SUCCESS) {
                        val user = auth?.currentUser
                        val userDocument = getUserDocument(user)
                        val firstLogin = userDocument["first_login"] as String

                        signInResult = if (userDocument["first_login"] != null) {
                            if (hasUserExpired(firstLogin)) {
                                ConfigInfo.LOGIN.USER_EXPIRED
                            } else {
                                saveUser(userDocument)
                                ConfigInfo.LOGIN.SUCCESS
                            }
                        } else {
                            initFirstLogin(user)
                            saveUser(userDocument)
                            ConfigInfo.LOGIN.SUCCESS
                        }
                    } else {
                        signInResult = ConfigInfo.LOGIN.AUTH_FAILED
                    }
                }
            } else {
                signInResult = ConfigInfo.LOGIN.NO_INTERNET
            }
        }
        catch(e: Exception) {
            e.printStackTrace()
            Log.d("Login", "$e")
            Log.d("Login", "$e.stackTrace")
            clearSession()
            signInResult = ConfigInfo.LOGIN.ERROR
        }
        return signInResult
    }


    private fun checkInputFields(email : String?, password : String?) : Boolean{
        return email != null && password != null && email.isNotEmpty() && password.isNotEmpty()
    }

    private suspend fun signInFirebaseUser(email: String, password: String) : Int = suspendCoroutine{ cont ->
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task->
            if (task.isSuccessful){
                cont.resume(ConfigInfo.LOGIN.SUCCESS)
            }
            else{
                cont.resume(ConfigInfo.LOGIN.AUTH_FAILED)
            }
        }
    }

    private suspend fun saveUser(userDocumentSnapshot: DocumentSnapshot) {
        val roleDocumentSnapshot = getRoleDocument(userDocumentSnapshot["role"] as DocumentReference)
        sessionManager?.userRole = (roleDocumentSnapshot["role"] as String?)!!
        sessionManager?.userID = userDocumentSnapshot.id
        sessionManager?.userEmail = (userDocumentSnapshot["email"] as String?)!!
        sessionManager?.forestryID = (userDocumentSnapshot["forestry"] as String?)!!
        sessionManager?.firstLogin = (userDocumentSnapshot["first_login"] as String?)!!

        //Admins don't have leaderID
        if ((userDocumentSnapshot["leaderID"] as String?) != null){
            sessionManager?.leaderID = userDocumentSnapshot["leaderID"] as String
        }

        //Update user token. TODO: Move token to shared preferences
        updateUserToken(auth?.currentUser!!)
    }

    private suspend fun getUserDocument(user: FirebaseUser?) : DocumentSnapshot = suspendCoroutine { cont->
        if (user != null){
            val userDocumentReference = fireStoreDB?.db?.collection("user")?.document(user.uid)
            userDocumentReference?.get()
                ?.addOnSuccessListener { cont.resume(it) }
                ?.addOnFailureListener { cont.resumeWithException(it) }
        } else {
            cont.resumeWithException(NullPointerException())
        }
    }

    private suspend fun getRoleDocument(roleRef : DocumentReference) : DocumentSnapshot = suspendCoroutine { cont->
        roleRef.get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private suspend fun initFirstLogin(user: FirebaseUser?) : Void = suspendCoroutine { cont ->
        if (user != null){
            val userDocumentReference = fireStoreDB?.db?.collection("user")?.document(user.uid)
            userDocumentReference?.update("first_login", Util.getCurrentDateString())
                ?.addOnSuccessListener { cont.resume(it) }
                ?.addOnFailureListener { cont.resumeWithException(it) }
        } else {
            cont.resumeWithException(NullPointerException())
        }

    }

    private fun hasUserExpired(firstLogin: String?) : Boolean {
        val firstLoginDate = DateTime.parse(firstLogin)
        val firstLoginDateThirtyAdded = firstLoginDate.plusDays(30)
        val currentDate = DateTime.now()
        val msDiff = firstLoginDateThirtyAdded.millis - currentDate.millis
        val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)

        return daysDiff <= 0
    }

    private suspend fun updateUserToken(user: FirebaseUser) : Boolean = suspendCoroutine{
        user.getIdToken(false)
            .addOnSuccessListener { userToken ->
                appServer?.updateApiService(userToken.token)
                it.resume(true)
            }
            .addOnFailureListener { e ->
                Log.d("Login", "Error getting user token", e)
                it.resume(false)
            }
    }

    private fun clearSession() {
        FirebaseAuth.getInstance().signOut()
        doAsync {
            roomModule?.database?.clearAllTables()
            sessionManager?.clearSession()
        }
    }
}