package com.example.douyinandroid.feature.feature_me.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.douyinandroid.core.ServiceLocator
import com.example.douyinandroid.domain.repository.AuthRepository
import com.example.douyinandroid.domain.repository.UserRepository
import com.example.douyinandroid.domain.repository.VideoRepository

class MeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeViewModel::class.java)) {
            return MeViewModel(
                ServiceLocator.get(UserRepository::class.java),
                ServiceLocator.get(AuthRepository::class.java),
                ServiceLocator.get(VideoRepository::class.java)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
