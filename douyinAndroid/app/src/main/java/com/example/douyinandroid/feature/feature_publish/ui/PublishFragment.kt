package com.example.douyinandroid.feature.feature_publish.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.douyinandroid.MainActivity
import com.example.douyinandroid.R
import com.example.douyinandroid.databinding.FragmentPublishBinding
import com.google.android.material.chip.Chip

@UnstableApi
class PublishFragment : Fragment() {

    private var _binding: FragmentPublishBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PublishViewModel by lazy {
        ViewModelProvider(this, PublishViewModelFactory(requireActivity().application))[PublishViewModel::class.java]
    }

    private var exoPlayer: ExoPlayer? = null

    private val videoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setVideoUri(uri)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.all { it.value }
        if (allGranted) {
            openVideoPicker()
        } else {
            Toast.makeText(context, "需要存储和相机权限", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPublishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // Video selection
        binding.layoutSelectVideo.setOnClickListener {
            checkPermissionsAndPickVideo()
        }

        binding.btnSelectVideo.setOnClickListener {
            checkPermissionsAndPickVideo()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            navigateBack()
        }

        // Title input
        binding.etTitle.doAfterTextChanged { text ->
            viewModel.setTitle(text?.toString() ?: "")
        }

        // Description input
        binding.etDescription.doAfterTextChanged { text ->
            viewModel.setDescription(text?.toString() ?: "")
        }

        // Publish button
        binding.btnPublish.setOnClickListener {
            viewModel.uploadVideo()
        }

        // Add topic button
        binding.btnAddTopic.setOnClickListener {
            showTopicSelectionDialog()
        }

        // Cover selection
        binding.btnSelectCover.setOnClickListener {
            openCoverPicker()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PublishUiState.Idle -> {
                    showSelectVideoState()
                }
                is PublishUiState.SelectVideo -> {
                    showSelectVideoState()
                }
                is PublishUiState.EditDetails -> {
                    showEditDetailsState()
                }
                is PublishUiState.ReadyToPublish -> {
                    showReadyToPublishState()
                }
                is PublishUiState.Uploading -> {
                    showUploadingState()
                }
                is PublishUiState.Success -> {
                    showSuccessState(state.result)
                }
                is PublishUiState.Error -> {
                    showErrorState(state.message)
                }
            }
        }

        viewModel.videoUri.observe(viewLifecycleOwner) { uri ->
            uri?.let { playPreview(it) }
        }

        viewModel.coverUri.observe(viewLifecycleOwner) { uri ->
            uri?.let { loadCoverPreview(it) }
        }

        viewModel.selectedTopics.observe(viewLifecycleOwner) { topics ->
            updateTopicChips(topics)
        }

        viewModel.uploadProgress.observe(viewLifecycleOwner) { progress ->
            binding.progressUpload.progress = progress
        }

        viewModel.navigateToMain.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToMain()
                viewModel.navigateToMainComplete()
            }
        }
    }

    private fun showSelectVideoState() {
        binding.layoutSelectVideo.visibility = View.VISIBLE
        binding.layoutVideoPreview.visibility = View.GONE
        binding.layoutEditDetails.visibility = View.GONE
        binding.layoutUploading.visibility = View.GONE
        binding.btnPublish.visibility = View.GONE
    }

    private fun showEditDetailsState() {
        binding.layoutSelectVideo.visibility = View.GONE
        binding.layoutVideoPreview.visibility = View.VISIBLE
        binding.layoutEditDetails.visibility = View.VISIBLE
        binding.layoutUploading.visibility = View.GONE
        binding.btnPublish.visibility = View.GONE

        binding.btnPublish.isEnabled = false
        binding.btnPublish.alpha = 0.5f
    }

    private fun showReadyToPublishState() {
        binding.layoutSelectVideo.visibility = View.GONE
        binding.layoutVideoPreview.visibility = View.VISIBLE
        binding.layoutEditDetails.visibility = View.VISIBLE
        binding.layoutUploading.visibility = View.GONE
        binding.btnPublish.visibility = View.VISIBLE

        binding.btnPublish.isEnabled = true
        binding.btnPublish.alpha = 1.0f
    }

    private fun showUploadingState() {
        binding.layoutSelectVideo.visibility = View.GONE
        binding.layoutVideoPreview.visibility = View.VISIBLE
        binding.layoutEditDetails.visibility = View.GONE
        binding.layoutUploading.visibility = View.VISIBLE
        binding.btnPublish.visibility = View.GONE
    }

    private fun showSuccessState(result: com.example.douyinandroid.domain.repository.PublishResult) {
        Toast.makeText(context, "发布成功!", Toast.LENGTH_SHORT).show()
        viewModel.triggerNavigateToMain()
    }

    private fun showErrorState(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        binding.layoutUploading.visibility = View.GONE
        binding.layoutEditDetails.visibility = View.VISIBLE
        binding.btnPublish.visibility = View.VISIBLE
        binding.btnPublish.isEnabled = true
    }

    private fun checkPermissionsAndPickVideo() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            openVideoPicker()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoPickerLauncher.launch(intent)
    }

    private val coverPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setCoverUri(uri)
            }
        }
    }

    private fun openCoverPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        coverPickerLauncher.launch(intent)
    }

    private fun playPreview(uri: Uri) {
        binding.layoutSelectVideo.visibility = View.GONE
        binding.layoutVideoPreview.visibility = View.VISIBLE

        releasePlayer()
        exoPlayer = ExoPlayer.Builder(requireContext()).build().apply {
            binding.playerView.player = this
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    private fun loadCoverPreview(uri: Uri) {
        // Using simple ImageView for cover preview
        binding.ivCoverPreview.setImageURI(uri)
        binding.ivCoverPreview.visibility = View.VISIBLE
    }

    private fun updateTopicChips(topics: List<TopicItem>) {
        binding.chipGroupTopics.removeAllViews()
        topics.forEach { topic ->
            val chip = Chip(requireContext()).apply {
                text = "#${topic.name}"
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeTopic(topic)
                }
            }
            binding.chipGroupTopics.addView(chip)
        }
    }

    private fun showTopicSelectionDialog() {
        // Simple topic selection dialog
        val topics = listOf(
            TopicItem(1, "搞笑"),
            TopicItem(2, "日常"),
            TopicItem(3, "美食"),
            TopicItem(4, "旅行"),
            TopicItem(5, "音乐"),
            TopicItem(6, "舞蹈"),
            TopicItem(7, "科技"),
            TopicItem(8, "游戏")
        )

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择话题")
            .setItems(topics.map { "#${it.name}" }.toTypedArray()) { _, which ->
                viewModel.addTopic(topics[which])
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()
    }

    private fun navigateBack() {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }

    private fun navigateToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayer?.play()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        binding.playerView.player = null
        _binding = null
    }

    companion object {
        fun newInstance() = PublishFragment()
    }
}
