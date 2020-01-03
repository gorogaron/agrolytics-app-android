package com.agrolytics.agrolytics_android.ui.imageFinished.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseFragment
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedScreen
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_upload_finished.*
import org.koin.android.ext.android.inject

class UploadFinishedFragment: BaseFragment() {

    private var listener: UploadFinishedScreen? = null

    private val sessionManager: SessionManager by inject()

    override fun getLayoutId(): Int {
        return R.layout.fragment_upload_finished
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            if (it.containsKey(ConfigInfo.UPLOAD_RESPONSE) && it.containsKey(ConfigInfo.PATH)) {
                if (it.containsKey(ConfigInfo.ID)) {
                    setUpView(it.getParcelable(ConfigInfo.UPLOAD_RESPONSE), it.getString(ConfigInfo.PATH), it.getString(ConfigInfo.ID))
                } else {
                    setUpView(it.getParcelable(ConfigInfo.UPLOAD_RESPONSE), it.getString(ConfigInfo.PATH), null)
                }
            }
        }
    }

    private fun setUpView(responseImageUpload: ResponseImageUpload?, path: String?, id: String?) {
        btn_decline?.setOnClickListener { listener?.onDeclineClicked(this, id) }
        btn_accept?.setOnClickListener { listener?.onAcceptClicked(responseImageUpload, path, this, id) }
        btn_new?.setOnClickListener { listener?.onDeclineClicked(this, id) }
        btn_next?.setOnClickListener { listener?.onNextPage(this) }
        btn_back?.setOnClickListener { listener?.onPreviousPage(this) }

        responseImageUpload?.image?.let {
            val decodedBytes = Base64.decode(it, 0)
            val image = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            activity?.let {
                Glide.with(it)
                        .load(image)
                        .into(iv_response_image)
            }

            responseImageUpload.result?.let { result ->
                tv_result.text =
                        getString(R.string.wood_volume_value, (result.toFloat() * sessionManager.length))
            }
        }
    }

    fun updateDeclineView() {
        container_declined.visibility = View.VISIBLE
        container_selection.visibility = View.GONE
    }

    fun updateView() {
        container_after_selection.visibility = View.VISIBLE
        container_selection.visibility = View.GONE
    }

    fun showNextArrow(show: Boolean) {
        if (show) btn_next?.visibility = View.VISIBLE else btn_next?.visibility = View.GONE
    }

    fun showBackArrow(show: Boolean) {
        if (show) btn_back?.visibility = View.VISIBLE else btn_back?.visibility = View.GONE
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UploadFinishedScreen) {
            listener = context
        } else {
            throw RuntimeException("$context must implement UploadFinishedScreen")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {

        private val UPLOAD_RESPONSE = ConfigInfo.UPLOAD_RESPONSE
        private val PATH = ConfigInfo.PATH
        private val ID = ConfigInfo.ID

        fun newInstance(responseImageUpload: ResponseImageUpload, path: String, id: String?): UploadFinishedFragment {
            val fragment = UploadFinishedFragment()
            val args = Bundle()
            args.putParcelable(UPLOAD_RESPONSE, responseImageUpload)
            args.putString(PATH, path)
            id?.let { args.putString(ID, id) }
            fragment.arguments = args
            return fragment
        }

        fun getTag(): String {
            return UploadFinishedFragment::class.qualifiedName!!
        }
    }

}