package com.agrolytics.agrolytics_android.utils.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.agrolytics.agrolytics_android.R

class CustomFragmentManager(private val supportFragmentManager: FragmentManager) {

    fun getVisibleFragment(): Fragment? {
        val fragments = supportFragmentManager.fragments
        if (fragments != null) {
            for (fragment: Fragment in fragments) {
                if (fragment.isVisible) {
                    return fragment
                }
            }
        }
        return null
    }

    fun showFragment(fragment: Fragment, tag: String) {
        FragmentTransaction.showFragment(
            fragment,
            tag,
            Animation.NONE,
            Animation.NONE,
            false,
            0,
            supportFragmentManager,
            R.id.frame_layout
        )
    }

    fun showFragmentForward(fragment: Fragment, tag: String) {
        FragmentTransaction.showFragment(
            fragment,
            tag,
            Animation.SLIDE_OUT_LEFT,
            Animation.SLIDE_IN_RIGHT,
            false,
            0,
            supportFragmentManager,
            R.id.frame_layout
        )
    }

    fun showFragmentBack(fragment: Fragment, tag: String) {
        FragmentTransaction.showFragment(
            fragment,
            tag,
            Animation.SLIDE_OUT_RIGHT,
            Animation.SLIDE_IN_LEFT,
            false,
            0,
            supportFragmentManager,
            R.id.frame_layout
        )
    }


}