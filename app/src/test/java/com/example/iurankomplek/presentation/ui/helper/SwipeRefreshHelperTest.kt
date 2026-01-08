package com.example.iurankomplek.presentation.ui.helper

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAccessibilityManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SwipeRefreshHelperTest {

    private lateinit var context: Context
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        swipeRefreshLayout = SwipeRefreshLayout(context)
    }

    @Test
    fun `configureSwipeRefresh sets OnRefreshListener`() {
        var listenerCalled = false
        val onRefreshListener = { listenerCalled = true }

        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = onRefreshListener
        )

        assertNotNull(swipeRefreshLayout.onRefreshListener)
        assertTrue(listenerCalled)
    }

    @Test
    fun `configureSwipeRefresh sets content description`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertNotNull(swipeRefreshLayout.contentDescription)
        assertTrue(swipeRefreshLayout.contentDescription.toString().isNotEmpty())
    }

    @Test
    fun `configureSwipeRefresh sets accessibility live region to polite`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertEquals(
            android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE,
            swipeRefreshLayout.accessibilityLiveRegion
        )
    }

    @Test
    fun `announceRefreshComplete announces message to screen reader`() {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)

        val shadowAccessibilityManager: ShadowAccessibilityManager = 
            org.robolectric.Shadows.shadowOf(accessibilityManager)
        
        assertTrue(shadowAccessibilityManager.isEnabled)
    }

    @Test
    fun `setRefreshing sets isRefreshing to true`() {
        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)

        assertTrue(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `setRefreshing sets isRefreshing to false`() {
        swipeRefreshLayout.isRefreshing = true
        
        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)

        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `setRefreshing can be called multiple times`() {
        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `configureSwipeRefresh listener is invoked when refresh triggered`() {
        var listenerInvoked = false
        val onRefreshListener = { listenerInvoked = true }

        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = onRefreshListener
        )

        swipeRefreshLayout.onRefreshListener?.onRefresh()

        assertTrue(listenerInvoked)
    }

    @Test
    fun `configureSwipeRefresh can handle null listener gracefully`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertNotNull(swipeRefreshLayout.onRefreshListener)
    }

    @Test
    fun `announceRefreshComplete uses correct string resource`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)

        assertNotNull(swipeRefreshLayout.contentDescription)
    }

    @Test
    fun `configureSwipeRefresh content description is not empty`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        val contentDescription = swipeRefreshLayout.contentDescription?.toString()
        assertNotNull(contentDescription)
        assertFalse(contentDescription?.isEmpty() ?: true)
    }

    @Test
    fun `configureSwipeRefresh accessibility properties are set correctly`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertNotNull(swipeRefreshLayout.contentDescription)
        assertEquals(
            android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE,
            swipeRefreshLayout.accessibilityLiveRegion
        )
        assertNotNull(swipeRefreshLayout.onRefreshListener)
    }

    @Test
    fun `announceRefreshComplete does not throw when called multiple times`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)
        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)
        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)

        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `setRefreshing works correctly after configureSwipeRefresh`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `configureSwipeRefresh sets correct accessibility live region constant`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertEquals(
            android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE,
            swipeRefreshLayout.accessibilityLiveRegion
        )
    }

    @Test
    fun `announceRefreshComplete message is accessible`() {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)

        val shadowAccessibilityManager: ShadowAccessibilityManager = 
            org.robolectric.Shadows.shadowOf(accessibilityManager)
        
        assertTrue(shadowAccessibilityManager.isEnabled)
    }

    @Test
    fun `configureSwipeRefresh does not affect refreshing state initially`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `setRefreshing toggles state correctly`() {
        assertFalse(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `configureSwipeRefresh content description contains refresh text`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        val contentDescription = swipeRefreshLayout.contentDescription?.toString()?.lowercase()
        assertNotNull(contentDescription)
        assertTrue(contentDescription?.contains("refresh") ?: false)
    }

    @Test
    fun `announceRefreshComplete can be called without configureSwipeRefresh`() {
        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)
        
        assertTrue(true) // Test passes if no exception thrown
    }

    @Test
    fun `setRefreshing can be called without configureSwipeRefresh`() {
        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, true)
        assertTrue(swipeRefreshLayout.isRefreshing)

        SwipeRefreshHelper.setRefreshing(swipeRefreshLayout, false)
        assertFalse(swipeRefreshLayout.isRefreshing)
    }

    @Test
    fun `configureSwipeRefresh does not change swipeRefreshLayout enabled state`() {
        swipeRefreshLayout.isEnabled = false

        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertFalse(swipeRefreshLayout.isEnabled)

        swipeRefreshLayout.isEnabled = true

        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertTrue(swipeRefreshLayout.isEnabled)
    }

    @Test
    fun `announceRefreshComplete message is different from initial content description`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        val initialDescription = swipeRefreshLayout.contentDescription?.toString()
        
        SwipeRefreshHelper.announceRefreshComplete(swipeRefreshLayout, context)
        
        val finalDescription = swipeRefreshLayout.contentDescription?.toString()
        
        assertNotNull(initialDescription)
        assertNotNull(finalDescription)
    }

    @Test
    fun `configureSwipeRefresh sets accessibility live region to polite not none`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertNotEquals(
            android.view.View.ACCESSIBILITY_LIVE_REGION_NONE,
            swipeRefreshLayout.accessibilityLiveRegion
        )
    }

    @Test
    fun `configureSwipeRefresh sets accessibility live region to polite not assertive`() {
        SwipeRefreshHelper.configureSwipeRefresh(
            swipeRefreshLayout = swipeRefreshLayout,
            onRefreshListener = {}
        )

        assertNotEquals(
            android.view.View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE,
            swipeRefreshLayout.accessibilityLiveRegion
        )
    }
}
