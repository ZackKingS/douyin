package com.example.douyinandroid.feature.feature_me.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.model.User
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.domain.repository.AuthRepository
import com.example.douyinandroid.domain.repository.UserRepository
import kotlinx.coroutines.launch

class MeViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(MeUiState(isLoading = true))
    val uiState: LiveData<MeUiState> = _uiState

    private val _profile = MutableLiveData<User?>()
    val profile: LiveData<User?> = _profile

    private val _videos = MutableLiveData<List<Video>>(emptyList())
    val videos: LiveData<List<Video>> = _videos

    private var nextPage = 1
    private var hasMore = true
    private var isLoadingMore = false

    init {
        refresh()
    }

    fun refresh() {
        nextPage = 1
        hasMore = true
        loadProfileAndVideos(refresh = true)
    }

    fun loadMoreVideos() {
        if (!hasMore || isLoadingMore) return
        isLoadingMore = true
        loadVideos(refresh = false)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.orEmpty().copy(logoutFinished = true)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.orEmpty().copy(error = null)
    }

    private fun loadProfileAndVideos(refresh: Boolean) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = MeUiState(logoutFinished = true)
            return
        }

        _uiState.value = _uiState.value.orEmpty().copy(
            isLoading = !refresh,
            isRefreshing = refresh,
            error = null
        )

        viewModelScope.launch {
            when (val result = userRepository.getUserProfile(userId)) {
                is Result.Success -> _profile.value = result.data
                is Result.Error -> _uiState.value = _uiState.value.orEmpty().copy(
                    error = result.message ?: "个人资料加载失败"
                )
                is Result.Loading -> Unit
            }
            loadVideos(refresh = true)
        }
    }

    private fun loadVideos(refresh: Boolean) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _uiState.value = MeUiState(logoutFinished = true)
            return
        }

        viewModelScope.launch {
            when (val result = userRepository.getUserVideos(userId, nextPage, PAGE_SIZE)) {
                is Result.Success -> {
                    val current = if (refresh) emptyList() else _videos.value.orEmpty()
                    _videos.value = current + result.data
                    hasMore = result.data.size >= PAGE_SIZE
                    nextPage++
                    _uiState.value = _uiState.value.orEmpty().copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.orEmpty().copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.message ?: "作品加载失败"
                    )
                }
                is Result.Loading -> Unit
            }
            isLoadingMore = false
        }
    }

    private fun MeUiState?.orEmpty(): MeUiState = this ?: MeUiState()

    companion object {
        private const val PAGE_SIZE = 18
    }
}
