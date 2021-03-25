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
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.presenter.SessionViewModel
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.measurement.utils.UploadWorker
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        location.setOnClickListener { /**TODO*/ }

        viewModel = ViewModelProvider(this).get(SessionViewModel::class.java)
        AgrolyticsApp.databaseChanged.observe(this, Observer {
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
            sum_volume.text = calculateVolume(it)
            recyclerViewAdapter.notifyDataSetChanged()
        })
    }

    private fun calculateVolume(imageItemList : ArrayList<BaseImageItem>) : String {
        var sum = 0.0
        for (imageItem in imageItemList) {
            when (imageItem.getItemType()) {
                ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> sum += (imageItem as ProcessedImageItem).woodVolume
                ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> return "Nincs kész"
                ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> sum += (imageItem as CachedImageItem).woodVolume
            }
        }
        return sum.round(2).toString()
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
                "A méréscsoport tartalmaz még nem feldolgozott elemeket. A legutóbb hozzáadott mérések" +
                        "feltöltése akkor kezdődik meg, ha a csoportban minden mérés fel lett dolgozva.",
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