package com.agrolytics.agrolytics_android.ui.guide

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.ui.setLength.LengthActivity
import com.agrolytics.agrolytics_android.utils.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject

class GuideActivity: BaseActivity(), View.OnClickListener  {

    private val TAG = "GuideActivity"
    private val sessionManager: SessionManager by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        btn_back.setOnClickListener { onBackPressed() }
        container_images.setOnClickListener(this)
        container_info.setOnClickListener(this)
        container_main_menu.setOnClickListener(this)
        container_map.setOnClickListener(this)
        container_set_length.setOnClickListener(this)
        container_sign_out.setOnClickListener(this)

        container_info.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen))
        tv_email.text = sessionManager.userEmail
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.container_images -> openActivity(MenuItem.IMAGES)
            R.id.container_info -> openActivity(MenuItem.INFO)
            R.id.container_main_menu -> openActivity(MenuItem.MAIN)
            R.id.container_map -> openActivity(MenuItem.MAP)
            R.id.container_set_length -> openActivity(MenuItem.LENGTH)
        }
    }

    private fun openActivity(menuItem: MenuItem) {
        when (menuItem) {
            MenuItem.LENGTH -> {
                if (MenuItem.LENGTH.tag != TAG) {
                    startActivity(LengthActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.MAIN -> {
                if (MenuItem.MAIN.tag != TAG) {
                    startActivity(MainActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.IMAGES -> {
                if (MenuItem.IMAGES.tag != TAG) {
                    startActivity(ImagesActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.MAP -> {
                if (MenuItem.MAP.tag != TAG) {
                    startActivity(MapActivity::class.java, Bundle(), false)
                }
            }
            MenuItem.INFO -> {
                if (MenuItem.INFO.tag != TAG) {
                    startActivity(InfoActivity::class.java, Bundle(), false)
                }
            }
        }
        drawer_layout.closeDrawers()
    }
}