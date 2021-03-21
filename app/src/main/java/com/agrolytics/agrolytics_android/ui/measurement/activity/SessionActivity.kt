package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.icu.util.Measure
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.agrolytics.agrolytics_android.AgrolyticsApp
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.main.MainViewModel
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.presenter.SessionViewModel
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.measurement.utils.UploadWorker
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

class SessionActivity : BaseActivity() {

    private val dataClient : DataClient by inject()
    lateinit var recyclerViewAdapter : SessionRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager : LinearLayoutManager
    private val workManager = WorkManager.getInstance(application)

    companion object {
        var sessionId : Long = 0
    }

    @KoinApiExtension
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        save.setOnClickListener{ saveBtnClicked() }

        val viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
        viewModel.getAllLocalImagesInSession(sessionId)
        AgrolyticsApp.firebaseUpdates.observe(this, Observer {
            viewModel.getAllLocalImagesInSession(sessionId)
        })

        viewModel.imageItemsInSession.observe(this, Observer {
            if (!::recyclerViewAdapter.isInitialized) {
                recyclerViewAdapter = SessionRecyclerViewAdapter(this@SessionActivity, it)
                recyclerViewLayoutManager = LinearLayoutManager(this@SessionActivity)
                recycler_view.layoutManager = recyclerViewLayoutManager
                recycler_view.adapter = recyclerViewAdapter
            }
            else {
                recyclerViewAdapter.itemList = it
            }
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }


    private fun saveBtnClicked() {
        if (MeasurementManager.recentlyAddedItemTimestamps.isNotEmpty()){
            GlobalScope.launch(Dispatchers.IO) {
                val sessionContainsUnprocessedImages = sessionContainsUnprocessedImages()

                if (!sessionContainsUnprocessedImages) {
                    // Background task indítása a processedImageItemek feltöltéséhez
                    startUploadWorker()
                    finishWithClear()
                }
                else {
                    showUnprocessedAlertDialog()
                }
            }
        }
        else {
            finishWithClear()
        }
    }

    private fun startUploadWorker() {
        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
        for (processedImageItem in processedImageItemsInSession) {
            val inputData = Data.Builder()
                .putLong(ConfigInfo.PROCESSED_IMAGE_ITEM_TIMESTAMP, processedImageItem.timestamp)
                .build()

            val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .addTag(processedImageItem.timestamp.toString())
                .setInputData(inputData)
                .build()

            workManager.enqueueUniqueWork(processedImageItem.timestamp.toString(), ExistingWorkPolicy.KEEP, uploadRequest)
        }
    }

    private suspend fun showUnprocessedAlertDialog() {
        val exitListener = {
            finishWithClear()
        }

        val cancelListener = {
            //Ne csináljunk semmit, csak zárjuk be a popup window-t
        }

        withContext(Dispatchers.Main) {
            show2OptionDialog(
                "A méréscsoport tartalmaz még nem feldolgozott elemeket. A ${MeasurementManager.recentlyAddedItemTimestamps.size}" +
                        "újonnan hozzáadott mérés feltöltése akkor kezdődik meg, ha a csoportban minden mérés fel lett dolgozva.",
                "Kilépés",
                "Mégse",
                exitListener,
                cancelListener)
        }
    }

    private suspend fun sessionContainsUnprocessedImages() : Boolean {
        withContext(Dispatchers.Main) { showLoading() }
        val cachedImageItemList = dataClient.local.unprocessed.getBySessionId(sessionId)
        withContext(Dispatchers.Main) { hideLoading() }
        return cachedImageItemList.isNotEmpty()
    }

    private fun finishWithClear() {
        MeasurementManager.clearMeasurementSession()
        setResult(Activity.RESULT_OK)
        finish()
    }


    override fun onBackPressed() {
        finish()
    }
}