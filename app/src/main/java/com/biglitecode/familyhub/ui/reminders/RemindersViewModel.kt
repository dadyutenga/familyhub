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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    fun createReminder(
        title: String,
        time: String,
        repeatType: RepeatType,
        daysOfWeek: String?
    ) {
        val user = currentUser.value ?: return
        if (user.role != FamilyRole.PARENT) return

        viewModelScope.launch {
            val reminder = FamilyReminder(
                id = "rem_${UUID.randomUUID().toString().take(8)}",
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
        }
    }

    fun toggleReminder(reminder: FamilyReminder, isActive: Boolean) {
        viewModelScope.launch {
            val updated = reminder.copy(isActive = isActive)
            repository.updateReminder(updated)
            if (isActive) {
                ReminderScheduler.scheduleReminder(application, updated)
            } else {
                ReminderScheduler.cancelReminder(application, reminder.id)
            }
        }
    }

    fun deleteReminder(reminderId: String) {
        val user = currentUser.value ?: return
        if (user.role != FamilyRole.PARENT) return

        viewModelScope.launch {
            repository.deleteReminder(reminderId)
            ReminderScheduler.cancelReminder(application, reminderId)
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
