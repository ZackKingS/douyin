package com.example.douyinandroid

import android.app.Application
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_network.network.RetrofitClient
import com.example.douyinandroid.core.core_database.database.AppDatabase
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import com.example.douyinandroid.core.ServiceLocator
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.data.repository.UserRepositoryImpl
import com.example.douyinandroid.data.repository.VideoRepositoryImpl
import com.example.douyinandroid.data.repository.PublishRepositoryImpl
import com.example.douyinandroid.data.repository.AuthRepositoryImpl
import com.example.douyinandroid.domain.repository.UserRepository
import com.example.douyinandroid.domain.repository.VideoRepository
import com.example.douyinandroid.domain.repository.PublishRepository
import com.example.douyinandroid.domain.repository.AuthRepository
import com.alibaba.android.arouter.launcher.ARouter

class DouyinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        initARouter()
        initRetrofit()
        initServiceLocator()
        initVideoPlayer()

        LogUtil.d(TAG, "Application initialized")
    }

    private fun initRetrofit() {
        RetrofitClient.init(this)
    }

    private fun initARouter() {
        if (BuildConfig.DEBUG) {
            ARouter.openLog()
            ARouter.openDebug()
            ARouter.printStackTrace()
        }
        ARouter.init(this)
        LogUtil.d(TAG, "ARouter initialized")
    }

    private fun initServiceLocator() {
        val authPreferences = AuthPreferences.getInstance(this)

        // Register auth preferences
        ServiceLocator.register(AuthPreferences::class.java, authPreferences)

        // Register network service
        ServiceLocator.register(
            com.example.douyinandroid.core.core_network.network.ApiService::class.java,
            RetrofitClient.apiService
        )

        // Register database
        val database = AppDatabase.getInstance(this)
        ServiceLocator.register(
            com.example.douyinandroid.core.core_database.database.dao.VideoDao::class.java,
            database.videoDao()
        )
        ServiceLocator.register(
            com.example.douyinandroid.core.core_database.database.dao.UserDao::class.java,
            database.userDao()
        )

        // Register repositories
        val videoRepository = VideoRepositoryImpl(
            RetrofitClient.apiService,
            database.videoDao()
        )
        ServiceLocator.register(VideoRepository::class.java, videoRepository)

        val userRepository = UserRepositoryImpl(
            RetrofitClient.apiService,
            database.userDao()
        )
        ServiceLocator.register(UserRepository::class.java, userRepository)

        // Register publish repository
        val publishRepository = PublishRepositoryImpl(this, RetrofitClient.apiService)
        ServiceLocator.register(PublishRepository::class.java, publishRepository)

        // Register auth repository
        val authRepository = AuthRepositoryImpl(RetrofitClient.apiService, authPreferences)
        ServiceLocator.register(AuthRepository::class.java, authRepository)

        LogUtil.d(TAG, "ServiceLocator initialized with repositories")
    }

    private fun initVideoPlayer() {
        VideoPlayerManager.instance.initialize(this)
        LogUtil.d(TAG, "VideoPlayer initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        VideoPlayerManager.instance.release()
        ARouter.getInstance().destroy()
    }

    companion object {
        private const val TAG = "DouyinApp"
        lateinit var instance: DouyinApp
            private set
    }
}
