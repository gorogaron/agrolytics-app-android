package com.agrolytics.agrolytics_android.ui.imageFinished

import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.adapter.ImagePagerAdapter
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_upload_finished.*
import org.koin.android.ext.android.inject
import android.content.Intent
import com.agrolytics.agrolytics_android.ui.main.MainActivity


class UploadFinishedActivity : BaseActivity(), UploadFinishedScreen {

	private val sessionManager: SessionManager by inject()
	private val roomModule: RoomModule by inject()
	private val presenter: UploadFinishedPresenter by inject()
	private val fireStoreDB: FireStoreDB by inject()

	private var pagerAdapter: ImagePagerAdapter? = null
	private val fragmentList = arrayListOf<UploadFinishedFragment>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upload_finished)

		responseList.let { responseList ->
			intent.getStringArrayListExtra(ConfigInfo.PATH)?.let { pathList ->
				if (intent.hasExtra(ConfigInfo.ID)) {
					intent.getStringArrayListExtra(ConfigInfo.ID)?.let { idList ->
						for (item in responseList) {
							fragmentList.add(UploadFinishedFragment.newInstance(item, pathList[responseList.indexOf(item)], idList[responseList.indexOf(item)]))
						}
					}
				} else {
					for (item in responseList) {
						fragmentList.add(UploadFinishedFragment.newInstance(item, pathList[responseList.indexOf(item)], null))
					}
				}
			}
		}

		pagerAdapter = ImagePagerAdapter(supportFragmentManager, fragmentList)
		pagerAdapter?.count?.let { viewpager.offscreenPageLimit = it }
		viewpager.adapter = pagerAdapter
		viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
			override fun onPageScrollStateChanged(state: Int) { }

			override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
				if (pagerAdapter != null) {
					val fragment = pagerAdapter?.getItem(position) as UploadFinishedFragment

					if (position == pagerAdapter!!.count - 1) {
						fragment.showNextArrow(false)
					}
					if (position < pagerAdapter!!.count - 1) {
						fragment.showNextArrow(true)
					} else {
						fragment.showBackArrow(true)
					}

					if (position == 0) {
						fragment.showBackArrow(false)
					} else {
						fragment.showBackArrow(true)
					}
				}
			}

			override fun onPageSelected(position: Int) {
				if (pagerAdapter != null) {
					val fragment = pagerAdapter?.getItem(position) as UploadFinishedFragment

					if (position == pagerAdapter!!.count) {
						fragment.showNextArrow(false)
					}
					if (position < pagerAdapter!!.count) {
						fragment.showNextArrow(true)
					} else {
						fragment.showBackArrow(true)
					}

					if (position == 0) {
						fragment.showBackArrow(false)
					} else {
						fragment.showBackArrow(true)
					}
				}
			}
		})

		btn_back.setOnClickListener { onBackPressed() }
		tv_done.setOnClickListener { onBackPressed() }

		presenter.addView(this)
		presenter.addInjections(arrayListOf(sessionManager, roomModule, fireStoreDB))
	}

	override fun onResume() {
		super.onResume()

		if (fragmentList.isNotEmpty()) {
			val fragment = fragmentList[0]
			fragment.showNextArrow(true)
			tv_done.visibility = View.VISIBLE
		}
	}

	override fun onNextPage(fragment: UploadFinishedFragment) {
		val currentItem = viewpager.currentItem
		viewpager.setCurrentItem(currentItem + 1,true)
	}

	override fun onPreviousPage(fragment: UploadFinishedFragment) {
		val currentItem = viewpager.currentItem
		viewpager.setCurrentItem(currentItem - 1,true)
	}

	override fun updateView(fragment: UploadFinishedFragment) {
		fragment.updateView()
	}

	override fun onDeclineClicked(fragment: UploadFinishedFragment, id: String?) {
		if (fragmentList.size == 1) {
			//Return to main activity
			val intent = Intent(this, MainActivity::class.java)
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		} else {
			fragment.updateDeclineView()
			presenter.deleteImageFromLocalDatabase(id)
		}
	}

	override fun onAcceptClicked(responseImageUpload: ResponseImageUpload?, path: String?,
								 fragment: UploadFinishedFragment, id: String?) {

		val processMethod = intent.extras.getString(ConfigInfo.METHOD)
		presenter.uploadImageToStorage(responseImageUpload, path, fragment, processMethod)
		presenter.deleteImageFromLocalDatabase(id)
	}

	companion object {
	    var responseList = arrayListOf<ResponseImageUpload>()
	}
}
