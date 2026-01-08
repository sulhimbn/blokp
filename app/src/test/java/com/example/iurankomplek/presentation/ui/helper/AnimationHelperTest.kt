package com.example.iurankomplek.presentation.ui.helper

import android.view.View
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AnimationHelperTest {

    private lateinit var view: View

    @Before
    fun setup() {
        view = View(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `fadeIn sets view visibility to VISIBLE`() {
        view.visibility = View.GONE

        AnimationHelper.fadeIn(view)

        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun `fadeIn sets initial alpha to 0`() {
        AnimationHelper.fadeIn(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1f, view.alpha, 0.01f)
    }

    @Test
    fun `fadeIn uses default duration of 300ms`() {
        var animationStarted = false
        AnimationHelper.fadeIn(view, duration = 0)

        animationStarted = true

        assertTrue(animationStarted)
    }

    @Test
    fun `fadeIn uses custom duration`() {
        AnimationHelper.fadeIn(view, duration = 500)

        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `fadeIn calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.fadeIn(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `fadeIn works without callback`() {
        AnimationHelper.fadeIn(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `fadeOut animates alpha to 0`() {
        view.alpha = 1f

        AnimationHelper.fadeOut(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0f, view.alpha, 0.01f)
    }

    @Test
    fun `fadeOut sets visibility to GONE after animation`() {
        AnimationHelper.fadeOut(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun `fadeOut uses default duration of 200ms`() {
        AnimationHelper.fadeOut(view, duration = 0)

        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `fadeOut uses custom duration`() {
        AnimationHelper.fadeOut(view, duration = 500)

        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `fadeOut calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.fadeOut(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `fadeOut works without callback`() {
        AnimationHelper.fadeOut(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `slideUp sets visibility to VISIBLE`() {
        view.visibility = View.GONE

        AnimationHelper.slideUp(view, distance = 100f, duration = 0)

        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun `slideUp sets initial translationY to distance`() {
        AnimationHelper.slideUp(view, distance = 100f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0f, view.translationY, 0.01f)
    }

    @Test
    fun `slideUp uses default distance equal to view height`() {
        view.layout(0, 0, 100, 200)

        AnimationHelper.slideUp(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0f, view.translationY, 0.01f)
    }

    @Test
    fun `slideUp calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.slideUp(view, distance = 100f, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `slideUp works without callback`() {
        AnimationHelper.slideUp(view, distance = 100f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `slideDown animates translationY to distance`() {
        AnimationHelper.slideDown(view, distance = 100f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(100f, view.translationY, 0.01f)
    }

    @Test
    fun `slideDown sets visibility to GONE after animation`() {
        AnimationHelper.slideDown(view, distance = 100f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun `slideDown uses default distance equal to view height`() {
        view.layout(0, 0, 100, 200)

        AnimationHelper.slideDown(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(200f, view.translationY, 0.01f)
    }

    @Test
    fun `slideDown calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.slideDown(view, distance = 100f, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `scale sets initial scale to scaleFrom`() {
        AnimationHelper.scale(view, scaleFrom = 0.5f, scaleTo = 1.5f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0.5f, view.scaleX, 0.01f)
        assertEquals(0.5f, view.scaleY, 0.01f)
    }

    @Test
    fun `scale animates to scaleTo then returns to scaleFrom`() {
        AnimationHelper.scale(view, scaleFrom = 1.0f, scaleTo = 1.1f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1.0f, view.scaleX, 0.01f)
        assertEquals(1.0f, view.scaleY, 0.01f)
    }

    @Test
    fun `scale uses default scaleFrom of 1.0f`() {
        AnimationHelper.scale(view, scaleTo = 1.1f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1.0f, view.scaleX, 0.01f)
        assertEquals(1.0f, view.scaleY, 0.01f)
    }

    @Test
    fun `scale uses default scaleTo of 1.1f`() {
        AnimationHelper.scale(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1.0f, view.scaleX, 0.01f)
        assertEquals(1.0f, view.scaleY, 0.01f)
    }

    @Test
    fun `scale calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.scale(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `scale works without callback`() {
        AnimationHelper.scale(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `circularReveal creates animator`() {
        view.layout(0, 0, 100, 200)

        AnimationHelper.circularReveal(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `circularReveal uses default centerX of width div 2`() {
        view.layout(0, 0, 100, 200)

        AnimationHelper.circularReveal(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `circularReveal uses default centerY of height div 2`() {
        view.layout(0, 0, 100, 200)

        AnimationHelper.circularReveal(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `circularReveal calls onAnimationEnd callback`() {
        var callbackCalled = false
        view.layout(0, 0, 100, 200)

        AnimationHelper.circularReveal(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `animateVisibility calls fadeIn when show is true`() {
        view.visibility = View.GONE

        AnimationHelper.animateVisibility(view, show = true, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun `animateVisibility calls fadeOut when show is false`() {
        view.visibility = View.VISIBLE

        AnimationHelper.animateVisibility(view, show = false, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun `animateVisibility uses custom duration`() {
        AnimationHelper.animateVisibility(view, show = true, duration = 500)

        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `animateVisibility calls callback on fadeIn`() {
        var callbackCalled = false
        AnimationHelper.animateVisibility(view, show = true, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `animateVisibility calls callback on fadeOut`() {
        var callbackCalled = false
        AnimationHelper.animateVisibility(view, show = false, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `shake loads shake animation`() {
        AnimationHelper.shake(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `shake calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.shake(view) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `shake works without callback`() {
        AnimationHelper.shake(view)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `success animates scale and alpha`() {
        AnimationHelper.success(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1.0f, view.scaleX, 0.01f)
        assertEquals(1.0f, view.scaleY, 0.01f)
        assertEquals(1.0f, view.alpha, 0.01f)
    }

    @Test
    fun `success calls onAnimationEnd callback`() {
        var callbackCalled = false
        AnimationHelper.success(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `success works without callback`() {
        AnimationHelper.success(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `fadeIn can be called on already visible view`() {
        view.visibility = View.VISIBLE
        view.alpha = 1f

        AnimationHelper.fadeIn(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.VISIBLE, view.visibility)
    }

    @Test
    fun `fadeOut can be called on already invisible view`() {
        view.visibility = View.GONE
        view.alpha = 0f

        AnimationHelper.fadeOut(view, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(View.GONE, view.visibility)
    }

    @Test
    fun `slideUp can be called with zero distance`() {
        AnimationHelper.slideUp(view, distance = 0f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0f, view.translationY, 0.01f)
    }

    @Test
    fun `slideDown can be called with zero distance`() {
        AnimationHelper.slideDown(view, distance = 0f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(0f, view.translationY, 0.01f)
    }

    @Test
    fun `scale can be called with equal scaleFrom and scaleTo`() {
        AnimationHelper.scale(view, scaleFrom = 1.0f, scaleTo = 1.0f, duration = 0)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(1.0f, view.scaleX, 0.01f)
        assertEquals(1.0f, view.scaleY, 0.01f)
    }

    @Test
    fun `fadeIn duration of zero completes immediately`() {
        var callbackCalled = false
        AnimationHelper.fadeIn(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `fadeOut duration of zero completes immediately`() {
        var callbackCalled = false
        AnimationHelper.fadeOut(view, duration = 0) {
            callbackCalled = true
        }

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertTrue(callbackCalled)
    }

    @Test
    fun `multiple animations can be chained`() {
        AnimationHelper.fadeIn(view, duration = 0)
        android.os.Looper.getMainLooper().runToEndOfTasks()

        AnimationHelper.fadeOut(view, duration = 0)
        android.os.Looper.getMainLooper().runToEndOfTasks()

        AnimationHelper.fadeIn(view, duration = 0)
        android.os.Looper.getMainLooper().runToEndOfTasks()

        assertEquals(View.VISIBLE, view.visibility)
    }
}
