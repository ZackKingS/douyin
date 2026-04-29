# 抖音短视频 Android 客户端技术架构文档

## 1. 项目概述

### 1.1 项目简介

本项目是一个类似抖音的短视频社交平台 Android 客户端，采用传统的 XML 布局开发，使用 ViewModel + LiveData 进行状态管理，提供流畅的视频浏览体验、丰富的社交互动功能和个性化推荐服务。

### 1.2 技术选型


| 类别 | 技术方案 | 版本 |
|------|----------|------|
| **编程语言** | Kotlin | 1.9.x |
| **最低 SDK** | Android 21 (Android 5.0) | - |
| **目标 SDK** | Android 34 (Android 14) | - |
| **UI 框架** | XML + ViewBinding | - |
| **架构模式** | MVVM + Clean Architecture | - |
| **网络层** | Retrofit + OkHttp | 2.9.0 / 4.12.0 |
| **图像加载** | Glide | 4.16.0 |
| **视频播放** | ExoPlayer (Media3) | 1.2.1 |
| **本地数据库** | Room | 2.6.1 |
| **状态管理** | ViewModel + LiveData | 2.7.0 |
| **页面路由** | ARouter | 1.5.2 |
| **序列化** | Gson | 2.10.1 |
| **构建工具** | Gradle (Kotlin DSL) | 8.2 |


---

## 2. 整体架构设计

### 2.1 架构分层

