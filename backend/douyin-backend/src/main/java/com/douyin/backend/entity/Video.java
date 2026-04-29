package com.douyin.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false, unique = true, length = 32)
    private String videoId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String coverUrl;

    private Integer duration = 0;
    private Integer width = 1080;
    private Integer height = 1920;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    private Long likeCount = 0L;
    private Long commentCount = 0L;
    private Long shareCount = 0L;
    private Long collectCount = 0L;
    private Long viewCount = 0L;
    private Integer status = 1;
    private String topicIds = "";
    private String location = "";
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String atUserIds = "";
    private String musicId = "";
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createTime = now;
        updateTime = now;
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getVideoId() { return videoId; }
    public void setVideoId(String videoId) { this.videoId = videoId; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getShareCount() { return shareCount; }
    public void setShareCount(Long shareCount) { this.shareCount = shareCount; }
    public Long getCollectCount() { return collectCount; }
    public void setCollectCount(Long collectCount) { this.collectCount = collectCount; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getTopicIds() { return topicIds; }
    public void setTopicIds(String topicIds) { this.topicIds = topicIds; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getAtUserIds() { return atUserIds; }
    public void setAtUserIds(String atUserIds) { this.atUserIds = atUserIds; }
    public String getMusicId() { return musicId; }
    public void setMusicId(String musicId) { this.musicId = musicId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
}
