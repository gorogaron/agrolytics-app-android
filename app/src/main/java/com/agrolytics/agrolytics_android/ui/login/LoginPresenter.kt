package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.util.Log
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreForestryField
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreUserField
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import org.jetbrains.anko.doAsync
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class LoginPresenter(val context: Context) : BasePresenter<LoginScreen>() {

    private lateinit var userDocument : DocumentSnapshot
    private lateinit var forestryDocument : DocumentSnapshot
    lateinit var activity: LoginActivity

    companion object {
        private const val TAG = "LoginPresenter"
    }

    suspend fun login(email: String, password: String) : ConfigInfo.LOGIN {
        var signInResultCode = ConfigInfo.LOGIN.NO_INTERNET
        if ((activity).isInternetAvailable) {
            try {
                signInResultCode = signInFirebaseUser(email, password)
                when (signInResultCode) {
                    ConfigInfo.LOGIN.AUTH_FAILED -> return signInResultCode
                    ConfigInfo.LOGIN.SUCCESS -> signInResultCode = checkLicence()
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

    suspend fun checkLicence(): ConfigInfo.LOGIN {
        userDocument = dataClient!!.fireBase.fireStore.getUserDocumentById(auth?.currentUser?.uid!!)
        forestryDocument = dataClient!!.fireBase.fireStore.getForestryDocumentById(userDocument[FireStoreUserField.FORESTRY_ID.tag] as String)
        return if (hasLicenceExpired()) {
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
        val roleDocumentSnapshot = dataClient!!.fireBase.fireStore.getRoleDocumentByRef(userDocument[FireStoreUserField.ROLE.tag] as DocumentReference)

        /**Save user data*/
        sessionManager?.userRole = roleDocumentSnapshot[FireStoreUserField.ROLE.tag] as String
        sessionManager?.userId = userDocument.id
        sessionManager?.userEmail = userDocument[FireStoreUserField.EMAIL.tag] as String
        if ((userDocument[FireStoreUserField.LEADER_ID.tag] as String?) != null){
            //Admins don't have leaderID
            sessionManager?.leaderId = userDocument[FireStoreUserField.LEADER_ID.tag] as String
        }

        /**Save forestry data*/
        sessionManager?.licenceExpirationDate = (forestryDocument[FireStoreForestryField.EXPIRATION.tag] as Timestamp).seconds
        sessionManager?.forestryName = forestryDocument[FireStoreForestryField.NAME.tag] as String
        sessionManager?.forestryId = userDocument[FireStoreUserField.FORESTRY_ID.tag] as String

        updateUserToken()
    }

    private suspend fun updateUserToken() : Boolean = suspendCoroutine{
        auth?.currentUser?.getIdToken(false)
            ?.addOnSuccessListener { userToken ->
                sessionManager?.userIdToken = userToken.token!!
                it.resume(true)
            }
            ?.addOnFailureListener { e ->
                Log.d(TAG, "Error getting user token", e)
                it.resume(false)
            }
    }

    private fun hasLicenceExpired() : Boolean {
        val expirationDate = forestryDocument[FireStoreForestryField.EXPIRATION.tag] as Timestamp
        val currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

        return expirationDate.seconds < currentTime
    }


    private fun clearSession() {
        auth?.signOut()
        doAsync {
            dataClient?.local?.clearDatabase()
            sessionManager?.clearSession()
        }
    }
}