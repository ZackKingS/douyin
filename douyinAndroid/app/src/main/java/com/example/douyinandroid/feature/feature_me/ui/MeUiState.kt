package com.example.douyinandroid.feature.feature_me.ui

data class MeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val logoutFinished: Boolean = false
)
