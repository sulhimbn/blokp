package com.example.iurankomplek.presentation.ui.helper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import com.example.iurankomplek.R

/**
 * Helper class for UI animations and transitions
 * Reduces boilerplate code across activities and fragments
 */
object AnimationHelper {

    /**
     * Fade in animation
     * 
     * @param view View to animate
     * @param duration Animation duration in milliseconds (default: 300ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun fadeIn(
        view: View,
        duration: Long = 300L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Fade out animation
     * 
     * @param view View to animate
     * @param duration Animation duration in milliseconds (default: 200ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun fadeOut(
        view: View,
        duration: Long = 200L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onAnimationEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide up animation
     * 
     * @param view View to animate
     * @param distance Distance to slide (default: 100% of height)
     * @param duration Animation duration in milliseconds (default: 300ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun slideUp(
        view: View,
        distance: Float = view.height.toFloat(),
        duration: Long = 300L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.visibility = View.VISIBLE
        view.translationY = distance
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide down animation
     * 
     * @param view View to animate
     * @param distance Distance to slide (default: 100% of height)
     * @param duration Animation duration in milliseconds (default: 200ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun slideDown(
        view: View,
        distance: Float = view.height.toFloat(),
        duration: Long = 200L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.animate()
            .translationY(distance)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onAnimationEnd?.invoke()
                }
            })
            .start()
    }

    /**
     * Scale animation (pulse effect)
     * 
     * @param view View to animate
     * @param scaleFrom Starting scale (default: 1.0f)
     * @param scaleTo Ending scale (default: 1.1f)
     * @param duration Animation duration in milliseconds (default: 150ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun scale(
        view: View,
        scaleFrom: Float = 1.0f,
        scaleTo: Float = 1.1f,
        duration: Long = 150L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        view.scaleX = scaleFrom
        view.scaleY = scaleFrom
        view.animate()
            .scaleX(scaleTo)
            .scaleY(scaleTo)
            .setDuration(duration)
            .withEndAction {
                view.animate()
                    .scaleX(scaleFrom)
                    .scaleY(scaleFrom)
                    .setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            onAnimationEnd?.invoke()
                        }
                    })
                    .start()
            }
            .start()
    }

    /**
     * Circular reveal animation
     * 
     * @param view View to animate
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param duration Animation duration in milliseconds (default: 300ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun circularReveal(
        view: View,
        centerX: Int = view.width / 2,
        centerY: Int = view.height / 2,
        duration: Long = 300L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        val finalRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
        val animator = ViewAnimationUtils.createCircularReveal(
            view,
            centerX,
            centerY,
            0f,
            finalRadius
        )
        animator.duration = duration
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd?.invoke()
            }
        })
        animator.start()
    }

    /**
     * Animate view visibility with fade
     * 
     * @param view View to animate
     * @param show Whether to show or hide
     * @param duration Animation duration in milliseconds (default: 200ms)
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun animateVisibility(
        view: View,
        show: Boolean,
        duration: Long = 200L,
        onAnimationEnd: (() -> Unit)? = null
    ) {
        if (show) {
            fadeIn(view, duration, onAnimationEnd)
        } else {
            fadeOut(view, duration, onAnimationEnd)
        }
    }

    /**
     * Shake animation for error feedback
     * 
     * @param view View to animate
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun shake(view: View, onAnimationEnd: (() -> Unit)? = null) {
        val shakeAnimation = AnimationUtils.loadAnimation(view.context, R.anim.shake)
        view.startAnimation(shakeAnimation)
        shakeAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                onAnimationEnd?.invoke()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
    }

    /**
     * Success animation (scale and fade)
     * 
     * @param view View to animate
     * @param onAnimationEnd Optional callback when animation ends
     */
    fun success(view: View, onAnimationEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .alpha(0.8f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(150)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            onAnimationEnd?.invoke()
                        }
                    })
                    .start()
            }
            .start()
    }
}
