package com.example.douyinandroid.feature.feature_auth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.domain.model.Result
import com.example.douyinandroid.domain.repository.AuthRepository
import com.example.douyinandroid.domain.repository.LoginResult
import kotlinx.coroutines.launch

private const val TAG = "AuthViewModel"

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val authPreferences: AuthPreferences
) : ViewModel() {

    private val _uiState = MutableLiveData<AuthUiState>(AuthUiState.Idle)
    val uiState: LiveData<AuthUiState> = _uiState

    private val _loginFormState = MutableLiveData(LoginFormState())
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _registerFormState = MutableLiveData(RegisterFormState())
    val registerFormState: LiveData<RegisterFormState> = _registerFormState

    val isLoggedIn: Boolean
        get() = authPreferences.isLoggedIn

    val currentUserId: Long?
        get() = authPreferences.userId.takeIf { it != -1L }

    val currentNickname: String?
        get() = authPreferences.nickname

    val currentAvatar: String?
        get() = authPreferences.avatar

    fun updateLoginUsername(username: String) {
        _loginFormState.value = _loginFormState.value?.copy(
            username = username,
            isUsernameError = false,
            usernameError = null
        )
    }

    fun updateLoginPassword(password: String) {
        _loginFormState.value = _loginFormState.value?.copy(
            password = password,
            isPasswordError = false,
            passwordError = null
        )
    }

    fun updateRegisterUsername(username: String) {
        _registerFormState.value = _registerFormState.value?.copy(
            username = username,
            isUsernameError = false,
            usernameError = null
        )
    }

    fun updateRegisterPassword(password: String) {
        _registerFormState.value = _registerFormState.value?.copy(
            password = password,
            isPasswordError = false,
            passwordError = null
        )
    }

    fun updateRegisterConfirmPassword(confirmPassword: String) {
        _registerFormState.value = _registerFormState.value?.copy(
            confirmPassword = confirmPassword,
            isConfirmPasswordError = false,
            confirmPasswordError = null
        )
    }

    fun updateRegisterNickname(nickname: String) {
        _registerFormState.value = _registerFormState.value?.copy(
            nickname = nickname,
            isNicknameError = false,
            nicknameError = null
        )
    }

    fun updateRegisterPhone(phone: String) {
        _registerFormState.value = _registerFormState.value?.copy(phone = phone)
    }

    fun updateRegisterEmail(email: String) {
        _registerFormState.value = _registerFormState.value?.copy(email = email)
    }

    fun login() {
        val formState = _loginFormState.value ?: return
        LogUtil.d(TAG, "login requested: username=${formState.username}")

        if (!validateLoginForm()) {
            LogUtil.w(TAG, "login validation failed: username=${formState.username}")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            LogUtil.d(TAG, "login state changed to Loading")
            val result = authRepository.login(formState.username, formState.password)
            handleResult(result)
        }
    }

    fun loginByPhone(phone: String, code: String) {
        LogUtil.d(TAG, "loginByPhone requested: phone=${phone.maskPhone()}, codeLength=${code.length}")
        if (phone.isBlank() || code.length < 4) {
            _uiState.value = AuthUiState.Error("请输入正确的手机号和验证码")
            LogUtil.w(TAG, "loginByPhone validation failed: phone=${phone.maskPhone()}, codeLength=${code.length}")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            LogUtil.d(TAG, "loginByPhone state changed to Loading")
            val result = authRepository.loginByPhone(phone, code)
            handleResult(result)
        }
    }

    fun register() {
        val formState = _registerFormState.value ?: return
        LogUtil.d(TAG, "register requested: username=${formState.username}, nickname=${formState.nickname}")

        if (!validateRegisterForm()) {
            LogUtil.w(TAG, "register validation failed: username=${formState.username}")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            LogUtil.d(TAG, "register state changed to Loading")
            val result = authRepository.register(
                username = formState.username,
                password = formState.password,
                nickname = formState.nickname,
                phone = formState.phone.takeIf { it.isNotBlank() },
                email = formState.email.takeIf { it.isNotBlank() }
            )
            handleResult(result)
        }
    }

    fun logout() {
        LogUtil.d(TAG, "logout requested: currentUserId=$currentUserId")
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
            _loginFormState.value = LoginFormState()
            _registerFormState.value = RegisterFormState()
            LogUtil.d(TAG, "logout completed, form state reset")
        }
    }

    private fun validateLoginForm(): Boolean {
        val formState = _loginFormState.value ?: return false
        var isValid = true

        if (formState.username.isBlank()) {
            _loginFormState.value = formState.copy(
                isUsernameError = true,
                usernameError = "请输入用户名"
            )
            isValid = false
        } else if (formState.username.length < 6) {
            _loginFormState.value = formState.copy(
                isUsernameError = true,
                usernameError = "用户名长度不能少于6位"
            )
            isValid = false
        }

        if (formState.password.isBlank()) {
            _loginFormState.value = _loginFormState.value?.copy(
                isPasswordError = true,
                passwordError = "请输入密码"
            )
            isValid = false
        } else if (formState.password.length < 6) {
            _loginFormState.value = _loginFormState.value?.copy(
                isPasswordError = true,
                passwordError = "密码长度不能少于6位"
            )
            isValid = false
        }

        return isValid
    }

    private fun validateRegisterForm(): Boolean {
        val formState = _registerFormState.value ?: return false
        var isValid = true

        if (formState.username.isBlank()) {
            _registerFormState.value = formState.copy(
                isUsernameError = true,
                usernameError = "请输入用户名"
            )
            isValid = false
        } else if (formState.username.length < 6) {
            _registerFormState.value = formState.copy(
                isUsernameError = true,
                usernameError = "用户名长度不能少于6位"
            )
            isValid = false
        }

        if (formState.password.isBlank()) {
            _registerFormState.value = _registerFormState.value?.copy(
                isPasswordError = true,
                passwordError = "请输入密码"
            )
            isValid = false
        } else if (formState.password.length < 6) {
            _registerFormState.value = _registerFormState.value?.copy(
                isPasswordError = true,
                passwordError = "密码长度不能少于6位"
            )
            isValid = false
        }

        if (formState.password != formState.confirmPassword) {
            _registerFormState.value = _registerFormState.value?.copy(
                isConfirmPasswordError = true,
                confirmPasswordError = "两次输入的密码不一致"
            )
            isValid = false
        }

        if (formState.nickname.isBlank()) {
            _registerFormState.value = _registerFormState.value?.copy(
                isNicknameError = true,
                nicknameError = "请输入昵称"
            )
            isValid = false
        } else if (formState.nickname.length < 2) {
            _registerFormState.value = _registerFormState.value?.copy(
                isNicknameError = true,
                nicknameError = "昵称长度不能少于2位"
            )
            isValid = false
        }

        return isValid
    }

    private fun handleResult(result: Result<LoginResult>) {
        when (result) {
            is Result.Success -> {
                LogUtil.d(TAG, "auth result success: userId=${result.data.userId}, nickname=${result.data.nickname}")
                _uiState.value = AuthUiState.LoginSuccess(
                    userId = result.data.userId,
                    nickname = result.data.nickname,
                    avatar = result.data.avatar
                )
            }
            is Result.Error -> {
                LogUtil.e(TAG, "auth result error: ${result.message}", result.exception)
                _uiState.value = AuthUiState.Error(
                    result.message ?: result.exception.message ?: "操作失败，请稍后重试"
                )
            }
            is Result.Loading -> {
                LogUtil.d(TAG, "auth result loading")
                _uiState.value = AuthUiState.Loading
            }
        }
    }

    fun resetUiState() {
        _uiState.value = AuthUiState.Idle
        LogUtil.d(TAG, "ui state reset to Idle")
    }

    private fun String.maskPhone(): String {
        return if (length >= 7) {
            take(3) + "****" + takeLast(4)
        } else {
            "***"
        }
    }
}
