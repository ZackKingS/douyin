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
│  (Repository Impl, Data Sources, DTOs, Mappers)       │
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
│   ├── core-video/         # 视频播放相关
│   └── core-auth/          # 认证相关
├── common/                 # 公共组件模块
│   ├── common-ui/          # 通用 UI 组件
│   ├── common-ext/         # 扩展函数
│   ├── common-utils/       # 工具类
│   └── router/             # 路由常量
└── feature/                # 功能模块
    ├── feature-main/       # 首页/推荐
    ├── feature-user/       # 用户模块
    ├── feature-video/      # 视频模块
    ├── feature-message/    # 消息模块
    ├── feature-publish/    # 发布模块
    ├── feature-auth/       # 登录注册模块
    └── feature-settings/   # 设置模块
```

### 3.2 模块职责


| 模块 | 职责 |
|------|------|
| **:app** | 应用入口、Application 类、Activity、进程初始化 |
| **:core:core-network** | Retrofit 实例、OkHttp 配置、网络拦截器、API 服务接口 |
| **:core:core-database** | Room 数据库、DAO、实体类 |
| **:core:core-video** | ExoPlayer 配置、视频播放器封装 |
| **:core:core-auth** | 认证偏好设置（Token 管理） |
| **:common:common-ui** | 通用 XML 组件（按钮、卡片、加载状态等） |
| **:common:common-ext** | Kotlin 扩展函数 |
| **:common:common-utils** | 日志工具等通用工具类 |
| **:common:router** | ARouter 路由路径常量 |
| **:feature:feature-main** | 首页推荐视频流、短视频浏览、点赞、评论、分享 |
| **:feature:feature-user** | 用户资料、关注列表、粉丝列表 |
| **:feature:feature-video** | 视频详情页、评论、分享 |
| **:feature:feature-publish** | 视频拍摄、剪辑、发布 |
| **:feature:feature-auth** | 登录、注册、Token 管理 |


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
interface ApiService {

    // ==================== 视频相关 ====================

    @GET(ApiConstants.Endpoints.VIDEO_LIST)
    suspend fun getVideoList(
        @Query("type") type: String = "recommend",
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<VideoListPageResponse>

    @GET(ApiConstants.Endpoints.VIDEO_DETAIL)
    suspend fun getVideoDetail(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoDetailResponse>

    @POST(ApiConstants.Endpoints.VIDEO_LIKE)
    suspend fun likeVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoActionResponse>

    @DELETE(ApiConstants.Endpoints.VIDEO_UNLIKE)
    suspend fun unlikeVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoActionResponse>

    @GET(ApiConstants.Endpoints.VIDEO_COMMENTS)
    suspend fun getVideoComments(
        @Path("videoId") videoId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<CommentListPageResponse>

    @POST(ApiConstants.Endpoints.COMMENT_CREATE)
    suspend fun createComment(
        @Path("videoId") videoId: String,
        @Body comment: CommentCreateRequest
    ): ApiResponse<CommentResponse>

    // ==================== 用户相关 ====================

    @POST(ApiConstants.Endpoints.USER_LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): ApiResponse<LoginResponse>

    @GET(ApiConstants.Endpoints.USER_INFO)
    suspend fun getUserInfo(
        @Path("userId") userId: Long
    ): ApiResponse<UserInfoResponse>

    // ==================== 上传相关 ====================

    @Multipart
    @POST(ApiConstants.Endpoints.VIDEO_UPLOAD)
    suspend fun uploadVideo(
        @Part videoFile: okhttp3.MultipartBody.Part,
        @Part("title") title: okhttp3.RequestBody,
        @Part("description") description: okhttp3.RequestBody?,
        @Part coverFile: okhttp3.MultipartBody.Part?,
        @Part("topicIds") topicIds: okhttp3.RequestBody?,
        @Part("location") location: okhttp3.RequestBody?,
        @Part("latitude") latitude: okhttp3.RequestBody?,
        @Part("longitude") longitude: okhttp3.RequestBody?,
        @Part("atUserIds") atUserIds: okhttp3.RequestBody?,
        @Part("musicId") musicId: okhttp3.RequestBody?
    ): ApiResponse<VideoUploadResponse>
}
```

**ApiConstants 端点定义：**

