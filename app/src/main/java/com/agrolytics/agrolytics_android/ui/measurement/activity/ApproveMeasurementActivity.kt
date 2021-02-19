package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.os.Bundle
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.database.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.ui.measurement.presenter.ApproveMeasurementPresenter
import kotlinx.android.synthetic.main.activity_upload_finished.*
import kotlinx.coroutines.*


class ApproveMeasurementActivity : BaseActivity() {

	private val sessionManager: SessionManager by inject()
	private val presenter: ApproveMeasurementPresenter by inject()
	private val dataClient: DataClient by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upload_finished)

		presenter.addView(this)
		presenter.addInjections(arrayListOf(sessionManager, dataClient))

		btn_decline.setOnClickListener{ onDeclineClicked() }
		btn_accept.setOnClickListener{ onAcceptClicked() }
		image.setImageBitmap(processedImageItem.image)
	}

	fun onDeclineClicked() {
		when (method){
			"online" -> {
				//TODO: Új mérés vagy session áttekintése?
			}
			"offline" -> {
				//TODO: Szeretnéd később online megpróbálni?
			}
		}

		/*if (fragmentList.size == 1) {
			//Return to main activity
			val intent = Intent(this, MainActivity::class.java)
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			fragment.updateDeclineView()
			presenter.deleteImageFromLocalDatabase(id)
		}*/
	}

	fun onAcceptClicked() {
		showLoading()
		GlobalScope.launch {
			withContext(Dispatchers.IO) {
				presenter.uploadMeasurementToFirebase(processedImageItem, method)
				hideLoading()
			}
		}
	}

	companion object Result {
	    lateinit var processedImageItem : ProcessedImageItem
		lateinit var method : String
	}
}
