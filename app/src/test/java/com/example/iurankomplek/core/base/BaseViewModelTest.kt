package com.example.iurankomplek.core.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class BaseViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    private class TestViewModel : BaseViewModel() {
        val testState = createMutableStateFlow<String>(UiState.Idle)
        
        fun loadSuccess(data: String) {
            executeWithLoadingState(testState) {
                data
            }
        }
        
        fun loadError(message: String) {
            executeWithLoadingState(testState) {
                throw Exception(message)
            }
        }
        
        fun loadWithoutDuplicatePrevention(data: String) {
            executeWithLoadingState(testState, preventDuplicate = false) {
                data
            }
        }
        
        fun loadWithoutState(
            data: String,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
        ) {
            executeWithoutLoadingState(
                operation = { data },
                onSuccess = onSuccess,
                onError = onError
            )
        }
    }
    
    @Test
    fun `executeWithLoadingState sets Loading state before operation`() = runTest {
        val viewModel = TestViewModel()
        viewModel.loadSuccess("test data")
        
        assertEquals(UiState.Loading, viewModel.testState.value)
    }
    
    @Test
    fun `executeWithLoadingState sets Success state after successful operation`() = runTest {
        val viewModel = TestViewModel()
        viewModel.loadSuccess("test data")
        
        advanceUntilIdle()
        
        assertTrue(viewModel.testState.value is UiState.Success)
        assertEquals("test data", (viewModel.testState.value as UiState.Success).data)
    }
    
    @Test
    fun `executeWithLoadingState sets Error state after failed operation`() = runTest {
        val viewModel = TestViewModel()
        viewModel.loadError("test error")
        
        advanceUntilIdle()
        
        assertTrue(viewModel.testState.value is UiState.Error)
        assertEquals("test error", (viewModel.testState.value as UiState.Error).error)
    }
    
    @Test
    fun `executeWithLoadingState prevents duplicate calls by default`() = runTest {
        val viewModel = TestViewModel()
        viewModel.testState.value = UiState.Loading
        
        viewModel.loadSuccess("test data")
        
        assertEquals(UiState.Loading, viewModel.testState.value)
    }
    
    @Test
    fun `executeWithLoadingState allows duplicate calls when preventDuplicate is false`() = runTest {
        val viewModel = TestViewModel()
        viewModel.testState.value = UiState.Loading
        
        viewModel.loadWithoutDuplicatePrevention("test data")
        
        advanceUntilIdle()
        
        assertTrue(viewModel.testState.value is UiState.Success)
    }
    
    @Test
    fun `executeWithoutLoadingState calls onSuccess on success`() = runTest {
        val viewModel = TestViewModel()
        var successData: String? = null
        var errorData: String? = null
        
        viewModel.loadWithoutState("test data",
            onSuccess = { successData = it },
            onError = { errorData = it }
        )
        
        advanceUntilIdle()
        
        assertEquals("test data", successData)
        assertNull(errorData)
    }
    
    @Test
    fun `executeWithoutLoadingState calls onError on failure`() = runTest {
        val viewModel = TestViewModel()
        var successData: String? = null
        var errorData: String? = null
        
        class TestViewModelWithError : BaseViewModel() {
            fun loadWithoutState(
                onSuccess: (String) -> Unit,
                onError: (String) -> Unit
            ) {
                executeWithoutLoadingState(
                    operation = { throw Exception("test error") },
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        }
        
        val viewModelWithError = TestViewModelWithError()
        viewModelWithError.loadWithoutState(
            onSuccess = { successData = it },
            onError = { errorData = it }
        )
        
        advanceUntilIdle()
        
        assertNull(successData)
        assertEquals("test error", errorData)
    }
    
    @Test
    fun `createMutableStateFlow creates correct initial state`() {
        val viewModel = TestViewModel()
        
        assertEquals(UiState.Idle, viewModel.testState.value)
    }
    
    @Test
    fun `executeWithLoadingState handles null message in exception`() = runTest {
        val viewModel = TestViewModel()
        viewModel.loadError(null as String)
        
        advanceUntilIdle()
        
        assertTrue(viewModel.testState.value is UiState.Error)
        assertEquals("Unknown error occurred", (viewModel.testState.value as UiState.Error).error)
    }
    
    @Test
    fun `executeWithLoadingState transitions from Idle to Loading to Success`() = runTest {
        val viewModel = TestViewModel()
        
        assertEquals(UiState.Idle, viewModel.testState.value)
        
        viewModel.loadSuccess("test data")
        assertEquals(UiState.Loading, viewModel.testState.value)
        
        advanceUntilIdle()
        assertTrue(viewModel.testState.value is UiState.Success)
    }
    
    @Test
    fun `executeWithLoadingState transitions from Success to Loading to Success on retry`() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.loadSuccess("first data")
        advanceUntilIdle()
        assertEquals("first data", (viewModel.testState.value as UiState.Success).data)
        
        viewModel.loadSuccess("second data")
        assertEquals(UiState.Loading, viewModel.testState.value)
        
        advanceUntilIdle()
        assertEquals("second data", (viewModel.testState.value as UiState.Success).data)
    }
    
    @Test
    fun `executeWithLoadingState transitions from Error to Loading to Success on retry`() = runTest {
        val viewModel = TestViewModel()
        
        viewModel.loadError("first error")
        advanceUntilIdle()
        assertEquals("first error", (viewModel.testState.value as UiState.Error).error)
        
        viewModel.loadSuccess("retry data")
        assertEquals(UiState.Loading, viewModel.testState.value)
        
        advanceUntilIdle()
        assertEquals("retry data", (viewModel.testState.value as UiState.Success).data)
    }
}