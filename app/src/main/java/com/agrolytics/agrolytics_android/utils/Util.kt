package com.agrolytics.agrolytics_android.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class Util {
    companion object {

        var lat: Double? = null
        var long: Double? = null

        fun hideKeyboard(activity: Activity, view: View) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }

        @SuppressLint("SimpleDateFormat")
        fun getFormattedDateTime(epochSeconds : Long) : String {
            //A SimpleDateFormat objektum már a rendszer időzónáját kezelni fogja, és annak megfelelően
            //konvertálja string-gé az unix timestampet.
            val date = Date(epochSeconds * 1000)
            return SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
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
            builder.setPositiveButton("Ok") { _, _ ->
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.darkGrey))
        }

        fun meter(context: Context, value : Double) : String {
            return value.toString() + " " + context.getString(R.string.meter)
        }

        fun cubicMeter(context: Context, value : Double) : String {
            return value.toString() + " " + context.getString(R.string.cubic_meter)
        }

        fun setLocale(context: Context, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources: Resources = context.resources
            val config: Configuration = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

    }
}