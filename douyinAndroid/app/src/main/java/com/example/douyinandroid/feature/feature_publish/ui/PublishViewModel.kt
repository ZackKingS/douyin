package com.example.douyinandroid.feature.feature_publish.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.repository.PublishRepository
import com.example.douyinandroid.domain.repository.PublishResult
import kotlinx.coroutines.launch

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
        updateState()
    }

    fun setCoverUri(uri: Uri?) {
        _coverUri.value = uri
    }

    fun setTitle(title: String) {
        _title.value = title
        updateState()
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setLocation(location: String?) {
        _location.value = location
    }

    fun addTopic(topic: TopicItem) {
        val current = _selectedTopics.value ?: emptyList()
        if (current.none { it.id == topic.id }) {
            _selectedTopics.value = current + topic
        }
    }

    fun removeTopic(topic: TopicItem) {
        _selectedTopics.value = _selectedTopics.value?.filter { it.id != topic.id }
    }

    fun canPublish(): Boolean {
        return _videoUri.value != null && !_title.value.isNullOrBlank()
    }

    private fun updateState() {
        _uiState.value = when {
            _videoUri.value == null -> PublishUiState.SelectVideo
            _title.value.isNullOrBlank() -> PublishUiState.EditDetails
            else -> PublishUiState.ReadyToPublish
        }
    }

    fun uploadVideo() {
        val videoUri = _videoUri.value ?: return
        val title = _title.value ?: return

        _uiState.value = PublishUiState.Uploading

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
                }
            )

            when (result) {
                is Result.Success -> {
                    _uiState.value = PublishUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = PublishUiState.Error(result.message ?: "上传失败")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun navigateToMainComplete() {
        _navigateToMain.value = false
    }

    fun triggerNavigateToMain() {
        _navigateToMain.value = true
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
