package com.example.douyinandroid.feature.feature_main.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

private const val TAG = "VideoAdapter"
private const val PAYLOAD_ATTACH_PLAYER = "attach_player"
private const val PAYLOAD_DETACH_PLAYER = "detach_player"

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
        when {
            payloads.contains(PAYLOAD_DETACH_PLAYER) -> holder.detachPlayer()
            payloads.contains(PAYLOAD_ATTACH_PLAYER) -> holder.attachPlayer(currentPlayer)
            else -> super.onBindViewHolder(holder, position, payloads)
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

        init {
            setupClickListeners()
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

            // Double tap to like
            binding.playerView.setOnClickListener { /* Prevent click */ }

            binding.layoutVideo.setOnClickListener {
                togglePlayPause()
            }
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
            }
        }

        private fun updateLikeState(isLiked: Boolean) {
            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_liked else R.drawable.ic_like
            )
        }

        fun attachPlayer(player: ExoPlayer?) {
            if (player != null && exoPlayer == null) {
                exoPlayer = player
                binding.playerView.player = player
                binding.ivCover.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }

        fun detachPlayer() {
            binding.playerView.player = null
            exoPlayer = null
            binding.ivCover.visibility = View.VISIBLE
        }

        fun releasePlayer() {
            detachPlayer()
        }

        private fun togglePlayPause() {
            exoPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                } else {
                    player.play()
                }
            }
        }

        fun showLoading() {
            binding.progressBar.visibility = View.VISIBLE
        }

        fun hideLoading() {
            binding.progressBar.visibility = View.GONE
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
}
