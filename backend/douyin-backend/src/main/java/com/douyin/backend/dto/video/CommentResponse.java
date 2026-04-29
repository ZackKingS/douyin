package com.douyin.backend.dto.video;

import com.douyin.backend.dto.user.UserInfoDto;

public record CommentResponse(
    String commentId,
    UserInfoDto user,
    String content,
    long likeCount,
    long replyCount,
    boolean isLiked,
    String createTime
) {
}
