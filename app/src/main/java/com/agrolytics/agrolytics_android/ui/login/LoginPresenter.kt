package com.agrolytics.agrolytics_android.ui.login

import android.content.Context
import android.util.Log
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class LoginPresenter(val context: Context) : BasePresenter<LoginScreen>() {

    val TAG = "LoginPresenter"

    fun login(email: String?, password: String?) {
        if (Util.isNetworkAvailable(context)) {
            if (email != null && password != null
                && email.isNotEmpty() && password.isNotEmpty()
            ) {
                screen?.showLoading()
                auth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth?.currentUser
                            getUser(user)
                        } else {
                            screen?.showToast("Hibás email vagy jelszó")
                            screen?.hideLoading()
                        }
                    }
            }
        } else {
            screen?.showToast("Nincs internetkapcsolat")
        }
    }

    private fun saveUser(
        role: String,
        userID: String?,
        leaderID: String?,
        userEmail: String?,
        forestryID: String?,
        firstLogin: String?
    ) {
        sessionManager?.userRole = role
        userID?.let { sessionManager?.userID = userID }
        leaderID?.let { sessionManager?.leaderID = leaderID }
        userEmail?.let { sessionManager?.userEmail = userEmail }
        forestryID?.let { sessionManager?.forestryID = forestryID }
        firstLogin?.let { sessionManager?.firstLogin = firstLogin }
        screen?.hideLoading()
        screen?.loginSuccess()
    }

    private fun getUser(user: FirebaseUser?) {
        user?.let { fbUser ->
            val userId = fbUser.uid
            val userColRef = fireStoreDB?.db?.collection("user")?.document(userId)
            userColRef?.get()
                ?.addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        val roleRef = document["role"] as DocumentReference
                        Log.d(TAG, "role document")
                        roleRef.get()
                            .addOnSuccessListener { roleDocument ->
                                Log.d(TAG, "role document")
                                val role = roleDocument["role"] as String
                                val firstLogin = document["first_login"] as String?

                                if (firstLogin != null) {

                                    val firstLoginDate = DateTime.parse(firstLogin)
                                    val firstLoginDateThirtyAdded = firstLoginDate.plusDays(30)
                                    val currentDate = DateTime.now()

                                    val msDiff = firstLoginDateThirtyAdded.millis - currentDate.millis
                                    val daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff)

                                    if (daysDiff > 0) {
                                        saveUser(
                                            role,
                                            document.id as String?,
                                            document["leaderID"] as String?,
                                            document["email"] as String?,
                                            document["forestry"] as String?,
                                            firstLogin
                                        )
                                    } else {
                                        FirebaseAuth.getInstance().signOut()
                                        doAsync {
                                            roomModule?.database?.clearAllTables()
                                            sessionManager?.clearSession()
                                            uiThread {
                                                screen?.showToast("Your free trial has expired.")
                                                screen?.hideLoading()
                                            }

                                        }
                                    }

                                } else {
                                    userColRef.update("first_login", Util.getCurrentDateString())
                                        .addOnSuccessListener {
                                            saveUser(
                                                role,
                                                document.id as String?,
                                                document["leaderID"] as String?,
                                                document["email"] as String?,
                                                document["forestry"] as String?,
                                                firstLogin
                                            )
                                        }
                                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                                }
                            }
                            .addOnFailureListener {
                                screen?.showToast("Something went wrong.")
                            }
                    } else {
                        screen?.showToast("Something went wrong.")
                    }
                }
                ?.addOnFailureListener { exception ->
                    screen?.showToast("Something went wrong.")
                }
        }
    }

}