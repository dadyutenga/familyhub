package com.biglitecode.familyhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.ui.splash.SplashScreen
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                SplashScreen(
                    animate = true,
                    onFinished = {
                        val next = if (SessionManager.isLoggedIn()) {
                            DashboardActivity::class.java
                        } else {
                            LoginActivity::class.java
                        }
                        startActivity(Intent(this@SplashActivity, next))
                        finish()
                    }
                )
            }
        }
    }
}
