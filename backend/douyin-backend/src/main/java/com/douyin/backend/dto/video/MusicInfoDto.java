package com.douyin.backend.dto.video;

public record MusicInfoDto(
    String musicId,
    String title,
    String author,
    String albumCover
) {
}
