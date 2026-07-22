package com.biglitecode.familyhub

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.biglitecode.familyhub.data.repository.FamilyRepository
import com.biglitecode.familyhub.data.repository.SupabaseFamilyRepository
import com.biglitecode.familyhub.data.repository.TaskRepository
import com.biglitecode.familyhub.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FamilyHubApp : Application() {
    val repository: FamilyRepository by lazy { SupabaseFamilyRepository.getInstance() }
    val taskRepository: TaskRepository by lazy { TaskRepository.getInstance(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.createChannel(this)
        registerNetworkCallback()
    }

    /**
     * Auto-sync pending tasks when network becomes available.
     */
    private fun registerNetworkCallback() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network came back — sync pending tasks
                appScope.launch {
                    runCatching { taskRepository.syncPendingTasks() }
                }
            }
        })
    }

    companion object {
        lateinit var instance: FamilyHubApp
            private set
    }
}
