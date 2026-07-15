package com.biglitecode.familyhub.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM family_members")
    fun observeAll(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members")
    suspend fun getAll(): List<FamilyMemberEntity>

    @Query("SELECT * FROM family_members WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): FamilyMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(complaint: ComplaintEntity)

    @Update
    suspend fun update(complaint: ComplaintEntity)
}

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedback")
    fun observeAll(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(feedback: FeedbackEntity)
}
