package com.example.douyinandroid.domain.model

data class Video(
    val id: String,
    val title: String?,
    val description: String?,
    val videoUrl: String,
    val coverUrl: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val author: User?,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val collectCount: Long,
    val viewCount: Long,
    val isLiked: Boolean,
    val isCollected: Boolean,
    val topicName: String?,
    val music: Music?,
    val location: String?,
    val createTime: String?
) {
    val formattedDuration: String
        get() {
            val seconds = duration % 60
            val minutes = duration / 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedLikeCount: String
        get() = formatCount(likeCount)

    val formattedCommentCount: String
        get() = formatCount(commentCount)

    val formattedShareCount: String
        get() = formatCount(shareCount)

    val formattedViewCount: String
        get() = formatCount(viewCount)

    private fun formatCount(count: Long): String {
        return when {
            count >= 1_0000_0000 -> String.format("%.1f亿", count / 1_0000_0000.0)
            count >= 1_0000 -> String.format("%.1f万", count / 1_0000.0)
            else -> count.toString()
        }
    }
}

data class User(
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    val signature: String? = null,
    val fansCount: Long = 0,
    val followCount: Long = 0,
    val likeCount: Long = 0,
    val videoCount: Long = 0,
    val isFollowing: Boolean = false,
    val isMutual: Boolean = false,
    val level: Int = 0
) {
    val formattedFansCount: String
        get() = formatCount(fansCount)

    val formattedFollowCount: String
        get() = formatCount(followCount)

    val formattedLikeCount: String
        get() = formatCount(likeCount)

    val formattedVideoCount: String
        get() = formatCount(videoCount)

    private fun formatCount(count: Long): String {
        return when {
            count >= 1_0000_0000 -> String.format("%.1f亿", count / 1_0000_0000.0)
            count >= 1_0000 -> String.format("%.1f万", count / 1_0000.0)
            else -> count.toString()
        }
    }
}

data class Music(
    val musicId: String,
    val title: String?,
    val author: String?,
    val albumCover: String?
)

data class Comment(
    val commentId: String,
    val user: User?,
    val content: String,
    val likeCount: Long,
    val replyCount: Long,
    val isLiked: Boolean,
    val createTime: String?,
    val replies: List<Comment>?
)

data class Topic(
    val id: Long,
    val name: String
)

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Error)?.exception
}
