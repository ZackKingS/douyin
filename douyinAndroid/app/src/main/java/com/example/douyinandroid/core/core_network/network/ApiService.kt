package com.example.douyinandroid.core.core_network.network

import com.example.douyinandroid.core.core_network.network.bean.*
import retrofit2.http.*

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

    @DELETE(ApiConstants.Endpoints.VIDEO_DETAIL)
    suspend fun deleteVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<Unit>

    @POST(ApiConstants.Endpoints.VIDEO_LIKE)
    suspend fun likeVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoActionResponse>

    @DELETE(ApiConstants.Endpoints.VIDEO_UNLIKE)
    suspend fun unlikeVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoActionResponse>

    @POST(ApiConstants.Endpoints.VIDEO_COLLECT)
    suspend fun collectVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<VideoActionResponse>

    @DELETE(ApiConstants.Endpoints.VIDEO_UNCOLLECT)
    suspend fun uncollectVideo(
        @Path("videoId") videoId: String
    ): ApiResponse<Unit>

    @GET(ApiConstants.Endpoints.VIDEO_MY_LIKES)
    suspend fun getMyLikes(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<VideoListPageResponse>

    @GET(ApiConstants.Endpoints.VIDEO_MY_COLLECTS)
    suspend fun getMyCollects(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<VideoListPageResponse>

    @GET(ApiConstants.Endpoints.VIDEO_USER)
    suspend fun getUserVideos(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<VideoListPageResponse>

    @GET(ApiConstants.Endpoints.VIDEO_COMMENTS)
    suspend fun getVideoComments(
        @Path("videoId") videoId: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<CommentListPageResponse>

    // ==================== 用户相关 ====================

    @POST(ApiConstants.Endpoints.USER_REGISTER)
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): ApiResponse<LoginResponse>

    @POST(ApiConstants.Endpoints.USER_LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): ApiResponse<LoginResponse>

    @POST(ApiConstants.Endpoints.USER_REFRESH)
    suspend fun refreshToken(
        @Body refreshRequest: RefreshTokenRequest
    ): ApiResponse<TokenResponse>

    @POST(ApiConstants.Endpoints.USER_LOGOUT)
    suspend fun logout(): ApiResponse<Unit>

    @GET(ApiConstants.Endpoints.USER_INFO)
    suspend fun getUserInfo(
        @Path("userId") userId: Long
    ): ApiResponse<UserInfoResponse>

    @PUT(ApiConstants.Endpoints.USER_UPDATE)
    suspend fun updateUserInfo(
        @Body updateRequest: UserUpdateRequest
    ): ApiResponse<Unit>

    @GET(ApiConstants.Endpoints.USER_FOLLOWERS)
    suspend fun getUserFollowers(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<UserListPageResponse>

    @GET(ApiConstants.Endpoints.USER_FOLLOWING)
    suspend fun getUserFollowing(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<UserListPageResponse>

    @POST(ApiConstants.Endpoints.USER_FOLLOW)
    suspend fun followUser(
        @Path("userId") userId: Long
    ): ApiResponse<FollowActionResponse>

    @DELETE(ApiConstants.Endpoints.USER_UNFOLLOW)
    suspend fun unfollowUser(
        @Path("userId") userId: Long
    ): ApiResponse<Unit>

    // ==================== 评论相关 ====================

    @POST(ApiConstants.Endpoints.COMMENT_CREATE)
    suspend fun createComment(
        @Body comment: CommentCreateRequest
    ): ApiResponse<CommentResponse>

    @DELETE(ApiConstants.Endpoints.COMMENT_DELETE)
    suspend fun deleteComment(
        @Path("commentId") commentId: Long
    ): ApiResponse<Unit>

    @POST(ApiConstants.Endpoints.COMMENT_LIKE)
    suspend fun likeComment(
        @Path("commentId") commentId: Long
    ): ApiResponse<CommentActionResponse>

    @DELETE(ApiConstants.Endpoints.COMMENT_UNLIKE)
    suspend fun unlikeComment(
        @Path("commentId") commentId: Long
    ): ApiResponse<CommentActionResponse>

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

    @Multipart
    @POST(ApiConstants.Endpoints.FILE_UPLOAD)
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: okhttp3.RequestBody
    ): ApiResponse<FileUploadResponse>
}
