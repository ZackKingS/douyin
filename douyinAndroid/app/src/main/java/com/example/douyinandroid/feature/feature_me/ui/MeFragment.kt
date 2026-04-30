package com.example.douyinandroid.feature.feature_me.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.douyinandroid.R
import com.example.douyinandroid.databinding.FragmentMeBinding
import com.example.douyinandroid.domain.model.User
import com.example.douyinandroid.domain.model.Video
import com.example.douyinandroid.feature.feature_auth.ui.LoginActivity
import com.example.douyinandroid.feature.feature_me.ui.adapter.ProfileVideoAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MeFragment : Fragment() {

    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MeViewModel by lazy {
        ViewModelProvider(this, MeViewModelFactory())[MeViewModel::class.java]
    }

    private lateinit var videoAdapter: ProfileVideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        videoAdapter = ProfileVideoAdapter(
            onVideoClick = { video -> openVideoPlayer(video) },
            onVideoLongClick = { video -> confirmDeleteVideo(video) }
        )
        binding.recyclerVideos.apply {
            adapter = videoAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            isNestedScrollingEnabled = false
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val manager = recyclerView.layoutManager as? GridLayoutManager ?: return
                    val lastVisible = manager.findLastVisibleItemPosition()
                    if (lastVisible >= videoAdapter.itemCount - 6) {
                        viewModel.loadMoreVideos()
                    }
                }
            })
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.buttonRetry.setOnClickListener {
            viewModel.refresh()
        }
        binding.buttonLogout.setOnClickListener {
            confirmLogout()
        }
        binding.statFollowing.textStatLabel.text = "关注"
        binding.statFans.textStatLabel.text = "粉丝"
        binding.statLikes.textStatLabel.text = "获赞"
        binding.statWorks.textStatLabel.text = "作品"
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { user ->
            renderProfile(user)
        }

        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            videoAdapter.submitList(videos)
            binding.layoutEmptyVideos.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerVideos.visibility = if (videos.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.isRefreshing
            binding.progressLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.layoutError.visibility = if (state.error != null) View.VISIBLE else View.GONE
            binding.textError.text = state.error.orEmpty()
            state.error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
            if (state.logoutFinished) {
                navigateToLogin()
            }
        }
    }

    private fun renderProfile(user: User?) {
        binding.textNickname.text = user?.nickname?.takeIf { it.isNotBlank() } ?: "抖音用户"
        binding.textUserId.text = "抖音号: ${user?.userId ?: "--"}"
        binding.textSignature.text = user?.signature?.takeIf { it.isNotBlank() } ?: "这个人很神秘，暂时还没有签名"
        binding.statFollowing.textStatValue.text = user?.formattedFollowCount ?: "0"
        binding.statFans.textStatValue.text = user?.formattedFansCount ?: "0"
        binding.statLikes.textStatValue.text = user?.formattedLikeCount ?: "0"
        binding.statWorks.textStatValue.text = user?.formattedVideoCount ?: "0"

        Glide.with(binding.imageAvatar)
            .load(user?.avatar?.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.ic_avatar_placeholder)
            .error(R.drawable.ic_avatar_placeholder)
            .circleCrop()
            .into(binding.imageAvatar)
    }

    private fun openVideoPlayer(video: Video) {
        val videos = videoAdapter.currentList
        val initialPosition = videos.indexOfFirst { it.id == video.id }.takeIf { it >= 0 } ?: 0
        ProfileVideoPlaybackStore.setPlayback(videos, initialPosition)
        startActivity(Intent(requireContext(), ProfileVideoPlayerActivity::class.java))
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出当前账号吗？")
            .setNegativeButton("取消", null)
            .setPositiveButton("退出") { _, _ -> viewModel.logout() }
            .show()
    }

    private fun confirmDeleteVideo(video: Video) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("删除作品")
            .setMessage("确定要删除这个作品吗？删除后将不会出现在主页和个人作品列表中。")
            .setNegativeButton("取消", null)
            .setPositiveButton("删除") { _, _ -> viewModel.deleteVideo(video) }
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerVideos.adapter = null
        _binding = null
    }

    companion object {
        fun newInstance() = MeFragment()
    }
}
