package com.example.iurankomplek.presentation.ui.helper

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import androidx.test.core.app.ApplicationProvider
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class StateManagerTest {

    private lateinit var context: Context
    private lateinit var lifecycleOwner: TestLifecycleOwner
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyStateTextView: TextView
    private lateinit var errorStateLayout: android.view.View
    private lateinit var errorStateTextView: TextView
    private lateinit var retryTextView: TextView
    private lateinit var recyclerView: android.view.View
    private lateinit var stateManager: StateManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        lifecycleOwner = TestLifecycleOwner()
        progressBar = ProgressBar(context)
        emptyStateTextView = TextView(context)
        errorStateLayout = android.view.View(context)
        errorStateTextView = TextView(context)
        retryTextView = TextView(context)
        recyclerView = android.view.View(context)
        
        stateManager = StateManager(
            progressBar = progressBar,
            emptyStateTextView = emptyStateTextView,
            errorStateLayout = errorStateLayout,
            errorStateTextView = errorStateTextView,
            retryTextView = retryTextView,
            recyclerView = recyclerView,
            scope = lifecycleOwner.lifecycleScope,
            context = context
        )
    }

    @Test
    fun `showLoading shows only progress bar`() {
        stateManager.showLoading()

        assertEquals(android.view.View.VISIBLE, progressBar.visibility)
        assertEquals(android.view.View.GONE, emptyStateTextView.visibility)
        assertEquals(android.view.View.GONE, errorStateLayout.visibility)
        assertEquals(android.view.View.GONE, recyclerView.visibility)
    }

    @Test
    fun `showSuccess shows only recyclerview`() {
        stateManager.showSuccess()

        assertEquals(android.view.View.GONE, progressBar.visibility)
        assertEquals(android.view.View.GONE, emptyStateTextView.visibility)
        assertEquals(android.view.View.GONE, errorStateLayout.visibility)
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun `showEmpty shows only empty state text`() {
        stateManager.showEmpty()

        assertEquals(android.view.View.GONE, progressBar.visibility)
        assertEquals(android.view.View.VISIBLE, emptyStateTextView.visibility)
        assertEquals(android.view.View.GONE, errorStateLayout.visibility)
        assertEquals(android.view.View.GONE, recyclerView.visibility)
    }

    @Test
    fun `showError shows only error layout and sets error message`() {
        val errorMessage = "Test error message"
        
        stateManager.showError(errorMessage)

        assertEquals(android.view.View.GONE, progressBar.visibility)
        assertEquals(android.view.View.GONE, emptyStateTextView.visibility)
        assertEquals(android.view.View.VISIBLE, errorStateLayout.visibility)
        assertEquals(android.view.View.GONE, recyclerView.visibility)
        assertEquals(errorMessage, errorStateTextView.text.toString())
    }

    @Test
    fun `showError with retry callback sets retry listener`() {
        var retryCalled = false
        val errorMessage = "Test error"
        val onRetry = { retryCalled = true }

        stateManager.showError(errorMessage, onRetry)

        assertTrue(retryTextView.hasOnClickListeners())
        retryTextView.performClick()
        assertTrue(retryCalled)
    }

    @Test
    fun `showError without retry callback does not set listener`() {
        stateManager.showError("Error", null)

        assertFalse(retryTextView.hasOnClickListeners())
    }

    @Test
    fun `setRetryCallback sets retry listener`() {
        var retryCalled = false
        val onRetry = { retryCalled = true }

        stateManager.setRetryCallback(onRetry)

        assertTrue(retryTextView.hasOnClickListeners())
        retryTextView.performClick()
        assertTrue(retryCalled)
    }

    @Test
    fun `observeState with Idle state does nothing`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Idle)
        
        stateManager.observeState(stateFlow)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.GONE, progressBar.visibility)
    }

    @Test
    fun `observeState with Loading state calls showLoading`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Loading)
        
        stateManager.observeState(stateFlow)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `observeState with Success state calls showSuccess and invokes onSuccess`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val testData = "Test data"
        var onSuccessCalled = false
        var receivedData: String? = null
        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Success(testData))
        
        stateManager.observeState(
            stateFlow,
            onSuccess = { data ->
                onSuccessCalled = true
                receivedData = data
            }
        )

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
        assertTrue(onSuccessCalled)
        assertEquals(testData, receivedData)
    }

    @Test
    fun `observeState with Error state calls showError and invokes onError`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val errorMessage = "Test error"
        var onErrorCalled = false
        var receivedError: String? = null
        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Error(errorMessage))
        
        stateManager.observeState(
            stateFlow,
            onError = { error ->
                onErrorCalled = true
                receivedError = error
            }
        )

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.VISIBLE, errorStateLayout.visibility)
        assertEquals(errorMessage, errorStateTextView.text.toString())
        assertTrue(onErrorCalled)
        assertEquals(errorMessage, receivedError)
    }

    @Test
    fun `observeState without callbacks does not crash`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Success("test"))
        
        stateManager.observeState(stateFlow)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun `observeState transitions through multiple states correctly`() = runTest {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        
        val stateFlow = MutableStateFlow<UiState<String>>(UiState.Idle)
        
        val capturedStates = mutableListOf<UiState<String>>()
        
        stateManager.observeState(
            stateFlow,
            onSuccess = { capturedStates.add(UiState.Success(it)) },
            onError = { capturedStates.add(UiState.Error(it)) }
        )

        android.os.Looper.getMainLooper().runToEndOfTasks()

        stateFlow.value = UiState.Loading
        android.os.Looper.getMainLooper().runToEndOfTasks()
        assertEquals(android.view.View.VISIBLE, progressBar.visibility)

        stateFlow.value = UiState.Success("data")
        android.os.Looper.getMainLooper().runToEndOfTasks()
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)

        stateFlow.value = UiState.Error("error")
        android.os.Looper.getMainLooper().runToEndOfTasks()
        assertEquals(android.view.View.VISIBLE, errorStateLayout.visibility)
    }

    @Test
    fun `create companion method returns StateManager instance`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        
        val createdManager = StateManager.create(
            progressBar = progressBar,
            emptyStateTextView = emptyStateTextView,
            errorStateLayout = errorStateLayout,
            errorStateTextView = errorStateTextView,
            retryTextView = retryTextView,
            recyclerView = recyclerView,
            scope = lifecycleOwner.lifecycleScope,
            context = context
        )
        
        assertNotNull(createdManager)
        assertTrue(createdManager is StateManager)
    }

    @Test
    fun `showError handles empty error message`() {
        stateManager.showError("")

        assertEquals("", errorStateTextView.text.toString())
    }

    @Test
    fun `showError handles long error message`() {
        val longMessage = "A".repeat(1000)
        
        stateManager.showError(longMessage)

        assertEquals(longMessage, errorStateTextView.text.toString())
    }

    @Test
    fun `multiple state transitions update views correctly`() {
        stateManager.showLoading()
        assertEquals(android.view.View.VISIBLE, progressBar.visibility)

        stateManager.showSuccess()
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)

        stateManager.showError("error")
        assertEquals(android.view.View.VISIBLE, errorStateLayout.visibility)

        stateManager.showEmpty()
        assertEquals(android.view.View.VISIBLE, emptyStateTextView.visibility)

        stateManager.showSuccess()
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun `observeState handles null data in Success state`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val stateFlow: StateFlow<UiState<String?>> = MutableStateFlow(UiState.Success(null))
        
        stateManager.observeState(stateFlow)

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun `retry callback is called on error state retry button click`() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        var retryCalled = false
        val errorMessage = "Network error"
        val stateFlow: StateFlow<UiState<String>> = MutableStateFlow(UiState.Error(errorMessage))
        
        stateManager.observeState(
            stateFlow,
            onError = { 
                stateManager.setRetryCallback { retryCalled = true }
            }
        )

        android.os.Looper.getMainLooper().runToEndOfTasks()
        
        retryTextView.performClick()
        assertTrue(retryCalled)
    }

    @Test
    fun `showLoading can be called multiple times`() {
        stateManager.showLoading()
        stateManager.showLoading()
        stateManager.showLoading()

        assertEquals(android.view.View.VISIBLE, progressBar.visibility)
    }

    @Test
    fun `showSuccess can be called multiple times`() {
        stateManager.showSuccess()
        stateManager.showSuccess()
        stateManager.showSuccess()

        assertEquals(android.view.View.VISIBLE, recyclerView.visibility)
    }

    @Test
    fun `showEmpty can be called multiple times`() {
        stateManager.showEmpty()
        stateManager.showEmpty()
        stateManager.showEmpty()

        assertEquals(android.view.View.VISIBLE, emptyStateTextView.visibility)
    }

    @Test
    fun `showError can be called multiple times with different messages`() {
        stateManager.showError("Error 1")
        assertEquals("Error 1", errorStateTextView.text.toString())

        stateManager.showError("Error 2")
        assertEquals("Error 2", errorStateTextView.text.toString())

        stateManager.showError("Error 3")
        assertEquals("Error 3", errorStateTextView.text.toString())
    }

    class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = registry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }
}
