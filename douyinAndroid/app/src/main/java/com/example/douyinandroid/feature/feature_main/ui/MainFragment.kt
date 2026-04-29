package com.example.douyinandroid.feature.feature_main.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
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
import com.example.douyinandroid.domain.model.Comment
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.feature.feature_main.ui.adapter.VideoAdapter
import com.example.douyinandroid.feature.feature_publish.ui.PublishActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

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
    private var commentDialog: BottomSheetDialog? = null
    private var commentSubmitButton: Button? = null
    private var commentListContainer: LinearLayout? = null
    private var commentLoadingView: ProgressBar? = null

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
            onCommentClick = { video -> showCommentSheet(video) },
            onShareClick = { video -> showShareDialog(video) },
            onAuthorClick = { _ -> /* TODO: navigate to profile */ },
            onFollowClick = { _ -> /* Handle follow */ }
        )
        binding.viewPagerVideo.adapter = videoAdapter
        binding.viewPagerVideo.orientation = ViewPager2.ORIENTATION_VERTICAL
    }

    private fun observeViewModel() {
        LogUtil.d(TAG, "observeViewModel: observing videos LiveData")
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            LogUtil.d(TAG, "videos observer callback: received ${videos.size} videos")
            binding.swipeRefresh.isRefreshing = false

            binding.layoutEmpty.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
            binding.viewPagerVideo.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE

            videoAdapter.submitList(videos) {
                syncPlaybackAfterListUpdate(videos)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefresh.isRefreshing = isRefreshing
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
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

        viewModel.commentEvent.observe(viewLifecycleOwner) { event ->
            event?.let {
                commentSubmitButton?.isEnabled = true
                commentSubmitButton?.text = "发送"
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                viewModel.onCommentEventHandled()
            }
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            renderComments(comments)
        }

        viewModel.isCommentsLoading.observe(viewLifecycleOwner) { isLoading ->
            commentLoadingView?.visibility = if (isLoading) View.VISIBLE else View.GONE
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
        val videos = videoAdapter.currentList
        if (position in videos.indices) {
            val video = videos[position]
            viewModel.onVideoChanged(position)
            viewModel.playVideo(video.videoUrl)
            videoAdapter.setCurrentPlayingPosition(position, VideoPlayerManager.instance.getPlayer())
        } else {
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
            LogUtil.e(TAG, "Navigate to publish failed", e)
        }
    }

    private fun showCommentSheet(video: Video) {
        val dialog = BottomSheetDialog(requireContext())
        commentDialog = dialog
        dialog.setContentView(createCommentSheetView(video))
        dialog.setOnDismissListener {
            if (commentDialog == dialog) {
                commentDialog = null
                commentSubmitButton = null
                commentListContainer = null
                commentLoadingView = null
            }
        }
        dialog.show()
        viewModel.loadComments(video.id)
    }

    private fun createCommentSheetView(video: Video): View {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(12))
            setBackgroundColor(android.graphics.Color.WHITE)
            minimumHeight = dp(420)
        }

        val titleView = TextView(requireContext()).apply {
            text = "评论 ${video.formattedCommentCount}"
            textSize = 18f
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER_VERTICAL
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        container.addView(titleView, matchWrapParams())

        val scrollView = ScrollView(requireContext())
        val commentsContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        commentListContainer = commentsContainer
        scrollView.addView(
            commentsContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        container.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                topMargin = dp(8)
            }
        )

        val loadingView = ProgressBar(requireContext()).apply {
            visibility = View.GONE
        }
        commentLoadingView = loadingView
        container.addView(
            loadingView,
            LinearLayout.LayoutParams(dp(36), dp(36)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )

        val inputRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        container.addView(
            inputRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        val editText = EditText(requireContext()).apply {
            hint = "留下你的评论"
            maxLines = 3
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            setSingleLine(false)
        }
        inputRow.addView(
            editText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val submitButton = Button(requireContext()).apply {
            text = "发送"
            setOnClickListener {
                val content = editText.text?.toString().orEmpty()
                if (content.trim().isEmpty()) {
                    Toast.makeText(context, "评论不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                isEnabled = false
                text = "发送中"
                viewModel.postComment(video, content)
                editText.text?.clear()
            }
        }
        commentSubmitButton = submitButton
        inputRow.addView(
            submitButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = dp(8)
            }
        )

        return container
    }

    private fun renderComments(comments: List<Comment>) {
        val container = commentListContainer ?: return
        container.removeAllViews()

        if (comments.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "还没有评论"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, dp(48), 0, dp(48))
            }
            container.addView(emptyView, matchWrapParams())
            return
        }

        comments.forEach { comment ->
            container.addView(createCommentItemView(comment))
        }
    }

    private fun createCommentItemView(comment: Comment): View {
        val itemContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }

        val authorView = TextView(requireContext()).apply {
            text = comment.user?.nickname?.takeIf { it.isNotBlank() } ?: "用户"
            textSize = 13f
            setTextColor(android.graphics.Color.DKGRAY)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        itemContainer.addView(authorView, matchWrapParams())

        val contentView = TextView(requireContext()).apply {
            text = comment.content
            textSize = 15f
            setTextColor(android.graphics.Color.BLACK)
            setPadding(0, dp(4), 0, 0)
        }
        itemContainer.addView(contentView, matchWrapParams())

        val metaView = TextView(requireContext()).apply {
            val timeText = comment.createTime.orEmpty()
            val likeText = if (comment.likeCount > 0) " · ${comment.likeCount}赞" else ""
            text = timeText + likeText
            textSize = 12f
            setTextColor(android.graphics.Color.GRAY)
            visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
            setPadding(0, dp(4), 0, 0)
        }
        itemContainer.addView(metaView, matchWrapParams())

        return itemContainer
    }

    private fun matchWrapParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
        commentDialog?.dismiss()
        commentDialog = null
        commentSubmitButton = null
        commentListContainer = null
        commentLoadingView = null
        binding.viewPagerVideo.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
