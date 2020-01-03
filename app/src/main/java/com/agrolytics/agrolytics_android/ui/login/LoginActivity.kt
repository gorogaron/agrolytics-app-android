package com.agrolytics.agrolytics_android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityOptionsCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject


class LoginActivity: BaseActivity(), LoginScreen {

    private val presenter: LoginPresenter by inject()
    private val sessionManager: SessionManager by inject()
    private val fireStoreDB: FireStoreDB by inject()
    private val roomModule: RoomModule by inject()

    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        presenter.addView(this)
        presenter.addInjections(arrayListOf(sessionManager, fireStoreDB, auth!!, roomModule))

        btn_login.setOnClickListener { presenter.login(et_email?.text?.toString(), et_password?.text?.toString()) }
    }

    override fun loginSuccess() {
        //val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, iv_logo, "logoImage")
        val intent = Intent(this, MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        //startActivity(intent, activityOptionsCompat.toBundle())
        startActivity(MainActivity::class.java, Bundle(), false)
        finish()
    }

    override fun onBackPressed() {
        finish()
        System.exit(0)
    }

    override fun onStop() {
        super.onStop()
        Log.d("LoginActivity","onStop")
    }

    override fun onPause() {
        finish()
        Log.d("LoginActivity","onPause")
        super.onPause()
    }
}