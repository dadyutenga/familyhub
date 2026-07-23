package com.biglitecode.familyhub.data.repository

import com.biglitecode.familyhub.core.SupabaseClientProvider
import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.remote.dto.ComplaintRow
import com.biglitecode.familyhub.data.remote.dto.FamilyGroupRow
import com.biglitecode.familyhub.data.remote.dto.FamilyMemberRow
import com.biglitecode.familyhub.data.remote.dto.FamilyReminderRow
import com.biglitecode.familyhub.data.remote.dto.FeedbackRow
import com.biglitecode.familyhub.data.remote.dto.TaskRow
import com.biglitecode.familyhub.data.remote.dto.toDomain
import com.biglitecode.familyhub.data.remote.dto.toRow
import com.biglitecode.familyhub.data.session.SessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Supabase-backed repository.
 *
 * - Auth flows (login/signUp/resetPassword) use GoTrue (Supabase Auth).
 * - Data queries use Postgrest against the tables defined in supabase/schema.sql.
 * - Local StateFlow caches keep the UI updated after writes without waiting for
 *   a full re-fetch. Replace with Realtime subscriptions for live updates.
 *
 * Prereqs on the Supabase project:
 *  1. Run `supabase/schema.sql` once in the SQL Editor.
 *  2. Enable "Email" provider in Authentication → Providers.
 *  3. For dev, disable "Confirm email" in Auth → Email Templates settings so
 *     sign-up returns a session immediately.
 */
class SupabaseFamilyRepository : FamilyRepository {

    private val client get() = SupabaseClientProvider.client

    // Local caches — populated on first observe() call and updated after writes.
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    private val _members = MutableStateFlow<List<FamilyMember>>(emptyList())
    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
    private val _feedback = MutableStateFlow<List<Feedback>>(emptyList())
    private val _group = MutableStateFlow<FamilyGroup?>(null)
    private val _reminders = MutableStateFlow<List<FamilyReminder>>(emptyList())

    // -------------------------------------------------------------------------
    // Observe — one fresh fetch per collector, then tail the local cache.
    // -------------------------------------------------------------------------

    override fun observeTasks(): Flow<List<Task>> = flow {
        runCatching { refreshTasks() }
        emitAll(_tasks.asStateFlow())
    }

    override fun observeMembers(): Flow<List<FamilyMember>> = flow {
        runCatching { refreshMembers() }
        emitAll(_members.asStateFlow())
    }

    override fun observeComplaints(): Flow<List<Complaint>> = flow {
        runCatching { refreshComplaints() }
        emitAll(_complaints.asStateFlow())
    }

    override fun observeFeedback(): Flow<List<Feedback>> = flow {
        runCatching { refreshFeedback() }
        emitAll(_feedback.asStateFlow())
    }

    override fun observeFamilyGroup(): Flow<FamilyGroup?> = flow {
        runCatching { refreshFamilyGroup() }
        emitAll(_group.asStateFlow())
    }

    override fun observeReminders(): Flow<List<FamilyReminder>> = flow {
        runCatching { refreshReminders() }
        emitAll(_reminders.asStateFlow())
    }

    // -------------------------------------------------------------------------
    // Reads — always hit Supabase so callers get fresh data.
    // -------------------------------------------------------------------------

    override suspend fun getTasks(): List<Task> = fetchTasks()
    override suspend fun getMembers(): List<FamilyMember> = fetchMembers()
    override suspend fun getReminders(): List<FamilyReminder> = fetchReminders()

