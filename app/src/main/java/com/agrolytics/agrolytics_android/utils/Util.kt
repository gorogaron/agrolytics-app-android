package com.agrolytics.agrolytics_android.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.*
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*


class Util {
    companion object {

        var lat: Double? = null
        var long: Double? = null

        fun hideKeyboard(activity: Activity, view: View) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getCurrentDateString(): String {
            val c = Calendar.getInstance().time

            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = df.format(c)
            return formattedDate
        }

        fun isNetworkAvailable(context: Context): Boolean {
            /* Old method:
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
             */

            /*New Method:*/
            return runBlocking { isOnline() }
        }

        suspend fun isOnline(): Boolean {
            try {
                lateinit var addresses: Array<out InetAddress>
                GlobalScope.async{
                    addresses = InetAddress.getAllByName("www.google.com")
                }.await()
                return addresses[0].hostAddress != ""
            } catch (e: UnknownHostException) {
                // TODO: handle error
            }
            return false
        }

    }
}