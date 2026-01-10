package com.example.iurankomplek.core.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testViewModel: TestViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testViewModel = TestViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `executeWithLoadingState with successful operation updates state to Success`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        val testData = "Test Data"

        testViewModel.executeWithLoadingState(stateFlow) {
            testData
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(testData), stateFlow.value)
    }

    @Test
    fun `executeWithLoadingState with exception updates state to Error`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        val errorMessage = "Test error"

        testViewModel.executeWithLoadingState(stateFlow) {
            throw Exception(errorMessage)
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(stateFlow.value is UiState.Error)
        assertEquals(errorMessage, (stateFlow.value as UiState.Error).error)
    }

    @Test
    fun `executeWithLoadingState prevents duplicate calls when preventDuplicate is true`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        stateFlow.value = UiState.Loading

        var callCount = 0
        testViewModel.executeWithLoadingState(stateFlow, preventDuplicate = true) {
            callCount++
            "Data"
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, callCount)
        assertEquals(UiState.Loading, stateFlow.value)
    }

    @Test
    fun `executeWithLoadingState allows duplicate calls when preventDuplicate is false`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        stateFlow.value = UiState.Loading

        var callCount = 0
        testViewModel.executeWithLoadingState(stateFlow, preventDuplicate = false) {
            callCount++
            "Data"
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, callCount)
        assertEquals(UiState.Success("Data"), stateFlow.value)
    }

    @Test
    fun `executeWithLoadingStateForResult with Result.Success updates state to Success`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        val testData = "Test Result"

        testViewModel.executeWithLoadingStateForResult(stateFlow) {
            Result.success(testData)
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(testData), stateFlow.value)
    }

    @Test
    fun `executeWithLoadingStateForResult with Result.Error updates state to Error`() = runTest {
        val stateFlow = testViewModel.getTestStateFlow()
        val errorMessage = "Result error"

        testViewModel.executeWithLoadingStateForResult(stateFlow) {
            Result.error(errorMessage)
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(stateFlow.value is UiState.Error)
        assertEquals(errorMessage, (stateFlow.value as UiState.Error).error)
    }

    @Test
    fun `executeWithoutLoadingState calls onSuccess on successful operation`() = runTest {
        val testData = "Test Success"
        var successCalled = false
        var receivedData: String? = null

        testViewModel.executeWithoutLoadingState(
            operation = { testData },
            onSuccess = { data ->
                successCalled = true
                receivedData = data
            },
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals(testData, receivedData)
    }

    @Test
    fun `executeWithoutLoadingState calls onError on failed operation`() = runTest {
        val errorMessage = "Test Error"
        var errorCalled = false
        var receivedError: String? = null

        testViewModel.executeWithoutLoadingState(
            operation = { throw Exception(errorMessage) },
            onSuccess = {},
            onError = { error ->
                errorCalled = true
                receivedError = error
            }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(errorCalled)
        assertEquals(errorMessage, receivedError)
    }

    @Test
    fun `createMutableStateFlow with default initial value creates Loading state`() {
        val stateFlow = testViewModel.createMutableStateFlow()

        assertTrue(stateFlow.value is UiState.Loading)
    }
}

class Result<T> private constructor(val type: Type, val data: T?, val message: String?) {
    enum class Type { SUCCESS, ERROR, LOADING, EMPTY }

    companion object {
        fun <T> success(data: T): Result<T> = Result(Type.SUCCESS, data, null)
        fun <T> error(message: String?): Result<T> = Result(Type.ERROR, null, message)
        fun <T> loading(): Result<T> = Result(Type.LOADING, null, null)
        fun <T> empty(): Result<T> = Result(Type.EMPTY, null, null)
    }
}

class TestViewModel : BaseViewModel() {
    fun <T> getTestStateFlow(): kotlinx.coroutines.flow.MutableStateFlow<UiState<T>> {
        return createMutableStateFlow(UiState.Idle)
    }
}
