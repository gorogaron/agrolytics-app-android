package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.measurement.utils.UploadWorker
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject

class SessionActivity : BaseActivity() {

    private val dataClient : DataClient by inject()
    lateinit var recyclerViewAdapter : SessionRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager : LinearLayoutManager
    private val workManager = WorkManager.getInstance(application)

    companion object {
        var sessionId : Long = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        save.setOnClickListener{ saveBtnClicked() }

        doAsync {

            val imageItemList = getAllLocalImagesInSession()
            recyclerViewAdapter = SessionRecyclerViewAdapter(this@SessionActivity, imageItemList!!)
            recyclerViewLayoutManager = LinearLayoutManager(this@SessionActivity)

            uiThread {
                recycler_view.layoutManager = recyclerViewLayoutManager
                recycler_view.adapter = recyclerViewAdapter
            }
        }
    }

    private fun getAllLocalImagesInSession() : ArrayList<BaseImageItem>?{
        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
        val unprocessedImageItemsInSession = dataClient.local.unprocessed.getBySessionId(sessionId)
        val cachedImageItemsInSession = dataClient.local.cache.getBySessionId(sessionId)

        val imageItemList = ArrayList<BaseImageItem>(processedImageItemsInSession)
        imageItemList.addAll(unprocessedImageItemsInSession)
        imageItemList.addAll(cachedImageItemsInSession)

        return imageItemList
    }

    private fun saveBtnClicked() {

        // Background task indítása a processedImageItemek feltöltéséhez
        GlobalScope.launch(Dispatchers.IO) {
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

        MeasurementManager.currentSessionId = 0
        MeasurementManager.recentlyAddedItemTimestamps.clear()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}