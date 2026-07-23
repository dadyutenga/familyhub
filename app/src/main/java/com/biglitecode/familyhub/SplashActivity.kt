package com.biglitecode.familyhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.ui.splash.SplashScreen
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (SessionManager.isLoggedIn()) {
            // Already in memory (rare — only if activity was recreated, not process-killed)
            setContent {
                FamilyHubTheme {
                    SplashScreen(
                        animate = true,
                        onFinished = { navigateAfterSplash() }
                    )
                }
            }
        } else {
            // Cold start: try DataStore first, then Supabase Auth session restore
            lifecycleScope.launch {
                // 1) Load from persistent DataStore (instant, no network)
                SessionManager.loadSavedUser()

                // 2) If DataStore had nothing, ask Supabase Auth if a session exists
                if (!SessionManager.isLoggedIn()) {
                    runCatching {
                        val repo = (application as FamilyHubApp).repository
                        repo.restoreSessionIfPossible()
                    }
                }

                navigateAfterSplash()
            }
        }
    }

    private fun navigateAfterSplash() {
        val next = if (SessionManager.isLoggedIn()) {
            DashboardActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this@SplashActivity, next))
        finish()
    }
}
