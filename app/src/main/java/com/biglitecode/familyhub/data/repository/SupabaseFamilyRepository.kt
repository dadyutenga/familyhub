package com.biglitecode.familyhub.data.repository

import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * STUB: Supabase-backed repository.
 *
 * Currently returns empty data for every query so the app shows empty states
 * instead of hardcoded demo data. Wire each method to Supabase Postgrest/Auth
 * as v2 implementation continues.
 */
class SupabaseFamilyRepository : FamilyRepository {

    private val emptyTasks = MutableStateFlow<List<Task>>(emptyList())
    private val emptyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    private val emptyComplaints = MutableStateFlow<List<Complaint>>(emptyList())
    private val emptyFeedback = MutableStateFlow<List<Feedback>>(emptyList())
    private val emptyGroup = MutableStateFlow<FamilyGroup?>(null)

    override fun observeTasks(): Flow<List<Task>> = emptyTasks.asStateFlow()
    override fun observeMembers(): Flow<List<FamilyMember>> = emptyMembers.asStateFlow()
    override fun observeComplaints(): Flow<List<Complaint>> = emptyComplaints.asStateFlow()
    override fun observeFeedback(): Flow<List<Feedback>> = emptyFeedback.asStateFlow()
    override fun observeFamilyGroup(): Flow<FamilyGroup?> = emptyGroup.asStateFlow()

    override suspend fun getTasks(): List<Task> = emptyList()
    override suspend fun getMembers(): List<FamilyMember> = emptyList()
    override suspend fun getTaskById(id: String): Task? = null

    override suspend fun addTask(task: Task) {
        // TODO(supabase): insert task into Supabase 'tasks' table
    }

    override suspend fun updateTask(task: Task) {
        // TODO(supabase): update task in Supabase 'tasks' table
    }

    override suspend fun deleteTask(taskId: String) {
        // TODO(supabase): delete task from Supabase 'tasks' table
    }

    override suspend fun addFeedback(feedback: Feedback) {
        // TODO(supabase): insert feedback into Supabase 'feedback' table
    }

    override suspend fun addComplaint(complaint: Complaint) {
        // TODO(supabase): insert complaint into Supabase 'complaints' table
    }

    override suspend fun updateComplaint(complaint: Complaint) {
        // TODO(supabase): update complaint in Supabase 'complaints' table
    }

    override suspend fun removeMember(memberId: String) {
        // TODO(supabase): remove family member from Supabase
    }

    override suspend fun login(email: String, password: String): Result<FamilyMember> {
        // TODO(supabase): replace with Supabase Auth signInWithEmail
        return Result.failure(Exception("Login not yet wired to Supabase."))
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: com.biglitecode.familyhub.data.model.FamilyRole,
        createGroup: Boolean,
        groupNameOrCode: String
    ): Result<FamilyMember> {
        // TODO(supabase): replace with Supabase Auth signUp + family_members insert
        return Result.failure(Exception("Sign up not yet wired to Supabase."))
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        // TODO(supabase): replace with Supabase Auth resetPasswordForEmail
        return Result.failure(Exception("Password reset not yet wired to Supabase."))
    }

    override suspend fun updateFamilyGroupName(name: String) {
        // TODO(supabase): update family_groups name in Supabase
    }

    companion object {
        @Volatile
        private var instance: SupabaseFamilyRepository? = null

        fun getInstance(): SupabaseFamilyRepository {
            return instance ?: synchronized(this) {
                instance ?: SupabaseFamilyRepository().also { instance = it }
            }
        }
    }
}
