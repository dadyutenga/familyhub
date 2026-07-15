package com.biglitecode.familyhub.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.biglitecode.familyhub.data.model.Complaint
import com.biglitecode.familyhub.data.model.FamilyGroup
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Feedback
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.data.repository.FamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class TasksViewModel(private val repository: FamilyRepository) : ViewModel() {

    val currentUser: StateFlow<FamilyMember?> = SessionManager.currentUser

    val tasks: StateFlow<List<Task>> = repository.observeTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<FamilyMember>> = repository.observeMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val complaints: StateFlow<List<Complaint>> = repository.observeComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feedback: StateFlow<List<Feedback>> = repository.observeFeedback()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyGroup: StateFlow<FamilyGroup?> = repository.observeFamilyGroup()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val visibleTasks: StateFlow<List<Task>> = combine(tasks, currentUser) { all, user ->
        if (user == null) emptyList()
        else if (user.role == FamilyRole.PARENT) all
        else all.filter { it.assignedTo == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(
        title: String,
        description: String,
        assignedTo: FamilyMember,
        dueDate: Long,
        rewardPoints: Int,
        onAdded: (Task) -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val task = Task(
                id = "t_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description,
                assignedTo = assignedTo.id,
                assignedToName = assignedTo.name,
                assignedBy = user.id,
                dueDate = dueDate,
                status = TaskStatus.PENDING,
                createdAt = System.currentTimeMillis(),
                rewardPoints = rewardPoints
            )
            repository.addTask(task)
            onAdded(task)
        }
    }

    fun markComplete(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = TaskStatus.DONE))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { repository.deleteTask(taskId) }
    }

    fun submitFeedback(taskId: String, rating: Int, comment: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addFeedback(
                Feedback(
                    id = "fb_${UUID.randomUUID().toString().take(8)}",
                    taskId = taskId,
                    userId = user.id,
                    comment = comment,
                    rating = rating
                )
            )
        }
    }

    fun submitComplaint(subject: String, description: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addComplaint(
                Complaint(
                    id = "c_${UUID.randomUUID().toString().take(8)}",
                    userId = user.id,
                    userName = user.name,
                    subject = subject,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    resolved = false
                )
            )
        }
    }

    fun removeMember(memberId: String) {
        viewModelScope.launch { repository.removeMember(memberId) }
    }

    fun updateFamilyGroupName(name: String) {
        viewModelScope.launch { repository.updateFamilyGroupName(name) }
    }

    fun resolveComplaint(complaint: Complaint) {
        viewModelScope.launch {
            repository.updateComplaint(complaint.copy(resolved = true))
        }
    }

    class Factory(private val repository: FamilyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(repository) as T
        }
    }
}
