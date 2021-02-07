package com.agrolytics.agrolytics_android.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.system.exitProcess


class LoginActivity: BaseActivity(), LoginScreen {

    private val presenter: LoginPresenter by inject()
    private val sessionManager: SessionManager by inject()
    private val fireStoreDB: FireStoreDB by inject()
    private val roomModule: RoomModule by inject()
    private val appServer: AppServer by inject()

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        0a. checkUserLoggedIn():
            True -> checkExpire()
            False -> startLoginActivity
        0b. checkExpire():
            True -> logoutUser & startLoginActivity
            False -> startMainActivity

        1. loginActivity:
        2. kell email + pw -> ellenőrizni, hogy meg van-e adva
        3. utána gomblistener, ha click, akkor login()
        4. presenter.login() -> SUCCESS alapján reroute vagy hibaüzenet



         */
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        presenter.addView(this)
        presenter.addInjections(arrayListOf(sessionManager, fireStoreDB, auth, roomModule, appServer))

        setLoginButtonBackground()
        btn_login.setOnClickListener {login()}
        setEditTextListeners()
    }

    private fun login() {
        GlobalScope.launch(Dispatchers.Main) {
            var loginResult: Int
            if (checkInputFields(et_email.text.toString(), et_password.text.toString())) {
                showLoading()
                withContext(Dispatchers.IO) { loginResult = presenter.login(et_email?.text?.toString(), et_password?.text?.toString()) }
                hideLoading()
            }
            else {
                loginResult = ConfigInfo.LOGIN.WRONG_INPUT
            }
            when (loginResult) {
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

    override fun loginSuccess() {
        startActivity(MainActivity::class.java, Bundle(), false)
        finish()
    }

    override fun onBackPressed() {
        finish()
        exitProcess(0)
    }

    private fun checkInputFields(email : String?, password : String?) : Boolean{
        return email != null && password != null && email.isNotEmpty() && password.isNotEmpty()
    }

    private fun setLoginButtonBackground(){
        if (checkInputFields(et_email.text.toString(), et_password.text.toString())) {
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