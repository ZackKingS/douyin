package com.douyin.backend.dto.video;

import com.douyin.backend.dto.user.UserInfoDto;
import java.util.List;

public record CommentItemResponse(
    String commentId,
    UserInfoDto user,
    String content,
    long likeCount,
    long replyCount,
    boolean isLiked,
    String createTime,
    List<CommentItemResponse> replies
) {
}
