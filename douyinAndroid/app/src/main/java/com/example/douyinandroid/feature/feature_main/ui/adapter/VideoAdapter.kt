package com.example.douyinandroid.feature.feature_main.ui.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.douyinandroid.R
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.databinding.ItemVideoBinding
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import java.util.Locale

private const val TAG = "VideoAdapter"
private const val PAYLOAD_ATTACH_PLAYER = "attach_player"
private const val PAYLOAD_DETACH_PLAYER = "detach_player"
private const val PAYLOAD_INTERACTION_STATE = "interaction_state"

@UnstableApi
class VideoAdapter(
    private val onLikeClick: (Video) -> Unit,
    private val onCommentClick: (Video) -> Unit,
    private val onShareClick: (Video) -> Unit,
    private val onAuthorClick: (Long) -> Unit,
    private val onFollowClick: (Video) -> Unit
) : ListAdapter<Video, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {

    private var currentPlayingPosition = -1
    private var currentPlayer: ExoPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        LogUtil.d(TAG, "onCreateViewHolder")
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        LogUtil.d(TAG, "onBindViewHolder at position $position")
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: VideoViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        if (payloads.contains(PAYLOAD_INTERACTION_STATE)) {
            holder.bindInteractionState(getItem(position))
        }
        if (payloads.contains(PAYLOAD_DETACH_PLAYER)) {
            holder.detachPlayer()
        }
        if (payloads.contains(PAYLOAD_ATTACH_PLAYER)) {
            holder.attachPlayer(currentPlayer)
        }
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        val position = holder.bindingAdapterPosition
        LogUtil.d(TAG, "onViewAttachedToWindow at position $position, currentPlayingPosition=$currentPlayingPosition")
        if (position == currentPlayingPosition) {
            LogUtil.d(TAG, "Attaching player to position $position")
            holder.attachPlayer(currentPlayer)
        }
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        val position = holder.bindingAdapterPosition
        LogUtil.d(TAG, "onViewDetachedFromWindow at position $position")
        holder.detachPlayer()
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    fun setCurrentPlayingPosition(position: Int, player: ExoPlayer?) {
        val oldPosition = currentPlayingPosition
        currentPlayingPosition = position
        currentPlayer = player
        LogUtil.d(TAG, "setCurrentPlayingPosition: old=$oldPosition, new=$position, player=$player")

        if (oldPosition in currentList.indices && oldPosition != position) {
            LogUtil.d(TAG, "Notifying old position $oldPosition to detach")
            notifyItemChanged(oldPosition, PAYLOAD_DETACH_PLAYER)
        }
        if (position in currentList.indices) {
            LogUtil.d(TAG, "Notifying new position $position to attach")
            notifyItemChanged(position, PAYLOAD_ATTACH_PLAYER)
        }
    }

    fun updateVideoItem(video: Video) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == video.id }
        if (index != -1) {
            currentList[index] = video
            submitList(currentList)
            notifyItemChanged(index, "update")
        }
    }

    inner class VideoViewHolder(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var video: Video? = null
        private var exoPlayer: ExoPlayer? = null
        private val progressHandler = Handler(Looper.getMainLooper())
        private var isUserSeeking = false

        private val progressRunnable = object : Runnable {
            override fun run() {
                updatePlaybackProgress()
                progressHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
            }
        }

        private val progressPlayerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackProgress()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlaybackProgress()
            }
        }

        init {
            setupClickListeners()
            setupProgressControls()
        }

        private fun setupClickListeners() {
            binding.ivLike.setOnClickListener {
                video?.let { onLikeClick(it) }
            }

            binding.ivComment.setOnClickListener {
                video?.let { onCommentClick(it) }
            }

            binding.ivShare.setOnClickListener {
                video?.let { onShareClick(it) }
            }

            binding.layoutAuthor.setOnClickListener {
                video?.author?.userId?.let { onAuthorClick(it) }
            }

            binding.tvAuthorName.setOnClickListener {
                video?.author?.userId?.let { onAuthorClick(it) }
            }

            binding.ivAvatar.setOnClickListener {
                video?.author?.userId?.let { onAuthorClick(it) }
            }

            binding.ivFollow.setOnClickListener {
                video?.let { onFollowClick(it) }
            }

            binding.playerView.setOnClickListener {
                togglePlayPause()
            }

            binding.layoutVideo.setOnClickListener {
                togglePlayPause()
            }

            // Also handle click on cover image and PlayerView
            binding.ivCover.setOnClickListener {
                togglePlayPause()
            }
        }

        private fun setupProgressControls() {
            binding.seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    val duration = getPlayableDuration() ?: return
                    val seekPosition = duration * progress / PROGRESS_MAX
                    binding.tvCurrentTime.text = formatTime(seekPosition)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val player = exoPlayer ?: return
                    val duration = getPlayableDuration()
                    if (duration != null) {
                        val seekPosition = duration * (seekBar?.progress ?: 0) / PROGRESS_MAX
                        player.seekTo(seekPosition)
                    }
                    isUserSeeking = false
                    updatePlaybackProgress()
                }
            })
        }

        fun bind(item: Video) {
            video = item
            LogUtil.d(TAG, "bind: binding video id=${item.id}, title=${item.title}, videoUrl=${item.videoUrl}, coverUrl=${item.coverUrl}")

            with(binding) {
                // Author info
                tvAuthorName.text = item.author?.nickname ?: ""
                tvDescription.text = item.description ?: item.title ?: ""

                // Author avatar
                Glide.with(ivAvatar)
                    .load(item.author?.avatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .into(ivAvatar)

                // Stats
                tvLikeCount.text = item.formattedLikeCount
                tvCommentCount.text = item.formattedCommentCount
                tvShareCount.text = item.formattedShareCount

                // Like state
                updateLikeState(item.isLiked)

                // Follow button
                val isFollowing = item.author?.isFollowing == true
                ivFollow.visibility = if (isFollowing) View.GONE else View.VISIBLE

                // Cover image (shown before video loads)
                Glide.with(ivCover)
                    .load(item.coverUrl)
                    .centerCrop()
                    .into(ivCover)

                // Music info
                item.music?.let { music ->
                    tvMusicTitle.text = buildString {
                        append(if (music.title.isNullOrEmpty()) "" else music.title)
                        if (!music.author.isNullOrEmpty()) {
                            append(" - ")
                            append(music.author)
                        }
                    }
                } ?: run {
                    tvMusicTitle.text = ""
                }

                // Duration
                tvDuration.text = item.formattedDuration

                resetPlaybackProgress()
                hidePlayPauseIcon()
            }
        }

        fun bindInteractionState(item: Video) {
            video = item
            with(binding) {
                tvLikeCount.text = item.formattedLikeCount
                tvCommentCount.text = item.formattedCommentCount
                tvShareCount.text = item.formattedShareCount
                updateLikeState(item.isLiked)
            }
        }

        private fun updateLikeState(isLiked: Boolean) {
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_liked else R.drawable.ic_like
            )
        }

        fun attachPlayer(player: ExoPlayer?) {
            if (player == null) return
            if (exoPlayer != player) {
                detachPlayer()
                exoPlayer = player
                player.addListener(progressPlayerListener)
            }
            binding.playerView.player = player
            binding.ivCover.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            hidePlayPauseIcon()
            startProgressUpdates()
        }

        fun detachPlayer() {
            stopProgressUpdates()
            exoPlayer?.removeListener(progressPlayerListener)
            binding.playerView.player = null
            exoPlayer = null
            binding.ivCover.visibility = View.VISIBLE
            hidePlayPauseIcon()
        }

        fun releasePlayer() {
            detachPlayer()
        }

        private fun togglePlayPause() {
            val player = VideoPlayerManager.instance.getPlayer()
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                    showPlayPauseIcon(true)
                } else {
                    it.play()
                    showPlayPauseIcon(false)
                }
            }
        }

        private fun showPlayPauseIcon(isPaused: Boolean) {
//            val iconRes = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause

            val iconRes = R.drawable.ic_play

            binding.ivPlayPause.animate().cancel()
            binding.ivPlayPause.setImageResource(iconRes)
            binding.ivPlayPause.visibility = View.VISIBLE
            binding.ivPlayPause.alpha = 1f

            if (isPaused) {
                return
            }

            binding.ivPlayPause.animate()
                .alpha(0f)
                .setDuration(500)
                .setStartDelay(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        binding.ivPlayPause.visibility = View.GONE
                    }
                })
                .start()
        }

        private fun hidePlayPauseIcon() {
            binding.ivPlayPause.animate()
                .setListener(null)
                .cancel()
            binding.ivPlayPause.visibility = View.GONE
            binding.ivPlayPause.alpha = 0f
        }

        fun showLoading() {
            binding.progressBar.visibility = View.VISIBLE
        }

        fun hideLoading() {
            binding.progressBar.visibility = View.GONE
        }

        private fun startProgressUpdates() {
            progressHandler.removeCallbacks(progressRunnable)
            updatePlaybackProgress()
            progressHandler.postDelayed(progressRunnable, PROGRESS_UPDATE_INTERVAL_MS)
        }

        private fun stopProgressUpdates() {
            progressHandler.removeCallbacks(progressRunnable)
            isUserSeeking = false
        }

        private fun updatePlaybackProgress() {
            val player = exoPlayer ?: run {
                resetPlaybackProgress()
                return
            }
            val duration = getPlayableDuration(player)
            val position = player.currentPosition.coerceAtLeast(0L)

            binding.tvCurrentTime.text = formatTime(position)
            binding.tvTotalTime.text = formatTime(duration ?: 0L)

            if (!isUserSeeking) {
                binding.seekBarProgress.progress = if (duration != null && duration > 0L) {
                    ((position.coerceAtMost(duration) * PROGRESS_MAX) / duration).toInt()
                } else {
                    0
                }
            }

            binding.seekBarProgress.secondaryProgress = if (duration != null && duration > 0L) {
                ((player.bufferedPosition.coerceAtMost(duration) * PROGRESS_MAX) / duration).toInt()
            } else {
                0
            }
        }

        private fun resetPlaybackProgress() {
            binding.seekBarProgress.progress = 0
            binding.seekBarProgress.secondaryProgress = 0
            binding.tvCurrentTime.text = formatTime(0L)
            binding.tvTotalTime.text = formatTime(0L)
        }

        private fun getPlayableDuration(player: ExoPlayer? = exoPlayer): Long? {
            val duration = player?.duration ?: return null
            return duration.takeIf { it > 0L && it != C.TIME_UNSET }
        }

        private fun formatTime(timeMs: Long): String {
            val totalSeconds = (timeMs.coerceAtLeast(0L) / 1000).toInt()
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Video, newItem: Video): Any? {
            val onlyInteractionChanged = oldItem.copy(
                likeCount = newItem.likeCount,
                commentCount = newItem.commentCount,
                shareCount = newItem.shareCount,
                isLiked = newItem.isLiked
            ) == newItem

            return if (onlyInteractionChanged) {
                PAYLOAD_INTERACTION_STATE
            } else {
                null
            }
        }
    }
}

private const val PROGRESS_MAX = 1000
private const val PROGRESS_UPDATE_INTERVAL_MS = 250L