```kotlin
object ApiConstants {
    object Endpoints {
        // 视频相关
        const val VIDEO_LIST = "/api/v1/video/list"
        const val VIDEO_DETAIL = "/api/v1/video/{videoId}"
        const val VIDEO_LIKE = "/api/v1/video/{videoId}/like"
        const val VIDEO_UNLIKE = "/api/v1/video/{videoId}/like"
        const val VIDEO_COMMENTS = "/api/v1/video/{videoId}/comments"
        const val COMMENT_CREATE = "/api/v1/video/{videoId}/comment"

        // 用户相关
        const val USER_LOGIN = "/api/v1/user/login"
        const val USER_REGISTER = "/api/v1/user/register"
        const val USER_INFO = "/api/v1/user/{userId}"
        const val USER_FOLLOW = "/api/v1/user/{userId}/follow"
        const val USER_UNFOLLOW = "/api/v1/user/{userId}/follow"

        // 上传相关
        const val VIDEO_UPLOAD = "/api/v1/video/upload"
        const val FILE_UPLOAD = "/api/v1/file/upload"
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
│  VideoEntity      │  UserEntity     │  CacheEntity      │
│  ─────────────── │ ───────────── │ ─────────────────── │
│  id (PK)         │ userId (PK)   │ key (PK)           │
│  title           │ nickname      │ value              │
│  description     │ avatar        │ expire_time         │
│  videoUrl        │ signature     │                    │
│  coverUrl        │ fansCount     │                    │
│  duration        │ followCount   │                    │
│  authorId        │ isFollowing   │                    │
│  likeCount       │ isMutual      │                    │
│  commentCount    │ level         │                    │
├─────────────────────────────────────────────────────────┤
│  FollowEntity     │  LikeEntity     │  HistoryEntity    │
│  ─────────────── │ ───────────── │ ─────────────────── │
│  id (PK)         │ id (PK)       │ id (PK)            │
│  userId (FK)     │ videoId (FK)  │ videoId (FK)       │
│  followId (FK)   │ userId (FK)   │ userId (FK)        │
│  createTime      │ createTime    │ watchTime          │
│                                  │ progress            │
└─────────────────────────────────────────────────────────┘
```

**Entity 示例：**

```kotlin
@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val description: String?,
    val videoUrl: String,
    val coverUrl: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val authorId: Long,
    val authorNickname: String,
    val authorAvatar: String?,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val collectCount: Long,
    val viewCount: Long,
    val isLiked: Boolean,
    val isCollected: Boolean,
    val topicName: String?,
    val musicTitle: String?,
    val musicAuthor: String?,
    val location: String?,
    val createTime: String?
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Long,
    val nickname: String,
    val avatar: String?,
    val signature: String?,
    val fansCount: Long,
    val followCount: Long,
    val isFollowing: Boolean,
    val isMutual: Boolean,
    val level: Int
)

// DAO 示例
@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createTime DESC")
    fun getAllVideos(): LiveData<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE authorId = :authorId")
    suspend fun getVideosByAuthor(authorId: Long): List<VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()
}
```

**AppDatabase 配置：**

