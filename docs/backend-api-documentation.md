# 抖音短视频后端 API 接口文档

## 文档信息

| 属性 | 值 |
|------|-----|
| 文档版本 | v1.0.1 |
| API 版本 | v1 |
| 基础路径 | `/api/v1` |
| 最后更新 | 2026-04-29 |

---

## 1. 通用说明

### 1.1 认证方式

除公开接口外，所有需要登录的接口需要在请求头中携带 Token：

```
Authorization: Bearer {token}
```

### 1.2 统一响应格式

所有接口响应均采用 JSON 格式：

```json
{
    "code": 200,
    "message": "success",
    "data": { ... },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

### 1.3 响应码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 / Token 失效 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如已关注、已点赞） |
| 500 | 服务器内部错误 |

### 1.4 分页参数

列表类接口支持分页：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20，最大 100 |

---

## 2. 认证模块 (Auth)

### 2.1 用户注册

**接口地址**: `POST /api/v1/auth/register`

**是否需要登录**: 否

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| username | string | 是 | 用户名 (6-20位) | "zhangsan" |
| password | string | 是 | 密码 (6-20位) | "123456" |
| nickname | string | 是 | 昵称 (2-20位) | "张三" |
| phone | string | 否 | 手机号 | "13800138000" |
| email | string | 否 | 邮箱 | "test@example.com" |

**请求示例**:

```json
{
    "username": "zhangsan",
    "password": "123456",
    "nickname": "张三",
    "phone": "13800138000",
    "email": "test@example.com"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 10001,
        "username": "zhangsan",
        "nickname": "张三",
        "token": "eyJhbGciOiJIUzI1NiJ9..."
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 400 | 参数格式错误 |
| 409 | 用户名/手机号/邮箱已存在 |

---

### 2.2 用户登录

**接口地址**: `POST /api/v1/auth/login`

**是否需要登录**: 否

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | 条件 | 用户名 (与 phone 二选一) |
| phone | string | 条件 | 手机号 (与 username 二选一) |
| password | string | 是 | 密码 |

**请求示例 1** (用户名登录):

```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

**请求示例 2** (手机号登录):

```json
{
    "phone": "13800138000",
    "password": "123456"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 10001,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://example.com/avatar.jpg",
        "token": "eyJhbGciOiJIUzI1NiJ9..."
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 400 | 参数格式错误 |
| 401 | 用户名或密码错误 |
| 403 | 账号已被禁用 |

---

### 2.3 刷新 Token

**接口地址**: `POST /api/v1/auth/refresh`

**是否需要登录**: 否

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| refreshToken | string | 是 | 刷新令牌 |

**请求示例**:

```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh..."
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.new...",
        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.new.refresh..."
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 2.4 用户登出

**接口地址**: `POST /api/v1/auth/logout`

**是否需要登录**: 是

**响应示例**:

```json
{
    "code": 200,
    "message": "登出成功",
    "data": null,
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 3. 用户模块 (User)

### 3.1 获取用户信息

**接口地址**: `GET /api/v1/users/{userId}`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 10001,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://example.com/avatar.jpg",
        "gender": 1,
        "birthday": "2000-01-01",
        "signature": "这个人很懒，什么都没写",
        "country": "中国",
        "province": "北京",
        "city": "北京市",
        "followCount": 100,
        "fansCount": 2000,
        "likeCount": 5000,
        "videoCount": 50,
        "isFollowing": true
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| gender | int | 性别: 0-未知, 1-男, 2-女 |
| isFollowing | boolean | 当前用户是否关注了该用户 (登录后可见) |

---

### 3.2 更新当前用户信息

**接口地址**: `PUT /api/v1/users/me`

**是否需要登录**: 是

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| nickname | string | 否 | 昵称 |
| avatar | string | 否 | 头像URL |
| gender | int | 否 | 性别: 0-未知, 1-男, 2-女 |
| birthday | string | 否 | 生日 (格式: yyyy-MM-dd) |
| signature | string | 否 | 个性签名 (最多500字) |
| country | string | 否 | 国家 |
| province | string | 否 | 省份 |
| city | string | 否 | 城市 |

**请求示例**:

```json
{
    "nickname": "新昵称",
    "avatar": "https://example.com/new-avatar.jpg",
    "gender": 1,
    "birthday": "2000-01-01",
    "signature": "新的个性签名",
    "country": "中国",
    "province": "上海",
    "city": "上海市"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "更新成功",
    "data": null,
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 3.3 关注用户

**接口地址**: `POST /api/v1/users/{userId}/follow`

**是否需要登录**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要关注的用户ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "关注成功",
    "data": {
        "followId": 10002
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 400 | 不能关注自己 |
| 404 | 用户不存在 |
| 409 | 已经关注过该用户 |

---

### 3.4 取消关注

**接口地址**: `DELETE /api/v1/users/{userId}/follow`

**是否需要登录**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 要取消关注的用户ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "取消关注成功",
    "data": null,
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 3.5 获取关注列表

**接口地址**: `GET /api/v1/users/{userId}/follows`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 100,
        "totalPages": 5,
        "list": [
            {
                "id": 10002,
                "username": "lisi",
                "nickname": "李四",
                "avatar": "https://example.com/avatar2.jpg",
                "signature": "我是李四",
                "followCount": 50,
                "fansCount": 100,
                "isFollowing": true
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 3.6 获取粉丝列表

**接口地址**: `GET /api/v1/users/{userId}/fans`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**: 参考 3.5 关注列表

---

### 3.7 获取用户作品列表

**接口地址**: `GET /api/v1/users/{userId}/videos`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | long | 是 | 用户ID |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 100,
        "totalPages": 5,
        "list": [
            {
                "id": 1,
                "videoId": "v1234567890abcdef",
                "author": {
                    "id": 10001,
                    "username": "zhangsan",
                    "nickname": "张三",
                    "avatar": "https://example.com/avatar.jpg",
                    "isFollowing": false
                },
                "title": "这是一个视频标题",
                "description": "视频描述内容",
                "videoUrl": "https://example.com/videos/10001/v1234567890abcdef.mp4",
                "coverUrl": "https://example.com/covers/v1234567890abcdef.jpg",
                "duration": 60,
                "width": 1080,
                "height": 1920,
                "likeCount": 1000,
                "commentCount": 50,
                "shareCount": 20,
                "collectCount": 100,
                "viewCount": 50000,
                "isLiked": true,
                "isCollected": false,
                "location": "北京",
                "topicIds": ["1", "2"],
                "createTime": "2026-04-29T10:00:00"
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 4. 视频模块 (Video)

### 4.1 获取视频流（推荐/关注）

**接口地址**: `GET /api/v1/videos/feed`

**是否需要登录**: 否

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 1000,
        "totalPages": 50,
        "list": [
            {
                "id": 1,
                "videoId": "v1234567890abcdef",
                "author": {
                    "id": 10001,
                    "username": "zhangsan",
                    "nickname": "张三",
                    "avatar": "https://example.com/avatar.jpg",
                    "isFollowing": false
                },
                "title": "这是一个视频标题",
                "description": "视频描述内容",
                "videoUrl": "https://example.com/videos/10001/v1234567890abcdef.mp4",
                "coverUrl": "https://example.com/covers/v1234567890abcdef.jpg",
                "duration": 60,
                "width": 1080,
                "height": 1920,
                "likeCount": 1000,
                "commentCount": 50,
                "shareCount": 20,
                "collectCount": 100,
                "viewCount": 50000,
                "isLiked": true,
                "isCollected": false,
                "location": "北京",
                "topicIds": ["1", "2"],
                "createTime": "2026-04-29T10:00:00"
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 4.2 获取视频详情

**接口地址**: `GET /api/v1/videos/{videoId}`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "videoId": "v1234567890abcdef",
        "author": {
            "id": 10001,
            "username": "zhangsan",
            "nickname": "张三",
            "avatar": "https://example.com/avatar.jpg",
            "signature": "这个人很懒",
            "followCount": 100,
            "fansCount": 2000,
            "isFollowing": false
        },
        "title": "这是一个视频标题",
        "description": "视频描述内容",
        "videoUrl": "https://example.com/videos/10001/v1234567890abcdef.mp4",
        "coverUrl": "https://example.com/covers/v1234567890abcdef.jpg",
        "duration": 60,
        "width": 1080,
        "height": 1920,
        "likeCount": 1000,
        "commentCount": 50,
        "shareCount": 20,
        "collectCount": 100,
        "viewCount": 50000,
        "isLiked": true,
        "isCollected": false,
        "location": "北京",
        "latitude": 39.9042,
        "longitude": 116.4074,
        "topicIds": ["1", "2"],
        "musicId": 1,
        "createTime": "2026-04-29T10:00:00"
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 404 | 视频不存在 |

---

### 4.3 上传视频

**接口地址**: `POST /api/v1/videos`

**是否需要登录**: 是

**Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 视频文件 (支持 mp4, avi, mov, webm) |
| title | string | 是 | 视频标题 (1-200字) |
| description | string | 否 | 视频描述 (最多 2000字) |
| cover | file | 否 | 自定义封面图片 |
| topicIds | string | 否 | 话题ID列表，逗号分隔 |
| location | string | 否 | 发布位置 |
| latitude | double | 否 | 纬度 |
| longitude | double | 否 | 经度 |
| atUserIds | string | 否 | @的用户ID列表，逗号分隔 |
| musicId | long | 否 | 使用的音乐ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "上传成功",
    "data": {
        "videoId": "v1234567890abcdef",
        "videoUrl": "https://example.com/videos/10001/v1234567890abcdef.mp4",
        "coverUrl": "https://example.com/covers/v1234567890abcdef.jpg",
        "duration": 60,
        "width": 1080,
        "height": 1920
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 400 | 参数错误或文件格式不支持 |
| 413 | 文件大小超过限制 (建议最大 500MB) |

---

### 4.4 点赞视频

**接口地址**: `POST /api/v1/videos/{videoId}/like`

**是否需要登录**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**响应示例**:

```json
{
    "code": 200,
    "message": "点赞成功",
    "data": {
        "videoId": "v1234567890abcdef",
        "likeCount": 1001
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**错误码**:

| code | 说明 |
|------|------|
| 404 | 视频不存在 |
| 409 | 已经点过赞 |

---

### 4.5 取消点赞

**接口地址**: `DELETE /api/v1/videos/{videoId}/like`

**是否需要登录**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**响应示例**:

```json
{
    "code": 200,
    "message": "取消点赞成功",
    "data": {
        "videoId": "v1234567890abcdef",
        "likeCount": 1000
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 4.6 分享视频

**接口地址**: `POST /api/v1/videos/{videoId}/share`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**响应示例**:

```json
{
    "code": 200,
    "message": "分享成功",
    "data": {
        "shareCount": 21
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 5. 评论模块 (Comment)

### 5.1 发布评论

**接口地址**: `POST /api/v1/videos/{videoId}/comments`

**是否需要登录**: 是

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| content | string | 是 | 评论内容 (1-500字) |
| parentId | long | 否 | 父评论ID (回复评论时必填) |

**请求示例**:

```json
{
    "content": "这个视频太棒了！",
    "parentId": null
}
```

**回复评论请求示例**:

```json
{
    "content": "同意你的观点",
    "parentId": 100
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "评论成功",
    "data": {
        "id": 1000,
        "videoId": "v1234567890abcdef",
        "userId": 10001,
        "user": {
            "id": 10001,
            "username": "zhangsan",
            "nickname": "张三",
            "avatar": "https://example.com/avatar.jpg"
        },
        "content": "这个视频太棒了！",
        "likeCount": 0,
        "replyCount": 0,
        "parentId": null,
        "rootId": null,
        "createTime": "2026-04-29T10:00:00"
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 5.2 获取视频评论列表

**接口地址**: `GET /api/v1/videos/{videoId}/comments`

**是否需要登录**: 否

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | string | 是 | 视频唯一标识 |

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 100,
        "totalPages": 5,
        "list": [
            {
                "id": 1000,
                "videoId": "v1234567890abcdef",
                "userId": 10001,
                "user": {
                    "id": 10001,
                    "username": "zhangsan",
                    "nickname": "张三",
                    "avatar": "https://example.com/avatar.jpg"
                },
                "content": "这个视频太棒了！",
                "likeCount": 50,
                "replyCount": 5,
                "parentId": null,
                "rootId": null,
                "isLiked": false,
                "createTime": "2026-04-29T10:00:00",
                "replies": [
                    {
                        "id": 1001,
                        "userId": 10002,
                        "user": {
                            "id": 10002,
                            "username": "lisi",
                            "nickname": "李四",
                            "avatar": "https://example.com/avatar2.jpg"
                        },
                        "content": "确实很棒！",
                        "likeCount": 10,
                        "parentId": 1000,
                        "rootId": 1000,
                        "isLiked": true,
                        "createTime": "2026-04-29T10:05:00"
                    }
                ]
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 6. 推荐模块 (Recommend)

### 6.1 获取推荐视频流

**接口地址**: `GET /api/v1/recommend/feed`

**是否需要登录**: 否

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例**: 参考 4.1 视频流

---

## 7. 搜索模块 (Search)

### 7.1 搜索

**接口地址**: `GET /api/v1/search`

**是否需要登录**: 否

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| type | string | 否 | 搜索类型: `video` / `user` / `all` (默认) |
| page | int | 否 | 页码，默认 1 |
| size | int | 否 | 每页条数，默认 20 |

**响应示例** (视频搜索):

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 100,
        "totalPages": 5,
        "list": [
            {
                "type": "video",
                "video": {
                    "id": 1,
                    "videoId": "v1234567890abcdef",
                    "author": { ... },
                    "title": "这是一个视频标题",
                    "description": "视频描述内容",
                    "videoUrl": "https://example.com/video.mp4",
                    "coverUrl": "https://example.com/cover.jpg",
                    "duration": 60,
                    "likeCount": 1000,
                    "commentCount": 50,
                    "viewCount": 50000,
                    "createTime": "2026-04-29T10:00:00"
                }
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

**响应示例** (用户搜索):

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "page": 1,
        "size": 20,
        "total": 10,
        "totalPages": 1,
        "list": [
            {
                "type": "user",
                "user": {
                    "id": 10001,
                    "username": "zhangsan",
                    "nickname": "张三",
                    "avatar": "https://example.com/avatar.jpg",
                    "signature": "这个人很懒",
                    "followCount": 100,
                    "fansCount": 2000,
                    "isFollowing": false
                }
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 7.2 获取热搜榜单

**接口地址**: `GET /api/v1/search/hot`

**是否需要登录**: 否

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "list": [
            {
                "id": 1,
                "keyword": "热门话题1",
                "count": 100000
            },
            {
                "id": 2,
                "keyword": "热门话题2",
                "count": 80000
            }
        ]
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 8. 文件模块 (File)

### 8.1 获取上传凭证

**接口地址**: `POST /api/v1/files/upload/token`

**是否需要登录**: 是

**请求参数** (JSON Body):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 是 | 文件名 |
| type | string | 否 | 文件类型: `video` / `image` / `avatar` / `cover` |

**请求示例**:

```json
{
    "filename": "video.mp4",
    "type": "video"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "uploadToken": "xxx",
        "uploadUrl": "https://upload.example.com",
        "fileUrl": "https://example.com/videos/xxx.mp4"
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

### 8.2 上传文件

**接口地址**: `POST /api/v1/files/upload`

**是否需要登录**: 是

**Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 文件 |
| type | string | 否 | 文件类型: `video` / `image` / `avatar` / `cover` |

**响应示例**:

```json
{
    "code": 200,
    "message": "上传成功",
    "data": {
        "filename": "abc123.jpg",
        "url": "https://example.com/images/users/10001/abc123.jpg",
        "size": 102400,
        "mimeType": "image/jpeg"
    },
    "requestId": "uuid-xxx",
    "timestamp": 1714368000000
}
```

---

## 9. 接口汇总表

### 9.1 认证模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| POST | `/api/v1/auth/register` | 用户注册 | 否 |
| POST | `/api/v1/auth/login` | 用户登录 | 否 |
| POST | `/api/v1/auth/refresh` | 刷新 Token | 否 |
| POST | `/api/v1/auth/logout` | 用户登出 | 是 |

### 9.2 用户模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| GET | `/api/v1/users/{userId}` | 获取用户信息 | 否 |
| PUT | `/api/v1/users/me` | 更新当前用户信息 | 是 |
| POST | `/api/v1/users/{userId}/follow` | 关注用户 | 是 |
| DELETE | `/api/v1/users/{userId}/follow` | 取消关注 | 是 |
| GET | `/api/v1/users/{userId}/follows` | 获取关注列表 | 否 |
| GET | `/api/v1/users/{userId}/fans` | 获取粉丝列表 | 否 |
| GET | `/api/v1/users/{userId}/videos` | 获取用户作品列表 | 否 |

### 9.3 视频模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| GET | `/api/v1/videos/feed` | 获取视频流 | 否 |
| GET | `/api/v1/videos/{videoId}` | 获取视频详情 | 否 |
| POST | `/api/v1/videos` | 上传视频 | 是 |
| POST | `/api/v1/videos/{videoId}/like` | 点赞视频 | 是 |
| DELETE | `/api/v1/videos/{videoId}/like` | 取消点赞 | 是 |
| POST | `/api/v1/videos/{videoId}/share` | 分享视频 | 否 |

### 9.4 评论模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| POST | `/api/v1/videos/{videoId}/comments` | 发布评论 | 是 |
| GET | `/api/v1/videos/{videoId}/comments` | 获取视频评论列表 | 否 |

### 9.5 推荐模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| GET | `/api/v1/recommend/feed` | 获取推荐视频流 | 否 |

### 9.6 搜索模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| GET | `/api/v1/search` | 搜索 | 否 |
| GET | `/api/v1/search/hot` | 获取热搜榜单 | 否 |

### 9.7 文件模块接口

| 方法 | 接口 | 描述 | 需要登录 |
|------|------|------|----------|
| POST | `/api/v1/files/upload/token` | 获取上传凭证 | 是 |
| POST | `/api/v1/files/upload` | 上传文件 | 是 |

---

## 附录

### A. 数据类型定义

#### UserInfo (用户信息)

```json
{
    "id": 10001,
    "username": "zhangsan",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "signature": "这个人很懒",
    "followCount": 100,
    "fansCount": 2000,
    "likeCount": 5000,
    "videoCount": 50,
    "isFollowing": false
}
```

#### VideoInfo (视频信息)

```json
{
    "id": 1,
    "videoId": "v1234567890abcdef",
    "author": { ... },
    "title": "视频标题",
    "description": "视频描述",
    "videoUrl": "https://example.com/video.mp4",
    "coverUrl": "https://example.com/cover.jpg",
    "duration": 60,
    "width": 1080,
    "height": 1920,
    "likeCount": 1000,
    "commentCount": 50,
    "shareCount": 20,
    "collectCount": 100,
    "viewCount": 50000,
    "isLiked": false,
    "isCollected": false,
    "location": "北京",
    "createTime": "2026-04-29T10:00:00"
}
```

#### CommentInfo (评论信息)

```json
{
    "id": 1000,
    "videoId": "v1234567890abcdef",
    "userId": 10001,
    "user": { ... },
    "content": "评论内容",
    "likeCount": 10,
    "replyCount": 5,
    "parentId": null,
    "rootId": null,
    "isLiked": false,
    "createTime": "2026-04-29T10:00:00",
    "replies": []
}
```

---

*文档版本：v1.0.1*
*最后更新：2026年4月*
