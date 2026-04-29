# 抖音短视频项目目录结构

## 1. 项目整体结构

```
douyin/
├── docs/                           # 项目文档
│   ├── android-architecture.md     # Android 技术架构文档
│   ├── backend-architecture.md     # 后端技术架构文档
│   ├── api-documentation.md         # API 接口文档
│   └── project-structure.md        # 项目目录结构文档
│
├── android/                        # Android 客户端
│   └── app/
│
├── backend/                        # 后端服务
│   └── douyin-backend/
│
├── design/                         # 设计资源
│   ├── ui/
│   └── icons/
│
├── docsify/                        # 文档站点
│   ├── index.html
│   ├── _sidebar.md
│   └── .nojekyll
│
├── README.md                       # 项目说明
├── SPEC.md                         # 产品规格说明
└── .gitignore
```

---

## 2. Android 客户端目录结构

```
android/
└── app/
    ├── build.gradle.kts            # 根构建配置
    ├── settings.gradle.kts         # 项目设置
    ├── gradle.properties           # Gradle 属性
    ├── local.properties             # 本地配置
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/douyin/
        │   │   ├── DouyinApplication.kt
        │   │   ├── MainActivity.kt
        │   │   │
        │   │   ├── app/                    # 应用壳模块
        │   │   │   ├── AppModule.kt
        │   │   │   ├── AppNavigation.kt
        │   │   │   └── AppViewModel.kt
        │   │   │
        │   │   ├── core/                   # 核心基础模块
        │   │   │   ├── core-network/       # 网络层
        │   │   │   │   ├── src/main/java/com/douyin/core/network/
        │   │   │   │   │   ├── api/
        │   │   │   │   │   │   ├── VideoApiService.kt
        │   │   │   │   │   │   ├── UserApiService.kt
        │   │   │   │   │   │   └── MessageApiService.kt
        │   │   │   │   │   ├── client/
        │   │   │   │   │   │   ├── RetrofitClient.kt
        │   │   │   │   │   │   └── OkHttpClient.kt
        │   │   │   │   │   ├── interceptor/
        │   │   │   │   │   │   ├── AuthInterceptor.kt
        │   │   │   │   │   │   ├── LoggingInterceptor.kt
        │   │   │   │   │   │   └── ErrorInterceptor.kt
        │   │   │   │   │   └── config/
        │   │   │   │   │       └── NetworkConfig.kt
        │   │   │   │   └── build.gradle.kts
        │   │   │   │
        │   │   │   ├── core-database/      # 数据库层
        │   │   │   │   ├── src/main/java/com/douyin/core/database/
        │   │   │   │   │   ├── dao/
        │   │   │   │   │   │   ├── VideoDao.kt
        │   │   │   │   │   │   ├── UserDao.kt
        │   │   │   │   │   │   └── HistoryDao.kt
        │   │   │   │   │   ├── entity/
        │   │   │   │   │   │   ├── VideoEntity.kt
        │   │   │   │   │   │   ├── UserEntity.kt
        │   │   │   │   │   │   └── CommentEntity.kt
        │   │   │   │   │   ├── AppDatabase.kt
        │   │   │   │   │   └── DatabaseModule.kt
        │   │   │   │   └── build.gradle.kts
        │   │   │   │
        │   │   │   ├── core-video/         # 视频播放
        │   │   │   │   ├── src/main/java/com/douyin/core/video/
        │   │   │   │   │   ├── player/
        │   │   │   │   │   │   ├── VideoPlayer.kt
        │   │   │   │   │   │   ├── PlayerManager.kt
        │   │   │   │   │   │   └── GestureHelper.kt
        │   │   │   │   │   ├── cache/
        │   │   │   │   │   │   └── VideoCacheManager.kt
        │   │   │   │   │   └── VideoModule.kt
        │   │   │   │   └── build.gradle.kts
        │   │   │   │
        │   │   │   └── core-storage/       # 本地存储
        │   │   │       ├── src/main/java/com/douyin/core/storage/
        │   │   │       │   ├── preferences/
        │   │   │       │   │   ├── PreferencesManager.kt
        │   │   │       │   │   └── UserPreferences.kt
        │   │   │       │   └── StorageModule.kt
        │   │   │       └── build.gradle.kts
        │   │   │
        │   │   ├── common/                  # 公共组件模块
        │   │   │   ├── common-ui/            # 通用UI组件
        │   │   │   │   ├── src/main/java/com/douyin/common/ui/
        │   │   │   │   │   ├── components/
        │   │   │   │   │   │   ├── DouyinButton.kt
        │   │   │   │   │   ├── DouyinTextField.kt
        │   │   │   │   │   ├── LoadingIndicator.kt
        │   │   │   │   │   ├── ErrorView.kt
        │   │   │   │   │   ├── EmptyView.kt
        │   │   │   │   │   ├── Avatar.kt
        │   │   │   │   │   └── VideoCover.kt
        │   │   │   │   │   └── theme/
        │   │   │   │   │       ├── Color.kt
        │   │   │   │   │       ├── Type.kt
        │   │   │   │   │       └── Shape.kt
        │   │   │   │   └── build.gradle.kts
        │   │   │   │
        │   │   │   ├── common-ext/           # 扩展函数
        │   │   │   │   ├── src/main/java/com/douyin/common/ext/
        │   │   │   │   │   ├── ContextExt.kt
        │   │   │   │   │   ├── StringExt.kt
        │   │   │   │   │   ├── DateExt.kt
        │   │   │   │   │   ├── ViewExt.kt
        │   │   │   │   │   └── NumberExt.kt
        │   │   │   │   └── build.gradle.kts
        │   │   │   │
        │   │   │   └── common-utils/         # 工具类
        │   │   │       ├── src/main/java/com/douyin/common/utils/
        │   │   │       │   ├── AppUtils.kt
        │   │   │       │   ├── FileUtils.kt
        │   │   │       │   ├── NetworkUtils.kt
        │   │   │       │   ├── SPUtils.kt
        │   │   │       │   ├── ToastUtils.kt
        │   │   │       │   ├── KeyboardUtils.kt
        │   │   │       │   └── PermissionUtils.kt
        │   │   │       └── build.gradle.kts
        │   │   │
        │   │   └── feature/                  # 功能模块
        │   │       ├── feature-main/          # 首页推荐
        │   │       │   ├── src/main/java/com/douyin/feature/main/
        │   │       │   │   ├── MainScreen.kt
        │   │       │   │   ├── MainViewModel.kt
        │   │       │   │   ├── MainUiState.kt
        │   │       │   │   ├── MainEvent.kt
        │   │       │   │   ├── MainNavigation.kt
        │   │       │   │   ├── video/
        │   │       │   │   │   ├── VideoFeedScreen.kt
        │   │       │   │   │   ├── VideoFeedViewModel.kt
        │   │       │   │   │   └── VideoItem.kt
        │   │       │   │   └── interaction/
        │   │       │   │       └── InteractionBar.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-user/          # 用户模块
        │   │       │   ├── src/main/java/com/douyin/feature/user/
        │   │       │   │   ├── profile/
        │   │       │   │   │   ├── ProfileScreen.kt
        │   │       │   │   │   ├── ProfileViewModel.kt
        │   │       │   │   │   └── ProfileUiState.kt
        │   │       │   │   ├── follow/
        │   │       │   │   │   ├── FollowListScreen.kt
        │   │       │   │   │   └── FansListScreen.kt
        │   │       │   │   └── UserModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-video/          # 视频模块
        │   │       │   ├── src/main/java/com/douyin/feature/video/
        │   │       │   │   ├── detail/
        │   │       │   │   │   ├── VideoDetailScreen.kt
        │   │       │   │   │   ├── VideoDetailViewModel.kt
        │   │       │   │   │   └── VideoDetailUiState.kt
        │   │       │   │   ├── comment/
        │   │       │   │   │   ├── CommentSheet.kt
        │   │       │   │   │   ├── CommentViewModel.kt
        │   │       │   │   │   └── CommentItem.kt
        │   │       │   │   └── VideoModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-message/       # 消息模块
        │   │       │   ├── src/main/java/com/douyin/feature/message/
        │   │       │   │   ├── conversation/
        │   │       │   │   │   ├── ConversationListScreen.kt
        │   │       │   │   │   └── ConversationViewModel.kt
        │   │       │   │   ├── chat/
        │   │       │   │   │   ├── ChatScreen.kt
        │   │       │   │   │   ├── ChatViewModel.kt
        │   │       │   │   │   └── MessageItem.kt
        │   │       │   │   ├── notification/
        │   │       │   │   │   ├── NotificationScreen.kt
        │   │       │   │   │   └── NotificationViewModel.kt
        │   │       │   │   └── MessageModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-publish/       # 发布模块
        │   │       │   ├── src/main/java/com/douyin/feature/publish/
        │   │       │   │   ├── record/
        │   │       │   │   │   ├── RecordScreen.kt
        │   │       │   │   │   ├── RecordViewModel.kt
        │   │       │   │   │   └── CameraController.kt
        │   │       │   │   ├── edit/
        │   │       │   │   │   ├── EditScreen.kt
        │   │       │   │   │   ├── EditViewModel.kt
        │   │       │   │   │   └── VideoTrimmer.kt
        │   │       │   │   ├── publish/
        │   │       │   │   │   ├── PublishScreen.kt
        │   │       │   │   │   └── PublishViewModel.kt
        │   │       │   │   └── PublishModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-search/        # 搜索模块
        │   │       │   ├── src/main/java/com/douyin/feature/search/
        │   │       │   │   ├── SearchScreen.kt
        │   │       │   │   ├── SearchViewModel.kt
        │   │       │   │   ├── SearchUiState.kt
        │   │       │   │   ├── SearchResultScreen.kt
        │   │       │   │   └── SearchModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       ├── feature-login/          # 登录模块
        │   │       │   ├── src/main/java/com/douyin/feature/login/
        │   │       │   │   ├── LoginScreen.kt
        │   │       │   │   ├── LoginViewModel.kt
        │   │       │   │   ├── RegisterScreen.kt
        │   │       │   │   └── LoginModule.kt
        │   │       │   └── build.gradle.kts
        │   │       │
        │   │       └── feature-settings/      # 设置模块
        │   │           ├── src/main/java/com/douyin/feature/settings/
        │   │           │   ├── SettingsScreen.kt
        │   │           │   ├── SettingsViewModel.kt
        │   │           │   ├── AboutScreen.kt
        │   │           │   ├── PrivacyScreen.kt
        │   │           │   └── SettingsModule.kt
        │   │           └── build.gradle.kts
        │   │
        │   ├── res/
        │   │   ├── values/
        │   │   │   ├── strings.xml
        │   │   │   ├── colors.xml
        │   │   │   ├── dimens.xml
        │   │   │   └── themes.xml
        │   │   ├── drawable/
        │   │   ├── mipmap/
        │   │   └── raw/
        │   │
        │   └── assets/
        │       └── fonts/
        │
        └── androidTest/
        │   └── java/com/douyin/
        └── test/
            └── java/com/douyin/
```