```kotlin
@Database(
    entities = [
        VideoEntity::class,
        UserEntity::class,
        CacheEntity::class,
        FollowEntity::class,
        LikeEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun userDao(): UserDao

    companion object {
        private const val DATABASE_NAME = "douyin_database"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
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
│  │  │MediaSource  │ │TrackSelector│ │ Renderers │ │   │
│  │  │ Factory     │ │ (轨道选择)   │ │ (渲染器)   │ │   │
│  │  └─────────────┘ └─────────────┘ └───────────┘ │   │
│  └─────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐   │
│  │              PlayerListener                      │   │
│  │  - onPlayerStateChanged()                       │   │
│  │  - onIsPlayingChanged()                         │   │
│  │  - onPlayerError()                              │   │
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
        exoPlayer?.let { player ->
            if (currentVideoUrl == videoUrl && isPrepared) {
                // Same video, just resume
                player.play()
            } else {
                // New video
                currentVideoUrl = videoUrl
                isPrepared = false
                val mediaItem = MediaItem.fromUri(videoUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        }
    }

    fun play() = exoPlayer?.play()
    fun pause() = exoPlayer?.pause()
    fun resume() = exoPlayer?.play()
    fun seekTo(position: Long) = exoPlayer?.seekTo(position)
    fun getCurrentPosition(): Long = exoPlayer?.currentPosition ?: 0
    fun getDuration(): Long = exoPlayer?.duration ?: 0
    fun isPlaying(): Boolean = exoPlayer?.isPlaying == true
    fun getPlayer(): ExoPlayer? = exoPlayer
    fun setVolume(volume: Float) = exoPlayer?.volume?.coerceIn(0f, 1f)
    fun setPlayWhenReady(playWhenReady: Boolean) = exoPlayer?.playWhenReady = playWhenReady

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentVideoUrl = null
        isPrepared = false
    }

    fun stop() {
        exoPlayer?.stop()
        isPrepared = false
    }

    private val playerListenerImpl = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> playerListener?.onPlayerStateChanged(PlayerState.IDLE)
                Player.STATE_BUFFERING -> playerListener?.onPlayerStateChanged(PlayerState.BUFFERING)
                Player.STATE_READY -> {
                    isPrepared = true
                    playerListener?.onPlayerStateChanged(PlayerState.READY)
                }
                Player.STATE_ENDED -> playerListener?.onPlayerStateChanged(PlayerState.ENDED)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            playerListener?.onIsPlayingChanged(isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            playerListener?.onPlayerError(error)
        }
    }

    enum class PlayerState {
        IDLE, BUFFERING, READY, ENDED
    }

    interface PlayerListener {
        fun onPlayerStateChanged(state: PlayerState) {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onPlayerError(error: PlaybackException) {}
    }
}
```

### 4.4 ServiceLocator 设计 (替代依赖注入)

**ServiceLocator 实现：**

```kotlin
object ServiceLocator {

    private val services = mutableMapOf<Class<*>, Any>()

    fun <T> register(clazz: Class<T>, instance: T) {
        services[clazz] = instance as Any
        LogUtil.d("ServiceLocator", "Registered: ${clazz.simpleName}")
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(clazz: Class<T>): T {
        return services[clazz] as? T ?: throw IllegalStateException(
            "Service ${clazz.simpleName} not found. Please register it in ServiceLocator."
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrNull(clazz: Class<T>): T? {
        return services[clazz] as? T
    }

    // 内联函数版本
    inline fun <reified T> get(): T = get(T::class.java)
    inline fun <reified T> getOrNull(): T? = getOrNull(T::class.java)
}
```

**在 Application 中初始化：**

```kotlin
class DouyinApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        initARouter()
        initRetrofit()
        initServiceLocator()
        initVideoPlayer()
    }

    private fun initServiceLocator() {
        val authPreferences = AuthPreferences.getInstance(this)

        // 注册认证偏好设置
        ServiceLocator.register(AuthPreferences::class.java, authPreferences)

        // 注册网络服务
        ServiceLocator.register(ApiService::class.java, RetrofitClient.apiService)

        // 注册数据库
        val database = AppDatabase.getInstance(this)
        ServiceLocator.register(VideoDao::class.java, database.videoDao())
        ServiceLocator.register(UserDao::class.java, database.userDao())

        // 注册 Repository
        val videoRepository = VideoRepositoryImpl(
            RetrofitClient.apiService,
            database.videoDao()
        )
        ServiceLocator.register(VideoRepository::class.java, videoRepository)

        val userRepository = UserRepositoryImpl(
            RetrofitClient.apiService,
            database.userDao()
        )
        ServiceLocator.register(UserRepository::class.java, userRepository)

        // 注册发布和认证 Repository
        val publishRepository = PublishRepositoryImpl(this, RetrofitClient.apiService)
        ServiceLocator.register(PublishRepository::class.java, publishRepository)

        val authRepository = AuthRepositoryImpl(RetrofitClient.apiService, authPreferences)
        ServiceLocator.register(AuthRepository::class.java, authRepository)
    }

    private fun initVideoPlayer() {
        VideoPlayerManager.instance.initialize(this)
    }

    companion object {
        lateinit var instance: DouyinApp
            private set
    }
}
```

### 4.5 认证模块设计 (core-auth)

**AuthPreferences - Token 管理：**

