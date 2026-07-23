package com.biglitecode.familyhub.data.model

enum class FamilyRole { PARENT, CHILD }

enum class TaskStatus { PENDING, DONE, OVERDUE }

enum class FamilyGroupOption { CREATE, JOIN }

enum class RepeatType { DAILY, WEEKLY, SPECIFIC_DAYS }

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: FamilyRole,
    val familyGroupId: String
)

data class FamilyMember(
    val id: String,
    val name: String,
    val role: FamilyRole,
    val avatarColor: String? = null,
    val phoneNumber: String = "",
    val email: String = "",
    val familyGroupId: String = ""
)

data class FamilyGroup(
    val id: String,
    val name: String,
    val createdBy: String,
    val inviteCode: String = ""
)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val assignedToName: String,
    val assignedBy: String,
    val dueDate: Long,
    val status: TaskStatus,
    val createdAt: Long,
    val rewardPoints: Int = 10
)

data class Feedback(
    val id: String,
    val taskId: String,
    val userId: String,
    val comment: String,
    val rating: Int
)

data class Complaint(
    val id: String,
    val userId: String,
    val userName: String = "",
    val subject: String,
    val description: String,
    val createdAt: Long,
    val resolved: Boolean = false
)

data class FamilyReminder(
    val id: String,
    val familyGroupId: String = "",
    val title: String,
    val reminderTime: String,       // "HH:mm" format
    val repeatType: RepeatType = RepeatType.DAILY,
    val daysOfWeek: String? = null, // comma-separated "MON,WED,FRI"
    val isActive: Boolean = true,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