    override suspend fun getTaskById(id: String): Task? {
        return client.postgrest["tasks"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<TaskRow>()
            ?.toDomain()
    }

    // -------------------------------------------------------------------------
    // Writes — insert/update on Supabase, then patch the local cache.
    // -------------------------------------------------------------------------

    override suspend fun addTask(task: Task) {
        val row = task.toRow().copy(familyGroupId = currentGroupId())
        client.postgrest["tasks"].insert(row)
        _tasks.update { it + task }
    }

    override suspend fun updateTask(task: Task) {
        val row = task.toRow()
        client.postgrest["tasks"].update(row) { filter { eq("id", row.id) } }
        _tasks.update { list -> list.map { if (it.id == task.id) task else it } }
    }

    override suspend fun deleteTask(taskId: String) {
        client.postgrest["tasks"].delete { filter { eq("id", taskId) } }
        _tasks.update { it.filterNot { t -> t.id == taskId } }
    }

    override suspend fun addFeedback(feedback: Feedback) {
        client.postgrest["feedback"].insert(feedback.toRow())
        _feedback.update { it + feedback }
    }

    override suspend fun addComplaint(complaint: Complaint) {
        val row = complaint.toRow().copy(familyGroupId = currentGroupId())
        client.postgrest["complaints"].insert(row)
        _complaints.update { it + complaint }
    }

    override suspend fun updateComplaint(complaint: Complaint) {
        val row = complaint.toRow()
        client.postgrest["complaints"].update(row) { filter { eq("id", row.id) } }
        _complaints.update { list -> list.map { if (it.id == complaint.id) complaint else it } }
    }

    override suspend fun removeMember(memberId: String) {
        client.postgrest["family_members"].delete { filter { eq("id", memberId) } }
        _members.update { it.filterNot { m -> m.id == memberId } }
    }

    override suspend fun updateFamilyGroupName(name: String) {
        val groupId = currentGroupId()
        client.postgrest["family_groups"].update(
            mapOf("name" to name)
        ) { filter { eq("id", groupId) } }
        _group.update { it?.copy(name = name) }
    }

    override suspend fun updatePhoneNumber(memberId: String, phoneNumber: String) {
        client.postgrest["family_members"].update(
            mapOf("phone_number" to phoneNumber)
        ) { filter { eq("id", memberId) } }
        _members.update { list ->
            list.map { if (it.id == memberId) it.copy(phoneNumber = phoneNumber) else it }
        }
    }

    override suspend fun addReminder(reminder: FamilyReminder) {
        val row = reminder.toRow().copy(familyGroupId = currentGroupId())
        client.postgrest["family_reminders"].insert(row)
        _reminders.update { it + reminder.copy(familyGroupId = currentGroupId()) }
    }

    override suspend fun updateReminder(reminder: FamilyReminder) {
        val row = reminder.toRow()
        client.postgrest["family_reminders"].update(row) { filter { eq("id", row.id) } }
        _reminders.update { list -> list.map { if (it.id == reminder.id) reminder else it } }
    }

    override suspend fun deleteReminder(reminderId: String) {
        client.postgrest["family_reminders"].delete { filter { eq("id", reminderId) } }
        _reminders.update { it.filterNot { r -> r.id == reminderId } }
    }

    // -------------------------------------------------------------------------
    // Auth — GoTrue
    // -------------------------------------------------------------------------

    override suspend fun login(email: String, password: String): Result<FamilyMember> =
        runCatching {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val member = fetchCurrentMemberOrThrow()
            SessionManager.setUser(member)
            // Warm caches now that we have a user.
            runCatching { refreshTasks() }
            runCatching { refreshMembers() }
            runCatching { refreshComplaints() }
            runCatching { refreshFeedback() }
            runCatching { refreshFamilyGroup() }
            runCatching { refreshReminders() }
            member
        }.recoverCatching { e ->
            throw mapAuthError(e, fallback = "Login failed. Check your email and password.")
        }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: FamilyRole,
        createGroup: Boolean,
        groupNameOrCode: String
    ): Result<FamilyMember> = runCatching {
        android.util.Log.d("SupabaseRepo", "Starting sign-up: name=$name, email=$email, role=$role, createGroup=$createGroup")
        
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        android.util.Log.d("SupabaseRepo", "Auth user created successfully")
        
        val authUserId = client.auth.currentUserOrNull()?.id?.toString()
            ?: error("Sign-up succeeded but no session was returned. " +
                "If email confirmation is enabled, disable it in Supabase Auth settings.")
        android.util.Log.d("SupabaseRepo", "Auth user ID: $authUserId")

        val memberId = "m_${UUID.randomUUID().toString().take(8)}"
        android.util.Log.d("SupabaseRepo", "Generated member ID: $memberId")
        
        // Step 1: Resolve or create the family group.
        // When creating a new group, use a placeholder created_by (updated after member insert).
        val groupId = if (createGroup) {
            val newGroupId = "g_${UUID.randomUUID().toString().take(8)}"
            val inviteCode = generateInviteCode()
            android.util.Log.d("SupabaseRepo", "Creating family group: id=$newGroupId, name=$groupNameOrCode, inviteCode=$inviteCode")
            try {
                client.postgrest["family_groups"].insert(
                    FamilyGroupRow(
                        id = newGroupId,
                        name = groupNameOrCode.ifBlank { "$name's Family" },
                        createdBy = memberId,  // will be valid once member is inserted
                        inviteCode = inviteCode
                    )
                )
                android.util.Log.d("SupabaseRepo", "Family group created successfully")
            } catch (e: Exception) {
                android.util.Log.e("SupabaseRepo", "Failed to create family group: ${e.message}", e)
                throw Exception("Failed to create family group: ${e.message}")
            }
            newGroupId
        } else {
            android.util.Log.d("SupabaseRepo", "Joining existing group with code: $groupNameOrCode")
            val group = client.postgrest["family_groups"]
                .select { filter { eq("invite_code", groupNameOrCode.trim()) } }
                .decodeSingleOrNull<FamilyGroupRow>()
                ?: error("No family found with that invite code.")
            android.util.Log.d("SupabaseRepo", "Found group: ${group.id}")
            group.id
        }

        // Step 2: Insert the family member (must happen after group exists for FK).
        val memberRow = FamilyMemberRow(
            id = memberId,
            userId = authUserId,
            name = name,
            role = role.name,
            email = email,
            familyGroupId = groupId
        )
        android.util.Log.d("SupabaseRepo", "Inserting family_member: id=$memberId, userId=$authUserId, name=$name, role=${role.name}, familyGroupId=$groupId")
        try {
            client.postgrest["family_members"].insert(memberRow)
            android.util.Log.d("SupabaseRepo", "Family member inserted successfully")
        } catch (e: Exception) {
            android.util.Log.e("SupabaseRepo", "Failed to insert family member: ${e.message}", e)
            throw Exception("Failed to create family member: ${e.message}")
        }

        val member = memberRow.toDomain()
        android.util.Log.d("SupabaseRepo", "Sign-up complete. Member: id=${member.id}, name=${member.name}, role=${member.role}")
        SessionManager.setUser(member)
        member
    }.recoverCatching { e ->
        android.util.Log.e("SupabaseRepo", "Sign-up failed: ${e.message}", e)
        throw mapAuthError(e, fallback = "Sign up failed. ${e.message.orEmpty()}")
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            client.auth.resetPasswordForEmail(email)
        }.recoverCatching { e ->
            throw mapAuthError(e, fallback = "Could not send reset email. ${e.message.orEmpty()}")
        }

