package com.biglitecode.familyhub.data.repository

import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.TimeUnit

class FakeTaskRepository : FamilyRepository {

    private val dayMs = TimeUnit.DAYS.toMillis(1)
    private val now = System.currentTimeMillis()

    private val members = mutableListOf(
        FamilyMember(
            id = "m1",
            name = "Amina Mwangi",
            role = FamilyRole.PARENT,
            avatarColor = "0xFF2F6B44",
            phoneNumber = "0712345678",
            email = "amina@familyhub.test",
            familyGroupId = "fg1"
        ),
        FamilyMember(
            id = "m2",
            name = "James Ochieng",
            role = FamilyRole.PARENT,
            avatarColor = "0xFFF5C242",
            phoneNumber = "0723456789",
            email = "james@familyhub.test",
            familyGroupId = "fg1"
        ),
        FamilyMember(
            id = "m3",
            name = "Wanjiku",
            role = FamilyRole.CHILD,
            avatarColor = "0xFFE05C5C",
            phoneNumber = "0734567890",
            email = "wanjiku@familyhub.test",
            familyGroupId = "fg1"
        ),
        FamilyMember(
            id = "m4",
            name = "Brian",
            role = FamilyRole.CHILD,
            avatarColor = "0xFF5C8AE0",
            phoneNumber = "0745678901",
            email = "brian@familyhub.test",
            familyGroupId = "fg1"
        ),
        FamilyMember(
            id = "m5",
            name = "Faith",
            role = FamilyRole.CHILD,
            avatarColor = "0xFF9B59B6",
            phoneNumber = "0756789012",
            email = "faith@familyhub.test",
            familyGroupId = "fg1"
        )
    )

    private val passwords = mutableMapOf(
        "amina@familyhub.test" to "parent123",
        "james@familyhub.test" to "parent123",
        "wanjiku@familyhub.test" to "child123",
        "brian@familyhub.test" to "child123",
        "faith@familyhub.test" to "child123"
    )

    private val tasks = mutableListOf(
        Task(
            id = "t1",
            title = "Wash the dishes",
            description = "Clean all plates and utensils after dinner.",
            assignedTo = "m3",
            assignedToName = "Wanjiku",
            assignedBy = "m1",
            dueDate = now + dayMs,
            status = TaskStatus.PENDING,
            createdAt = now - dayMs,
            rewardPoints = 15
        ),
        Task(
            id = "t2",
            title = "Homework – Math",
            description = "Complete chapter 4 exercises.",
            assignedTo = "m4",
            assignedToName = "Brian",
            assignedBy = "m1",
            dueDate = now + 2 * dayMs,
            status = TaskStatus.PENDING,
            createdAt = now - dayMs / 2,
            rewardPoints = 20
        ),
        Task(
            id = "t3",
            title = "Water the garden",
            description = "Water vegetables and flower beds.",
            assignedTo = "m5",
            assignedToName = "Faith",
            assignedBy = "m2",
            dueDate = now - dayMs,
            status = TaskStatus.OVERDUE,
            createdAt = now - 3 * dayMs,
            rewardPoints = 10
        ),
        Task(
            id = "t4",
            title = "Take out trash",
            description = "Empty bins and put bags outside.",
            assignedTo = "m3",
            assignedToName = "Wanjiku",
            assignedBy = "m1",
            dueDate = now - 2 * dayMs,
            status = TaskStatus.DONE,
            createdAt = now - 4 * dayMs,
            rewardPoints = 10
        ),
        Task(
            id = "t5",
            title = "Clean room",
            description = "Make bed, organize desk, vacuum floor.",
            assignedTo = "m4",
            assignedToName = "Brian",
            assignedBy = "m2",
            dueDate = now - dayMs,
            status = TaskStatus.DONE,
            createdAt = now - 5 * dayMs,
            rewardPoints = 15
        ),
        Task(
            id = "t6",
            title = "Set the dinner table",
            description = "Plates, cups, and cutlery for 5 people.",
            assignedTo = "m5",
            assignedToName = "Faith",
            assignedBy = "m1",
            dueDate = now + dayMs / 2,
            status = TaskStatus.PENDING,
            createdAt = now,
            rewardPoints = 10
        ),
        Task(
            id = "t7",
            title = "Feed the dog",
            description = "Morning and evening portions.",
            assignedTo = "m3",
            assignedToName = "Wanjiku",
            assignedBy = "m2",
            dueDate = now + 3 * dayMs,
            status = TaskStatus.PENDING,
            createdAt = now,
            rewardPoints = 12
        ),
        Task(
            id = "t8",
            title = "Practice piano",
            description = "30 minutes of scales and one song.",
            assignedTo = "m4",
            assignedToName = "Brian",
            assignedBy = "m1",
            dueDate = now - 3 * dayMs,
            status = TaskStatus.DONE,
            createdAt = now - 6 * dayMs,
            rewardPoints = 25
        )
    )

    private val feedbackList = mutableListOf<Feedback>()
    private val complaints = mutableListOf(
        Complaint(
            id = "c1",
            userId = "m3",
            userName = "Wanjiku",
            subject = "Too many chores",
            description = "I feel overloaded with tasks this week.",
            createdAt = now - 2 * dayMs,
            resolved = false
        ),
        Complaint(
            id = "c2",
            userId = "m4",
            userName = "Brian",
            subject = "Wi‑Fi password changed",
            description = "Cannot finish online homework without Wi‑Fi.",
            createdAt = now - dayMs,
            resolved = true
        )
    )

