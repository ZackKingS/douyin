package com.douyin.backend.dto.video;

public record CommentResponse(
    String commentId,
    String content,
    long likeCount,
    long replyCount,
    boolean isLiked,
    String createTime
) {
}
