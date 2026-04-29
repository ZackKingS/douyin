package com.example.douyinandroid.feature.feature_main.ui

sealed class MainUiState<out T> {
    data object Loading : MainUiState<Nothing>()
    data class Success<T>(val data: T) : MainUiState<T>()
    data class Error(val message: String) : MainUiState<Nothing>()
    data object Empty : MainUiState<Nothing>()
}

sealed class VideoItemState {
    data object Default : VideoItemState()
    data object Loading : VideoItemState()
    data class Error(val message: String) : VideoItemState()
}
