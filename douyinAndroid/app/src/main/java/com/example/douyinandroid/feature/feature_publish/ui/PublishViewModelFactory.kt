package com.example.douyinandroid.feature.feature_publish.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.douyinandroid.core.ServiceLocator
import com.example.douyinandroid.domain.repository.PublishRepository

class PublishViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PublishViewModel::class.java) -> {
                val repository = ServiceLocator.get(PublishRepository::class.java)
                PublishViewModel(application, repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
