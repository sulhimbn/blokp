package com.example.iurankomplek.utils

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: String) : UiState<Nothing>()
}

sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Error(val exception: Throwable, val message: String) : OperationResult<Nothing>()
    object Loading : OperationResult<Nothing>()
    object Empty : OperationResult<Nothing>()
}

inline fun <T> OperationResult<T>.onSuccess(action: suspend (T) -> Unit): OperationResult<T> {
    if (this is OperationResult.Success) {
        kotlinx.coroutines.runBlocking {
            action(data)
        }
    }
    return this
}

inline fun <T> OperationResult<T>.onError(action: (OperationResult.Error) -> Unit): OperationResult<T> {
    if (this is OperationResult.Error) {
        action(this)
    }
    return this
}

inline fun <T, R> OperationResult<T>.map(transform: (T) -> R): OperationResult<R> {
    return when (this) {
        is OperationResult.Success -> OperationResult.Success(transform(data))
        is OperationResult.Error -> this
        is OperationResult.Loading -> this
        is OperationResult.Empty -> this
    }
}