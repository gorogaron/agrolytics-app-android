package com.agrolytics.agrolytics_android.ui.profile
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.Util.Companion.getFormattedDateTime
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.android.ext.android.inject
import java.util.*


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
        btn_change_language.setOnClickListener { showLanguageSelectionDialog() }
    }

    fun showLanguageSelectionDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        val view = LayoutInflater.from(this).inflate(R.layout.language_selection_dialog, null, false)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.btn_english).setOnClickListener {
            changeAppLanguage("en")
            dialog.cancel()
        }
        view.findViewById<TextView>(R.id.btn_russian).setOnClickListener {
            changeAppLanguage("ru")
            dialog.cancel()
        }
        view.findViewById<TextView>(R.id.btn_hungarian).setOnClickListener {
            changeAppLanguage("hu")
            dialog.cancel()
        }

        dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
        dialog.show()
    }

    private fun changeAppLanguage(languageCode : String) {
        sessionManager.language = languageCode
        Util.setLocale(this@ProfileActivity, languageCode)

        //Új MainActivity indítása, hogy érvénybe lépjen a nyelv változtatás
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}