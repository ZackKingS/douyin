package com.example.douyinandroid.feature.feature_main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.douyinandroid.domain.repository.VideoRepository
import com.example.douyinandroid.core.ServiceLocator

class MainViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                val videoRepository = ServiceLocator.get(VideoRepository::class.java)
                MainViewModel(videoRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
