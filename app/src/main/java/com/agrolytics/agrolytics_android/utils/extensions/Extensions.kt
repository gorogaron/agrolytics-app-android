package com.agrolytics.agrolytics_android.utils.extensions

import android.view.View
import androidx.core.view.ViewCompat
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

fun View.animateSlide(duration: Long, toY: Float, toX: Float, alpha: Float) {
	this.animate()
			.setDuration(duration)
			.translationY(toY)
			.translationX(toX)
			.alpha(alpha)
			.setListener(null)
}
