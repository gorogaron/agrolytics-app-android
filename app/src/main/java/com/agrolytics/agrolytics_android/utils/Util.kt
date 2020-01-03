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

        interface RxTimer {
            fun done()
        }

        fun dpFromPx(context: Context, px: Float): Float {
            return px / context.resources.displayMetrics.density
        }

        fun pxFromDp(context: Context, dp: Float): Float {
            return dp * context.resources.displayMetrics.density
        }

        fun hideKeyboard(activity: Activity, view: View) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun showKeyboard(activity: Activity) {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        fun getCurrentDateString(): String {
            val c = Calendar.getInstance().time

            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = df.format(c)
            return formattedDate
        }

        fun isEmailValid(email: String): Boolean {
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(email)
            return matcher.matches()
        }

        fun getScreenSize(activity: Activity?): Pair<Int, Int> {
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            return Pair(displayMetrics.heightPixels, displayMetrics.widthPixels)
        }

        fun getActionBarHeight(activity: Activity): Int? {
            val tv = TypedValue()
            return if (activity.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                TypedValue.complexToDimensionPixelSize(tv.data, activity.resources.displayMetrics)
            } else {
                null
            }
        }

        fun getDateMillis(dateString: String, patternFrom: String): Long {
            val formatter = DateTimeFormat.forPattern(patternFrom)
            val dateTo = formatter.withLocale(Locale.ENGLISH).parseDateTime(dateString)
            return dateTo.millis
        }

        fun formatDate(dateString: String, patternFrom: String, patternTo: String): String {
            val formatter = DateTimeFormat.forPattern(patternFrom)
            val date = formatter.withLocale(Locale.ENGLISH).parseDateTime(dateString)
            return date.toString(patternTo)
        }

        fun getDate(milliSeconds: Long, dateFormat: String): String {
            // Create a DateFormatter object for displaying date in specified format.
            val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

            // Create a calendar object that will convert the date and time value in milliseconds to date.
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = milliSeconds
            return formatter.format(calendar.time)
        }

        fun getDateDay(milliSeconds: Long): String {
            return TimeUnit.MILLISECONDS.toDays(milliSeconds).toInt().toString()
        }

        fun setTimer(milliseconds: Long, listener: RxTimer) {
            Observable.timer(milliseconds,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listener.done() })
        }

        fun convert(t: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = t

            val mYear = calendar.get(Calendar.YEAR).toString()
            val mMonth = addZero(calendar.get(Calendar.MONTH) + 1)
            val mDay = addZero(calendar.get(Calendar.DAY_OF_MONTH))

            val hour = addZero(calendar.get(Calendar.HOUR_OF_DAY))
            val min = addZero(calendar.get(Calendar.MINUTE))

            return "$mDay-$mMonth-$mYear $hour:$min"
        }

        private fun addZero(num: Int): String {
            return if (num < 10) {
                "0$num"
            } else {
                "$num"
            }
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

//        fun formatDate(dateString: String, patternFrom: String, patternTo: String): String {
//            val formatter = DateTimeFormat.forPattern(patternFrom)
//            val date = formatter.withLocale(Locale.ENGLISH).parseDateTime(dateString)
//            return date.toString(patternTo)
//        }

        fun setStatusBarGradient(activity: Activity, colors: IntArray) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val window = activity.window
                val background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.statusBarColor = Color.TRANSPARENT
                window.setBackgroundDrawable(background)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                val background = ColorDrawable(colors.first())
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                window.statusBarColor = Color.TRANSPARENT
                window.setBackgroundDrawable(background)
            }
        }
    }
}