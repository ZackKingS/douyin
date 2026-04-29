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
import com.example.douyinandroid.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        binding.etUsername.addTextChangedListener {
            viewModel.updateLoginUsername(it?.toString() ?: "")
        }

        binding.etPassword.addTextChangedListener {
            viewModel.updateLoginPassword(it?.toString() ?: "")
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login()
        }

        binding.tvRegister.setOnClickListener {
            navigateToRegister()
        }

        binding.tvPhoneLogin.setOnClickListener {
            navigateToPhoneLogin()
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
                    Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthUiState.RegisterSuccess -> {
                    showLoading(false)
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

        viewModel.loginFormState.observe(this) { state ->
            binding.tilUsername.error = state.usernameError
            binding.tilPassword.error = state.passwordError
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.etUsername.isEnabled = !show
        binding.etPassword.isEnabled = !show
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun navigateToPhoneLogin() {
        Toast.makeText(this, "手机号登录功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMain() {
        setResult(RESULT_OK)
        finish()
    }
}