    // -------------------------------------------------------------------------
    // Internal refresh helpers
    // -------------------------------------------------------------------------

    private suspend fun refreshTasks() {
        _tasks.value = fetchTasks()
    }

    private suspend fun refreshMembers() {
        _members.value = fetchMembers()
    }

    private suspend fun refreshComplaints() {
        _complaints.value = fetchComplaints()
    }

    private suspend fun refreshFeedback() {
        _feedback.value = fetchFeedback()
    }

    private suspend fun refreshFamilyGroup() {
        _group.value = fetchFamilyGroup()
    }

    private suspend fun refreshReminders() {
        _reminders.value = fetchReminders()
    }

    private suspend fun fetchTasks(): List<Task> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        return client.postgrest["tasks"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<TaskRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchMembers(): List<FamilyMember> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        return client.postgrest["family_members"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<FamilyMemberRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchComplaints(): List<Complaint> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        return client.postgrest["complaints"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<ComplaintRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchFeedback(): List<Feedback> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        // feedback has no family_group_id; scope through tasks.
        val taskIds = client.postgrest["tasks"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<TaskRow>()
            .map { it.id }
        if (taskIds.isEmpty()) return emptyList()
        return client.postgrest["feedback"]
            .select { filter { isIn("task_id", taskIds) } }
            .decodeList<FeedbackRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchFamilyGroup(): FamilyGroup? {
        val groupId = currentGroupIdOrNull() ?: return null
        return client.postgrest["family_groups"]
            .select { filter { eq("id", groupId) } }
            .decodeSingleOrNull<FamilyGroupRow>()
            ?.toDomain()
    }

    private suspend fun fetchReminders(): List<FamilyReminder> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        return client.postgrest["family_reminders"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<FamilyReminderRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchCurrentMemberOrThrow(): FamilyMember {
        val authUserId = client.auth.currentUserOrNull()?.id?.toString()
            ?: error("Not authenticated.")
        android.util.Log.d("SupabaseRepo", "Fetching family_member for auth user: $authUserId")
        val row = client.postgrest["family_members"]
            .select { filter { eq("user_id", authUserId) } }
            .decodeSingleOrNull<FamilyMemberRow>()
        if (row == null) {
            android.util.Log.e("SupabaseRepo", "No family_member row found for user_id: $authUserId")
            error("No family member record for this account. Ask a parent to add you.")
        }
        android.util.Log.d("SupabaseRepo", "Found family_member: id=${row.id}, name=${row.name}, role=${row.role}, familyGroupId=${row.familyGroupId}")
        return row.toDomain()
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun currentGroupId(): String =
        SessionManager.currentUser.value?.familyGroupId
            ?: error("No signed-in user. Call login() first.")

    private fun currentGroupIdOrNull(): String? =
        SessionManager.currentUser.value?.familyGroupId

    private fun generateInviteCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun mapAuthError(e: Throwable, fallback: String): Exception {
        val message = when (e) {
            is HttpRequestException -> "Network error. Check your connection."
            else -> e.message ?: fallback
        }
        return Exception(message)
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
