package com.example.douyinandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import com.example.douyinandroid.feature.feature_auth.ui.LoginActivity
import com.example.douyinandroid.feature.feature_main.ui.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var authPreferences: AuthPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize video player manager
        VideoPlayerManager.instance.initialize(this)

        authPreferences = AuthPreferences.getInstance(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!authPreferences.isLoggedIn) {
            navigateToLogin()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, MainFragment.newInstance())
                .commit()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGIN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main, MainFragment.newInstance())
                    .commit()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoPlayerManager.instance.release()
    }

    companion object {
        private const val REQUEST_LOGIN = 1001
    }
}
