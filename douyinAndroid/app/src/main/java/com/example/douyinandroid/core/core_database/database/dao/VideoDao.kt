package com.example.douyinandroid.core.core_database.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.douyinandroid.core.core_database.database.entity.VideoEntity

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY createTime DESC")
    fun getAllVideos(): LiveData<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY createTime DESC")
    suspend fun getAllVideosSync(): List<VideoEntity>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getVideoByIdLive(videoId: String): LiveData<VideoEntity?>

    @Query("SELECT * FROM videos WHERE authorId = :authorId ORDER BY createTime DESC")
    fun getVideosByAuthor(authorId: Long): LiveData<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Delete
    suspend fun deleteVideo(video: VideoEntity)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("UPDATE videos SET isLiked = :isLiked, likeCount = likeCount + :delta WHERE id = :videoId")
    suspend fun updateLikeStatus(videoId: String, isLiked: Boolean, delta: Int)

    @Query("UPDATE videos SET isCollected = :isCollected WHERE id = :videoId")
    suspend fun updateCollectStatus(videoId: String, isCollected: Boolean)

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' ORDER BY createTime DESC")
    fun searchVideos(keyword: String): LiveData<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideoCount(): Int
}
