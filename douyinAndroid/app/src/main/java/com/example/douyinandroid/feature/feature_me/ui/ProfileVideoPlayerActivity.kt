package com.example.douyinandroid.feature.feature_me.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import com.example.douyinandroid.databinding.FragmentMainBinding
import com.example.douyinandroid.feature.feature_main.ui.adapter.VideoAdapter

@UnstableApi
class ProfileVideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var videoAdapter: VideoAdapter

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            playVideoAt(position)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        VideoPlayerManager.instance.initialize(this)
        setupViews()
        submitVideos()
    }

    private fun setupViews() {
        binding.swipeRefresh.isEnabled = false
        binding.layoutLoading.visibility = View.GONE
        binding.layoutError.root.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        videoAdapter = VideoAdapter(
            onLikeClick = { showComingSoon() },
            onCommentClick = { showComingSoon() },
            onShareClick = { showComingSoon() },
            onAuthorClick = { },
            onFollowClick = { showComingSoon() }
        )

        binding.viewPagerVideo.adapter = videoAdapter
        binding.viewPagerVideo.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewPagerVideo.registerOnPageChangeCallback(pageChangeCallback)

        (binding.viewPagerVideo.getChildAt(0) as? RecyclerView)?.apply {
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun submitVideos() {
        val videos = ProfileVideoPlaybackStore.videos
        if (videos.isEmpty()) {
            finish()
            return
        }

        val initialPosition = ProfileVideoPlaybackStore.initialPosition
        videoAdapter.submitList(videos) {
            binding.viewPagerVideo.setCurrentItem(initialPosition, false)
            playVideoAt(initialPosition)
        }
    }

    private fun playVideoAt(position: Int) {
        val videos = videoAdapter.currentList
        if (position !in videos.indices) {
            videoAdapter.setCurrentPlayingPosition(-1, null)
            return
        }

        VideoPlayerManager.instance.playVideo(videos[position].videoUrl)
        videoAdapter.setCurrentPlayingPosition(position, VideoPlayerManager.instance.getPlayer())
    }

    private fun showComingSoon() {
        Toast.makeText(this, "功能开发中", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        VideoPlayerManager.instance.resume()
    }

    override fun onPause() {
        super.onPause()
        VideoPlayerManager.instance.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPagerVideo.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.viewPagerVideo.adapter = null
        if (isFinishing) {
            ProfileVideoPlaybackStore.clear()
        }
    }
}
