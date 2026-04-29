package com.example.douyinandroid.feature.feature_auth.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.douyinandroid.R
import com.example.douyinandroid.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.etUsername.addTextChangedListener {
            viewModel.updateRegisterUsername(it?.toString() ?: "")
        }

        binding.etNickname.addTextChangedListener {
            viewModel.updateRegisterNickname(it?.toString() ?: "")
        }

        binding.etPassword.addTextChangedListener {
            viewModel.updateRegisterPassword(it?.toString() ?: "")
        }

        binding.etConfirmPassword.addTextChangedListener {
            viewModel.updateRegisterConfirmPassword(it?.toString() ?: "")
        }

        binding.etPhone.addTextChangedListener {
            viewModel.updateRegisterPhone(it?.toString() ?: "")
        }

        binding.etEmail.addTextChangedListener {
            viewModel.updateRegisterEmail(it?.toString() ?: "")
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AuthUiState.Loading -> {
                    showLoading(true)
                }
                is AuthUiState.LoginSuccess -> {
                    showLoading(false)
                }
                is AuthUiState.RegisterSuccess -> {
                    showLoading(false)
                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthUiState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is AuthUiState.Idle -> {
                    showLoading(false)
                }
            }
        }

        viewModel.registerFormState.observe(this) { state ->
            binding.tilUsername.error = state.usernameError
            binding.tilNickname.error = state.nicknameError
            binding.tilPassword.error = state.passwordError
            binding.tilConfirmPassword.error = state.confirmPasswordError
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.etUsername.isEnabled = !show
        binding.etNickname.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.etConfirmPassword.isEnabled = !show
        binding.etPhone.isEnabled = !show
        binding.etEmail.isEnabled = !show
    }

    private fun navigateToMain() {
        setResult(RESULT_OK)
        finish()
    }
}
