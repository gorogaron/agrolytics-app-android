package com.agrolytics.agrolytics_android.ui.images

import android.content.Intent
import android.graphics.BitmapFactory
import android.icu.util.Measure
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.ui.images.adapter.ImagesAdapter
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.setLength.LengthActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.extensions.animateSlide
import com.agrolytics.agrolytics_android.utils.extensions.fadeIn
import com.agrolytics.agrolytics_android.utils.extensions.fadeOut
import com.agrolytics.agrolytics_android.utils.extensions.showMessageWithSnackBar
import com.agrolytics.agrolytics_android.utils.gallery.GalleryDialog
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.custom_bottom_toolbar.*
import kotlinx.android.synthetic.main.custom_bottom_toolbar.view.*
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject


class ImagesActivity: BaseActivity(), ImagesScreen, ImagesAdapter.OnImageListener,
		View.OnClickListener, BaseActivity.OnDialogActions {

	private val TAG = "ImagesActivity"

	private var selectedTab = 0
	private var toolbarOpened = false

	private var adapter: ImagesAdapter? = null

	private val presenter: ImagesPresenter by inject()
	private val sessionManager: SessionManager by inject()
	private val roomModule: RoomModule by inject()
	private val appServer: AppServer by inject()
	private val fireStoreDB: FireStoreDB by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)

		adapter = ImagesAdapter(this, sessionManager.length)
		list_images.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
		list_images.adapter = adapter

		presenter.addView(this)
		presenter.addInjections(arrayListOf(sessionManager, roomModule, appServer, fireStoreDB))
		presenter.setActivity(this)

		btn_back.setOnClickListener(this)
		btn_cancel.setOnClickListener(this)
		btn_send.setOnClickListener(this)
		btn_delete.setOnClickListener(this)
		btn_settings.setOnClickListener(this)
		btn_select_all.setOnClickListener(this)
		container_images.setOnClickListener(this)
		container_info.setOnClickListener(this)
		container_main_menu.setOnClickListener(this)
		container_map.setOnClickListener(this)
		container_set_length.setOnClickListener(this)
		container_sign_out.setOnClickListener(this)

		setUpView()
	}

	override fun onResume() {
		super.onResume()
		//presenter.fireBaseList.clear()
		if (selectedTab == 0) {
			presenter.subscribeForImageEvents()
		} else {
			presenter.getImages(false)
		}
	}

	override fun onClick(v: View?) {
		when (v?.id) {
			R.id.btn_back -> onBackPressed()
			R.id.btn_send -> {
				adapter?.getAllSelected()?.let {
					if (it.isEmpty()) {
						btn_send.showMessageWithSnackBar(getString(R.string.select_item),3000)
					} else {
						sendImage()
					}
				}
			}
			R.id.btn_delete -> showSelected()
			R.id.btn_settings -> {
				if (toolbarOpened) {
					closeBottomToolbar()
				} else {
					showBottomToolbar()
				}
			}
			R.id.btn_select_all -> adapter?.selectAll()
			R.id.btn_cancel -> closeBottomToolbar()
			R.id.container_images -> openActivity(MenuItem.IMAGES)
			R.id.container_info -> openActivity(MenuItem.INFO)
			R.id.container_main_menu -> openActivity(MenuItem.MAIN)
			R.id.container_map -> openActivity(MenuItem.MAP)
			R.id.container_set_length -> openActivity(MenuItem.LENGTH)
		}
	}

	private fun setUpView() {

		container_images.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGreen))
		tv_email.text = sessionManager.userEmail

		tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.processed)))
		tab_layout.addTab(tab_layout.newTab().setText(getString(R.string.not_processed)))

		tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) { }

			override fun onTabUnselected(tab: TabLayout.Tab?) { }

			override fun onTabSelected(tab: TabLayout.Tab?) {
				adapter?.getAllSelected()?.let { selectedList ->
					if (selectedList.size == 0) {
						tab?.let {
							selectedTab = it.position
							when (it.position) {
								0 -> changeToProcessed()
								1 -> changeToNotProcessed()
							}
						}
					}
				}
			}
		})
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
					Toast.makeText(this,"Map deleted", Toast.LENGTH_SHORT).show()
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

	override fun sendResultsToFragment(responseList: ArrayList<Pair<MeasurementResult, Pair<String, String>>>?) {
		val intent = Intent(this, UploadFinishedActivity::class.java)
		val responses = arrayListOf<MeasurementResult>()
		val pathList = arrayListOf<String>()
		val idList = arrayListOf<String>()
		responseList?.let {
			if (it.isNotEmpty()) {
				for (pair in responseList) {
					if (pair.first.getMaskedInput() != null && pair.first.getVolume() != null) {
						responses.add(pair.first)
						pathList.add(pair.second.first)
						idList.add(pair.second.second)
					}
				}
				if (pathList.isNotEmpty() && idList.isNotEmpty() && responseList.isNotEmpty()) {
					UploadFinishedActivity.responseList = responses
					intent.putStringArrayListExtra(ConfigInfo.PATH, pathList)
					intent.putStringArrayListExtra(ConfigInfo.ID, idList)
					intent.putExtra(ConfigInfo.METHOD, "online")
					startActivity(intent)
					finish()
				} else {
					showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
				}
			} else {
				showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
			}
		} ?: run {
			showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
		}
	}

	override fun onBackPressed() {
		if (toolbarOpened) {
			closeBottomToolbar()
		} else {
			super.onBackPressed()
		}
	}

	override fun onImageSelected(item: ImageItem?) {
		adapter?.getAllSelected()?.let {
			if (it.isNotEmpty()) {
				setTabsUnClickable()
			} else {
				setTabsClickable()
			}
		}
	}

	override fun onLongClicked(item: ImageItem) {
		showBottomToolbar()
		adapter?.setImageItemSelected(item)
		adapter?.getAllSelected()?.let {
			if (it.isNotEmpty()) {
				setTabsUnClickable()
			}
		}
	}

	private fun setTabsUnClickable() {
		val tabStrip = tab_layout.getChildAt(0) as LinearLayout
		tabStrip.isEnabled = false
		for (i in 0 until tabStrip.childCount) {
			tabStrip.getChildAt(i).isClickable = false
		}
	}

	private fun setTabsClickable() {
		val tabStrip = tab_layout.getChildAt(0) as LinearLayout
		tabStrip.isEnabled = true
		for (i in 0 until tabStrip.childCount) {
			tabStrip.getChildAt(i).isClickable = true
		}
	}

	private fun sendImage() {
		presenter.sendImages(adapter?.getAllSelected())
	}

	private fun showBottomToolbar() {
		adapter?.itemCount?.let { listSize ->
			if (listSize != 0) {
				custom_toolbar?.animateSlide(200, 0.0f, 0.0f, 1.0f)
				toolbarOpened = true
				adapter?.showDeleteBoxes()
			} else {
				btn_settings.showMessageWithSnackBar(getString(R.string.empty_list),3000)
			}
		}
	}

	private fun closeBottomToolbar() {
		custom_toolbar.animateSlide(200, custom_toolbar.height.toFloat(), 0.0f, 1.0f)
		toolbarOpened = false
		adapter?.showDeleteBoxes()
		adapter?.deselectAll()
		setTabsClickable()
	}

	private fun showSelected() {
		val selectedList = adapter?.getAllSelected()
		selectedList?.size?.let {
			if (it != 0) {
				showAlertDialog(getString(R.string.delete_image_title), getString(R.string.delete_image_desc),this,true,"Törlés")
			} else {
				btn_delete.showMessageWithSnackBar(getString(R.string.select_item),3000)
			}
		}
	}


	override fun negativeButtonClicked() { }

	override fun positiveButtonClicked() {
		adapter?.getAllSelected()?.let {
			presenter.deleteImages(it)
			closeBottomToolbar()
		}
	}

	override fun deleted() {
		if (tab_layout.selectedTabPosition == 0) {
			presenter.fireBaseList.clear()
			presenter.subscribeForImageEvents()
		} else {
			presenter.getImages(false)
		}
		showToast(getString(R.string.deleted))
	}

	override fun uploaded() {
		tab_layout.selectTab(tab_layout.getTabAt(0))
	}

	override fun loadImages(images: ArrayList<ImageItem>) {
		if (images.isNotEmpty()) {
			tv_no_images_saved.visibility = View.GONE
		} else {
			tv_no_images_saved.visibility = View.VISIBLE
		}
		adapter?.setList(images)
	}

	override fun showImage(imageItem: ImageItem) {
		imageItem.serverImage?.let {
			if (it.isNotEmpty()) {
				GalleryDialog.getInstance(arrayListOf(imageItem.serverImage), null,0,true).show(this.supportFragmentManager, "gallery")
			} else {
				GalleryDialog.getInstance(null, arrayListOf(BitmapFactory.decodeFile(imageItem.localPath)),0,true).show(this.supportFragmentManager, "gallery")
			}
		} ?: run {
			GalleryDialog.getInstance(null, arrayListOf(BitmapFactory.decodeFile(imageItem.localPath)),0,true).show(this.supportFragmentManager, "gallery")
		}
	}

	private fun changeToNotProcessed() {
		custom_toolbar.btn_send.fadeIn(300).subscribe()
		custom_toolbar.btn_send.isClickable = true
		custom_toolbar.btn_send.isEnabled = true
		presenter.getImages(false)
		presenter.isProcessed = false
	}

	private fun changeToProcessed() {
		custom_toolbar.btn_send.fadeOut(300).subscribe()
		custom_toolbar.btn_send.isClickable = false
		custom_toolbar.btn_send.isEnabled = false
		presenter.loadImagesFromFireBase()
		presenter.isProcessed = true
	}
}