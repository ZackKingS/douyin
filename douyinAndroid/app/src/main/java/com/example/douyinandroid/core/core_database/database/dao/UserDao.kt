package com.example.douyinandroid.core.core_database.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.douyinandroid.core.core_database.database.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdLive(userId: Long): LiveData<UserEntity?>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    suspend fun getUsersByIds(userIds: List<Long>): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)

    @Query("UPDATE users SET isFollowing = :isFollowing WHERE id = :userId")
    suspend fun updateFollowStatus(userId: Long, isFollowing: Boolean)

    @Query("SELECT * FROM users WHERE nickname LIKE '%' || :keyword || '%' ORDER BY fansCount DESC")
    fun searchUsers(keyword: String): LiveData<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
