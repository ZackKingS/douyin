package com.example.douyinandroid.feature.feature_auth.ui

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class LoginSuccess(
        val userId: Long,
        val nickname: String,
        val avatar: String?
    ) : AuthUiState()
    data class RegisterSuccess(
        val userId: Long,
        val nickname: String,
        val avatar: String?
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val isUsernameError: Boolean = false,
    val isPasswordError: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean
        get() = username.isNotBlank() && password.length >= 6
}

data class RegisterFormState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val phone: String = "",
    val email: String = "",
    val isUsernameError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isConfirmPasswordError: Boolean = false,
    val isNicknameError: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nicknameError: String? = null
) {
    val isValid: Boolean
        get() = username.length >= 6 &&
                password.length >= 6 &&
                password == confirmPassword &&
                nickname.length >= 2
}
