package com.example.douyinandroid.feature.feature_main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.domain.repository.VideoRepository
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"

class MainViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentIndex = MutableLiveData<Int>(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _likeEvent = MutableLiveData<LikeEvent?>()
    val likeEvent: LiveData<LikeEvent?> = _likeEvent

    private val _shareEvent = MutableLiveData<ShareEvent?>()
    val shareEvent: LiveData<ShareEvent?> = _shareEvent

    private var nextPage = 1
    private var hasMore = true
    private var isLoadingMore = false

    private var currentVideoUrl: String? = null

    init {
        LogUtil.d(TAG, "MainViewModel initialized, start loading videos")
        loadVideos()
    }

    fun loadVideos(refresh: Boolean = false) {
        LogUtil.d(TAG, "loadVideos called, refresh=$refresh, nextPage=$nextPage, hasMore=$hasMore")

        if (refresh) {
            nextPage = 1
            hasMore = true
        }

        if (!hasMore && !refresh) {
            LogUtil.d(TAG, "No more videos to load, skip")
            return
        }
        if (isLoadingMore && !refresh) {
            LogUtil.d(TAG, "Already loading more, skip")
            return
        }

        if (refresh) {
            _isRefreshing.value = true
        } else {
            _isLoading.value = true
        }

        viewModelScope.launch {
            LogUtil.d(TAG, "Start fetching videos from repository, page=$nextPage")
            when (val result = videoRepository.getVideoFeed(nextPage)) {
                is Result.Success -> {
                    val newVideos = result.data
                    LogUtil.d(TAG, "Repository returned ${newVideos.size} videos: $newVideos")
                    val currentList = if (refresh) emptyList() else _videos.value ?: emptyList()
                    LogUtil.d(TAG, "Current list size: ${currentList.size}, adding ${newVideos.size} new videos")
                    _videos.value = currentList + newVideos
                    LogUtil.d(TAG, "Updated videos list size: ${_videos.value?.size}")

                    hasMore = newVideos.isNotEmpty()
                    nextPage++

                    _error.value = null
                    LogUtil.d(TAG, "Videos loaded successfully, hasMore=$hasMore, nextPage=$nextPage")
                }
                is Result.Error -> {
                    LogUtil.e(TAG, "Error loading videos: ${result.message}", result.exception)
                    _error.value = result.message ?: "加载失败"
                }
                is Result.Loading -> {
                    LogUtil.d(TAG, "Loading state received")
                }
            }

            _isLoading.value = false
            _isRefreshing.value = false
            isLoadingMore = false
        }
    }

    fun loadMoreVideos() {
        if (!hasMore || isLoadingMore) {
            LogUtil.d(TAG, "loadMoreVideos skipped: hasMore=$hasMore, isLoadingMore=$isLoadingMore")
            return
        }
        LogUtil.d(TAG, "loadMoreVideos started: nextPage=$nextPage")
        isLoadingMore = true
        loadVideos()
    }

    fun onVideoChanged(position: Int) {
        LogUtil.d(TAG, "onVideoChanged: position=$position, total=${_videos.value?.size ?: 0}")
        _currentIndex.value = position

        // Preload next videos
        if (position >= (_videos.value?.size ?: 0) - 3) {
            LogUtil.d(TAG, "Preload threshold reached at position=$position")
            loadMoreVideos()
        }
    }

    fun playVideo(videoUrl: String) {
        currentVideoUrl = videoUrl
        LogUtil.d(TAG, "playVideo requested: url=$videoUrl")
        VideoPlayerManager.instance.playVideo(videoUrl)
    }

    fun pauseVideo() {
        LogUtil.d(TAG, "pauseVideo requested: currentVideoUrl=$currentVideoUrl")
        VideoPlayerManager.instance.pause()
    }

    fun resumeVideo() {
        LogUtil.d(TAG, "resumeVideo requested: currentVideoUrl=$currentVideoUrl")
        VideoPlayerManager.instance.resume()
    }

    fun likeVideo(video: Video) {
        LogUtil.d(TAG, "likeVideo requested: videoId=${video.id}, currentlyLiked=${video.isLiked}, likeCount=${video.likeCount}")
        viewModelScope.launch {
            val result = if (video.isLiked) {
                videoRepository.unlikeVideo(video.id)
            } else {
                videoRepository.likeVideo(video.id)
            }

            when (result) {
                is Result.Success -> {
                    // Update local video list
                    val updatedVideos = _videos.value?.map {
                        if (it.id == video.id) {
                            it.copy(
                                isLiked = !video.isLiked,
                                likeCount = if (video.isLiked) it.likeCount - 1 else it.likeCount + 1
                            )
                        } else it
                    }
                    _videos.value = updatedVideos

                    _likeEvent.value = LikeEvent(
                        videoId = video.id,
                        isLiked = !video.isLiked,
                        likeCount = if (video.isLiked) video.likeCount - 1 else video.likeCount + 1
                    )
                    LogUtil.d(TAG, "likeVideo success: videoId=${video.id}, newLiked=${!video.isLiked}")
                }
                is Result.Error -> {
                    LogUtil.e(TAG, "likeVideo failed: videoId=${video.id}, message=${result.message}", result.exception)
                    _error.value = result.message ?: "操作失败"
                }
                is Result.Loading -> {
                    LogUtil.d(TAG, "likeVideo result loading: videoId=${video.id}")
                }
            }
        }
    }

    fun shareVideo(video: Video, platform: String) {
        LogUtil.d(TAG, "shareVideo requested: videoId=${video.id}, platform=$platform")
        viewModelScope.launch {
            when (val result = videoRepository.shareVideo(video.id, platform)) {
                is Result.Success -> {
                    _shareEvent.value = ShareEvent(
                        videoId = video.id,
                        shareUrl = result.data,
                        platform = platform
                    )
                    LogUtil.d(TAG, "shareVideo success: videoId=${video.id}, platform=$platform")
                }
                is Result.Error -> {
                    LogUtil.e(TAG, "shareVideo failed: videoId=${video.id}, platform=$platform, message=${result.message}", result.exception)
                    _error.value = result.message ?: "分享失败"
                }
                is Result.Loading -> {
                    LogUtil.d(TAG, "shareVideo result loading: videoId=${video.id}")
                }
            }
        }
    }

    fun onLikeEventHandled() {
        _likeEvent.value = null
    }

    fun onShareEventHandled() {
        _shareEvent.value = null
    }

    fun clearError() {
        _error.value = null
    }

    data class LikeEvent(
        val videoId: String,
        val isLiked: Boolean,
        val likeCount: Long
    )

    data class ShareEvent(
        val videoId: String,
        val shareUrl: String,
        val platform: String
    )
}
