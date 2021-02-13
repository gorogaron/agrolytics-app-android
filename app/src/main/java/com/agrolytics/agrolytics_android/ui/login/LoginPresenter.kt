package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.util.Log
import com.agrolytics.agrolytics_android.database.firestore.FireStoreCollection
import com.agrolytics.agrolytics_android.database.firestore.FireStoreUserField
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.types.ConfigInfo
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

    suspend fun login(email: String, password: String) : ConfigInfo.LOGIN {
        var signInResultCode = ConfigInfo.LOGIN.NO_INTERNET
        if (Util.isNetworkAvailable()) {
            try {
                signInResultCode = signInFirebaseUser(email, password)
                when (signInResultCode) {
                    ConfigInfo.LOGIN.AUTH_FAILED -> return signInResultCode
                    ConfigInfo.LOGIN.SUCCESS -> signInResultCode = hasLoggedInUserExpired()
                }
                when (signInResultCode) {
                    ConfigInfo.LOGIN.USER_EXPIRED -> return signInResultCode
                    ConfigInfo.LOGIN.SUCCESS -> saveCurrentUser()
                }
            }
            catch(e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "$e")
                Log.d(TAG, "$e.stackTrace")
                clearSession()
                signInResultCode = ConfigInfo.LOGIN.ERROR
            }
        }
        return signInResultCode
    }

    suspend fun hasLoggedInUserExpired(): ConfigInfo.LOGIN {
        userDocument = getUserDocument(auth?.currentUser)
        val firstLogin = getFirstLogin()
        return if (checkUserExpired(firstLogin)) {
            auth?.signOut()
            ConfigInfo.LOGIN.USER_EXPIRED
        } else {
            ConfigInfo.LOGIN.SUCCESS
        }
    }

    suspend fun signInFirebaseUser(email: String, password: String) : ConfigInfo.LOGIN = suspendCoroutine{ cont ->
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                    cont.resume(ConfigInfo.LOGIN.SUCCESS)
            }
            else {
                cont.resume(ConfigInfo.LOGIN.AUTH_FAILED)
            }
        }
    }

    suspend fun saveCurrentUser() {
        // TODO: felhasználók egybezárása
        val roleDocumentSnapshot = getRoleDocument(userDocument[FireStoreUserField.ROLE.tag] as DocumentReference)
        sessionManager?.userRole = (roleDocumentSnapshot[FireStoreUserField.ROLE.tag] as String?)!!
        sessionManager?.userID = userDocument.id
        sessionManager?.userEmail = (userDocument[FireStoreUserField.EMAIL.tag] as String?)!!
        sessionManager?.forestryID = (userDocument[FireStoreUserField.FORESTRY_ID.tag] as String?)!!
        sessionManager?.forestryName = (userDocument[FireStoreUserField.FORESTRY_ID.tag] as String?)!!
        sessionManager?.firstLogin = (userDocument[FireStoreUserField.FIRST_LOGIN.tag] as String?)!!

        //Admins don't have leaderID
        if ((userDocument[FireStoreUserField.LEADER_ID.tag] as String?) != null){
            sessionManager?.leaderID = userDocument[FireStoreUserField.LEADER_ID.tag] as String
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
            val userDocumentReference = fireStoreDB?.db?.collection(FireStoreCollection.USER.tag)?.document(user.uid)
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
        return userDocument[FireStoreUserField.FIRST_LOGIN.tag] as String
    }

    private fun clearSession() {
        auth?.signOut()
        doAsync {
            roomModule?.database?.clearAllTables()
            sessionManager?.clearSession()
        }
    }
}