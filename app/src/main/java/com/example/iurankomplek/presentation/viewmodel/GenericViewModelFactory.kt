package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Generic ViewModel Factory that eliminates boilerplate Factory classes in ViewModels.
 *
 * This provides two usage patterns:
 * 1. Return Factory directly for use with ViewModelProvider(this, factory)
 * 2. Return ViewModel instance directly for DependencyContainer pattern
 *
 * Usage Pattern 1 (ViewModelProvider):
 * ```
 * class MyViewModel(...) : BaseViewModel() {
 *     companion object {
 *         fun Factory(...) = viewModelFactory { MyViewModel(...) }
 *     }
 * }
 * // In Activity/Fragment:
 * viewModel = ViewModelProvider(this, MyViewModel.Factory(dep1, dep2))[MyViewModel::class.java]
 * ```
 *
 * Usage Pattern 2 (DependencyContainer):
 * ```
 * class MyViewModel(...) : BaseViewModel() {
 *     companion object {
 *         fun Factory(...) = viewModelInstance { MyViewModel(...) }
 *     }
 * }
 * // In DependencyContainer:
 * fun provideMyViewModel() = MyViewModel.Factory(dep1, dep2)
 * ```
 *
 * @param creator Lambda that creates a ViewModel instance
 * @param T Type of ViewModel to create
 */
class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        if (modelClass.isAssignableFrom(creator().javaClass)) {
            return creator() as VM
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Extension function to create a GenericViewModelFactory for any ViewModel.
 *
 * This returns a ViewModelProvider.Factory for use with ViewModelProvider.
 *
 * Usage:
 * ```
 * class MyViewModel(...) : BaseViewModel() {
 *     companion object {
 *         fun Factory(...) = viewModelFactory { MyViewModel(...) }
 *     }
 * }
 * // In Activity/Fragment:
 * viewModel = ViewModelProvider(this, MyViewModel.Factory(dep1, dep2))[MyViewModel::class.java]
 * ```
 *
 * @param creator Lambda that creates a ViewModel instance
 * @return GenericViewModelFactory instance
 */
fun <T : ViewModel> viewModelFactory(creator: () -> T): ViewModelProvider.Factory {
    return GenericViewModelFactory(creator)
}

/**
 * Extension function to create a ViewModel instance directly.
 *
 * This is used by DependencyContainer pattern to provide ViewModel instances.
 *
 * Usage:
 * ```
 * class MyViewModel(...) : BaseViewModel() {
 *     companion object {
 *         fun Factory(...) = viewModelInstance { MyViewModel(...) }
 *     }
 * }
 * // In DependencyContainer:
 * fun provideMyViewModel() = MyViewModel.Factory(dep1, dep2)
 * ```
 *
 * @param creator Lambda that creates a ViewModel instance
 * @return ViewModel instance
 */
fun <T : ViewModel> viewModelInstance(creator: () -> T): T {
    return creator()
}

/**
 * Extension function to create a GenericViewModelFactory for any ViewModel.
 *
 * This eliminates the need for nested Factory classes in ViewModels.
 *
 * Usage:
 * ```
 * class MyViewModel(...) : BaseViewModel() {
 *     companion object {
 *         val Factory = viewModelFactory(::MyViewModel)
 *     }
 * }
 * ```
 *
 * @param creator Lambda that creates the ViewModel instance
 * @return GenericViewModelFactory instance
 */
fun <T : ViewModel> viewModelFactory(creator: () -> T): ViewModelProvider.Factory {
    return GenericViewModelFactory(creator)
}

/**
 * Creates a GenericViewModelFactory for ViewModels with multiple dependencies.
 *
 * Usage with multiple parameters:
 * ```
 * class MyViewModel(
 *     private val useCase1: UseCase1,
 *     private val useCase2: UseCase2
 * ) : BaseViewModel() {
 *     companion object {
 *         fun Factory(useCase1: UseCase1, useCase2: UseCase2) = 
 *             viewModelFactory { MyViewModel(useCase1, useCase2) }
 *     }
 * }
 * ```
 *
 * @param creator Lambda that creates the ViewModel instance
 * @return GenericViewModelFactory instance
 */
fun <T : ViewModel> viewModelFactoryWithDeps(creator: () -> T): ViewModelProvider.Factory {
    return GenericViewModelFactory(creator)
}
