package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.ImageItemBase
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.api.Distribution
import kotlinx.android.synthetic.main.activity_session.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject

class SessionActivity : BaseActivity() {

    private val dataClient : DataClient by inject()
    private val sessionManager : SessionManager by inject()
    lateinit var recyclerViewAdapter : SessionRecyclerViewAdapter
    lateinit var recyclerViewLayoutManager : LinearLayoutManager

    companion object {
        var sessionId : String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        upload.setOnClickListener{uploadSession()}

        doAsync {

            val imageItemList = getAllLocalImagesInSession()
            recyclerViewAdapter = SessionRecyclerViewAdapter(imageItemList!!)
            recyclerViewLayoutManager = LinearLayoutManager(this@SessionActivity)

            uiThread {
                recycler_view.layoutManager = recyclerViewLayoutManager
                recycler_view.adapter = recyclerViewAdapter
            }
        }
    }

    private fun getAllLocalImagesInSession() : ArrayList<ImageItemBase>?{
        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
        val unprocessedImageItemsInSession = dataClient.local.unprocessed.getBySessionId(sessionId)

        val imageItemList = ArrayList<ImageItemBase>(processedImageItemsInSession)
        imageItemList.addAll(unprocessedImageItemsInSession)

        return imageItemList
    }

    private fun uploadSession() {
        sessionManager.sessionId = ""
    }
}