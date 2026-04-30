package com.example.douyinandroid.feature.feature_me.ui

import com.example.douyinandroid.domain.model.Video

object ProfileVideoPlaybackStore {
    var videos: List<Video> = emptyList()
        private set

    var initialPosition: Int = 0
        private set

    fun setPlayback(videos: List<Video>, initialPosition: Int) {
        this.videos = videos
        this.initialPosition = if (videos.isEmpty()) {
            0
        } else {
            initialPosition.coerceIn(videos.indices)
        }
    }

    fun clear() {
        videos = emptyList()
        initialPosition = 0
    }
}
