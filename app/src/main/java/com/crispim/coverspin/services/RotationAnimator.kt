package com.crispim.coverspin.services

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.crispim.coverspin.Constants
import com.crispim.coverspin.models.AnimationType

class RotationAnimator(private val context: Context) {
    private val cacheHelper = CacheHelper(
        context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE)
    )

    fun animateRotation(view: View, onComplete: () -> Unit) {
        val animationType = cacheHelper.getAnimationType()
        val duration = cacheHelper.getAnimationDuration()

        when (animationType) {
            AnimationType.MINIMAL -> {
                // No animation, just callback
                onComplete()
            }
            AnimationType.FADE -> {
                animateFade(view, duration, onComplete)
            }
            AnimationType.SLIDE -> {
                animateSlide(view, duration, onComplete)
            }
            AnimationType.ROTATE -> {
                animateRotate(view, duration, onComplete)
            }
        }
    }

    private fun animateFade(view: View, duration: Int, onComplete: () -> Unit) {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = (duration / 2).toLong()
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                onComplete()
                val fadeIn = AlphaAnimation(0.0f, 1.0f)
                fadeIn.duration = (duration / 2).toLong()
                view.startAnimation(fadeIn)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(fadeOut)
    }

    private fun animateSlide(view: View, duration: Int, onComplete: () -> Unit) {
        val slideOut = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 1f,
            Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f
        )
        slideOut.duration = (duration / 2).toLong()
        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                onComplete()
                val slideIn = TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, -1f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f
                )
                slideIn.duration = (duration / 2).toLong()
                view.startAnimation(slideIn)
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        view.startAnimation(slideOut)
    }

    private fun animateRotate(view: View, duration: Int, onComplete: () -> Unit) {
        val animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 180f)
        animator.duration = duration.toLong()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
                // Rotate back
                val reverseAnimator = ObjectAnimator.ofFloat(view, "rotationY", 180f, 0f)
                reverseAnimator.duration = duration.toLong()
                reverseAnimator.interpolator = AccelerateDecelerateInterpolator()
                reverseAnimator.start()
            }
        })
        animator.start()
    }
}

