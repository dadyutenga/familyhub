package com.biglitecode.familyhub.data.repository

import com.biglitecode.familyhub.data.model.AppUsageEntry
import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeMembers(): Flow<List<FamilyMember>>
    fun observeComplaints(): Flow<List<Complaint>>
    fun observeFeedback(): Flow<List<Feedback>>
    fun observeFamilyGroup(): Flow<FamilyGroup?>
    fun observeReminders(): Flow<List<FamilyReminder>>
    fun observeAppUsage(): Flow<List<AppUsageEntry>>

    suspend fun getTasks(): List<Task>
    suspend fun getMembers(): List<FamilyMember>
    suspend fun getReminders(): List<FamilyReminder>
    suspend fun getAppUsage(): List<AppUsageEntry>
    suspend fun getTaskById(id: String): Task?
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    suspend fun addFeedback(feedback: Feedback)
    suspend fun addComplaint(complaint: Complaint)
    suspend fun updateComplaint(complaint: Complaint)
    suspend fun removeMember(memberId: String)
    suspend fun addReminder(reminder: FamilyReminder)
    suspend fun updateReminder(reminder: FamilyReminder)
    suspend fun deleteReminder(reminderId: String)
    suspend fun addAppUsageLogs(entries: List<AppUsageEntry>)
    suspend fun deleteOldAppUsageLogs(beforeDate: String)
    suspend fun restoreSessionIfPossible(): FamilyMember?
    suspend fun login(email: String, password: String): Result<FamilyMember>
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: com.biglitecode.familyhub.data.model.FamilyRole,
        createGroup: Boolean,
        groupNameOrCode: String
    ): Result<FamilyMember>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun updateFamilyGroupName(name: String)
    suspend fun updatePhoneNumber(memberId: String, phoneNumber: String)
}
