package com.picmewall.picmewall.dao

import androidx.room.*
import com.picmewall.picmewall.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addUser(user: User): Long

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT COUNT(id) FROM users")
    fun getTotalNoOfUsers(): Flow<Int>
}