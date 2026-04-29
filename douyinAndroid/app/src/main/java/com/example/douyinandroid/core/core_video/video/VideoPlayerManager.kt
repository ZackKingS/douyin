package com.example.douyinandroid.core.core_video.video

import android.content.Context
import android.view.SurfaceHolder
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.douyinandroid.common.common_utils.LogUtil

@OptIn(UnstableApi::class)
class VideoPlayerManager private constructor() {

    companion object {
        val instance: VideoPlayerManager by lazy { VideoPlayerManager() }
        private const val TAG = "VideoPlayerManager"
    }

    private var exoPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var currentVideoUrl: String? = null
    private var isPrepared = false

    var playerListener: PlayerListener? = null

    fun initialize(context: Context) {
        if (exoPlayer == null) {
            trackSelector = DefaultTrackSelector(context)
            exoPlayer = ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector!!)
                .setMediaSourceFactory(DefaultMediaSourceFactory(context))
                .build()
                .apply {
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE
                    addListener(playerListenerImpl)
                }
            LogUtil.d(TAG, "ExoPlayer initialized")
        }
    }

    fun playVideo(videoUrl: String) {
        LogUtil.d(TAG, "playVideo called with url: $videoUrl")
        exoPlayer?.let { player ->
            if (currentVideoUrl == videoUrl && isPrepared) {
                // Same video, just resume
                player.play()
                LogUtil.d(TAG, "Resuming video: $videoUrl")
            } else {
                // New video
                currentVideoUrl = videoUrl
                isPrepared = false
                val mediaItem = MediaItem.fromUri(videoUrl)
                LogUtil.d(TAG, "Setting new media item: $mediaItem")
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
                LogUtil.d(TAG, "Playing new video: $videoUrl")
            }
        } ?: run {
            LogUtil.e(TAG, "exoPlayer is null!")
        }
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    fun setVolume(volume: Float) {
        exoPlayer?.volume = volume.coerceIn(0f, 1f)
    }

    fun setPlayWhenReady(playWhenReady: Boolean) {
        exoPlayer?.playWhenReady = playWhenReady
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentVideoUrl = null
        isPrepared = false
        LogUtil.d(TAG, "ExoPlayer released")
    }

    fun stop() {
        exoPlayer?.stop()
        isPrepared = false
    }

    private val playerListenerImpl = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    LogUtil.d(TAG, "Player state: IDLE")
                    playerListener?.onPlayerStateChanged(PlayerState.IDLE)
                }
                Player.STATE_BUFFERING -> {
                    LogUtil.d(TAG, "Player state: BUFFERING")
                    playerListener?.onPlayerStateChanged(PlayerState.BUFFERING)
                }
                Player.STATE_READY -> {
                    LogUtil.d(TAG, "Player state: READY")
                    isPrepared = true
                    playerListener?.onPlayerStateChanged(PlayerState.READY)
                }
                Player.STATE_ENDED -> {
                    LogUtil.d(TAG, "Player state: ENDED")
                    playerListener?.onPlayerStateChanged(PlayerState.ENDED)
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            LogUtil.d(TAG, "Is playing: $isPlaying")
            playerListener?.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            LogUtil.e(TAG, "Player error: ${error.message}", error)
            playerListener?.onPlayerError(error)
        }
    }

    enum class PlayerState {
        IDLE,
        BUFFERING,
        READY,
        ENDED
    }

    interface PlayerListener {
        fun onPlayerStateChanged(state: PlayerState) {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onPlayerError(error: PlaybackException) {}
    }
}
