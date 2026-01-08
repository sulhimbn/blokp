package com.example.iurankomplek.presentation.ui.helper

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.R
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.launch

/**
 * Helper class for managing UI states (Loading, Success, Error, Empty)
 * Reduces boilerplate code across activities and fragments
 */
class StateManager(
    private val progressBar: ProgressBar,
    private val emptyStateTextView: TextView,
    private val errorStateLayout: View,
    private val errorStateTextView: TextView,
    private val retryTextView: TextView,
    private val recyclerView: View,
    private val scope: LifecycleCoroutineScope,
    private val context: android.content.Context
) {

    /**
     * Observe UI state and update views accordingly
     * 
     * @param stateFlow The StateFlow to observe
     * @param onSuccess Callback for success state with data
     * @param onError Callback for error state
     */
    fun <T> observeState(
        stateFlow: kotlinx.coroutines.flow.StateFlow<UiState<T>>,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        scope.launch {
            stateFlow.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                        // Do nothing
                    }
                    is UiState.Loading -> {
                        showLoading()
                    }
                    is UiState.Success -> {
                        showSuccess()
                        onSuccess(state.data)
                    }
                    is UiState.Error -> {
                        showError(state.error)
                        onError(state.error)
                    }
                }
            }
        }
    }

    /**
     * Show loading state
     */
    fun showLoading() {
        progressBar.visibility = View.VISIBLE
        emptyStateTextView.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    /**
     * Show success state
     */
    fun showSuccess() {
        progressBar.visibility = View.GONE
        emptyStateTextView.visibility = View.GONE
        errorStateLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    /**
     * Show empty state
     */
    fun showEmpty() {
        progressBar.visibility = View.GONE
        emptyStateTextView.visibility = View.VISIBLE
        errorStateLayout.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    /**
     * Show error state
     * 
     * @param errorMessage Error message to display
     * @param onRetry Optional retry callback
     */
    fun showError(
        errorMessage: String,
        onRetry: (() -> Unit)? = null
    ) {
        progressBar.visibility = View.GONE
        emptyStateTextView.visibility = View.GONE
        errorStateLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        
        errorStateTextView.text = errorMessage
        
        if (onRetry != null) {
            retryTextView.setOnClickListener {
                onRetry()
            }
        }
    }

    /**
     * Set retry callback
     * 
     * @param onRetry Callback to execute on retry
     */
    fun setRetryCallback(onRetry: () -> Unit) {
        retryTextView.setOnClickListener {
            onRetry()
        }
    }

    companion object {
        /**
         * Create StateManager from view binding
         * Use this if you have a layout with include_state_management
         */
        fun create(
            progressBar: ProgressBar,
            emptyStateTextView: TextView,
            errorStateLayout: View,
            errorStateTextView: TextView,
            retryTextView: TextView,
            recyclerView: View,
            scope: LifecycleCoroutineScope,
            context: android.content.Context
        ): StateManager {
            return StateManager(
                progressBar = progressBar,
                emptyStateTextView = emptyStateTextView,
                errorStateLayout = errorStateLayout,
                errorStateTextView = errorStateTextView,
                retryTextView = retryTextView,
                recyclerView = recyclerView,
                scope = scope,
                context = context
            )
        }
    }
}
