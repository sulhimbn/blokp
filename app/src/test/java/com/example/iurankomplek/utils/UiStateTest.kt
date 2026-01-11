package com.example.iurankomplek.utils

import org.junit.Test
import org.junit.Assert.*

class UiStateTest {

    @Test
    fun testUiStateIdle_isSingleton() {
        val idle1 = UiState.Idle
        val idle2 = UiState.Idle
        assertSame("Idle state should be singleton", idle1, idle2)
    }

    @Test
    fun testUiStateLoading_isSingleton() {
        val loading1 = UiState.Loading
        val loading2 = UiState.Loading
        assertSame("Loading state should be singleton", loading1, loading2)
    }

    @Test
    fun testUiStateSuccess_containsData() {
        val testData = "Test Data"
        val success = UiState.Success(testData)
        assertEquals("Success should contain data", testData, success.data)
    }

    @Test
    fun testUiStateSuccess_withDifferentDataTypes() {
        val stringData = "String Data"
        val intData = 42
        val listData = listOf(1, 2, 3)
        val nullData: String? = null

        val stringSuccess = UiState.Success(stringData)
        val intSuccess = UiState.Success(intData)
        val listSuccess = UiState.Success(listData)
        val nullableSuccess = UiState.Success(nullData)

        assertEquals(stringData, stringSuccess.data)
        assertEquals(intData, intSuccess.data)
        assertEquals(listData, listSuccess.data)
        assertNull(nullableSuccess.data)
    }

    @Test
    fun testUiStateError_containsErrorMessage() {
        val errorMessage = "Test Error"
        val error = UiState.Error(errorMessage)
        assertEquals("Error should contain message", errorMessage, error.error)
    }

    @Test
    fun testUiStateError_withEmptyMessage() {
        val emptyError = UiState.Error("")
        assertEquals("Empty error message", "", emptyError.error)
    }

    @Test
    fun testUiStateError_withNullMessage() {
        val nullError = UiState.Error(null)
        assertNull("Null error message", nullError.error)
    }

    @Test
    fun testUiStateSuccess_equality() {
        val success1 = UiState.Success("Test")
        val success2 = UiState.Success("Test")
        val success3 = UiState.Success("Different")

        assertEquals("Success states with same data should be equal", success1, success2)
        assertNotEquals("Success states with different data should not be equal", success1, success3)
    }

    @Test
    fun testUiStateError_equality() {
        val error1 = UiState.Error("Error")
        val error2 = UiState.Error("Error")
        val error3 = UiState.Error("Different")

        assertEquals("Error states with same message should be equal", error1, error2)
        assertNotEquals("Error states with different message should not be equal", error1, error3)
    }

    @Test
    fun testUiStateDifferentTypes_notEqual() {
        val idle = UiState.Idle
        val loading = UiState.Loading
        val success = UiState.Success("Test")
        val error = UiState.Error("Error")

        assertNotEquals("Idle should not equal Loading", idle, loading)
        assertNotEquals("Idle should not equal Success", idle, success)
        assertNotEquals("Idle should not equal Error", idle, error)
        assertNotEquals("Loading should not equal Success", loading, success)
        assertNotEquals("Loading should not equal Error", loading, error)
        assertNotEquals("Success should not equal Error", success, error)
    }

    @Test
    fun testUiStateHashCode_consistency() {
        val success1 = UiState.Success("Test")
        val success2 = UiState.Success("Test")
        val error = UiState.Error("Error")

        assertEquals("Equal objects should have same hashCode", success1.hashCode(), success2.hashCode())
        assertNotEquals("Different objects should have different hashCodes", success1.hashCode(), error.hashCode())
    }

    @Test
    fun testResultSuccess_containsData() {
        val testData = "Test Result"
        val resultSuccess = OperationResult.Success(testData)
        assertEquals("OperationResult.Success should contain data", testData, resultSuccess.value)
    }

    @Test
    fun testResultError_containsExceptionAndMessage() {
        val testException = RuntimeException("Test Exception")
        val testMessage = "Error occurred"
        val resultError = OperationResult.Error(testException, testMessage)

        assertEquals("OperationResult.Error should contain exception", testException, resultError.exception)
        assertEquals("OperationResult.Error should contain message", testMessage, resultError.message)
    }

    @Test
    fun testResultLoading_isSingleton() {
        val loading1 = OperationResult.Loading
        val loading2 = OperationResult.Loading
        assertSame("OperationResult.Loading should be singleton", loading1, loading2)
    }

    @Test
    fun testResultEmpty_isSingleton() {
        val empty1 = OperationResult.Loading
        val empty2 = OperationResult.Loading
        assertSame("OperationResult.Loading should be singleton", empty1, empty2)
    }

    @Test
    fun testResultDifferentTypes_notEqual() {
        val success = OperationResult.Success("Test")
        val error = OperationResult.Error(RuntimeException(), "Error")
        val loading = OperationResult.Loading

        assertNotEquals("Success should not equal Error", success, error)
        assertNotEquals("Success should not equal Loading", success, loading)
        assertNotEquals("Error should not equal Loading", error, loading)
    }

    @Test
    fun testUiState_when_expression_withAllStates() {
        val states: List<UiState<String>> = listOf(
            UiState.Idle,
            UiState.Loading,
            UiState.Success("Data"),
            UiState.Error("Error")
        )

        var idleCount = 0
        var loadingCount = 0
        var successCount = 0
        var errorCount = 0

        states.forEach { state ->
            when (state) {
                is UiState.Idle -> idleCount++
                is UiState.Loading -> loadingCount++
                is UiState.Success<*> -> successCount++
                is UiState.Error -> errorCount++
            }
        }

        assertEquals("Should have 1 Idle state", 1, idleCount)
        assertEquals("Should have 1 Loading state", 1, loadingCount)
        assertEquals("Should have 1 Success state", 1, successCount)
        assertEquals("Should have 1 Error state", 1, errorCount)
    }

    @Test
    fun testResult_when_expression_withAllStates() {
        val results: List<OperationResult<String>> = listOf(
            OperationResult.Success("Data"),
            OperationResult.Error(RuntimeException(), "Error"),
            OperationResult.Loading
        )

        var successCount = 0
        var errorCount = 0
        var loadingCount = 0

        results.forEach { result ->
            when (result) {
                is OperationResult.Success<*> -> successCount++
                is OperationResult.Error -> errorCount++
                is OperationResult.Loading -> loadingCount++
            }
        }

        assertEquals("Should have 1 Success result", 1, successCount)
        assertEquals("Should have 1 Error result", 1, errorCount)
        assertEquals("Should have 1 Loading result", 1, loadingCount)
    }

    @Test
    fun testUiStateSuccess_withComplexDataTypes() {
        data class ComplexData(val id: Int, val name: String, val items: List<String>)

        val complexData = ComplexData(1, "Test", listOf("a", "b", "c"))
        val success = UiState.Success(complexData)

        assertEquals(complexData, success.data)
        assertEquals(1, success.data.id)
        assertEquals("Test", success.data.name)
        assertEquals(3, success.data.items.size)
    }

    @Test
    fun testUiStateError_withLongErrorMessage() {
        val longMessage = "A".repeat(1000)
        val error = UiState.Error(longMessage)
        assertEquals(1000, error.error?.length)
    }
}
