package com.agrolytics.agrolytics_android

import android.app.Application
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.agrolytics.agrolytics_android.koin.appModule
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageSegmentation
import okhttp3.internal.notifyAll
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.io.File
import java.io.IOException


class AgrolyticsApp: Application() {

	companion object {
		/**MainViewModel-ben triggereljük, amikor firebase-re érkezik vagy törlődik egy userhez tartozó item*/
		//TODO: Jobb módszer keresése egy globális LiveData objektum kezelésére
		val databaseChanged = MutableLiveData<Unit>()
	}

	override fun onCreate() {
		super.onCreate()
		ImageSegmentation.init(assets)
		startKoin {
			androidContext(this@AgrolyticsApp)
			modules(appModule)
		}

		if (BuildConfig.DEBUG){
			writeLogsToTxt()
		}
	}

	private fun writeLogsToTxt(){
		if (isExternalStorageWritable()) {
			val appDirectory = File(Environment.getExternalStorageDirectory().toString() + "/AgrolyticsApp")
			val logDirectory = File("$appDirectory/logs")
			val logFileName = "logcat_" + System.currentTimeMillis() + ".txt"
			val logFile = File(logDirectory, logFileName)

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
				Runtime.getRuntime().exec("logcat -c")
				Runtime.getRuntime().exec("logcat -f $logFile")
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}

	/* Checks if external storage is available for read and write */
	private fun isExternalStorageWritable(): Boolean {
		val state: String = Environment.getExternalStorageState()
		return Environment.MEDIA_MOUNTED == state
	}
}