package com.agrolytics.agrolytics_android.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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
            return runBlocking { withContext(Dispatchers.IO){ isOnline()} }
        }

        suspend fun isOnline(): Boolean = suspendCoroutine { cont ->
            try {
                var addresses = InetAddress.getAllByName("www.google.com")
                cont.resume(addresses[0].hostAddress != "")
            } catch (e: UnknownHostException) {
                // TODO: handle error
                cont.resume(false)
            }
        }

        fun isInternetAvailable(): Boolean {
            return try {
                val sock = Socket()
                val sockaddr: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                runBlocking {
                    withContext(Dispatchers.IO) {
                        sock.connect(sockaddr, 1000) // This will block no more than timeoutMs
                    }
                }
                sock.close()
                true
            } catch (e: IOException) {
                false
            }
        }

    }
}