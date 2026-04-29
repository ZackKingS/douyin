package com.example.douyinandroid.feature.feature_publish.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.douyinandroid.R
import com.example.douyinandroid.common.router.RouterConstants
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.android.arouter.facade.annotation.Route

@Route(path = RouterConstants.PATH_PUBLISH)
class PublishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_publish)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, PublishFragment.newInstance())
                .commit()
        }
    }
}
