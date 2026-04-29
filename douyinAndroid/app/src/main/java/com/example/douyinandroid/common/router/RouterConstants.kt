package com.example.douyinandroid.common.router

import com.alibaba.android.arouter.facade.template.ISyringe

class RouterConstants {
    companion object {
        // Module paths
        const val MODULE_MAIN = "/main"
        const val MODULE_USER = "/user"
        const val MODULE_VIDEO = "/video"
        const val MODULE_MESSAGE = "/message"
        const val MODULE_PUBLISH = "/publish"
        const val MODULE_SEARCH = "/search"

        // Main module
        const val PATH_MAIN = "$MODULE_MAIN/main"
        const val PATH_HOME = "$MODULE_MAIN/home"
        const val PATH_DISCOVER = "$MODULE_MAIN/discover"
        const val PATH_PUBLISH = "$MODULE_MAIN/publish"
        const val PATH_MESSAGE = "$MODULE_MAIN/message"
        const val PATH_PROFILE = "$MODULE_MAIN/profile"

        // Video module
        const val PATH_VIDEO_DETAIL = "$MODULE_VIDEO/detail"
        const val PATH_VIDEO_COMMENT = "$MODULE_VIDEO/comment"

        // User module
        const val PATH_USER_PROFILE = "$MODULE_USER/profile"
        const val PATH_USER_FOLLOWS = "$MODULE_USER/follows"
        const val PATH_USER_FANS = "$MODULE_USER/fans"

        // Search module
        const val PATH_SEARCH = "$MODULE_SEARCH/search"
        const val PATH_SEARCH_RESULT = "$MODULE_SEARCH/result"

        // Auth module
        const val PATH_LOGIN = "/auth/login"
        const val PATH_REGISTER = "/auth/register"

        // Settings module
        const val PATH_SETTINGS = "/settings/settings"
    }
}
