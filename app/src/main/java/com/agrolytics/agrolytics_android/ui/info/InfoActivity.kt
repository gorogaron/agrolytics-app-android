package com.agrolytics.agrolytics_android.ui.info

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.utils.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject

class InfoActivity: BaseActivity(), View.OnClickListener {

	private val TAG = "InfoActivity"

	private val sessionManager: SessionManager by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_info)

		btn_back.setOnClickListener { onBackPressed() }

		container_profile.setOnClickListener(this)
		container_guide.setOnClickListener(this)
		container_impressum.setOnClickListener(this)
		container_logout.setOnClickListener(this)

		container_impressum.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen))
	}

	override fun onClick(v: View?) {
		when (v?.id) {
			R.id.container_profile -> {/*TODO*/}
			R.id.container_impressum -> openActivity(MenuItem.INFO)
			R.id.container_guide -> openActivity(MenuItem.GUIDE)
			R.id.container_logout -> {/*TODO*/}
		}
	}

	private fun openActivity(menuItem: MenuItem) {
		when (menuItem) {
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