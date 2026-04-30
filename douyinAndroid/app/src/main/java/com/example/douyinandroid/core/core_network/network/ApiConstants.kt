package com.example.douyinandroid.core.core_network.network

object ApiConstants {
    const val BASE_URL = "http://192.168.31.105:8080/api/v1/"

    const val TIMEOUT = 30_000L

    // API Endpoints
    object Endpoints {
        // Video endpoints
        const val VIDEO_LIST = "videos/feed"
        const val VIDEO_DETAIL = "videos/{videoId}"
        const val VIDEO_UPLOAD = "videos"
        const val VIDEO_DELETE = "videos/{videoId}"
        const val VIDEO_LIKE = "videos/{videoId}/like"
        const val VIDEO_UNLIKE = "videos/{videoId}/like"
        const val VIDEO_SHARE = "videos/{videoId}/share"
        const val VIDEO_COMMENTS = "videos/{videoId}/comments"
        const val VIDEO_USER = "users/{userId}/videos"

        // User endpoints
        const val USER_REGISTER = "auth/register"
        const val USER_LOGIN = "auth/login"
        const val USER_REFRESH = "auth/refresh"
        const val USER_LOGOUT = "auth/logout"
        const val USER_INFO = "users/{userId}"
        const val USER_UPDATE = "users/me"
        const val USER_FOLLOW = "users/{userId}/follow"
        const val USER_UNFOLLOW = "users/{userId}/follow"
        const val USER_FOLLOWERS = "users/{userId}/fans"
        const val USER_FOLLOWING = "users/{userId}/follows"

        // Comment endpoints
        const val COMMENT_CREATE = "videos/{videoId}/comments"

        // File endpoints
        const val FILE_UPLOAD = "files/upload"
    }
}
