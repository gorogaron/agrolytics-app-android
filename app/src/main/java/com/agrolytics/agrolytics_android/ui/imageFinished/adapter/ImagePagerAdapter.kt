package com.agrolytics.agrolytics_android.ui.imageFinished.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment

class ImagePagerAdapter(fm: FragmentManager,
                        private val fragmentList: ArrayList<UploadFinishedFragment>) : FragmentStatePagerAdapter(fm) {

	override fun getItem(position: Int): Fragment {
		return fragmentList[position]
	}

	override fun getItemPosition(`object`: Any): Int {
		return fragmentList.indexOf(`object`)
	}

	override fun getCount(): Int {
		return fragmentList.size
	}
}