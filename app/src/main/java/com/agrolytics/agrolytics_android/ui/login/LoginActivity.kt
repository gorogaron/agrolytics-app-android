package com.agrolytics.agrolytics_android.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess


class LoginActivity: BaseActivity(), LoginScreen {

    private val presenter: LoginPresenter by inject()
    private val sessionManager: SessionManager by inject()
    private val dataClient: DataClient by inject()
    private val appServer: AppServer by inject()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        presenter.addView(this)
        presenter.activity = this
        presenter.addInjections(arrayListOf(sessionManager, dataClient, auth, appServer))
        
        // TODO: Blocking LoginActivity until checkUserLoggedInState() finished
        checkUserLoggedInState()

        setContentView(R.layout.activity_login)
        setLoginButtonBackground()
        btn_login.setOnClickListener {
            loginUser()
        }
        setEditTextListeners()
    }

    private fun loginUser() {
        GlobalScope.launch(Dispatchers.Main) {
            var loginResultCode: ConfigInfo.LOGIN
            val email = et_email.text.toString()
            val password = et_password.text.toString()
            if (email.isNotBlank() && password.isNotEmpty()) {
                showLoading()
                withContext(Dispatchers.IO) {
                    loginResultCode = presenter.login(email, password)
                }
                hideLoading()
            }
            else {
                loginResultCode = ConfigInfo.LOGIN.WRONG_INPUT
            }
            when (loginResultCode) {
                ConfigInfo.LOGIN.NO_INTERNET -> showToast("Nincs internetkapcsolat")
                ConfigInfo.LOGIN.AUTH_FAILED -> showToast("Hibás email cím vagy jelszó")
                ConfigInfo.LOGIN.USER_EXPIRED -> showToast("A felhasználóhoz tartozó licensz lejárt, kérjük vegye fel velünk a kapcsolatot")
                ConfigInfo.LOGIN.UNDEFINED ->  {/*DO NOTHING*/}
                ConfigInfo.LOGIN.WRONG_INPUT -> showToast("Írja be a felhasználónevet és jelszót.")
                ConfigInfo.LOGIN.ERROR -> showToast("Váratlan hiba történt a bejelentkezés során")
                ConfigInfo.LOGIN.SUCCESS -> {
                    showToast("Sikeres bejelentkezés")
                    loginSuccess()
                }
            }
        }
    }

    private fun checkUserLoggedInState() {
        if (auth.currentUser != null) {
            GlobalScope.launch(Dispatchers.IO) {
                when (presenter.checkLicence()) {
                    ConfigInfo.LOGIN.SUCCESS -> loginSuccess()
                    else -> {}
                }
            }
        }
    }

    override fun loginSuccess() {
        startActivity(MainActivity::class.java, Bundle(), false)
        finish()
    }

    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

    private fun setLoginButtonBackground(){
        if (et_email.text.toString().isNotEmpty() && et_password.text.toString().isNotEmpty()) {
            btn_login.setBackgroundResource(R.drawable.login_btn_clickable)
        }
        else {
            btn_login.setBackgroundResource(R.drawable.login_btn_unclickable)
        }
    }

    private fun setEditTextListeners(){
        et_email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                setLoginButtonBackground()
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })

        et_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                setLoginButtonBackground()
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        })
    }
}