```kotlin
class AuthPreferences private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getString(KEY_TOKEN, null)?.isNotEmpty() == true
        private set(value) {}

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var currentUserId: Long
        get() = prefs.getLong(KEY_USER_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_USER_ID, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var nickname: String?
        get() = prefs.getString(KEY_NICKNAME, null)
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    var avatar: String?
        get() = prefs.getString(KEY_AVATAR, null)
        set(value) = prefs.edit().putString(KEY_AVATAR, value).apply()

    fun saveLoginResult(result: LoginResult) {
        token = result.token
        refreshToken = result.refreshToken
        currentUserId = result.userId
        username = result.username
        nickname = result.nickname
        avatar = result.avatar
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "douyin_auth"
        private const val KEY_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_AVATAR = "avatar"

        @Volatile
        private var instance: AuthPreferences? = null

        fun getInstance(context: Context): AuthPreferences {
            return instance ?: synchronized(this) {
                instance ?: AuthPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
```

---

## 5. Domain 层设计

### 5.1 领域模型

```kotlin
data class Video(
    val id: String,
    val title: String?,
    val description: String?,
    val videoUrl: String,
    val coverUrl: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val author: User?,
    val likeCount: Long,
    val commentCount: Long,
    val shareCount: Long,
    val collectCount: Long,
    val viewCount: Long,
    val isLiked: Boolean,
    val isCollected: Boolean,
    val topicName: String?,
    val music: Music?,
    val location: String?,
    val createTime: String?
) {
    val formattedDuration: String
        get() {
            val seconds = duration % 60
            val minutes = duration / 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    val formattedLikeCount: String
        get() = formatCount(likeCount)

    val formattedCommentCount: String
        get() = formatCount(commentCount)

    val formattedShareCount: String
        get() = formatCount(shareCount)

    val formattedViewCount: String
        get() = formatCount(viewCount)

    private fun formatCount(count: Long): String {
        return when {
            count >= 1_0000_0000 -> String.format("%.1f亿", count / 1_0000_0000.0)
            count >= 1_0000 -> String.format("%.1f万", count / 1_0000.0)
            else -> count.toString()
        }
    }
}

data class User(
    val userId: Long,
    val nickname: String,
    val avatar: String?,
    val signature: String? = null,
    val fansCount: Long = 0,
    val followCount: Long = 0,
    val isFollowing: Boolean = false,
    val isMutual: Boolean = false,
    val level: Int = 0
)

data class Music(
    val musicId: String,
    val title: String?,
    val author: String?,
    val albumCover: String?
)

data class Comment(
    val commentId: String,
    val user: User?,
    val content: String,
    val likeCount: Long,
    val replyCount: Long,
    val isLiked: Boolean,
    val createTime: String?,
    val replies: List<Comment>?
)

data class Topic(
    val id: Long,
    val name: String
)

// 统一结果类
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data
    fun exceptionOrNull(): Throwable? = (this as? Error)?.exception
}
```

### 5.2 仓库接口

```kotlin
interface VideoRepository {
    suspend fun getVideoFeed(page: Int = 1, size: Int = 10): Result<List<Video>>
    suspend fun getVideoDetail(videoId: String): Result<Video>
    suspend fun likeVideo(videoId: String): Result<Unit>
    suspend fun unlikeVideo(videoId: String): Result<Unit>
    suspend fun shareVideo(videoId: String, platform: String): Result<String>
    suspend fun getVideoComments(videoId: String, page: Int = 1, size: Int = 20): Result<List<Comment>>
    suspend fun postComment(videoId: String, content: String, parentId: String? = null): Result<Comment>

    fun getLocalVideos(): LiveData<List<Video>>
    fun getVideoById(videoId: String): LiveData<Video?>
    suspend fun getVideosByAuthor(authorId: Long): List<Video>
}

interface UserRepository {
    suspend fun getUserProfile(userId: Long): Result<User>
    suspend fun getUserFollows(userId: Long, page: Int = 1, size: Int = 20): Result<List<User>>
    suspend fun getUserFans(userId: Long, page: Int = 1, size: Int = 20): Result<List<User>>
    suspend fun followUser(userId: Long): Result<Unit>
    suspend fun unfollowUser(userId: Long): Result<Unit>
    suspend fun getUserVideos(userId: Long, page: Int = 1, size: Int = 20): Result<List<Video>>

    fun getLocalUser(userId: Long): LiveData<User?>
}

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoginResult>
    suspend fun loginByPhone(phone: String, code: String): Result<LoginResult>
    suspend fun register(
        username: String,
        password: String,
        nickname: String,
        phone: String? = null,
        email: String? = null
    ): Result<LoginResult>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<LoginResult>
    fun isLoggedIn(): Boolean
    fun getCurrentUserId(): Long?
    fun getToken(): String?
}

interface PublishRepository {
    suspend fun uploadVideo(
        videoUri: Uri,
        title: String,
        description: String?,
        topicIds: List<Long>?,
        location: String?,
        coverUri: Uri?,
        onProgress: (Int) -> Unit
    ): Result<PublishResult>

    suspend fun uploadCover(
        coverUri: Uri,
        onProgress: (Int) -> Unit
    ): Result<String>
}

data class LoginResult(
    val userId: Long,
    val username: String?,
    val nickname: String,
    val avatar: String?,
    val token: String,
    val refreshToken: String?,
    val expiresIn: Long
)

data class PublishResult(
    val videoId: String,
    val title: String?,
    val status: String,
    val coverUrl: String?,
    val videoUrl: String?
)
```

