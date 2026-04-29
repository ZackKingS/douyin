package com.example.douyinandroid.feature.feature_publish.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.repository.PublishRepository
import com.example.douyinandroid.domain.repository.PublishResult
import kotlinx.coroutines.launch

private const val TAG = "PublishViewModel"

class PublishViewModel(
    application: Application,
    private val publishRepository: PublishRepository
) : ViewModel() {

    private val context = application.applicationContext

    private val _uiState = MutableLiveData<PublishUiState>(PublishUiState.Idle)
    val uiState: LiveData<PublishUiState> = _uiState

    private val _videoUri = MutableLiveData<Uri?>()
    val videoUri: LiveData<Uri?> = _videoUri

    private val _coverUri = MutableLiveData<Uri?>()
    val coverUri: LiveData<Uri?> = _coverUri

    private val _title = MutableLiveData<String>("")
    val title: LiveData<String> = _title

    private val _description = MutableLiveData<String>("")
    val description: LiveData<String> = _description

    private val _location = MutableLiveData<String?>()
    val location: LiveData<String?> = _location

    private val _selectedTopics = MutableLiveData<List<TopicItem>>(emptyList())
    val selectedTopics: LiveData<List<TopicItem>> = _selectedTopics

    private val _uploadProgress = MutableLiveData<Int>(0)
    val uploadProgress: LiveData<Int> = _uploadProgress

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain

    fun setVideoUri(uri: Uri?) {
        _videoUri.value = uri
        LogUtil.d(TAG, "setVideoUri: hasVideo=${uri != null}, uri=$uri")
        updateState()
    }

    fun setCoverUri(uri: Uri?) {
        _coverUri.value = uri
        LogUtil.d(TAG, "setCoverUri: hasCover=${uri != null}, uri=$uri")
    }

    fun setTitle(title: String) {
        _title.value = title
        LogUtil.d(TAG, "setTitle: length=${title.length}")
        updateState()
    }

    fun setDescription(description: String) {
        _description.value = description
        LogUtil.d(TAG, "setDescription: length=${description.length}")
    }

    fun setLocation(location: String?) {
        _location.value = location
        LogUtil.d(TAG, "setLocation: hasLocation=${!location.isNullOrBlank()}")
    }

    fun addTopic(topic: TopicItem) {
        val current = _selectedTopics.value ?: emptyList()
        if (current.none { it.id == topic.id }) {
            _selectedTopics.value = current + topic
            LogUtil.d(TAG, "addTopic: id=${topic.id}, name=${topic.name}, topicCount=${_selectedTopics.value?.size ?: 0}")
        } else {
            LogUtil.d(TAG, "addTopic skipped duplicate: id=${topic.id}, name=${topic.name}")
        }
    }

    fun removeTopic(topic: TopicItem) {
        _selectedTopics.value = _selectedTopics.value?.filter { it.id != topic.id }
        LogUtil.d(TAG, "removeTopic: id=${topic.id}, name=${topic.name}, topicCount=${_selectedTopics.value?.size ?: 0}")
    }

    fun canPublish(): Boolean {
        return _videoUri.value != null && !_title.value.isNullOrBlank()
    }

    private fun updateState() {
        val newState = when {
            _videoUri.value == null -> PublishUiState.SelectVideo
            _title.value.isNullOrBlank() -> PublishUiState.EditDetails
            else -> PublishUiState.ReadyToPublish
        }
        _uiState.value = newState
        LogUtil.d(TAG, "updateState: state=${newState.javaClass.simpleName}, hasVideo=${_videoUri.value != null}, titleLength=${_title.value?.length ?: 0}")
    }

    fun uploadVideo() {
        val videoUri = _videoUri.value ?: run {
            LogUtil.w(TAG, "uploadVideo ignored: videoUri is null")
            return
        }
        val title = _title.value ?: run {
            LogUtil.w(TAG, "uploadVideo ignored: title is null")
            return
        }

        _uiState.value = PublishUiState.Uploading
        LogUtil.d(TAG, "uploadVideo started: titleLength=${title.length}, topicCount=${_selectedTopics.value?.size ?: 0}, hasCover=${_coverUri.value != null}")

        viewModelScope.launch {
            val topicIds = _selectedTopics.value?.map { it.id }

            val result = publishRepository.uploadVideo(
                videoUri = videoUri,
                title = title,
                description = _description.value,
                topicIds = topicIds,
                location = _location.value,
                coverUri = _coverUri.value,
                onProgress = { progress ->
                    _uploadProgress.postValue(progress)
                    if (progress == 0 || progress == 100 || progress % 25 == 0) {
                        LogUtil.d(TAG, "upload progress: $progress")
                    }
                }
            )

            when (result) {
                is Result.Success -> {
                    LogUtil.d(TAG, "uploadVideo success: videoId=${result.data.videoId}, title=${result.data.title}")
                    _uiState.value = PublishUiState.Success(result.data)
                }
                is Result.Error -> {
                    LogUtil.e(TAG, "uploadVideo error: ${result.message}", result.exception)
                    _uiState.value = PublishUiState.Error(result.message ?: "上传失败")
                }
                is Result.Loading -> {
                    LogUtil.d(TAG, "uploadVideo result loading")
                }
            }
        }
    }

    fun navigateToMainComplete() {
        _navigateToMain.value = false
    }

    fun triggerNavigateToMain() {
        _navigateToMain.value = true
        LogUtil.d(TAG, "triggerNavigateToMain")
    }

    fun reset() {
        _videoUri.value = null
        _coverUri.value = null
        _title.value = ""
        _description.value = ""
        _location.value = null
        _selectedTopics.value = emptyList()
        _uploadProgress.value = 0
        _uiState.value = PublishUiState.Idle
        LogUtil.d(TAG, "reset publish form")
    }
}

sealed class PublishUiState {
    data object Idle : PublishUiState()
    data object SelectVideo : PublishUiState()
    data object EditDetails : PublishUiState()
    data object ReadyToPublish : PublishUiState()
    data object Uploading : PublishUiState()
    data class Success(val result: PublishResult) : PublishUiState()
    data class Error(val message: String) : PublishUiState()
}

data class TopicItem(
    val id: Long,
    val name: String
)
