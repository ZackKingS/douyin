# 抖音短视频后端技术架构文档（简化版）

## 1. 项目概述

### 1.1 项目简介

本项目是类似抖音短视频社交平台的后端服务系统，采用微服务架构设计，支持用户管理、视频上传播放、社交互动等功能。

### 1.2 技术选型


| 类别        | 技术方案        | 版本    |
| --------- | ----------- | ----- |
| **核心框架**  | Spring Boot | 3.2.x |
| **编程语言**  | Java        | 17    |
| **数据库**   | MySQL       | 8.0.x |
| **文件存储**  | 本地文件系统      | -     |
| **构建工具**  | Maven       | 3.9.x |
| **API文档** | Knife4j     | -     |


---

## 2. 系统架构设计

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                               用户终端                                        │
│                           Android / iOS / Web                                │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        │                             │                             │
        ▼                             ▼                             ▼
┌───────────────┐           ┌───────────────┐           ┌───────────────┐
│  用户服务      │           │   视频服务    │           │   文件服务    │
│  user-service │           │ video-service │           │ file-service │
└───────┬───────┘           └───────┬───────┘           └───────┬───────┘
        │                           │                           │
        │                           │                           │
        └───────────────────────────┼───────────────────────────┘
                                    │
                                    ▼
                            ┌───────────────┐
                            │     MySQL     │
                            └───────────────┘
```

### 2.2 服务模块说明


| 模块                | 功能描述                 |
| ----------------- | -------------------- |
| **user-service**  | 用户注册、登录、个人信息管理、关注/粉丝 |
| **video-service** | 视频发布、播放、点赞、评论、收藏     |
| **file-service**  | 文件上传、下载、存储管理         |


---

## 3. 数据库设计

### 3.1 核心表结构

#### 3.1.1 用户表 (users)

```sql
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `signature` VARCHAR(500) DEFAULT '' COMMENT '个性签名',
    `country` VARCHAR(50) DEFAULT '' COMMENT '国家',
    `province` VARCHAR(50) DEFAULT '' COMMENT '省份',
    `city` VARCHAR(50) DEFAULT '' COMMENT '城市',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `follow_count` INT DEFAULT 0 COMMENT '关注数',
    `fans_count` INT DEFAULT 0 COMMENT '粉丝数',
    `like_count` BIGINT DEFAULT 0 COMMENT '获赞数',
    `video_count` INT DEFAULT 0 COMMENT '作品数',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
```

#### 3.1.2 视频表 (videos)

```sql
CREATE TABLE `videos` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id` VARCHAR(32) NOT NULL COMMENT '视频唯一标识',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `description` TEXT COMMENT '描述',
    `video_url` VARCHAR(500) NOT NULL COMMENT '视频URL',
    `cover_url` VARCHAR(500) NOT NULL COMMENT '封面URL',
    `duration` INT DEFAULT 0 COMMENT '时长(秒)',
    `width` INT DEFAULT 0 COMMENT '宽度',
    `height` INT DEFAULT 0 COMMENT '高度',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `like_count` BIGINT DEFAULT 0 COMMENT '点赞数',
    `comment_count` BIGINT DEFAULT 0 COMMENT '评论数',
    `share_count` BIGINT DEFAULT 0 COMMENT '分享数',
    `collect_count` BIGINT DEFAULT 0 COMMENT '收藏数',
    `view_count` BIGINT DEFAULT 0 COMMENT '播放数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待审核, 1-已发布, 2-已下架',
    `topic_ids` VARCHAR(255) DEFAULT '' COMMENT '话题ID列表',
    `location` VARCHAR(200) DEFAULT '' COMMENT '发布位置',
    `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    `at_user_ids` VARCHAR(500) DEFAULT '' COMMENT '@的用户ID列表',
    `music_id` BIGINT DEFAULT NULL COMMENT '使用的音乐ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_video_id` (`video_id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频表';
```

#### 3.1.3 关注表 (follows)

```sql
CREATE TABLE `follows` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `follow_id` BIGINT NOT NULL COMMENT '关注对象ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_follow_id` (`follow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';
```

#### 3.1.4 点赞表 (video_likes)

```sql
CREATE TABLE `video_likes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_video` (`user_id`, `video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频点赞表';
```

#### 3.1.5 评论表 (comments)

```sql
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `video_id` BIGINT NOT NULL COMMENT '视频ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID',
    `root_id` BIGINT DEFAULT NULL COMMENT '根评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT DEFAULT 0 COMMENT '回复数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-删除, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_video_id` (`video_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';
```

---

## 4. API 接口设计

### 4.1 用户模块


| 方法     | 接口                                | 描述     |
| ------ | --------------------------------- | ------ |
| POST   | `/api/v1/user/register`           | 用户注册   |
| POST   | `/api/v1/user/login`              | 用户登录   |
| GET    | `/api/v1/user/info/{userId}`      | 获取用户信息 |
| PUT    | `/api/v1/user/info`               | 更新个人信息 |
| POST   | `/api/v1/user/follow/{userId}`    | 关注用户   |
| DELETE | `/api/v1/user/follow/{userId}`    | 取消关注   |
| GET    | `/api/v1/user/followers/{userId}` | 获取粉丝列表 |
| GET    | `/api/v1/user/following/{userId}` | 获取关注列表 |


### 4.2 视频模块


| 方法     | 接口                             | 描述          |
| ------ | ------------------------------ | ----------- |
| POST   | `/api/v1/video/upload`         | 上传视频        |
| GET    | `/api/v1/video/list`           | 视频列表（推荐/关注） |
| GET    | `/api/v1/video/{videoId}`      | 视频详情        |
| DELETE | `/api/v1/video/{videoId}`      | 删除视频        |
| POST   | `/api/v1/video/like/{videoId}` | 点赞          |
| DELETE | `/api/v1/video/like/{videoId}` | 取消点赞        |
| GET    | `/api/v1/video/likes`          | 我的点赞列表      |


### 4.3 评论模块


| 方法     | 接口                                 | 描述     |
| ------ | ---------------------------------- | ------ |
| POST   | `/api/v1/comment`                  | 发布评论   |
| DELETE | `/api/v1/comment/{commentId}`      | 删除评论   |
| GET    | `/api/v1/video/{videoId}/comments` | 获取视频评论 |


### 4.4 文件模块


| 方法     | 接口                        | 描述   |
| ------ | ------------------------- | ---- |
| POST   | `/api/v1/file/upload`     | 上传文件 |
| GET    | `/api/v1/file/{filename}` | 下载文件 |
| DELETE | `/api/v1/file/{filename}` | 删除文件 |


---

## 5. 文件存储设计

### 5.1 存储架构

使用本地文件系统存储，结构如下：

```
/data/douyin/
├── videos/          # 视频文件
│   └── {userId}/
│       └── {videoId}.mp4
├── covers/          # 封面图片
│   └── {videoId}.jpg
├── images/          # 用户图片
│   └── users/{userId}/avatar.jpg
└── temp/            # 临时文件
```

---

## 6. 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MySQL 官方文档](https://dev.mysql.com/doc/)

---

*文档版本：v2.1.0（简化版）*
*最后更新：2026年4月*