---

## 6. Feature 模块设计

### 6.1 首页推荐模块 (feature-main)

**功能职责：**

- 短视频信息流展示
- 视频自动播放
- 下拉刷新/上拉加载更多
- 点赞、评论、分享交互
- 关注作者
- 评论列表查看和发布

**ViewModel 设计：**

```kotlin
class MainViewModel(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _videos = MutableLiveData<List<Video>>()
    val videos: LiveData<List<Video>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentIndex = MutableLiveData<Int>(0)
    val currentIndex: LiveData<Int> = _currentIndex

    // 事件 LiveData
    private val _likeEvent = MutableLiveData<LikeEvent?>()
    val likeEvent: LiveData<LikeEvent?> = _likeEvent

    private val _shareEvent = MutableLiveData<ShareEvent?>()
    val shareEvent: LiveData<ShareEvent?> = _shareEvent

    private val _commentEvent = MutableLiveData<CommentEvent?>()
    val commentEvent: LiveData<CommentEvent?> = _commentEvent

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> = _comments

    private val _isCommentsLoading = MutableLiveData<Boolean>(false)
    val isCommentsLoading: LiveData<Boolean> = _isCommentsLoading

    private var nextPage = 1
    private var hasMore = true
    private var isLoadingMore = false

    private var currentVideoUrl: String? = null

    init {
        loadVideos()
    }

    fun loadVideos(refresh: Boolean = false) {
        if (refresh) {
            nextPage = 1
            hasMore = true
        }

        if (!hasMore && !refresh) return
        if (isLoadingMore && !refresh) return

        if (refresh) _isRefreshing.value = true else _isLoading.value = true

        viewModelScope.launch {
            when (val result = videoRepository.getVideoFeed(nextPage)) {
                is Result.Success -> {
                    val newVideos = result.data
                    val currentList = if (refresh) emptyList() else _videos.value ?: emptyList()
                    _videos.value = currentList + newVideos
                    hasMore = newVideos.isNotEmpty()
                    nextPage++
                    _error.value = null
                }
                is Result.Error -> {
                    _error.value = result.message ?: "加载失败"
                }
                is Result.Loading -> {}
            }

            _isLoading.value = false
            _isRefreshing.value = false
            isLoadingMore = false
        }
    }

    fun loadMoreVideos() {
        if (!hasMore || isLoadingMore) return
        isLoadingMore = true
        loadVideos()
    }

    fun onVideoChanged(position: Int) {
        _currentIndex.value = position

        // Preload next videos
        if (position >= (_videos.value?.size ?: 0) - 3) {
            loadMoreVideos()
        }
    }

    fun playVideo(videoUrl: String) {
        currentVideoUrl = videoUrl
        VideoPlayerManager.instance.playVideo(videoUrl)
    }

    fun pauseVideo() = VideoPlayerManager.instance.pause()
    fun resumeVideo() = VideoPlayerManager.instance.resume()

    fun likeVideo(video: Video) {
        viewModelScope.launch {
            val result = if (video.isLiked) {
                videoRepository.unlikeVideo(video.id)
            } else {
                videoRepository.likeVideo(video.id)
            }

            when (result) {
                is Result.Success -> {
                    val updatedVideos = _videos.value?.map {
                        if (it.id == video.id) {
                            it.copy(
                                isLiked = !video.isLiked,
                                likeCount = if (video.isLiked) it.likeCount - 1 else it.likeCount + 1
                            )
                        } else it
                    }
                    _videos.value = updatedVideos

                    _likeEvent.value = LikeEvent(
                        videoId = video.id,
                        isLiked = !video.isLiked,
                        likeCount = if (video.isLiked) video.likeCount - 1 else video.likeCount + 1
                    )
                }
                is Result.Error -> {
                    _error.value = result.message ?: "操作失败"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun shareVideo(video: Video, platform: String) {
        viewModelScope.launch {
            when (val result = videoRepository.shareVideo(video.id, platform)) {
                is Result.Success -> {
                    _shareEvent.value = ShareEvent(
                        videoId = video.id,
                        shareUrl = result.data,
                        platform = platform
                    )
                }
                is Result.Error -> {
                    _error.value = result.message ?: "分享失败"
                }
                is Result.Loading -> {}
            }
        }
    }

    fun postComment(video: Video, content: String) {
        val trimmedContent = content.trim()
        if (trimmedContent.isEmpty()) {
            _commentEvent.value = CommentEvent(
                videoId = video.id,
                isSuccess = false,
                message = "评论不能为空"
            )
            return
        }

        viewModelScope.launch {
            when (val result = videoRepository.postComment(video.id, trimmedContent)) {
                is Result.Success -> {
                    _comments.value = listOf(result.data) + (_comments.value ?: emptyList())

                    val updatedVideos = _videos.value?.map {
                        if (it.id == video.id) {
                            it.copy(commentCount = it.commentCount + 1)
                        } else it
                    }
                    _videos.value = updatedVideos

                    _commentEvent.value = CommentEvent(
                        videoId = video.id,
                        isSuccess = true,
                        message = "评论成功"
                    )
                }
                is Result.Error -> {
                    _commentEvent.value = CommentEvent(
                        videoId = video.id,
                        isSuccess = false,
                        message = result.message ?: "评论失败"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadComments(videoId: String) {
        _isCommentsLoading.value = true
        viewModelScope.launch {
            when (val result = videoRepository.getVideoComments(videoId)) {
                is Result.Success -> {
                    _comments.value = result.data
                    _isCommentsLoading.value = false
                }
                is Result.Error -> {
                    _comments.value = emptyList()
                    _isCommentsLoading.value = false
                    _commentEvent.value = CommentEvent(
                        videoId = videoId,
                        isSuccess = false,
                        message = result.message ?: "评论加载失败"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onLikeEventHandled() { _likeEvent.value = null }
    fun onShareEventHandled() { _shareEvent.value = null }
    fun onCommentEventHandled() { _commentEvent.value = null }
    fun clearError() { _error.value = null }

    data class LikeEvent(val videoId: String, val isLiked: Boolean, val likeCount: Long)
    data class ShareEvent(val videoId: String, val shareUrl: String, val platform: String)
    data class CommentEvent(val videoId: String, val isSuccess: Boolean, val message: String)
}
```

