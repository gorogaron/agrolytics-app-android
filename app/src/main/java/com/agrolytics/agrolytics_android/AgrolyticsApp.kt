package com.agrolytics.agrolytics_android

import android.app.Application
import android.os.Environment
import com.agrolytics.agrolytics_android.koin.appModule
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.android.startKoin
import java.io.File
import java.io.IOException


class AgrolyticsApp: Application() {

	override fun onCreate() {
		super.onCreate()
		startKoin(this, listOf(appModule))
		JodaTimeAndroid.init(this)

		if (BuildConfig.DEBUG){
			writeLogsToTxt()
		}

	}

	private  fun writeLogsToTxt(){
		if (isExternalStorageWritable()) {
			val appDirectory =
				File(Environment.getExternalStorageDirectory().toString() + "/AgrolyticsApp")
			val logDirectory = File(appDirectory.toString() + "/logs")
			val logFile =
				File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")

			// create app folder
			if (!appDirectory.exists()) {
				appDirectory.mkdir()
			}

			// create log folder
			if (!logDirectory.exists()) {
				logDirectory.mkdir()
			}

			// clear the previous logcat and then write the new one to the file
			try {
				var process = Runtime.getRuntime().exec("logcat -c")
				process = Runtime.getRuntime().exec("logcat -f $logFile")
			} catch (e: IOException) {
				e.printStackTrace()
			}
		} else if (isExternalStorageReadable()) {
			// only readable
		} else {
			// not accessible
		}
	}

	/* Checks if external storage is available for read and write */
	private fun isExternalStorageWritable(): Boolean {
		val state: String = Environment.getExternalStorageState()
		return Environment.MEDIA_MOUNTED.equals(state)
	}

	/* Checks if external storage is available to at least read */
	private fun isExternalStorageReadable(): Boolean {
		val state: String = Environment.getExternalStorageState()
		return Environment.MEDIA_MOUNTED == state ||
				Environment.MEDIA_MOUNTED_READ_ONLY == state
	}
}