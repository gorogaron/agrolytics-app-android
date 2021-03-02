package com.agrolytics.agrolytics_android.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.SessionActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.internal.UTC
import java.io.IOException
import java.net.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.round


class Util {
    companion object {

        var lat: Double? = null
        var long: Double? = null

        fun hideKeyboard(activity: Activity, view: View) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getFormattedDateTime(epochSeconds : Long) : String {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("UTC+1"))
            val localDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            return localDateTimeFormatter.format(localDateTime)
        }

        fun isNetworkAvailable(): Boolean {
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

        fun Double.round(decimals: Int): Double {
            var multiplier = 1.0
            repeat(decimals) { multiplier *= 10 }
            return round(this * multiplier) / multiplier
        }

        fun showParameterSettingsWindow(context: Context, sessionManager: SessionManager, blurFunction: ((Int) -> Unit)? = null) {
            if (blurFunction != null) {
                blurFunction(10)
            }
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Adatok")
            val view = LayoutInflater.from(context).inflate(R.layout.parameter_settings_dialog, null, false)

            val et_length_rod = view.findViewById<EditText>(R.id.et_length_rod)
            val et_length_wood = view.findViewById<EditText>(R.id.et_wood_length)

            et_length_rod.setText(sessionManager.rodLength.toString())
            et_length_wood.setText(sessionManager.woodLength.toString())

            val spinner = view.findViewById<Spinner>(R.id.wood_type_spinner)
            val spinnerAdapter = ArrayAdapter.createFromResource(context, R.array.wood_types, android.R.layout.simple_spinner_item)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter

            builder.setView(view)
            builder.setPositiveButton("Ok") { dialog, which ->
                if (et_length_rod.text.isNotEmpty()) {
                    sessionManager.rodLength = et_length_rod.text.toString().toFloat()
                    sessionManager.woodType = spinner.selectedItem.toString()
                    sessionManager.woodLength = et_length_wood.text.toString().toFloat()
                }
                hideKeyboard(context as Activity, et_length_rod)
                if (blurFunction != null){
                    blurFunction(0)
                }
            }

            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
            dialog.window.setDimAmount(0.0f)
            dialog.show()
        }

    }
}