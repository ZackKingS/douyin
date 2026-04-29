package com.example.douyinandroid.common.common_utils

import android.util.Log
import com.example.douyinandroid.BuildConfig

object LogUtil {
    private const val TAG = "Douyin"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG-$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i("$TAG-$tag", message)
        }
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w("$TAG-$tag", message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e("$TAG-$tag", message, throwable)
            } else {
                Log.e("$TAG-$tag", message)
            }
        }
    }
}