---

## 3. 后端服务目录结构

```
backend/
└── douyin-backend/
    ├── pom.xml                           # Maven 父项目配置
    ├── docker-compose.yml                # Docker 编排配置
    ├── config/
    │   ├── application.yml              # 主配置文件
    │   ├── application-dev.yml          # 开发环境
    │   ├── application-test.yml         # 测试环境
    │   └── application-prod.yml         # 生产环境
    │
    ├── douyin-common/                   # 公共模块
    │   ├── pom.xml
    │   └── src/main/java/com/douyin/common/
    │       ├── annotation/             # 注解
    │       │   ├── LoginRequired.java
    │       │   └── RepeatSubmit.java
    │       ├── constant/                # 常量定义
    │       │   ├── RedisKeys.java
    │       │   └── BusinessType.java
    │       ├── enums/                   # 枚举
    │       │   ├── ErrorCode.java
    │       │   ├── UserStatus.java
    │       │   └── VideoStatus.java
    │       ├── exception/               # 异常处理
    │       │   ├── BusinessException.java
    │       │   ├── GlobalExceptionHandler.java
    │       │   └── ErrorCodeException.java
    │       ├── model/                   # 通用模型
    │       │   ├── ApiResponse.java
    │       │   ├── PageRequest.java
    │       │   └── PageResult.java
    │       ├── utils/                   # 工具类
    │       │   ├── JsonUtils.java
    │       │   ├── DateUtils.java
    │       │   ├── StringUtils.java
    │       │   ├── IdUtils.java
    │       │   └── IpUtils.java
    │       └── result/                  # 统一返回
    │           ├── Result.java
    │           └── ResultCode.java
    │
    ├── douyin-gateway/                  # API 网关
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/gateway/
    │       │   ├── GatewayApplication.java
    │       │   ├── config/
    │       │   │   ├── GatewayConfig.java
    │       │   │   └── CorsConfig.java
    │       │   ├── filter/
    │       │   │   ├── AuthFilter.java
    │       │   │   ├── RateLimitFilter.java
    │       │   │   └── LogFilter.java
    │       │   └── handler/
    │       │       └── FallbackHandler.java
    │       └── resources/
    │           └── application.yml
    │
    ├── douyin-user/                    # 用户服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/user/
    │       │   ├── UserApplication.java
    │       │   ├── controller/
    │       │   │   ├── AuthController.java
    │       │   │   └── UserController.java
    │       │   ├── service/
    │       │   │   ├── UserService.java
    │       │   │   ├── UserServiceImpl.java
    │       │   │   ├── AuthService.java
    │       │   │   └── AuthServiceImpl.java
    │       │   ├── repository/
    │       │   │   └── UserRepository.java
    │       │   ├── mapper/
    │       │   │   └── UserMapper.java
    │       │   ├── entity/
    │       │   │   └── User.java
    │       │   ├── dto/
    │       │   │   ├── LoginRequest.java
    │       │   │   ├── RegisterRequest.java
    │       │   │   ├── UserInfoDTO.java
    │       │   │   └── UpdateUserRequest.java
    │       │   └── config/
    │       │       ├── WebConfig.java
    │       │       └── ThreadPoolConfig.java
    │       └── resources/
    │           ├── mapper/
    │           │   └── UserMapper.xml
    │           └── application.yml
    │
    ├── douyin-video/                   # 视频服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/video/
    │       │   ├── VideoApplication.java
    │       │   ├── controller/
    │       │   │   ├── VideoController.java
    │       │   │   └── VideoUploadController.java
    │       │   ├── service/
    │       │   │   ├── VideoService.java
    │       │   │   ├── VideoServiceImpl.java
    │       │   │   ├── VideoUploadService.java
    │       │   │   └── VideoCacheService.java
    │       │   ├── repository/
    │       │   │   └── VideoRepository.java
    │       │   ├── mapper/
    │       │   │   └── VideoMapper.java
    │       │   ├── entity/
    │       │   │   └── Video.java
    │       │   ├── dto/
    │       │   │   ├── VideoFeedDTO.java
    │       │   │   ├── VideoDetailDTO.java
    │       │   │   └── VideoUploadRequest.java
    │       │   ├── kafka/
    │       │   │   ├── VideoEventProducer.java
    │       │   │   └── VideoEventConsumer.java
    │       │   └── config/
    │       │       └── VideoConfig.java
    │       └── resources/
    │           ├── mapper/
    │           │   └── VideoMapper.xml
    │           └── application.yml
    │
    ├── douyin-interaction/             # 互动服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/interaction/
    │       │   ├── InteractionApplication.java
    │       │   ├── controller/
    │       │   │   ├── LikeController.java
    │       │   │   ├── CommentController.java
    │       │   │   └── CollectController.java
    │       │   ├── service/
    │       │   │   ├── LikeService.java
    │       │   │   ├── CommentService.java
    │       │   │   └── InteractionCacheService.java
    │       │   ├── entity/
    │       │   │   ├── VideoLike.java
    │       │   │   └── Comment.java
    │       │   └── kafka/
    │       │       └── InteractionEventConsumer.java
    │       └── resources/
    │           ├── mapper/
    │           └── application.yml
    │
    ├── douyin-message/                 # 消息服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/message/
    │       │   ├── MessageApplication.java
    │       │   ├── controller/
    │       │   │   ├── ConversationController.java
    │       │   │   ├── ChatController.java
    │       │   │   └── NotificationController.java
    │       │   ├── service/
    │       │   │   ├── MessageService.java
    │       │   │   ├── NotificationService.java
    │       │   │   └── WebSocketService.java
    │       │   ├── entity/
    │       │   │   ├── Message.java
    │       │   │   └── Notification.java
    │       │   └── websocket/
    │       │       └── ChatWebSocket.java
    │       └── resources/
    │           ├── mapper/
    │           └── application.yml
    │
    ├── douyin-search/                  # 搜索服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/search/
    │       │   ├── SearchApplication.java
    │       │   ├── controller/
    │       │   │   └── SearchController.java
    │       │   ├── service/
    │       │   │   ├── SearchService.java
    │       │   │   ├── ElasticsearchService.java
    │       │   │   └── HotSearchService.java
    │       │   ├── entity/
    │       │   │   └── SearchIndex.java
    │       │   └── config/
    │       │       └── ElasticsearchConfig.java
    │       └── resources/
    │           └── application.yml
    │
    ├── douyin-recommend/               # 推荐服务
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/douyin/recommend/
    │       │   ├── RecommendApplication.java
    │       │   ├── controller/
    │       │   │   └── RecommendController.java
    │       │   ├── service/
    │       │   │   ├── RecommendService.java
    │       │   │   ├── RecallService.java
    │       │   │   ├── RankService.java
    │       │   │   └── RerankService.java
    │       │   ├── engine/
    │       │   │   ├── CandidateGenerator.java
    │       │   │   ├── FeatureExtractor.java
    │       │   │   └── RankingModel.java
    │       │   └── config/
    │       │       └── RecommendConfig.java
    │       └── resources/
    │           ├── model/              # 模型文件
    │           └── application.yml
    │
    ├── douyin-notification/            # 通知服务
    │   ├── pom.xml
    │   └── src/main/
    │       └── java/com/douyin/notification/
    │
    └── douyin-file/                    # 文件服务
        ├── pom.xml
        └── src/main/
            ├── java/com/douyin/file/
            │   ├── FileApplication.java
            │   ├── controller/
            │   │   └── FileController.java
            │   ├── service/
            │   │   ├── FileService.java
            │   │   ├── MinioService.java
            │   │   └── FileTranscodeService.java
            │   └── config/
            │       └── MinioConfig.java
            └── resources/
                └── application.yml
```

