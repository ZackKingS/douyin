package com.example.douyinandroid.feature.feature_me.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.douyinandroid.R
import com.example.douyinandroid.databinding.ItemProfileVideoBinding
import com.example.douyinandroid.domain.model.Video

class ProfileVideoAdapter(
    private val onVideoClick: (Video) -> Unit,
    private val onVideoLongClick: (Video) -> Unit
) : ListAdapter<Video, ProfileVideoAdapter.VideoViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemProfileVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding, onVideoClick, onVideoLongClick)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class VideoViewHolder(
        private val binding: ItemProfileVideoBinding,
        private val onVideoClick: (Video) -> Unit,
        private val onVideoLongClick: (Video) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(video: Video) {
            binding.textPlayCount.text = video.formattedViewCount
            binding.root.setOnClickListener { onVideoClick(video) }
            binding.root.setOnLongClickListener {
                onVideoLongClick(video)
                true
            }
            Glide.with(binding.imageCover)
                .load(video.coverUrl.takeIf { it.isNotBlank() })
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .centerCrop()
                .into(binding.imageCover)
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
}