**ViewModelFactory 设计：**

```kotlin
class MainViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                val videoRepository = ServiceLocator.get(VideoRepository::class.java)
                MainViewModel(videoRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
```

**Fragment 实现：**

```kotlin
@UnstableApi
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModelFactory())[MainViewModel::class.java]
    }

    private lateinit var videoAdapter: VideoAdapter
    private var pendingScrollToFirstVideo = false
    private var commentDialog: BottomSheetDialog? = null

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
        viewModel.videos.observe(viewLifecycleOwner) { videos ->
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
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                viewModel.onCommentEventHandled()
            }
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            renderComments(comments)
        }
    }

    private fun setupPlayerListener() {
        VideoPlayerManager.instance.playerListener = object : VideoPlayerManager.PlayerListener {
            override fun onPlayerStateChanged(state: VideoPlayerManager.PlayerState) {
                // Handle player state
            }

            override fun onPlayerError(error: PlaybackException) {
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
}
```

### 6.2 认证模块 (feature-auth)

**AuthUiState 状态类：**

```kotlin
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class LoginSuccess(
        val userId: Long,
        val nickname: String,
        val avatar: String?
    ) : AuthUiState()
    data class RegisterSuccess(
        val userId: Long,
        val nickname: String,
        val avatar: String?
    ) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class LoginFormState(
    val username: String = "",
    val password: String = "",
    val isUsernameError: Boolean = false,
    val isPasswordError: Boolean = false,
    val usernameError: String? = null,
    val passwordError: String? = null
) {
    val isValid: Boolean
        get() = username.isNotBlank() && password.length >= 6
}

data class RegisterFormState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nickname: String = "",
    val phone: String = "",
    val email: String = "",
    // ... validation states
) {
    val isValid: Boolean
        get() = username.length >= 6 &&
                password.length >= 6 &&
                password == confirmPassword &&
                nickname.length >= 2
}
```

