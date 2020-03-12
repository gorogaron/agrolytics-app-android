package com.agrolytics.agrolytics_android.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import android.net.NetworkInfo
import android.net.ConnectivityManager
import org.joda.time.format.DateTimeFormat
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat.getSystemService




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
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val nc = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (nc != null) {
                    val downSpeed = nc.linkDownstreamBandwidthKbps
                    val upSpeed = nc.linkUpstreamBandwidthKbps
                    activeNetworkInfo != null && activeNetworkInfo.isConnected && downSpeed > 100000
                } else {
                    activeNetworkInfo != null && activeNetworkInfo.isConnected
                }
            } else {
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            }
        }


    }
}