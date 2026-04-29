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
        const val VIDEO_LIKE = "videos/{videoId}/like"
        const val VIDEO_UNLIKE = "videos/{videoId}/like"
        const val VIDEO_COLLECT = "videos/{videoId}/collect"
        const val VIDEO_UNCOLLECT = "videos/{videoId}/collect"
        const val VIDEO_COMMENTS = "videos/{videoId}/comments"
        const val VIDEO_MY_LIKES = "videos/likes"
        const val VIDEO_MY_COLLECTS = "videos/collects"
        const val VIDEO_USER = "videos/user/{userId}"

        // User endpoints
        const val USER_REGISTER = "auth/register"
        const val USER_LOGIN = "auth/login"
        const val USER_REFRESH = "auth/refresh"
        const val USER_LOGOUT = "auth/logout"
        const val USER_INFO = "users/{userId}"
        const val USER_UPDATE = "users/info"
        const val USER_FOLLOW = "users/{userId}/follow"
        const val USER_UNFOLLOW = "users/{userId}/follow"
        const val USER_FOLLOWERS = "users/{userId}/followers"
        const val USER_FOLLOWING = "users/{userId}/following"

        // Comment endpoints
        const val COMMENT_CREATE = "comments"
        const val COMMENT_DELETE = "comments/{commentId}"
        const val COMMENT_LIKE = "comments/{commentId}/like"
        const val COMMENT_UNLIKE = "comments/{commentId}/like"

        // File endpoints
        const val FILE_UPLOAD = "files/upload"
        const val FILE_GET = "files/{filename}"
        const val FILE_DELETE = "files/{filename}"
    }
}
