package com.douyin.backend.dto.video;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank(message = "content不能为空")
    private String content;
    private String parentId;
    private String atUserIds;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getAtUserIds() { return atUserIds; }
    public void setAtUserIds(String atUserIds) { this.atUserIds = atUserIds; }
}
