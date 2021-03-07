package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_session.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject

class SessionActivity : BaseActivity() {

    private val dataClient : DataClient by inject()
    lateinit var recyclerViewAdapter : SessionRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager : LinearLayoutManager

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

        val imageItemList = ArrayList<BaseImageItem>(processedImageItemsInSession)
        imageItemList.addAll(unprocessedImageItemsInSession)

        return imageItemList
    }

    private fun saveBtnClicked() {
        MeasurementManager.currentSessionId = 0
        MeasurementManager.recentlyAddedItemsIds.clear()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}