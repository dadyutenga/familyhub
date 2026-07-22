package com.biglitecode.familyhub.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.biglitecode.familyhub.data.local.AppDatabase
import com.biglitecode.familyhub.data.local.TaskEntity
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.data.remote.dto.TaskRow
import com.biglitecode.familyhub.data.remote.dto.toDomain
import com.biglitecode.familyhub.data.remote.dto.toRow
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.core.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Offline-first Task repository.
 *
 * Architecture:
 * - READS:  Room emits instantly → Supabase refreshes in background → Room auto-updates UI
 * - WRITES: Supabase first → on success, write to Room. On failure (offline), write to Room with pendingSync=true
 * - SYNC:   On reconnect, push all pendingSync rows to Supabase
 */
class TaskRepository private constructor(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val taskDao = db.taskDao()
    private val client get() = SupabaseClientProvider.client
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // -------------------------------------------------------------------------
    // Observe — Room emits instantly, then Supabase refreshes in background
    // -------------------------------------------------------------------------

    fun observeTasks(): Flow<List<Task>> {
        // Trigger background refresh from Supabase
        scope.launch { runCatching { refreshFromSupabase() } }

        // Room Flow auto-updates whenever the DB changes
        return taskDao.observeAll().map { entities ->
            entities.map { it.toModel() }
        }
    }

    // -------------------------------------------------------------------------
    // Single reads — from Room (fast, offline-capable)
    // -------------------------------------------------------------------------

    suspend fun getTaskById(id: String): Task? = withContext(Dispatchers.IO) {
        taskDao.getById(id)?.toModel()
    }

    // -------------------------------------------------------------------------
    // Writes — Supabase first, then Room. Offline → Room with pendingSync
    // -------------------------------------------------------------------------

    suspend fun addTask(task: Task) = withContext(Dispatchers.IO) {
        val groupId = SessionManager.currentUser.value?.familyGroupId ?: ""
        val row = task.toRow().copy(familyGroupId = groupId)

        if (isOnline()) {
            try {
                client.postgrest["tasks"].insert(row)
                // Success → save to Room as synced
                taskDao.upsert(TaskEntity.from(task, pendingSync = false))
            } catch (e: Exception) {
                // Supabase failed → save to Room as pending
                taskDao.upsert(TaskEntity.from(task, pendingSync = true))
                throw e
            }
        } else {
            // Offline → save to Room as pending
            taskDao.upsert(TaskEntity.from(task, pendingSync = true))
        }
    }

    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        val row = task.toRow()

        if (isOnline()) {
            try {
                client.postgrest["tasks"].update(row) { filter { eq("id", row.id) } }
                taskDao.upsert(TaskEntity.from(task, pendingSync = false))
            } catch (e: Exception) {
                taskDao.upsert(TaskEntity.from(task, pendingSync = true))
                throw e
            }
        } else {
            taskDao.upsert(TaskEntity.from(task, pendingSync = true))
        }
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        if (isOnline()) {
            try {
                client.postgrest["tasks"].delete { filter { eq("id", taskId) } }
                taskDao.delete(taskId)
            } catch (e: Exception) {
                // Mark for sync deletion later (or just delete locally)
                taskDao.delete(taskId)
                throw e
            }
        } else {
            taskDao.delete(taskId)
        }
    }

    // -------------------------------------------------------------------------
    // Sync — push pending rows when back online
    // -------------------------------------------------------------------------

    suspend fun syncPendingTasks() = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext

        val pending = taskDao.getPendingSync()
        for (entity in pending) {
            try {
                val row = entity.toModel().toRow().copy(
                    familyGroupId = SessionManager.currentUser.value?.familyGroupId ?: ""
                )
                client.postgrest["tasks"].upsert(row)
                taskDao.upsert(entity.copy(pendingSync = false))
            } catch (_: Exception) {
                // Will retry next time
            }
        }
    }

    // -------------------------------------------------------------------------
    // Background refresh — pull latest from Supabase into Room
    // -------------------------------------------------------------------------

    private suspend fun refreshFromSupabase() {
        if (!isOnline()) return

        val groupId = SessionManager.currentUser.value?.familyGroupId ?: return
        val remoteTasks = client.postgrest["tasks"]
            .select { filter { eq("family_group_id", groupId) } }
            .decodeList<TaskRow>()
            .map { it.toDomain() }

        // Replace Room cache with fresh Supabase data
        // But keep any pendingSync rows that haven't been pushed yet
        val pending = taskDao.getPendingSync()
        taskDao.deleteAll()
        taskDao.upsertAll(remoteTasks.map { TaskEntity.from(it, pendingSync = false) })
        // Re-insert pending rows (they weren't on Supabase yet)
        taskDao.upsertAll(pending)
    }

    // -------------------------------------------------------------------------
    // Connectivity check
    // -------------------------------------------------------------------------

    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    // -------------------------------------------------------------------------
    // Sync status
    // -------------------------------------------------------------------------

    fun observePendingCount(): Flow<Int> {
        return taskDao.observeAll().map { tasks ->
            tasks.count { it.pendingSync }
        }
    }

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    companion object {
        @Volatile
        private var instance: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return instance ?: synchronized(this) {
                instance ?: TaskRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
