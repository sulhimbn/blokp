package com.example.iurankomplek.utils

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val error: String) : UiState<Nothing>()
    
    companion object {
        fun <T> success(data: T): UiState<T> = Success(data)
        fun <T> error(message: String): UiState<T> = Error(message)
        fun <T> loading(): UiState<T> = Loading
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
    object Empty : Result<Nothing>()
}