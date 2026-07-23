package com.biglitecode.familyhub.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manages the currently signed-in user's session.
 *
 * - In-memory [StateFlow] keeps the UI updated instantly.
 * - Jetpack DataStore persists the user across process deaths.
 * - On cold start, call [loadSavedUser] (from SplashActivity) to restore from disk.
 */
object SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var dataStore: DataStore<Preferences>

    // Preference keys
    private val KEY_ID = stringPreferencesKey("session_user_id")
    private val KEY_NAME = stringPreferencesKey("session_user_name")
    private val KEY_ROLE = stringPreferencesKey("session_user_role")
    private val KEY_AVATAR = stringPreferencesKey("session_user_avatar")
    private val KEY_PHONE = stringPreferencesKey("session_user_phone")
    private val KEY_EMAIL = stringPreferencesKey("session_user_email")
    private val KEY_FAMILY_GROUP_ID = stringPreferencesKey("session_family_group_id")

    private val _currentUser = MutableStateFlow<FamilyMember?>(null)
    val currentUser: StateFlow<FamilyMember?> = _currentUser.asStateFlow()

    /**
     * Call once from [android.app.Application.onCreate]. Must be called before
     * any other method.
     */
    fun init(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }

    fun isLoggedIn(): Boolean = _currentUser.value != null

    /**
     * Restore the user from DataStore (disk) into the in-memory [currentUser] flow.
     * Call this from SplashActivity on cold start.
     *
     * @return the restored [FamilyMember], or null if nothing was persisted.
     */
    suspend fun loadSavedUser(): FamilyMember? {
        if (!::dataStore.isInitialized) return null
        return try {
            val prefs = dataStore.data.first()
            val id = prefs[KEY_ID] ?: return null
            val member = FamilyMember(
                id = id,
                name = prefs[KEY_NAME] ?: "",
                role = runCatching { FamilyRole.valueOf(prefs[KEY_ROLE] ?: "CHILD") }
                    .getOrDefault(FamilyRole.CHILD),
                avatarColor = prefs[KEY_AVATAR]?.takeIf { it.isNotEmpty() },
                phoneNumber = prefs[KEY_PHONE] ?: "",
                email = prefs[KEY_EMAIL] ?: "",
                familyGroupId = prefs[KEY_FAMILY_GROUP_ID] ?: ""
            )
            _currentUser.value = member
            android.util.Log.d("SessionManager", "Loaded saved user: ${member.id} (${member.name})")
            member
        } catch (e: Exception) {
            android.util.Log.e("SessionManager", "Failed to load saved user", e)
            null
        }
    }

    /**
     * Set the current user. Updates in-memory state immediately and persists
     * to DataStore on a background coroutine.
     *
     * Pass `null` to clear the session (logout).
     */
    fun setUser(user: FamilyMember?) {
        _currentUser.value = user
        if (!::dataStore.isInitialized) return
        scope.launch {
            try {
                if (user == null) {
                    dataStore.edit { it.clear() }
                } else {
                    dataStore.edit { prefs ->
                        prefs[KEY_ID] = user.id
                        prefs[KEY_NAME] = user.name
                        prefs[KEY_ROLE] = user.role.name
                        prefs[KEY_AVATAR] = user.avatarColor.orEmpty()
                        prefs[KEY_PHONE] = user.phoneNumber
                        prefs[KEY_EMAIL] = user.email
                        prefs[KEY_FAMILY_GROUP_ID] = user.familyGroupId
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SessionManager", "Failed to persist user", e)
            }
        }
    }

    /**
     * Clear the in-memory session and wipe DataStore. Also signs out of Supabase Auth.
     */
    fun logout() {
        _currentUser.value = null
        if (!::dataStore.isInitialized) return
        scope.launch {
            try {
                dataStore.edit { it.clear() }
                // Also clear the Supabase Auth session so tokens don't linger
                try {
                    com.biglitecode.familyhub.core.SupabaseClientProvider.client.auth.signOut()
                } catch (_: Exception) { }
                android.util.Log.d("SessionManager", "Session cleared")
            } catch (e: Exception) {
                android.util.Log.e("SessionManager", "Failed to clear session", e)
            }
        }
    }
}
