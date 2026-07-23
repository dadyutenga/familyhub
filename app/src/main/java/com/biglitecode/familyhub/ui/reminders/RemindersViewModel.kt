package com.biglitecode.familyhub.ui.reminders

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.RepeatType
import com.biglitecode.familyhub.data.repository.FamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.util.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RemindersViewModel(
    private val repository: FamilyRepository,
    private val application: Application
) : ViewModel() {

    val currentUser: StateFlow<FamilyMember?> = SessionManager.currentUser

    val reminders: StateFlow<List<FamilyReminder>> = repository.observeReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    fun createReminder(
        title: String,
        time: String,
        repeatType: RepeatType,
        daysOfWeek: String?
    ) {
        val user = currentUser.value ?: run {
            _error.value = "You must be signed in to create a reminder."
            return
        }
        if (user.role != FamilyRole.PARENT) {
            _error.value = "Only parents can create reminders."
            return
        }
        if (user.familyGroupId.isBlank()) {
            _error.value = "Your account is not linked to a family. Please sign up again."
            return
        }

        viewModelScope.launch {
            try {
                val reminder = FamilyReminder(
                    id = "rem_${UUID.randomUUID().toString().take(8)}",
                    familyGroupId = user.familyGroupId,
                    title = title,
                    reminderTime = time,
                    repeatType = repeatType,
                    daysOfWeek = daysOfWeek,
                    isActive = true,
                    createdBy = user.id,
                    createdAt = System.currentTimeMillis()
                )
                repository.addReminder(reminder)
                ReminderScheduler.scheduleReminder(application, reminder)
            } catch (e: Exception) {
                android.util.Log.e("RemindersVM", "Failed to create reminder", e)
                _error.value = "Could not create reminder: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun toggleReminder(reminder: FamilyReminder, isActive: Boolean) {
        viewModelScope.launch {
            try {
                val updated = reminder.copy(isActive = isActive)
                repository.updateReminder(updated)
                if (isActive) {
                    ReminderScheduler.scheduleReminder(application, updated)
                } else {
                    ReminderScheduler.cancelReminder(application, reminder.id)
                }
            } catch (e: Exception) {
                android.util.Log.e("RemindersVM", "Failed to toggle reminder", e)
                _error.value = "Could not update reminder: ${e.message ?: "unknown error"}"
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        val user = currentUser.value ?: return
        if (user.role != FamilyRole.PARENT) return

        viewModelScope.launch {
            try {
                repository.deleteReminder(reminderId)
                ReminderScheduler.cancelReminder(application, reminderId)
            } catch (e: Exception) {
                android.util.Log.e("RemindersVM", "Failed to delete reminder", e)
                _error.value = "Could not delete reminder: ${e.message ?: "unknown error"}"
            }
        }
    }

    class Factory(
        private val repository: FamilyRepository,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RemindersViewModel(repository, application) as T
        }
    }
}
