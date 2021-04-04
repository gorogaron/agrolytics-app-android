package com.agrolytics.agrolytics_android.ui.profile

import android.os.Bundle
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.android.ext.android.inject

class ProfileActivity : BaseActivity() {

    private val sessionManager : SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        email.text = sessionManager.userEmail
        forestry.text = sessionManager.forestryName
        expiration_date.text = getFormattedDateTime(sessionManager.licenceExpirationDate)

        btn_back.setOnClickListener { onBackPressed() }
        btn_change_password.setOnClickListener { /**TODO*/ }
    }
}