package com.biglitecode.familyhub

import android.app.Application
import com.biglitecode.familyhub.data.repository.FakeTaskRepository
import com.biglitecode.familyhub.data.repository.FamilyRepository
import com.biglitecode.familyhub.util.NotificationHelper

class FamilyHubApp : Application() {
    val repository: FamilyRepository by lazy { FakeTaskRepository.getInstance() }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.createChannel(this)
    }

    companion object {
        lateinit var instance: FamilyHubApp
            private set
    }
}
