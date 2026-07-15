package com.biglitecode.familyhub.data.session

import com.biglitecode.familyhub.data.model.FamilyMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _currentUser = MutableStateFlow<FamilyMember?>(null)
    val currentUser: StateFlow<FamilyMember?> = _currentUser.asStateFlow()

    fun setUser(user: FamilyMember?) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null
}
