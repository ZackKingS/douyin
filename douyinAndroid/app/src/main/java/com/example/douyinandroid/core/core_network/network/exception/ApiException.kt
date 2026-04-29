package com.example.douyinandroid.core.core_network.network.exception

class ApiException(
    val code: Int,
    override val message: String,
    val errorField: String? = null,
    val errorDetail: String? = null
) : RuntimeException(message) {

    companion object {
        const val CODE_SUCCESS = 200
        const val CODE_BAD_REQUEST = 400
        const val CODE_UNAUTHORIZED = 401
        const val CODE_FORBIDDEN = 403
        const val CODE_NOT_FOUND = 404
        const val CODE_TOO_MANY_REQUESTS = 429
        const val CODE_SERVER_ERROR = 500
        const val CODE_SERVICE_UNAVAILABLE = 503
    }

    val isSuccess: Boolean
        get() = code == CODE_SUCCESS

    val isUnauthorized: Boolean
        get() = code == CODE_UNAUTHORIZED

    val isServerError: Boolean
        get() = code >= CODE_SERVER_ERROR

    fun isErrorCode(vararg codes: Int): Boolean = code in codes

    override fun toString(): String {
        return "ApiException(code=$code, message='$message', errorField=$errorField, errorDetail=$errorDetail)"
    }
}
