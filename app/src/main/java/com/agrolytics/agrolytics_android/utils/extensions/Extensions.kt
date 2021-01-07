package com.agrolytics.agrolytics_android.utils.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.agrolytics.agrolytics_android.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject


fun View.showMessageWithSnackBar(message: String, duration: Int) {
	Snackbar.make(this, message, duration).show()
}

fun View.fadeIn(duration: Long): Completable {
	val animationSubject = CompletableSubject.create()
	return animationSubject.doOnSubscribe {
		ViewCompat.animate(this)
				.setDuration(duration)
				.alpha(1f)
				.withEndAction {
					animationSubject.onComplete()
				}
	}
}

fun View.fadeOut(duration: Long): Completable {
	val animationSubject = CompletableSubject.create()
	return animationSubject.doOnSubscribe {
		ViewCompat.animate(this)
				.setDuration(duration)
				.alpha(0f)
				.withEndAction {
					animationSubject.onComplete()
				}
	}
}

fun View.animateSolidFocusIn(duration: Long, colorFrom: Int, colorTo: Int) {
	val drawable = background as GradientDrawable
	val colorAnimationSolid = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
	colorAnimationSolid.duration = duration
	colorAnimationSolid.addUpdateListener { animator ->
		drawable.setColor(animator.animatedValue as Int)
	}
	colorAnimationSolid.start()
}

fun View.animateSolidFocusOut(duration: Long, colorFrom: Int, colorTo: Int) {
	val drawable = background as GradientDrawable
	val colorAnimationSolid = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
	colorAnimationSolid.duration = duration
	colorAnimationSolid.addUpdateListener { animator ->
		drawable.setColor(animator.animatedValue as Int)
	}
	colorAnimationSolid.start()
}

fun View.animateStrokeFocusOut(duration: Long, colorFrom: Int, colorTo: Int) {
	val drawable = background as GradientDrawable
	val colorAnimationStroke = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
	colorAnimationStroke.duration = duration
	colorAnimationStroke.addUpdateListener { animator ->
		drawable.setStroke(3, animator.animatedValue as Int)
	}
	colorAnimationStroke.start()
}

fun View.animateStrokeFocusIn(duration: Long, colorFrom: Int, colorTo: Int) {
	val drawable = background as GradientDrawable
	val colorAnimationStroke = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
	colorAnimationStroke.duration = duration
	colorAnimationStroke.addUpdateListener { animator ->
		drawable.setStroke(6, animator.animatedValue as Int)
	}
	colorAnimationStroke.start()
}

fun View.animateSlide(duration: Long, toY: Float, toX: Float, alpha: Float) {
	this.animate()
			.setDuration(duration)
			.translationY(toY)
			.translationX(toX)
			.alpha(alpha)
			.setListener(null)
}

fun TextView.animateTextChange(duration: Long, title: String) {
	val animIn = AlphaAnimation(0.0f, 1.0f)
	animIn.duration = duration

	val animOut = AlphaAnimation(1.0f, 0.0f)
	animOut.duration = duration

	startAnimation(animOut)

	animOut.setAnimationListener(object : Animation.AnimationListener{
		override fun onAnimationStart(animation: Animation?) {
		}

		override fun onAnimationRepeat(animation: Animation?) {

		}

		override fun onAnimationEnd(animation: Animation?) {
			text = title
			startAnimation(animIn)
		}
	})
}

fun View.showScaleAnimation() {
	val anim = ScaleAnimation(0f, 1f, 0f, 1f)
	anim.fillBefore = true
	anim.fillAfter = true
	anim.isFillEnabled = true
	anim.duration = 1000
	anim.interpolator = OvershootInterpolator()
	this.startAnimation(anim)
}

fun View.hideScaleAnimation() {
	val anim = ScaleAnimation(1f, 0f, 1f, 0f)
	anim.fillBefore = true
	anim.fillAfter = true
	anim.isFillEnabled = true
	anim.duration = 1000
	anim.interpolator = OvershootInterpolator()
	this.startAnimation(anim)
}

fun ImageView.animateArrowRotation(degree: Float, duration: Long) {
	animate()
			.rotationBy(degree)
			.setDuration(duration)
			.start()
}