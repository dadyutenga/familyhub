package com.biglitecode.familyhub.data.repository

import com.biglitecode.familyhub.core.SupabaseClientProvider
import com.biglitecode.familyhub.data.model.AppUsageEntry
import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.remote.dto.AppUsageRow
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
    private val _appUsage = MutableStateFlow<List<AppUsageEntry>>(emptyList())

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

    override fun observeAppUsage(): Flow<List<AppUsageEntry>> = flow {
        runCatching { refreshAppUsage() }
        emitAll(_appUsage.asStateFlow())
    }

    // -------------------------------------------------------------------------
    // Reads — always hit Supabase so callers get fresh data.
    // -------------------------------------------------------------------------

    override suspend fun getTasks(): List<Task> = fetchTasks()
    override suspend fun getMembers(): List<FamilyMember> = fetchMembers()
    override suspend fun getReminders(): List<FamilyReminder> = fetchReminders()
    override suspend fun getAppUsage(): List<AppUsageEntry> = fetchAppUsage()

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
        val groupId = currentGroupIdOrNull()
            ?: error("Cannot create reminder: no family linked to this account. Please sign up again.")
        if (groupId.isBlank()) {
            error("Cannot create reminder: family group ID is missing. Please sign up again.")
        }
        val row = reminder.toRow().copy(familyGroupId = groupId)
        client.postgrest["family_reminders"].insert(row)
        _reminders.update { it + reminder.copy(familyGroupId = groupId) }
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

    override suspend fun addAppUsageLogs(entries: List<AppUsageEntry>) {
        if (entries.isEmpty()) return
        val rows = entries.map { it.toRow().copy(familyGroupId = currentGroupId()) }
        client.postgrest["app_usage_logs"].insert(rows)
        _appUsage.update { it + entries }
    }

    override suspend fun deleteOldAppUsageLogs(beforeDate: String) {
        client.postgrest["app_usage_logs"].delete { filter { lt("date", beforeDate) } }
        _appUsage.update { it.filterNot { entry -> entry.date < beforeDate } }
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
            runCatching { refreshAppUsage() }
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

        // ── Step 1: Create auth session ──────────────────────────────────
        // If the user already exists (from a previous failed attempt), fall
        // through to sign-in so the flow can resume instead of hard-failing.
        val authUserId: String = try {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            resolveSessionOrSignIn(email, password)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            val alreadyExists = msg.contains("already", ignoreCase = true) ||
                msg.contains("registered", ignoreCase = true) ||
                msg.contains("exists", ignoreCase = true)
            if (alreadyExists) {
                android.util.Log.w("SupabaseRepo", "Auth user already exists — resuming via sign-in", e)
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                resolveSessionOrSignIn(email, password)
            } else {
                throw e
            }
        }
        android.util.Log.d("SupabaseRepo", "Auth user ID: $authUserId")

        // ── Step 2: Check for leftover family_member (previous partial attempt) ──
        val existingRow = try {
            client.postgrest["family_members"]
                .select { filter { eq("user_id", authUserId) } }
                .decodeSingleOrNull<FamilyMemberRow>()
        } catch (e: Exception) {
            android.util.Log.w("SupabaseRepo", "Could not check for existing member row", e)
            null
        }
        if (existingRow != null) {
            android.util.Log.d("SupabaseRepo", "Found existing family_member from previous attempt: ${existingRow.id}")
            val member = existingRow.toDomain()
            SessionManager.setUser(member)
            warmCaches()
            return@runCatching member
        }

        // ── Step 3: Resolve or create the family group ───────────────────
        val memberId = "m_${UUID.randomUUID().toString().take(8)}"
        val groupId = if (createGroup) {
            val newGroupId = "g_${UUID.randomUUID().toString().take(8)}"
            val inviteCode = generateInviteCode()
            android.util.Log.d("SupabaseRepo", "Creating family group: id=$newGroupId, name=$groupNameOrCode, inviteCode=$inviteCode")
            try {
                client.postgrest["family_groups"].insert(
                    FamilyGroupRow(
                        id = newGroupId,
                        name = groupNameOrCode.ifBlank { "$name's Family" },
                        createdBy = memberId,
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
            android.util.Log.d("SupabaseRepo", "Joining existing group by name: $groupNameOrCode")
            val groups = client.postgrest.rpc(
                "lookup_family_by_name",
                buildJsonObject { put("family_name", groupNameOrCode.trim()) }
            ).decodeList<FamilyGroupRow>()
            val group = groups.firstOrNull()
                ?: error("No family found with the name \"$groupNameOrCode\". Check the name and try again.")
            android.util.Log.d("SupabaseRepo", "Found group: ${group.id} (${group.name})")
            group.id
        }

        // ── Step 4: Insert the family member ─────────────────────────────
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
            // Sign out so user can retry cleanly with the same email
            runCatching { client.auth.signOut() }
            throw Exception(
                "Could not link your account to the family. This may be a temporary server issue — " +
                    "please try again. (${e.message})"
            )
        }

        val member = memberRow.toDomain()
        android.util.Log.d("SupabaseRepo", "Sign-up complete. Member: id=${member.id}, name=${member.name}, role=${member.role}")
        SessionManager.setUser(member)
        warmCaches()
        member
    }.recoverCatching { e ->
        android.util.Log.e("SupabaseRepo", "Sign-up failed: ${e.message}", e)
        throw mapAuthError(e, fallback = "Sign up failed. ${e.message.orEmpty()}")
    }

    /**
     * Restore session from Supabase Auth on cold start.
     *
     * - If a valid Auth session exists AND the user has a family_member row → restore.
     * - If Auth session exists but no family_member row → orphaned account, sign out.
     * - If no Auth session → nothing to restore.
     */
    override suspend fun restoreSessionIfPossible(): FamilyMember? {
        val userId = try {
            client.auth.currentUserOrNull()?.id?.toString()
        } catch (e: Exception) {
            android.util.Log.w("SupabaseRepo", "restoreSession: error checking auth state", e)
            null
        }
        if (userId == null) {
            android.util.Log.d("SupabaseRepo", "restoreSession: no auth session found")
            return null
        }

        // Auth session exists — try to fetch the family_member row
        return try {
            val row = client.postgrest["family_members"]
                .select { filter { eq("user_id", userId) } }
                .decodeSingleOrNull<FamilyMemberRow>()

            if (row != null) {
                val member = row.toDomain()
                SessionManager.setUser(member)
                android.util.Log.d("SupabaseRepo", "restoreSession: restored member ${member.id} (${member.name})")
                member
            } else {
                // Authenticated but no family_member row — orphaned account from a
                // previous partial sign-up. Sign out so the user can re-sign-up cleanly.
                android.util.Log.w("SupabaseRepo", "restoreSession: auth user $userId has no family_member row — orphaned")
                runCatching { client.auth.signOut() }
                null
            }
        } catch (e: Exception) {
            // Network error or other issue — can't restore right now
            android.util.Log.e("SupabaseRepo", "restoreSession: failed to fetch member", e)
            null
        }
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> =
        runCatching {
            client.auth.resetPasswordForEmail(email)
        }.recoverCatching { e ->
            throw mapAuthError(e, fallback = "Could not send reset email. ${e.message.orEmpty()}")
        }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * After `signUpWith`, verify we actually have a session. If email confirmation
     * is enabled (or the client didn't auto-sign-in), fall back to explicit sign-in.
     */
    private suspend fun resolveSessionOrSignIn(email: String, password: String): String {
        val id = client.auth.currentUserOrNull()?.id?.toString()
        if (id != null) return id

        // No session after sign-up — try explicit sign-in
        android.util.Log.w("SupabaseRepo", "No session after signUpWith — attempting explicit signIn")
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return client.auth.currentUserOrNull()?.id?.toString()
            ?: error(
                "Sign-up succeeded but no session could be established. " +
                    "If email confirmation is enabled in Supabase, disable it in " +
                    "Auth → Providers → Email settings, then try again."
            )
    }

    private suspend fun warmCaches() {
        runCatching { refreshTasks() }
        runCatching { refreshMembers() }
        runCatching { refreshComplaints() }
        runCatching { refreshFeedback() }
        runCatching { refreshFamilyGroup() }
        runCatching { refreshReminders() }
        runCatching { refreshAppUsage() }
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

    private suspend fun refreshAppUsage() {
        _appUsage.value = fetchAppUsage()
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

    private suspend fun fetchAppUsage(): List<AppUsageEntry> {
        val groupId = currentGroupIdOrNull() ?: return emptyList()
        return client.postgrest["app_usage_logs"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<AppUsageRow>()
            .map { it.toDomain() }
    }

    private suspend fun fetchCurrentMemberOrThrow(): FamilyMember {
        val authUserId = client.auth.currentUserOrNull()?.id?.toString()
            ?: error("Not authenticated. Please log in.")
        android.util.Log.d("SupabaseRepo", "Fetching family_member for auth user: $authUserId")
        val row = client.postgrest["family_members"]
            .select { filter { eq("user_id", authUserId) } }
            .decodeSingleOrNull<FamilyMemberRow>()
        if (row == null) {
            android.util.Log.e("SupabaseRepo", "No family_member row found for user_id: $authUserId")
            error(
                "Your account exists but is not linked to any family. " +
                    "This can happen if sign-up was interrupted. " +
                    "Please sign up again — your existing credentials will be reused."
            )
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
