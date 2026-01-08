package com.example.iurankomplek.presentation.ui.helper

import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Helper class for RecyclerView configuration and keyboard navigation
 * Reduces boilerplate code across activities and fragments
 */
object RecyclerViewHelper {

    /**
     * Configure RecyclerView with responsive layout and optimizations
     * 
     * @param recyclerView The RecyclerView to configure
     * @param itemCount Cache size for view recycling (default: 20)
     * @param enableKeyboardNav Enable keyboard navigation (default: true)
     * @param adapter RecyclerView adapter
     * @param orientation Device orientation
     * @param screenWidthDp Screen width in dp
     */
    fun configureRecyclerView(
        recyclerView: RecyclerView,
        itemCount: Int = 20,
        enableKeyboardNav: Boolean = true,
        adapter: RecyclerView.Adapter<ViewHolder>,
        orientation: Int,
        screenWidthDp: Int
    ) {
        // Determine layout type and column count based on screen size
        val (isTablet, isLandscape) = (screenWidthDp >= 600) to (orientation == Configuration.ORIENTATION_LANDSCAPE)
        
        val columnCount = when {
            isTablet && isLandscape -> 3
            isTablet -> 2
            isLandscape -> 2
            else -> 1
        }
        
        // Set layout manager
        recyclerView.layoutManager = when (columnCount) {
            1 -> LinearLayoutManager(recyclerView.context)
            else -> GridLayoutManager(recyclerView.context, columnCount)
        }
        
        // Set optimizations
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(itemCount)
        recyclerView.adapter = adapter
        
        // Enable keyboard navigation if requested
        if (enableKeyboardNav) {
            setupKeyboardNavigation(recyclerView, adapter, columnCount)
        }
    }

    /**
     * Setup keyboard navigation for RecyclerView
     * Handles DPAD navigation for both single and multi-column layouts
     * 
     * @param recyclerView The RecyclerView to setup
     * @param adapter RecyclerView adapter
     * @param columnCount Number of columns in the grid (1 for LinearLayoutManager)
     */
    private fun setupKeyboardNavigation(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        columnCount: Int
    ) {
        recyclerView.focusable = true
        recyclerView.focusableInTouchMode = true
        
        recyclerView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_DOWN,
                    KeyEvent.KEYCODE_DPAD_UP,
                    KeyEvent.KEYCODE_DPAD_LEFT,
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        handleKeyNavigation(recyclerView, adapter, keyCode, columnCount)
                    }
                    else -> false
                }
            } else {
                false
            }
        }
    }

    /**
     * Handle keyboard navigation events
     * 
     * @param recyclerView The RecyclerView
     * @param adapter RecyclerView adapter
     * @param keyCode The key code pressed
     * @param columnCount Number of columns
     * @return true if key was handled
     */
    private fun handleKeyNavigation(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        keyCode: Int,
        columnCount: Int
    ): Boolean {
        val layoutManager = recyclerView.layoutManager
        
        return when (layoutManager) {
            is LinearLayoutManager -> handleLinearNavigation(recyclerView, layoutManager, adapter, keyCode)
            is GridLayoutManager -> handleGridNavigation(recyclerView, layoutManager, adapter, keyCode, columnCount)
            else -> false
        }
    }

    /**
     * Handle navigation for LinearLayoutManager (single column)
     */
    private fun handleLinearNavigation(
        recyclerView: RecyclerView,
        layoutManager: LinearLayoutManager,
        adapter: RecyclerView.Adapter<*>,
        keyCode: Int
    ): Boolean {
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (lastVisible < adapter.itemCount - 1) {
                    recyclerView.smoothScrollToPosition(lastVisible + 1)
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (firstVisible > 0) {
                    recyclerView.smoothScrollToPosition(firstVisible - 1)
                    true
                } else {
                    false
                }
            }
            else -> false // Horizontal navigation not applicable
        }
    }

    /**
     * Handle navigation for GridLayoutManager (multi-column)
     */
    private fun handleGridNavigation(
        recyclerView: RecyclerView,
        layoutManager: GridLayoutManager,
        adapter: RecyclerView.Adapter<*>,
        keyCode: Int,
        columnCount: Int
    ): Boolean {
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()
        
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                val targetPosition = lastVisible + columnCount
                if (targetPosition < adapter.itemCount) {
                    recyclerView.smoothScrollToPosition(targetPosition)
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                val targetPosition = firstVisible - columnCount
                if (targetPosition >= 0) {
                    recyclerView.smoothScrollToPosition(targetPosition)
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (lastVisible < adapter.itemCount - 1) {
                    recyclerView.smoothScrollToPosition(lastVisible + 1)
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (firstVisible > 0) {
                    recyclerView.smoothScrollToPosition(firstVisible - 1)
                    true
                } else {
                    false
                }
            }
            else -> false
        }
    }
}
