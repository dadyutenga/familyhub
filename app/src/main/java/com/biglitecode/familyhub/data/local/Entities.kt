package com.biglitecode.familyhub.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val avatarColor: String?,
    val phoneNumber: String,
    val email: String,
    val familyGroupId: String
) {
    fun toModel() = FamilyMember(
        id = id,
        name = name,
        role = FamilyRole.valueOf(role),
        avatarColor = avatarColor,
        phoneNumber = phoneNumber,
        email = email,
        familyGroupId = familyGroupId
    )

    companion object {
        fun from(m: FamilyMember) = FamilyMemberEntity(
            id = m.id,
            name = m.name,
            role = m.role.name,
            avatarColor = m.avatarColor,
            phoneNumber = m.phoneNumber,
            email = m.email,
            familyGroupId = m.familyGroupId
        )
    }
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val assignedToName: String,
    val assignedBy: String,
    val dueDate: Long,
    val status: String,
    val createdAt: Long,
    val rewardPoints: Int
) {
    fun toModel() = Task(
        id = id,
        title = title,
        description = description,
        assignedTo = assignedTo,
        assignedToName = assignedToName,
        assignedBy = assignedBy,
        dueDate = dueDate,
        status = TaskStatus.valueOf(status),
        createdAt = createdAt,
        rewardPoints = rewardPoints
    )

    companion object {
        fun from(t: Task) = TaskEntity(
            id = t.id,
            title = t.title,
            description = t.description,
            assignedTo = t.assignedTo,
            assignedToName = t.assignedToName,
            assignedBy = t.assignedBy,
            dueDate = t.dueDate,
            status = t.status.name,
            createdAt = t.createdAt,
            rewardPoints = t.rewardPoints
        )
    }
}

@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val userId: String,
    val comment: String,
    val rating: Int
) {
    fun toModel() = Feedback(id, taskId, userId, comment, rating)

    companion object {
        fun from(f: Feedback) = FeedbackEntity(f.id, f.taskId, f.userId, f.comment, f.rating)
    }
}

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val subject: String,
    val description: String,
    val createdAt: Long,
    val resolved: Boolean
) {
    fun toModel() = Complaint(id, userId, userName, subject, description, createdAt, resolved)

    companion object {
        fun from(c: Complaint) = ComplaintEntity(
            c.id, c.userId, c.userName, c.subject, c.description, c.createdAt, c.resolved
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val familyGroupId: String,
    val passwordHash: String = ""
)