---

## 7. 路由设计 (ARouter)

### 7.1 路由常量

```kotlin
object RouterConstants {
    // 主页面路由
    const val MAIN = "/main"
    const val HOME = "/main/home"
    const val DISCOVER = "/main/discover"
    const val PUBLISH = "/main/publish"
    const val MESSAGE = "/main/message"
    const val PROFILE = "/main/profile"

    // 功能页面路由
    const val VIDEO_DETAIL = "/video/detail"
    const val USER_PROFILE = "/user/profile"
    const val SEARCH = "/search"
    const val SETTINGS = "/settings"
    const val LOGIN = "/login"
    const val REGISTER = "/register"
    const val VIDEO_EDIT = "/video/edit"
}
```

### 7.2 Activity 路由配置

```kotlin
@Route(path = RouterConstants.MAIN)
class MainActivity : AppCompatActivity() {
    // ...
}

@Route(path = RouterConstants.LOGIN)
class LoginActivity : AppCompatActivity() {
    // ...
}

@Route(path = RouterConstants.REGISTER)
class RegisterActivity : AppCompatActivity() {
    // ...
}
```

### 7.3 导航图

```
┌─────────────────────────────────────────────────────────┐
│                    MainActivity                          │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────┐    ┌─────────┐    ┌─────────┐             │
│  │  Home   │◀──▶│ Discover│◀──▶│ Publish │             │
│  │Fragment │    │Fragment │    │Activity │             │
│  └────┬────┘    └────┬────┘    └────┬────┘             │
│       │              │              │                   │
│       ▼              ▼              ▼                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │
│  │ VideoDetail │ │ Search     │ │ VideoEdit   │       │
│  │ Activity    │ │ Activity   │ │ Activity    │       │
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

## 8. MainActivity 设计

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var authPreferences: AuthPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        VideoPlayerManager.instance.initialize(this)
        authPreferences = AuthPreferences.getInstance(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!authPreferences.isLoggedIn) {
            navigateToLogin()
            return
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, MainFragment.newInstance())
                .commit()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGIN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main, MainFragment.newInstance())
                    .commit()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoPlayerManager.instance.release()
    }

    companion object {
        private const val REQUEST_LOGIN = 1001
    }
}
```

---

## 9. 性能优化策略

### 9.1 启动优化

- **多级启动器**：AppStartup + Lazy Initialization
- **异步初始化**：非核心组件后台初始化
- **预加载**：首页数据提前请求

### 9.2 内存优化

- **图片压缩**：根据 View 尺寸加载对应大小图片
- **内存泄漏检测**：LeakCanary
- **对象池**：复用 RecyclerView 列表项

### 9.3 列表优化

- **ViewHolder 复用**：RecyclerView 回收机制
- **图片缓存**：Glide 自动管理
- **预取**：Prefetch 相邻元素
- **分页加载**：Pagination 库

### 9.4 网络优化

- **请求合并**：批量请求
- **本地缓存**：Room + OkHttp Cache
- **增量更新**：只请求变化数据

---

## 10. 安全策略

### 10.1 通信安全

- **HTTPS**：全站强制 HTTPS
- **证书锁定**：Certificate Pinning
- **请求签名**：参数签名防篡改

### 10.2 数据安全

- **敏感数据加密**：Android Keystore
- **Dex 加固**：ProGuard/R8
- **日志脱敏**：发布版本禁用日志

### 10.3 权限管理

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

## 11. 测试策略

### 11.1 单元测试

- **ViewModel 测试**：使用 InstantTaskExecutorRule 测试 LiveData
- **Repository 测试**：Mock DataSource
- **UseCase 测试**：Mock Repository

