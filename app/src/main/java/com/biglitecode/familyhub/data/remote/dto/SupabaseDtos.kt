package com.biglitecode.familyhub.data.remote.dto

import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// Supabase row DTOs (snake_case column names ↔ Kotlin camelCase)
// =============================================================================

@Serializable
data class FamilyMemberRow(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val role: String,
    @SerialName("avatar_color") val avatarColor: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = "",
    val email: String,
    @SerialName("family_group_id") val familyGroupId: String
)

@Serializable
data class FamilyGroupRow(
    val id: String,
    val name: String,
    @SerialName("created_by") val createdBy: String,
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("created_at") val createdAt: Long? = null
)

@Serializable
data class TaskRow(
    val id: String,
    val title: String,
    val description: String = "",
    @SerialName("assigned_to") val assignedTo: String,
    @SerialName("assigned_to_name") val assignedToName: String,
    @SerialName("assigned_by") val assignedBy: String,
    @SerialName("due_date") val dueDate: Long,
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("reward_points") val rewardPoints: Int = 10,
    @SerialName("family_group_id") val familyGroupId: String? = null
)

@Serializable
data class FeedbackRow(
    val id: String,
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    val comment: String = "",
    val rating: Int,
    @SerialName("created_at") val createdAt: Long? = null
)

@Serializable
data class ComplaintRow(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String,
    val subject: String,
    val description: String,
    @SerialName("created_at") val createdAt: Long? = null,
    val resolved: Boolean = false,
    @SerialName("family_group_id") val familyGroupId: String? = null
)

// =============================================================================
// DTO → Domain mappers
// =============================================================================

fun FamilyMemberRow.toDomain(): FamilyMember = FamilyMember(
    id = id,
    name = name,
    role = runCatching { FamilyRole.valueOf(role.trim().uppercase()) }
        .getOrElse { 
            // Debug: log the actual value from DB if it fails
            android.util.Log.w("SupabaseDtos", "Invalid role from DB: '$role', defaulting to CHILD")
            FamilyRole.CHILD 
        },
    avatarColor = avatarColor,
    phoneNumber = phoneNumber ?: "",
    email = email,
    familyGroupId = familyGroupId
)

fun FamilyGroupRow.toDomain(): FamilyGroup = FamilyGroup(
    id = id,
    name = name,
    createdBy = createdBy,
    inviteCode = inviteCode
)

fun TaskRow.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    assignedTo = assignedTo,
    assignedToName = assignedToName,
    assignedBy = assignedBy,
    dueDate = dueDate,
    status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
    createdAt = createdAt ?: System.currentTimeMillis(),
    rewardPoints = rewardPoints
)

fun FeedbackRow.toDomain(): Feedback = Feedback(
    id = id,
    taskId = taskId,
    userId = userId,
    comment = comment,
    rating = rating
)

fun ComplaintRow.toDomain(): Complaint = Complaint(
    id = id,
    userId = userId,
    userName = userName,
    subject = subject,
    description = description,
    createdAt = createdAt ?: System.currentTimeMillis(),
    resolved = resolved
)

// =============================================================================
// Domain → DTO mappers (for inserts/updates)
// =============================================================================

fun Task.toRow(): TaskRow = TaskRow(
    id = id,
    title = title,
    description = description,
    assignedTo = assignedTo,
    assignedToName = assignedToName,
    assignedBy = assignedBy,
    dueDate = dueDate,
    status = status.name,
    createdAt = createdAt,
    rewardPoints = rewardPoints
)

fun Feedback.toRow(): FeedbackRow = FeedbackRow(
    id = id,
    taskId = taskId,
    userId = userId,
    comment = comment,
    rating = rating
)

fun Complaint.toRow(): ComplaintRow = ComplaintRow(
    id = id,
    userId = userId,
    userName = userName,
    subject = subject,
    description = description,
    createdAt = createdAt,
    resolved = resolved
)
