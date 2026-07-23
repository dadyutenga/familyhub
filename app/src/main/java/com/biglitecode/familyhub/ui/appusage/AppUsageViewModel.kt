package com.biglitecode.familyhub.ui.appusage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.biglitecode.familyhub.data.model.AppUsageEntry
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.repository.FamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AppUsageViewModel(
    private val repository: FamilyRepository
) : ViewModel() {

    val currentUser: StateFlow<FamilyMember?> = SessionManager.currentUser

    val members: StateFlow<List<FamilyMember>> = repository.observeMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsage: StateFlow<List<AppUsageEntry>> = repository.observeAppUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Children in this family (for parent to pick which child to view). */
    val children: StateFlow<List<FamilyMember>> = members
        .map { list -> list.filter { it.role == FamilyRole.CHILD } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Usage entries for a specific child, filtered from allUsage. */
    fun usageForChild(childId: String): StateFlow<List<AppUsageEntry>> = allUsage
        .map { list -> list.filter { it.childId == childId }.sortedByDescending { it.usageMinutes } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Unique dates available in the usage data for a child. */
    fun availableDates(childId: String): StateFlow<List<String>> = allUsage
        .map { list ->
            list.filter { it.childId == childId }
                .map { it.date }
                .distinct()
                .sortedDescending()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val repository: FamilyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppUsageViewModel(repository) as T
        }
    }
}