采用 Clean Architecture 架构思想，将应用分为三个主要层次：

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│     (UI Layer: XML Layouts, Activities/Fragments,       │
│                ViewModels, UI State)                     │
├─────────────────────────────────────────────────────────┤
│                      Domain Layer                        │
│  (Use Cases, Domain Models, Repository Interfaces)      │
├─────────────────────────────────────────────────────────┤
│                       Data Layer                         │
│  (Repository Impl, Data Sources, DTOs, Mappers)        │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模块依赖关系

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│    :app      │────▶│   :feature/* │────▶│   :core      │
│  (Application)│     │   (功能模块)  │     │  (核心模块)   │
└──────────────┘     └──────────────┘     └──────────────┘
                            │
                            ▼
                    ┌──────────────┐
                    │  :common     │
                    │  (公共组件)   │
                    └──────────────┘
```

### 2.3 架构原则

1. **单向数据流 (Unidirectional Data Flow)**
   - UI 状态从 ViewModel 流向 Activity/Fragment（通过 LiveData）
   - 用户事件从 Activity/Fragment 流向 ViewModel（通过方法调用）
   - 遵循 `User Action -> ViewModel -> State -> UI` 的数据流

2. **依赖倒置 (Dependency Inversion)**
   - Domain 层不依赖具体实现
   - 通过接口解耦业务逻辑和数据层
   - 使用 ServiceLocator 进行依赖获取

3. **关注点分离 (Separation of Concerns)**
   - Presentation 层只负责 UI 渲染
   - Domain 层处理业务逻辑
   - Data 层负责数据获取和存储

---

## 3. 项目模块划分

### 3.1 模块结构

```
app/
├── app/                    # 应用壳模块
├── core/                   # 核心基础模块
│   ├── core-network/       # 网络相关
│   ├── core-database/      # 数据库相关
│   ├── core-storage/       # 本地存储相关
│   └── core-video/         # 视频播放相关
├── common/                 # 公共组件模块
│   ├── common-ui/          # 通用 UI 组件
│   ├── common-ext/         # 扩展函数
│   └── common-utils/       # 工具类
└── feature/                # 功能模块
    ├── feature-main/       # 首页/推荐
    ├── feature-user/       # 用户模块
    ├── feature-video/      # 视频模块
    ├── feature-message/    # 消息模块
    ├── feature-publish/    # 发布模块
    ├── feature-search/     # 搜索模块
    └── feature-settings/   # 设置模块
```

### 3.2 模块职责


| 模块 | 职责 |
|------|------|
| **:app** | 应用入口、Application 类、Activity、进程初始化 |
| **:core:core-network** | Retrofit 实例、OkHttp 配置、网络拦截器 |
| **:core:core-database** | Room 数据库、DAO、实体类 |
| **:core:core-video** | ExoPlayer 配置、视频播放器封装 |
| **:common:common-ui** | 通用 XML 组件（按钮、卡片、加载状态等） |
| **:feature:feature-main** | 首页推荐视频流、短视频浏览 |
| **:feature:feature-user** | 用户资料、关注列表、粉丝列表 |
| **:feature:feature-video** | 视频详情页、评论、分享 |
| **:feature:feature-message** | 消息列表、通知、聊天 |
| **:feature:feature-publish** | 视频拍摄、剪辑、发布 |
| **:feature:feature-search** | 搜索功能 |


---

## 4. 核心模块设计

### 4.1 网络层设计 (core-network)

**网络架构图：**

```
┌─────────────────────────────────────────────────────────┐
│                    Repository                           │
│                  (业务层调用)                             │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                      ApiService                         │
│                  (Retrofit 接口)                        │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    OkHttpClient                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │ Logging     │ │ Auth        │ │ Error           │   │
│  │ Interceptor │ │ Interceptor │ │ Interceptor     │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                    Retrofit                             │
│           (网络请求构建器)                                │
└─────────────────────────────────────────────────────────┘
```

**核心组件：**

```kotlin
// ApiService 示例
interface VideoApiService {
    @GET("/api/v1/videos/feed")
    suspend fun getVideoFeed(@Query("page") page: Int): ApiResponse<VideoFeedResponse>
    
    @GET("/api/v1/videos/{id}")
    suspend fun getVideoDetail(@Path("id") videoId: String): ApiResponse<VideoDetailResponse>
    
    @POST("/api/v1/videos/{id}/like")
    suspend fun likeVideo(@Path("id") videoId: String): ApiResponse<Unit>
}

// OkHttp 配置
object OkHttpConfig {
    const val TIMEOUT = 30_000L
    
    fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(LogInterceptor())
            .addInterceptor(ErrorInterceptor())
            .build()
    }
}

// Retrofit 实例
object RetrofitClient {
    private const val BASE_URL = "https://api.douyin.com/api/v1/"
    
    val videoApiService: VideoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpConfig.createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VideoApiService::class.java)
    }
}
```

### 4.2 数据库层设计 (core-database)

**数据库架构：**

```
┌─────────────────────────────────────────────────────────┐
│                     Room Database                       │
│                   (douyin_database)                     │
├─────────────────────────────────────────────────────────┤
│  videos          │  users        │  cache              │
│  ─────────────── │ ─────────────│ ─────────────────── │
│  id (PK)         │ id (PK)      │ key (PK)            │
│  title           │ nickname     │ value               │
│  description     │ avatar       │ expire_time         │
│  video_url       │ follow_count │                    │
│  cover_url       │ fans_count   │                    │
│  author_id (FK)  │              │                    │
│  like_count      │              │                    │
│  comment_count   │              │                    │
│  share_count     │              │                    │
│  create_time     │              │                    │
├─────────────────────────────────────────────────────────┤
│  follows         │  likes        │  history           │
│  ─────────────── │ ─────────────│ ─────────────────── │
│  id (PK)         │ id (PK)      │ id (PK)            │
│  user_id (FK)    │ video_id (FK)│ video_id (FK)      │
│  follow_id (FK)  │ user_id (FK) │ user_id (FK)       │
│  create_time     │ create_time  │ watch_time         │
│                  │              │ progress           │
└─────────────────────────────────────────────────────────┘
```

**Entity 示例：**

```kotlin
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val coverUrl: String,
    val authorId: String,
    val authorNickname: String,
    val authorAvatar: String,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val duration: Long,
    val createTime: Long
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val avatar: String,
    val signature: String,
    val followCount: Long,
    val fansCount: Long,
    val likeCount: Long,
    val isFollowing: Boolean = false
)

// DAO 示例
@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createTime DESC")
    fun getAllVideos(): LiveData<List<VideoEntity>>
    
    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): VideoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)
    
    @Delete
    suspend fun deleteVideo(video: VideoEntity)
}
```

### 4.3 视频播放设计 (core-video)

**视频播放器架构：**

```
┌─────────────────────────────────────────────────────────┐
│                  VideoPlayerManager                     │
│              (播放器管理器 - 单例)                        │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │              ExoPlayer Instance                  │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │   │
│  │  │ MediaSource │ │ TrackSelector│ │ Renderers │ │   │
│  │  │ (视频源)     │ │ (轨道选择)   │ │ (渲染器)   │ │   │
│  │  └─────────────┘ └─────────────┘ └───────────┘ │   │
│  └─────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │              PlayerController                    │   │
│  │  - play() / pause() / seek()                  │   │
│  │  - 全屏/小屏切换                                 │   │
│  │  - 播放进度管理                                   │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

**核心功能：**

1. **预加载机制**
   - 当前视频播放时预加载下一个视频
   - 滑动切换时无缝衔接

2. **手势控制**
   - 上下滑动切换视频
   - 左右滑动调整进度
   - 双击点赞

3. **画中画模式**
   - 支持 Android 8.0+ PIP
   - 后台继续播放

4. **缓存管理**
   - LRU 缓存策略
   - 最大缓存 500MB
   - 自动清理过期缓存

**视频播放器管理器：**

```kotlin
class VideoPlayerManager private constructor() {
    
    private var exoPlayer: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        val instance: VideoPlayerManager by lazy { VideoPlayerManager() }
    }
    
    fun initialize(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context)
                .build()
                .apply {
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE
                }
        }
    }
    
    fun playVideo(videoUrl: String) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun resume() {
        exoPlayer?.play()
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
    
    fun getPlayer(): ExoPlayer? = exoPlayer
}
```

### 4.4 ServiceLocator 设计 (替代依赖注入)

**ServiceLocator 架构：**

```
┌─────────────────────────────────────────────────────────┐
│                    ServiceLocator                        │
│                 (依赖服务定位器)                           │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                     Repository                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │ VideoRepo   │ │  UserRepo   │ │  MessageRepo    │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Data Sources                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐   │
│  │ RemoteDataSrc│ │ LocalDataSrc│ │  CacheDataSrc   │   │
│  └─────────────┘ └─────────────┘ └─────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

**ServiceLocator 实现：**

```kotlin
object ServiceLocator {
    
    private val services = mutableMapOf<Class<*>, Any>()
    
    fun <T> register(clazz: Class<T>, instance: T) {
        services[clazz] = instance as Any
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T> get(clazz: Class<T>): T {
        return services[clazz] as? T ?: createInstance(clazz)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> createInstance(clazz: Class<T>): T {
        // 通过反射创建实例
        val constructor = clazz.getDeclaredConstructor()
        constructor.isAccessible = true
        return constructor.newInstance()
    }
}

// 在 Application 中初始化
class DouyinApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        initServiceLocator()
    }
    
    private fun initServiceLocator() {
        // 注册网络服务
        ServiceLocator.register(VideoApiService::class.java, RetrofitClient.videoApiService)
        
        // 注册数据库
        val database = AppDatabase.getInstance(this)
        ServiceLocator.register(VideoDao::class.java, database.videoDao())
        ServiceLocator.register(UserDao::class.java, database.userDao())
        
        // 注册 Repository
        ServiceLocator.register(
            VideoRepository::class.java,
            VideoRepositoryImpl(ServiceLocator.get(VideoApiService::class.java))
        )
        
        // 注册 ViewModelFactory
        ServiceLocator.register(
            ViewModelFactory::class.java,
            ViewModelFactory()
        )
    }
}

// ViewModelFactory（用于创建 ViewModel）
class ViewModelFactory : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(
                    ServiceLocator.get(VideoRepository::class.java)
                ) as T
            }
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(
                    ServiceLocator.get(UserRepository::class.java)
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
```

---

## 5. Feature 模块设计

### 5.1 首页推荐模块 (feature-main)

**功能职责：**

- 短视频信息流展示
- 视频自动播放
- 下拉刷新/上拉加载更多
- 点赞、评论、分享交互
- 关注作者

**XML 布局示例：**

```xml
<!-- res/layout/fragment_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPagerVideo"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical" />

    <LinearLayout
        android:id="@+id/layoutLoading"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:background="#80000000"
        android:visibility="gone">

        <ProgressBar
            android:layout_width="48dp"
            android:layout_height="48dp" />
    </LinearLayout>

    <include
        android:id="@+id/layoutError"
        layout="@layout/layout_error_view"
        android:visibility="gone" />

</FrameLayout>
```

```xml
<!-- res/layout/item_video.xml -->
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black">

    <com.google.android.exoplayer2.ui.StyledPlayerView
        android:id="@+id/playerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:use_controller="false"
        app:resize_mode="zoom" />

    <LinearLayout
        android:id="@+id/layoutInfo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvAuthorName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textColor="@android:color/white"
            android:textSize="16sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:textColor="@android:color/white"
            android:textSize="14sp"
            android:maxLines="2"
            android:ellipsize="end" />

    </LinearLayout>

    <LinearLayout
        android:id="@+id/layoutActions"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end|center_vertical"
        android:orientation="vertical"
        android:padding="16dp">

        <ImageView
            android:id="@+id/ivLike"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_like" />

        <TextView
            android:id="@+id/tvLikeCount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:textColor="@android:color/white"
            android:textSize="12sp" />

        <ImageView
            android:id="@+id/ivComment"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginTop="16dp"
            android:src="@drawable/ic_comment" />

        <TextView
            android:id="@+id/tvCommentCount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:textColor="@android:color/white"
            android:textSize="12sp" />

        <ImageView
            android:id="@+id/ivShare"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginTop="16dp"
            android:src="@drawable/ic_share" />

        <TextView
            android:id="@+id/tvShareCount"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:textColor="@android:color/white"
            android:textSize="12sp" />

    </LinearLayout>

</FrameLayout>
```

**ViewModel 设计：**

```kotlin
class MainViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {
    
    // LiveData for UI State
    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _currentIndex = MutableLiveData<Int>()
    val currentIndex: LiveData<Int> = _currentIndex
    
    private var nextPage = 1
    private var hasMore = true
    
    fun loadVideos() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = videoRepository.getVideoFeed(nextPage)
                if (response.isSuccess) {
                    val newVideos = response.data ?: emptyList()
                    val currentList = _videos.value ?: emptyList()
                    _videos.value = if (nextPage == 1) newVideos else currentList + newVideos
                    hasMore = response.hasMore
                    nextPage++
                } else {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "加载失败"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun onVideoChanged(position: Int) {
        _currentIndex.value = position
    }
    
    fun likeVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.likeVideo(videoId)
        }
    }
    
    fun shareVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.shareVideo(videoId)
        }
    }
}
```

**Fragment 实现：**

```kotlin
class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            ServiceLocator.get(ViewModelFactory::class.java)
        )[MainViewModel::class.java]
    }
    
    private val videoAdapter: VideoAdapter by lazy {
        VideoAdapter(
            onLikeClick = { video -> viewModel.likeVideo(video.id) },
            onCommentClick = { video -> navigateToComment(video.id) },
            onShareClick = { video -> viewModel.shareVideo(video.id) },
            onAuthorClick = { userId -> navigateToProfile(userId) }
        )
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
        observeViewModel()
        viewModel.loadVideos()
    }
    
    private fun setupViews() {
        binding.viewPagerVideo.apply {
            adapter = videoAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.onVideoChanged(position)
                }
            })
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadVideos()
        }
    }
    
    private fun observeViewModel() {
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
            videoAdapter.submitList(videos)
            binding.swipeRefresh.isRefreshing = false
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.layoutLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### 5.2 用户模块 (feature-user)

**功能职责：**

- 用户资料展示
- 关注/取关操作
- 粉丝列表
- 关注列表
- 用户作品列表

**XML 布局：**

```xml
<!-- res/layout/fragment_profile.xml -->
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appBarLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:id="@+id/collapsingToolbar"
            android:layout_width="match_parent"
            android:layout_height="200dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">

            <ImageView
                android:id="@+id/ivAvatar"
                android:layout_width="80dp"
                android:layout_height="80dp"
                android:layout_gravity="center"
                android:scaleType="centerCrop"
                app:layout_collapseMode="parallax" />

        </com.google.android.material.appbar.CollapsingToolbarLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:id="@+id/tvNickname"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="20sp"
                android:textStyle="bold" />

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:orientation="horizontal">

                <TextView
                    android:id="@+id/tvFollowCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content" />

                <TextView
                    android:id="@+id/tvFansCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="24dp" />

                <TextView
                    android:id="@+id/tvLikeCount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="24dp" />

            </LinearLayout>

            <com.google.android.material.button.MaterialButton
                android:id="@+id/btnFollow"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:text="关注" />

        </LinearLayout>

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvVideos"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 5.3 视频发布模块 (feature-publish)

**功能职责：**

- 视频拍摄（CameraX）
- 视频剪辑
- 封面选择
- 话题添加
- @好友
- 位置信息
- 发布

### 5.4 消息模块 (feature-message)

**功能职责：**

- 消息列表
- 评论通知
- 点赞通知
- 关注通知
- 私信聊天

---

## 6. 路由设计 (ARouter)

### 6.1 路由配置

```kotlin
// 路由路径常量
object RouterPath {
    const val MAIN = "/main"
    const val HOME = "/main/home"
    const val DISCOVER = "/main/discover"
    const val PUBLISH = "/main/publish"
    const val MESSAGE = "/main/message"
    const val PROFILE = "/main/profile"
    
    const val VIDEO_DETAIL = "/video/detail"
    const val USER_PROFILE = "/user/profile"
    const val SEARCH = "/search"
    const val SETTINGS = "/settings"
    const val LOGIN = "/login"
}

// Activity 路由配置
@Router(path = RouterPath.MAIN)
class MainActivity : BaseActivity() {
    // ...
}

@Router(path = RouterPath.VIDEO_DETAIL)
class VideoDetailActivity : BaseActivity() {
    // ...
}

// Fragment 路由配置
@Router(path = RouterPath.HOME)
class HomeFragment : BaseFragment() {
    // ...
}
```

### 6.2 路由跳转

```kotlin
// 跳转到指定页面
ARouter.getInstance()
    .build(RouterPath.VIDEO_DETAIL)
    .withString("videoId", videoId)
    .navigation()

// 带结果返回
ARouter.getInstance()
    .build(RouterPath.LOGIN)
    .navigation(activity, requestCode)

// Fragment 获取参数
@Route(path = RouterPath.VIDEO_DETAIL)
class VideoDetailFragment : BaseFragment() {
    
    @Autowired(name = "videoId")
    lateinit var videoId: String
}
```

### 6.3 导航图

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐             │
│  │  Home   │◀──▶│ Discover│◀──▶│ Publish │             │
│  │Fragment │    │Fragment │    │Fragment │             │
│  └────┬────┘    └────┬────┘    └────┬────┘             │
│       │              │              │                   │
│       ▼              ▼              ▼                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │ VideoDetail │ │ Search      │ │ VideoEdit   │       │
│  │ Activity    │ │ Activity    │ │ Activity    │       │
│  └──────┬──────┘ └─────────────┘ └─────────────┘       │
│         │                                              │
│         ▼                                              │
│  ┌─────────────┐                                       │
│  │ UserProfile │                                       │
│  │ Activity    │                                       │
│  └─────────────┘                                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 7. 状态管理

### 7.1 UI State 管理

```kotlin
// 统一的 UI 状态包装类
sealed class UiState<out T> {
    class Loading<T> : UiState<T>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error<T>(val message: String) : UiState<T>()
}

// BaseViewModel 基类
abstract class BaseViewModel : ViewModel() {
    
    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    protected val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    protected fun showLoading() {
        _isLoading.value = true
    }
    
    protected fun hideLoading() {
        _isLoading.value = false
    }
    
    protected fun showError(message: String) {
        _error.value = message
    }
}

// ViewModel 中的使用
class VideoListViewModel(
    private val videoRepository: VideoRepository
) : BaseViewModel() {
    
    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos
    
    fun loadVideos() {
        showLoading()
        viewModelScope.launch {
            try {
                val result = videoRepository.getVideoFeed(1)
                _videos.value = result
                hideLoading()
            } catch (e: Exception) {
                showError(e.message ?: "加载失败")
                hideLoading()
            }
        }
    }
}
```

### 7.2 事件处理

```kotlin
// 单次事件（如 Toast、导航）- 使用 SingleLiveEvent
class SingleLiveEvent<T> : MutableLiveData<T>() {
    
    private val pending = AtomicBoolean(false)
    
    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }
    
    @MainThread
    override fun setValue(value: T?) {
        pending.set(true)
        super.setValue(value)
    }
}

// ViewModel 中的事件
class MainViewModel(
    private val videoRepository: VideoRepository
) : BaseViewModel() {
    
    private val _toastEvent = SingleLiveEvent<String>()
    val toastEvent: LiveData<String> = _toastEvent
    
    private val _navigateEvent = SingleLiveEvent<NavigationEvent>()
    val navigateEvent: LiveData<NavigationEvent> = _navigateEvent
    
    fun onVideoLike(video: Video) {
        viewModelScope.launch {
            try {
                videoRepository.likeVideo(video.id)
                _toastEvent.value = "已点赞"
            } catch (e: Exception) {
                showError(e.message ?: "操作失败")
            }
        }
    }
    
    fun onNavigateToProfile(userId: String) {
        _navigateEvent.value = NavigationEvent.ToProfile(userId)
    }
}

// Fragment 中的事件处理
class MainFragment : Fragment() {
    
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory)[MainViewModel::class.java]
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel.toastEvent.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.navigateEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigationEvent.ToProfile -> {
                    ARouter.getInstance()
                        .build(RouterPath.USER_PROFILE)
                        .withString("userId", event.userId)
                        .navigation()
                }
            }
        }
    }
}
```

---

## 8. 性能优化策略

### 8.1 启动优化

- **多级启动器**：AppStartup + Lazy Initialization
- **异步初始化**：非核心组件后台初始化
- **预加载**：首页数据提前请求

### 8.2 内存优化

- **图片压缩**：根据 View 尺寸加载对应大小图片
- **内存泄漏检测**：LeakCanary
- **对象池**：复用 RecyclerView 列表项

### 8.3 列表优化

- **ViewHolder 复用**：RecyclerView 回收机制
- **图片缓存**：Glide 自动管理
- **预取**：Prefetch 相邻元素
- **分页加载**：Pagination 库

### 8.4 网络优化

- **请求合并**：批量请求
- **本地缓存**：Room + OkHttp Cache
- **增量更新**：只请求变化数据

---

## 9. 安全策略

### 9.1 通信安全

- **HTTPS**：全站强制 HTTPS
- **证书锁定**：Certificate Pinning
- **请求签名**：参数签名防篡改

### 9.2 数据安全

- **敏感数据加密**：Android Keystore
- **Dex 加固**：ProGuard/R8
- **日志脱敏**：发布版本禁用日志

### 9.3 权限管理

```kotlin
// 动态权限申请
class PublishActivity : AppCompatActivity() {
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.all { it.value }
        if (allGranted) {
            startCamera()
        } else {
            showPermissionDeniedDialog()
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        } else {
            startCamera()
        }
    }
}
```

---

## 10. 测试策略

### 10.1 单元测试

- **ViewModel 测试**：使用 InstantTaskExecutorRule 测试 LiveData
- **UseCase 测试**：Mock Repository
- **Repository 测试**：Mock DataSource

### 10.2 UI 测试

- **Espresso 测试**：Activity/Fragment UI 测试
- **截图测试**：Screenshot Tests
- **E2E 测试**：UI Automator

### 10.3 集成测试

- **Mock Server**：MockWebServer
- **数据库测试**：Instrumentation Test
- **模块集成测试**：Test Fixture

---

## 11. 附录

### 11.1 技术社区资源

- [Android Developers](https://developer.android.com)
- [Android XML Layouts](https://developer.android.com/guide/topics/ui/overview)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [Kotlin](https://kotlinlang.org)
- [ARouter](https://github.com/alibaba/ARouter)
- [Glide](https://github.com/bumptech/glide)

### 11.2 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2024-XX-XX | 初始版本 |


---

*文档版本：v2.0.0*
*最后更新：2024年*
