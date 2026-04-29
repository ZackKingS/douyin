package com.example.douyinandroid.feature.feature_main.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.douyinandroid.R
import com.example.douyinandroid.common.common_utils.LogUtil
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import com.example.douyinandroid.databinding.FragmentMainBinding
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.feature.feature_main.ui.adapter.VideoAdapter
import com.example.douyinandroid.feature.feature_publish.ui.PublishActivity

private const val TAG = "MainFragment"

@UnstableApi
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            MainViewModelFactory()
        )[MainViewModel::class.java]
    }

    private lateinit var videoAdapter: VideoAdapter
    private var pendingScrollToFirstVideo = false

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            playVideoAtPosition(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupAdapter()
        observeViewModel()
        setupPlayerListener()
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            pendingScrollToFirstVideo = true
            viewModel.loadVideos(refresh = true)
        }

        binding.viewPagerVideo.registerOnPageChangeCallback(pageChangeCallback)

        (binding.viewPagerVideo.getChildAt(0) as? RecyclerView)?.apply {
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        binding.layoutError.btnRetry.setOnClickListener {
            pendingScrollToFirstVideo = true
            viewModel.loadVideos(refresh = true)
        }

        binding.fabPublish.setOnClickListener {
            navigateToPublish()
        }
    }

    private fun setupAdapter() {
        videoAdapter = VideoAdapter(
            onLikeClick = { video -> viewModel.likeVideo(video) },
            onCommentClick = { video -> /* TODO: navigate to comment */ },
            onShareClick = { video -> showShareDialog(video) },
            onAuthorClick = { userId -> /* TODO: navigate to profile */ },
            onFollowClick = { video -> /* Handle follow */ }
        )
        binding.viewPagerVideo.adapter = videoAdapter
        binding.viewPagerVideo.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    private fun observeViewModel() {
        LogUtil.d(TAG, "observeViewModel: observing videos LiveData")
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            LogUtil.d(TAG, "videos observer callback: received ${videos.size} videos")
            LogUtil.d(TAG, "videos list: $videos")
            binding.swipeRefresh.isRefreshing = false

            binding.layoutEmpty.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
            binding.viewPagerVideo.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE
            LogUtil.d(TAG, "Empty view visibility: ${if (videos.isEmpty()) "VISIBLE" else "GONE"}")

            videoAdapter.submitList(videos) {
                syncPlaybackAfterListUpdate(videos)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            LogUtil.d(TAG, "isLoading changed to: $isLoading")
            binding.layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            LogUtil.d(TAG, "isRefreshing changed to: $isRefreshing")
            binding.swipeRefresh.isRefreshing = isRefreshing
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                LogUtil.e(TAG, "Error received: $it")
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.likeEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                Toast.makeText(
                    context,
                    if (it.isLiked) "已点赞" else "取消点赞",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.onLikeEventHandled()
            }
        }

        viewModel.shareEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                shareVideo(it.shareUrl, it.platform)
                viewModel.onShareEventHandled()
            }
        }
    }

    private fun setupPlayerListener() {
        VideoPlayerManager.instance.playerListener = object : VideoPlayerManager.PlayerListener {
            override fun onPlayerStateChanged(state: VideoPlayerManager.PlayerState) {
                when (state) {
                    VideoPlayerManager.PlayerState.BUFFERING -> {}
                    VideoPlayerManager.PlayerState.READY -> {}
                    else -> {}
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Toast.makeText(context, "视频播放失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playVideoAtPosition(position: Int) {
        LogUtil.d(TAG, "playVideoAtPosition: position=$position")
        val videos = videoAdapter.currentList
        LogUtil.d(TAG, "playVideoAtPosition: currentList has ${videos.size} videos")
        if (position in videos.indices) {
            val video = videos[position]
            LogUtil.d(TAG, "playVideoAtPosition: video=$video")
            LogUtil.d(TAG, "playVideoAtPosition: videoUrl=${video.videoUrl}")
            viewModel.onVideoChanged(position)
            viewModel.playVideo(video.videoUrl)
            videoAdapter.setCurrentPlayingPosition(position, VideoPlayerManager.instance.getPlayer())
        } else {
            LogUtil.w(TAG, "playVideoAtPosition: position $position is out of bounds, list size=${videos.size}")
            videoAdapter.setCurrentPlayingPosition(-1, null)
        }
    }

    private fun syncPlaybackAfterListUpdate(videos: List<Video>) {
        if (videos.isEmpty()) {
            pendingScrollToFirstVideo = false
            videoAdapter.setCurrentPlayingPosition(-1, null)
            viewModel.pauseVideo()
            return
        }

        val currentPage = binding.viewPagerVideo.currentItem
        val targetPage = if (pendingScrollToFirstVideo) {
            0
        } else {
            currentPage.coerceIn(videos.indices)
        }
        pendingScrollToFirstVideo = false

        if (currentPage != targetPage) {
            binding.viewPagerVideo.setCurrentItem(targetPage, false)
        }
        playVideoAtPosition(targetPage)
    }

    private fun navigateToPublish() {
        try {
            val intent = Intent(requireContext(), PublishActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开发布页面: ${e.message}", Toast.LENGTH_LONG).show()
            LogUtil.e("MainFragment", "Navigate to publish failed", e)
        }
    }

    private fun showShareDialog(video: Video) {
        val platforms = arrayOf("微信", "朋友圈", "微博", "QQ", "复制链接")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("分享到")
            .setItems(platforms) { _, which ->
                val platform = when (which) {
                    0 -> "wechat"
                    1 -> "moments"
                    2 -> "weibo"
                    3 -> "qq"
                    4 -> "copy"
                    else -> "copy"
                }
                viewModel.shareVideo(video, platform)
            }
            .show()
    }

    private fun shareVideo(shareUrl: String, platform: String) {
        when (platform) {
            "copy" -> {
                val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("分享链接", shareUrl)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareUrl)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, "分享到"))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeVideo()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseVideo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPagerVideo.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