    private var familyGroup = FamilyGroup(
        id = "fg1",
        name = "Mwangi Family",
        createdBy = "m1",
        inviteCode = "MWNG2026"
    )

    private val _tasksFlow = MutableStateFlow(tasks.toList())
    private val _membersFlow = MutableStateFlow(members.toList())
    private val _complaintsFlow = MutableStateFlow(complaints.toList())
    private val _feedbackFlow = MutableStateFlow(feedbackList.toList())
    private val _groupFlow = MutableStateFlow<FamilyGroup?>(familyGroup)

    override fun observeTasks(): Flow<List<Task>> = _tasksFlow.asStateFlow()
    override fun observeMembers(): Flow<List<FamilyMember>> = _membersFlow.asStateFlow()
    override fun observeComplaints(): Flow<List<Complaint>> = _complaintsFlow.asStateFlow()
    override fun observeFeedback(): Flow<List<Feedback>> = _feedbackFlow.asStateFlow()
    override fun observeFamilyGroup(): Flow<FamilyGroup?> = _groupFlow.asStateFlow()

    override suspend fun getTasks(): List<Task> = tasks.toList()
    override suspend fun getMembers(): List<FamilyMember> = members.toList()
    override suspend fun getTaskById(id: String): Task? = tasks.find { it.id == id }

    override suspend fun addTask(task: Task) {
        tasks.add(0, task)
        _tasksFlow.value = tasks.toList()
    }

    override suspend fun updateTask(task: Task) {
        val idx = tasks.indexOfFirst { it.id == task.id }
        if (idx >= 0) {
            tasks[idx] = task
            _tasksFlow.value = tasks.toList()
        }
    }

    override suspend fun deleteTask(taskId: String) {
        tasks.removeAll { it.id == taskId }
        _tasksFlow.value = tasks.toList()
    }

    override suspend fun addFeedback(feedback: Feedback) {
        feedbackList.removeAll { it.taskId == feedback.taskId && it.userId == feedback.userId }
        feedbackList.add(feedback)
        _feedbackFlow.value = feedbackList.toList()
    }

    override suspend fun addComplaint(complaint: Complaint) {
        complaints.add(0, complaint)
        _complaintsFlow.value = complaints.toList()
    }

    override suspend fun updateComplaint(complaint: Complaint) {
        val idx = complaints.indexOfFirst { it.id == complaint.id }
        if (idx >= 0) {
            complaints[idx] = complaint
            _complaintsFlow.value = complaints.toList()
        }
    }

    override suspend fun removeMember(memberId: String) {
        members.removeAll { it.id == memberId }
        _membersFlow.value = members.toList()
    }

    override suspend fun login(email: String, password: String): Result<FamilyMember> {
        delay(400)
        val member = members.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("No account found for that email."))
        val expected = passwords[member.email]
        if (expected != password) {
            return Result.failure(Exception("Incorrect password."))
        }
        return Result.success(member)
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: FamilyRole,
        createGroup: Boolean,
        groupNameOrCode: String
    ): Result<FamilyMember> {
        delay(400)
        if (members.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val groupId = if (createGroup) {
            val newId = "fg_${UUID.randomUUID().toString().take(8)}"
            familyGroup = FamilyGroup(
                id = newId,
                name = groupNameOrCode.ifBlank { "$name's Family" },
                createdBy = "pending",
                inviteCode = groupNameOrCode.take(4).uppercase().ifBlank { "FAM" } +
                    (1000..9999).random()
            )
            _groupFlow.value = familyGroup
            newId
        } else {
            if (!groupNameOrCode.equals(familyGroup.inviteCode, ignoreCase = true) &&
                groupNameOrCode != "MWNG2026"
            ) {
                return Result.failure(Exception("Invalid invite code. Try MWNG2026 for demo."))
            }
            familyGroup.id
        }

        val member = FamilyMember(
            id = "m_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            role = role,
            avatarColor = listOf(
                "0xFF2F6B44", "0xFFF5C242", "0xFFE05C5C", "0xFF5C8AE0", "0xFF9B59B6"
            ).random(),
            phoneNumber = "",
            email = email.trim().lowercase(),
            familyGroupId = groupId
        )
        if (createGroup) {
            familyGroup = familyGroup.copy(createdBy = member.id)
            _groupFlow.value = familyGroup
        }
        members.add(member)
        passwords[member.email] = password
        _membersFlow.value = members.toList()
        return Result.success(member)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        delay(500)
        val exists = members.any { it.email.equals(email.trim(), ignoreCase = true) }
        return if (exists) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("No account found for that email."))
        }
    }

    override suspend fun updateFamilyGroupName(name: String) {
        familyGroup = familyGroup.copy(name = name)
        _groupFlow.value = familyGroup
    }

    companion object {
        @Volatile
        private var instance: FakeTaskRepository? = null

        fun getInstance(): FakeTaskRepository {
            return instance ?: synchronized(this) {
                instance ?: FakeTaskRepository().also { instance = it }
            }
        }
    }
}