---

## 4. 数据库表结构汇总

```
douyin_user
├── users                          # 用户表
├── user_extended_info             # 用户扩展信息
└── user_tag                       # 用户标签

douyin_video
├── videos                         # 视频表
├── video_tag                      # 视频话题关联
├── video_music                    # 视频音乐关联
└── video_transcode_task           # 转码任务表

douyin_interaction
├── follows                        # 关注表
├── video_likes                    # 点赞表
├── comments                       # 评论表
├── comment_likes                  # 评论点赞表
├── collections                    # 收藏表
└── shares                         # 分享记录表

douyin_message
├── messages                       # 私信表
├── notifications                  # 通知表
└── chat_sessions                  # 聊天会话表

douyin_search
├── search_keywords                # 搜索关键词
└── hot_search                     # 热搜榜

douyin_system
├── configs                        # 系统配置表
├── banners                        # 首页横幅
└── operations_log                 # 操作日志
```

---

## 5. 关键配置文件说明

### 5.1 Android Gradle 配置

```
android/app/build.gradle.kts
```

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

android {
    namespace = "com.douyin.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.douyin.app"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "BASE_URL", "\"https://api.douyin.com/api/v1/\"")
    }
}

dependencies {
    implementation(project(":core:core-network"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-video"))
    implementation(project(":common:common-ui"))
    implementation(project(":feature:feature-main"))
    // ... more feature modules
}
```

### 5.2 后端 Spring Boot 配置

```
backend/douyin-video/src/main/resources/application.yml
```

```yaml
server:
  port: 8082
  servlet:
    context-path: /

spring:
  application:
    name: video-service
  datasource:
    url: jdbc:mysql://localhost:3306/douyin_video
    username: root
    password: root123
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    database: 0
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: video-service-group

nacos:
  server-addr: localhost:8848
  namespace: dev

logging:
  level:
    com.douyin: DEBUG
```

---

## 6. 环境配置说明

### 6.1 开发环境准备

**Android 开发环境：**
- Android Studio Hedgehog 或更高版本
- JDK 17+
- Android SDK 34
- Gradle 8.2+

**后端开发环境：**
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.x
- Kafka 3.6+
- Nacos 2.2+
- MinIO / OSS

### 6.2 环境变量

```bash
# Android
BASE_URL=https://api.douyin.com/api/v1/

# Backend
SPRING_PROFILES_ACTIVE=dev
MYSQL_HOST=localhost
REDIS_HOST=localhost
KAFKA_HOSTS=localhost:9092
NACOS_SERVER_ADDR=localhost:8848
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin
```

---

*文档版本：v1.0.0*
*最后更新：2024年*
