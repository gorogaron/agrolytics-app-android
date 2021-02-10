package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.util.Log
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import java.time.LocalDate
import java.time.Period
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoginPresenter(val context: Context) : BasePresenter<LoginScreen>() {

    private lateinit var userDocument : DocumentSnapshot

    companion object {
        private const val TAG = "LoginPresenter"
    }

    suspend fun login(email: String, password: String) : Int {
        var signInResult = ConfigInfo.LOGIN.NO_INTERNET
        if (Util.isNetworkAvailable()) {
            try {
                signInResult = signInFirebaseUser(email, password)
                when (signInResult) {
                    ConfigInfo.LOGIN.AUTH_FAILED -> return signInResult
                    ConfigInfo.LOGIN.SUCCESS -> signInResult = hasLoggedInUserExpired()
                }
                when (signInResult) {
                    ConfigInfo.LOGIN.USER_EXPIRED -> return signInResult
                    ConfigInfo.LOGIN.SUCCESS -> saveCurrentUser()
                }
            }
            catch(e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "$e")
                Log.d(TAG, "$e.stackTrace")
                clearSession()
                signInResult = ConfigInfo.LOGIN.ERROR
            }
        }
        return signInResult
    }

    suspend fun hasLoggedInUserExpired(): Int {
        userDocument = getUserDocument(auth?.currentUser)
        val firstLogin = getFirstLogin()
        return if (checkUserExpired(firstLogin)) {
            auth?.signOut()
            ConfigInfo.LOGIN.USER_EXPIRED
        } else {
            ConfigInfo.LOGIN.SUCCESS
        }
    }

    suspend fun signInFirebaseUser(email: String, password: String) : Int = suspendCoroutine{ cont ->
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                GlobalScope.launch {
                    cont.resume(ConfigInfo.LOGIN.SUCCESS)
                }
            }
            else {
                cont.resume(ConfigInfo.LOGIN.AUTH_FAILED)
            }
        }
    }

    suspend fun saveCurrentUser() {
        // TODO: felhasználók egybezárása
        val roleDocumentSnapshot = getRoleDocument(userDocument["role"] as DocumentReference)
        sessionManager?.userRole = (roleDocumentSnapshot["role"] as String?)!!
        sessionManager?.userID = userDocument.id
        sessionManager?.userEmail = (userDocument["email"] as String?)!!
        sessionManager?.forestryID = (userDocument["forestry"] as String?)!!
        sessionManager?.firstLogin = (userDocument["first_login"] as String?)!!

        //Admins don't have leaderID
        if ((userDocument["leaderID"] as String?) != null){
            sessionManager?.leaderID = userDocument["leaderID"] as String
        }

        //Update user token. TODO: Move token to shared preferences
        updateUserToken()
    }

    private suspend fun updateUserToken() : Boolean = suspendCoroutine{
        auth?.currentUser?.getIdToken(false)
            ?.addOnSuccessListener { userToken ->
                appServer?.updateApiService(userToken.token)
                it.resume(true)
            }
            ?.addOnFailureListener { e ->
                Log.d(TAG, "Error getting user token", e)
                it.resume(false)
            }
    }

    private fun checkUserExpired(firstLogin: String) : Boolean {
        val firstLoginDate = LocalDate.parse(firstLogin)
        val period = Period.between(firstLoginDate, LocalDate.now())

        return period.months >= 1
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

    private fun getFirstLogin() : String {
        return userDocument["first_login"] as String
    }

    private fun clearSession() {
        auth?.signOut()
        doAsync {
            roomModule?.database?.clearAllTables()
            sessionManager?.clearSession()
        }
    }
}