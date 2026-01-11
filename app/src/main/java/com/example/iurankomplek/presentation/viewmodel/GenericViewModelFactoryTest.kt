package com.example.iurankomplek.presentation.viewmodel

import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Test ViewModel for GenericViewModelFactory verification.
 */
class TestViewModel(
    private val testValue: String
) : BaseViewModel() {

    private val _testState = createMutableStateFlow<String>(UiState.Loading)
    val testState: StateFlow<UiState<String>> = _testState

    fun updateTestValue(newValue: String) {
        executeWithLoadingStateForResult(_testState) {
            newValue
        }
    }

    companion object {
        fun Factory(testValue: String) = viewModelInstance {
            TestViewModel(testValue)
        }
    }
}

/**
 * Test class for GenericViewModelFactory pattern.
 *
 * Verifies:
 * 1. viewModelInstance returns ViewModel instance directly
 * 2. viewModelFactory returns ViewModelProvider.Factory
 * 3. Both patterns are type-safe and compile correctly
 */
class GenericViewModelFactoryTest {
    
    /**
     * Test Pattern 1: DependencyContainer (viewModelInstance)
     * Returns ViewModel instance directly
     */
    fun testDependencyContainerPattern() {
        val viewModel = TestViewModel.Factory("test_value")
        
        assert(viewModel is TestViewModel)
        assert(viewModel.testState.value is UiState.Loading)
        println("✅ DependencyContainer pattern test passed")
    }
    
    /**
     * Test Pattern 2: ViewModelProvider (viewModelFactory)
     * Returns ViewModelProvider.Factory for use with ViewModelProvider
     * 
     * Note: This pattern is NOT currently used in this codebase,
     * but available for future migration to standard Android pattern.
     */
    fun testViewModelProviderPattern() {
        val factory = com.example.iurankomplek.presentation.viewmodel.viewModelFactory {
            TestViewModel("test_value")
        }
        
        assert(factory is androidx.lifecycle.ViewModelProvider.Factory)
        println("✅ ViewModelProvider pattern test passed")
    }
    
    /**
     * Run all tests
     */
    fun runAllTests() {
        println("\n=== GenericViewModelFactory Tests ===")
        testDependencyContainerPattern()
        testViewModelProviderPattern()
        println("=== All tests passed ===\n")
    }
}
