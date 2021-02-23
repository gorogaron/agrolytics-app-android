package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.utils.SessionManager
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
        cancel_delete.setOnClickListener{hideDeleteButtons()}

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
//        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
//        val unprocessedImageItemsInSession = dataClient.local.unprocessed.getBySessionId(sessionId)
        val processedImageItemsInSession = dataClient.local.processed.getAll()
        val unprocessedImageItemsInSession = dataClient.local.unprocessed.getAll()

        val imageItemList = ArrayList<BaseImageItem>(processedImageItemsInSession)
        imageItemList.addAll(unprocessedImageItemsInSession)

        return imageItemList
    }

    private fun uploadSession() {
        sessionManager.sessionId = ""
    }


    fun showDeleteButtons(){
        confirm_delete.visibility = View.VISIBLE
        cancel_delete.visibility = View.VISIBLE
        confirm_delete.animate()
            .translationY(-100f)
            .alpha(1f)
        cancel_delete.animate()
            .translationY(-100f)
            .alpha(1f)
    }

    fun hideDeleteButtons(){
        cancel_delete.animate()
            .translationY(100f)
            .alpha(0f)
        confirm_delete.animate()
            .translationY(100f)
            .alpha(0f)
    }
}