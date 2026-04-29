package com.example.douyinandroid.feature.feature_auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.domain.repository.AuthRepository

class AuthViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val authRepository = com.example.douyinandroid.core.ServiceLocator.get<AuthRepository>(
                AuthRepository::class.java
            )
            val authPreferences = com.example.douyinandroid.core.ServiceLocator.get<AuthPreferences>(
                AuthPreferences::class.java
            )
            return AuthViewModel(authRepository, authPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
