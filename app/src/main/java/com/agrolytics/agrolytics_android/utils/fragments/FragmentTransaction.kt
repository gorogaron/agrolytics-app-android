package com.agrolytics.agrolytics_android.utils.fragments

import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.agrolytics.agrolytics_android.R

class FragmentTransaction {
    companion object {
        fun showFragment(fragment: Fragment, tag: String, enterAnim: Animation, exitAnim: Animation, delay: Boolean, delayMillis: Long, supportFragmentManager: FragmentManager, frameLayout: Int) {
            val handler = Handler(Looper.getMainLooper())
            val animations =
                selectAnimation(
                    exitAnim,
                    enterAnim
                )
            if (delay) {
                handler.postDelayed({
                    val transaction = supportFragmentManager.beginTransaction()
                    if (enterAnim != Animation.NONE && exitAnim != Animation.NONE) {
                        transaction.setCustomAnimations(animations.first, animations.second)
                    }
                    transaction.replace(frameLayout, fragment, tag)
                    transaction.addToBackStack(tag)
                    transaction.commitAllowingStateLoss()
                }, delayMillis)
            } else {
                val transaction = supportFragmentManager.beginTransaction()
                if (enterAnim != Animation.NONE && exitAnim != Animation.NONE) {
                    transaction.setCustomAnimations(animations.first, animations.second)
                }
                transaction.replace(frameLayout, fragment, tag)
                transaction.addToBackStack(tag)
                transaction.commitAllowingStateLoss()
            }
        }

        private fun selectAnimation(enterAnim: Animation, exitAnim: Animation): Pair<Int,Int> {
            var enterAnimInt = 0
            var exitAnimInt = 0
            enterAnimInt = when(enterAnim) {
                Animation.NONE -> 0
                Animation.FADE_IN -> R.animator.fade_in
                Animation.FADE_OUT -> R.animator.fade_out
                Animation.SLIDE_IN_RIGHT -> R.anim.slide_in_right
                Animation.SLIDE_IN_LEFT -> R.anim.slide_in_left
                Animation.SLIDE_OUT_LEFT -> R.anim.slide_out_left
                Animation.SLIDE_OUT_RIGHT -> R.anim.slide_out_right
            }
            exitAnimInt = when(exitAnim) {
                Animation.NONE -> 0
                Animation.FADE_IN -> R.animator.fade_in
                Animation.FADE_OUT -> R.animator.fade_out
                Animation.SLIDE_IN_RIGHT -> R.anim.slide_in_right
                Animation.SLIDE_IN_LEFT -> R.anim.slide_in_left
                Animation.SLIDE_OUT_LEFT -> R.anim.slide_out_left
                Animation.SLIDE_OUT_RIGHT -> R.anim.slide_out_right
            }
            return Pair(enterAnimInt,exitAnimInt)
        }
    }
}