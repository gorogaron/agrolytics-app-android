package com.agrolytics.agrolytics_android.ui.setLength

import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.utils.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.extensions.showMessageWithSnackBar
import kotlinx.android.synthetic.main.activity_set_length.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject

class LengthActivity: BaseActivity(), View.OnClickListener {

	private val TAG = "LengthActivity"

	private val sessionManager: SessionManager by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_set_length)

		tv_previous.text = "Jelenlegi hosszúság: ${sessionManager.woodLength}" + " m"

		btn_back.setOnClickListener { onBackPressed() }

		btn_save_length.setOnClickListener {
			if (et_length.text.isNotEmpty()) {
				sessionManager.woodLength = et_length.text.toString().toFloat()
				tv_previous.text = "Jelenlegi hosszúság: ${sessionManager.woodLength}" + " m"
				btn_save_length.showMessageWithSnackBar("Hossz mentve",4000)
				Util.hideKeyboard(this,et_length)
			} else {
				btn_save_length.showMessageWithSnackBar("Üres mező",3000)
			}
		}

		container_profile.setOnClickListener(this)
		container_guide.setOnClickListener(this)
		container_impressum.setOnClickListener(this)
		container_logout.setOnClickListener(this)

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
			MenuItem.LENGTH -> {
				if (MenuItem.LENGTH.tag != TAG) {
					startActivity(LengthActivity::class.java, Bundle(), false)
				}
			}
			MenuItem.MAIN -> {
				if (MenuItem.MAIN.tag != TAG) {
					startActivity(MainActivity::class.java, Bundle(),false)
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