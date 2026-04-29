# Douyin Backend

这是一个面向当前 Android 客户端的单体 Spring Boot 后端实现，优先保证接口可用和本地联调顺畅，后续可以再按 `docs/backend-architecture-simple.md` 继续拆成微服务。

## 技术栈

- Java 17
- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- MySQL 8.0+

## 已实现接口

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users/{userId}`
- `PUT /api/v1/users/me`
- `GET /api/v1/users/{userId}/follows`
- `GET /api/v1/users/{userId}/fans`
- `POST /api/v1/users/{userId}/follow`
- `DELETE /api/v1/users/{userId}/follow`
- `GET /api/v1/users/{userId}/videos`
- `GET /api/v1/videos/feed`
- `GET /api/v1/videos/{videoId}`
- `POST /api/v1/videos/{videoId}/like`
- `DELETE /api/v1/videos/{videoId}/like`
- `POST /api/v1/videos/{videoId}/share`
- `GET /api/v1/videos/{videoId}/comments`
- `POST /api/v1/videos/{videoId}/comments`
- `POST /api/v1/videos`
- `GET /api/v1/search`
- `GET /api/v1/search/hot`
- `GET /api/v1/recommend/feed`
- `POST /api/v1/files/upload/token`
- `POST /api/v1/files/upload`

## 本地启动

默认使用 MySQL 数据库，请确保 MySQL 服务已启动并创建好数据库。

1. 创建数据库：

```sql
CREATE DATABASE douyin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本：`src/main/resources/schema-mysql.sql`

3. 配置环境变量（可选，默认为 localhost/root/root）：

```bash
MYSQL_HOST=localhost
MYSQL_DATABASE=douyin
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
```

4. 启动服务：

```bash
mvn spring-boot:run
```

启动后默认地址：

- API: `http://192.168.31.105:8080/api/v1`

## 演示账号

首次启动后使用注册功能创建账号。

## 文件存储

上传文件会写入项目内 `storage/` 目录，并通过 `/media/**` 暴露静态访问地址。
