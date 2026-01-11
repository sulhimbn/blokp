package com.example.iurankomplek.presentation.ui.helper

import android.content.Context
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.iurankomplek.R

/**
 * Helper class for SwipeRefreshLayout configuration
 * Reduces boilerplate code across activities and fragments
 */
object SwipeRefreshHelper {

    /**
     * Configure SwipeRefreshLayout with accessibility support
     * 
     * @param swipeRefreshLayout The SwipeRefreshLayout to configure
     * @param onRefreshListener Callback when user swipes to refresh
     */
    fun configureSwipeRefresh(
        swipeRefreshLayout: SwipeRefreshLayout,
        onRefreshListener: () -> Unit
    ) {
        swipeRefreshLayout.setOnRefreshListener {
            onRefreshListener()
        }
        
        // Accessibility: Set content description
        swipeRefreshLayout.contentDescription = 
            swipeRefreshLayout.context.getString(R.string.swipe_refresh_desc)
        
        // Accessibility: Mark as live region for screen readers
        swipeRefreshLayout.accessibilityLiveRegion = 
            android.view.View.ACCESSIBILITY_LIVE_REGION_POLITE
    }

    /**
     * Announce refresh completion for accessibility
     * 
     * @param swipeRefreshLayout The SwipeRefreshLayout
     * @param context The context
     */
    fun announceRefreshComplete(
        swipeRefreshLayout: SwipeRefreshLayout,
        context: Context
    ) {
        val message = context.getString(R.string.swipe_refresh_complete)
        swipeRefreshLayout.announceForAccessibility(message)
    }

    /**
     * Set refreshing state
     * 
     * @param swipeRefreshLayout The SwipeRefreshLayout
     * @param isRefreshing Whether to show refreshing indicator
     */
    fun setRefreshing(
        swipeRefreshLayout: SwipeRefreshLayout,
        isRefreshing: Boolean
    ) {
        swipeRefreshLayout.isRefreshing = isRefreshing
    }
}
