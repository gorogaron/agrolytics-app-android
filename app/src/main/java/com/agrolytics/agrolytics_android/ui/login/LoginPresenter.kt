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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoginPresenter(val context: Context) : BasePresenter<LoginScreen>() {

    private lateinit var userDocument : DocumentSnapshot

    suspend fun login(email: String?, password: String?) : Int {
        var signInResult: Int
        try {
            if (Util.isNetworkAvailable(context)) {
                    signInResult = signInFirebaseUser(email!!, password!!)
                    if (signInResult == ConfigInfo.LOGIN.SUCCESS) {
                        val firstLogin = getFirstLogin()
                        signInResult = if (hasUserExpired(firstLogin)) {
                                ConfigInfo.LOGIN.USER_EXPIRED
                            } else {
                                saveUser()
                                ConfigInfo.LOGIN.SUCCESS
                            }
                    } else {
                        signInResult = ConfigInfo.LOGIN.AUTH_FAILED
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

    fun getFirstLogin() : String? {
        return userDocument["first_login"] as String?
    }

    suspend fun signInFirebaseUser(email: String, password: String) : Int = suspendCoroutine{ cont ->
        auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                GlobalScope.launch {
                    userDocument = getUserDocument(auth?.currentUser)
                    cont.resume(ConfigInfo.LOGIN.SUCCESS)
                }
            }
            else{
                cont.resume(ConfigInfo.LOGIN.AUTH_FAILED)
            }
        }
    }

    suspend fun saveUser() {
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
        auth?.signOut()
        doAsync {
            roomModule?.database?.clearAllTables()
            sessionManager?.clearSession()
        }
    }
}