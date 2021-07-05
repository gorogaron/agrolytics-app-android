package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.content.Intent
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
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.presenter.SessionViewModel
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.measurement.utils.UploadWorker
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
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
    private lateinit var viewModel : SessionViewModel

    companion object {
        var sessionId : Long = 0
        var correspondingApproveMeasurementActivity : ApproveMeasurementActivity? = null
    }

    @KoinApiExtension
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        save.setOnClickListener{ saveBtnClicked() }
        add.setOnClickListener { MeasurementManager.addNewMeasurementForSession(this, sessionId) }
        location.setOnClickListener { MapActivity.openMapForSession(this, sessionId) }

        viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
        AgrolyticsApp.databaseChanged.observe(this, Observer {
            viewModel.getAllLocalImagesInSession(sessionId)
        })
        viewModel.imageItemsInSession.observe(this, Observer {
            if (it.size > 0) {
                if (!::recyclerViewAdapter.isInitialized) {
                    recyclerViewAdapter = SessionRecyclerViewAdapter(this@SessionActivity, it)
                    recyclerViewLayoutManager = LinearLayoutManager(this@SessionActivity)
                    recycler_view.layoutManager = recyclerViewLayoutManager
                    recycler_view.adapter = recyclerViewAdapter
                }
                else {
                    recyclerViewAdapter.itemList = it
                }
                val sumVolumeValue = calculateVolume(it)
                sum_volume.text = if (viewModel.isSessionDone()) Util.cubicMeter(this, sumVolumeValue) else getString(R.string.not_done)
                recyclerViewAdapter.notifyDataSetChanged()
            }
            else {
                showToast(getString(R.string.empty_session))
                finish()
            }
        })
    }


    private fun calculateVolume(imageItemList : ArrayList<BaseImageItem>) : Double {
        var sum = 0.0
        for (imageItem in imageItemList) {
            when (imageItem.getItemType()) {
                ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> sum += (imageItem as ProcessedImageItem).woodVolume + imageItem.addedWoodVolume
                ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> return 0.0
                ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> sum += (imageItem as CachedImageItem).woodVolume + imageItem.addedWoodVolume
            }
        }
        return sum.round(2)
    }

    private fun saveBtnClicked() {
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

    private fun startUploadWorker() {
        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
        for (processedImageItem in processedImageItemsInSession) {
            val inputData = Data.Builder()
                .putLong(ConfigInfo.PROCESSED_IMAGE_ITEM_TIMESTAMP, processedImageItem.timestamp)
                .build()

            val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .addTag("UPLOAD" + processedImageItem.timestamp.toString())
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
                getString(R.string.unprocessed_warning),
                getString(R.string.exit),
                getString(R.string.cancel),
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
        if (correspondingApproveMeasurementActivity != null) {
            correspondingApproveMeasurementActivity!!.finish()
        }
        MeasurementManager.clearMeasurementSession()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllLocalImagesInSession(sessionId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK ){
            when (requestCode) {
                ConfigInfo.IMAGE_CAPTURE, ConfigInfo.IMAGE_BROWSE -> {
                    if (correspondingApproveMeasurementActivity != null) {
                        correspondingApproveMeasurementActivity!!.finish()
                    }
                    finish()
                }
            }
        }
    }

}