### 11.2 UI 测试

- **Espresso 测试**：Activity/Fragment UI 测试
- **截图测试**：Screenshot Tests
- **E2E 测试**：UI Automator

### 11.3 集成测试

- **Mock Server**：MockWebServer
- **数据库测试**：Instrumentation Test
- **模块集成测试**：Test Fixture

---

## 12. 附录

### 12.1 项目文件结构

```
douyinAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/douyinandroid/
│   │   │   ├── DouyinApp.kt                    # Application 类
│   │   │   ├── MainActivity.kt                 # 主 Activity
│   │   │   ├── core/
│   │   │   │   ├── ServiceLocator.kt           # 依赖服务定位器
│   │   │   │   ├── core_auth/
│   │   │   │   │   └── AuthPreferences.kt      # Token 管理
│   │   │   │   ├── core_database/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── AppDatabase.kt       # Room 数据库
│   │   │   │   │   │   └── dao/
│   │   │   │   │   │       ├── VideoDao.kt
│   │   │   │   │   │       └── UserDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       └── Entities.kt
│   │   │   │   ├── core_network/
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── ApiService.kt       # API 接口
│   │   │   │   │   │   ├── ApiConstants.kt     # 端点常量
│   │   │   │   │   │   ├── RetrofitClient.kt   # Retrofit 配置
│   │   │   │   │   │   ├── bean/               # 数据 Bean
│   │   │   │   │   │   └── interceptor/        # 网络拦截器
│   │   │   │   └── core_video/
│   │   │   │       └── video/
│   │   │   │           └── VideoPlayerManager.kt # 视频播放器管理
│   │   │   ├── common/
│   │   │   │   ├── common_ext/
│   │   │   │   │   └── Extensions.kt           # 扩展函数
│   │   │   │   ├── common_utils/
│   │   │   │   │   └── LogUtil.kt              # 日志工具
│   │   │   │   └── router/
│   │   │   │       └── RouterConstants.kt       # 路由常量
│   │   │   ├── data/
│   │   │   │   └── repository/
│   │   │   │       ├── VideoRepositoryImpl.kt
│   │   │   │       ├── UserRepositoryImpl.kt
│   │   │   │       ├── AuthRepositoryImpl.kt
│   │   │   │       └── PublishRepositoryImpl.kt
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   └── Models.kt               # 领域模型
│   │   │   │   └── repository/
│   │   │   │       └── Repositories.kt         # 仓库接口
│   │   │   └── feature/
│   │   │       ├── feature_auth/
│   │   │       │   └── ui/
│   │   │       │       ├── LoginActivity.kt
│   │   │       │       ├── RegisterActivity.kt
│   │   │       │       ├── AuthViewModel.kt
│   │   │       │       └── AuthUiState.kt
│   │   │       ├── feature_main/
│   │   │       │   └── ui/
│   │   │       │       ├── MainFragment.kt
│   │   │       │       ├── MainViewModel.kt
│   │   │       │       ├── MainViewModelFactory.kt
│   │   │       │       ├── MainUiState.kt
│   │   │       │       └── adapter/
│   │   │       │           └── VideoAdapter.kt
│   │   │       └── feature_publish/
│   │   │           └── ui/
│   │   │               ├── PublishActivity.kt
│   │   │               ├── PublishFragment.kt
│   │   │               └── PublishViewModel.kt
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── fragment_main.xml
│   │       │   └── ...
│   │       ├── drawable/
│   │       ├── values/
│   │       └── ...
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

### 12.2 技术社区资源

- [Android Developers](https://developer.android.com)
- [Android XML Layouts](https://developer.android.com/guide/topics/ui/overview)
- [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
- [Kotlin](https://kotlinlang.org)
- [ARouter](https://github.com/alibaba/ARouter)
- [Glide](https://github.com/bumptech/glide)
- [Media3/ExoPlayer](https://developer.android.com/guide/topics/media/media3)
- [Room](https://developer.android.com/training/data-storage/room)
- [Retrofit](https://square.github.io/retrofit/)

### 12.3 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2024-XX-XX | 初始版本 |
| 2.0.0 | 2026-04-29 | 根据实际代码更新架构文档 |


---

*文档版本：v2.0.0*
*最后更新：2026年4月29日*
