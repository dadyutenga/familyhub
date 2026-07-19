package com.biglitecode.familyhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.biglitecode.familyhub.data.repository.SupabaseFamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.ui.login.LoginScreen
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import com.biglitecode.familyhub.util.NetworkUtils
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                var loading by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                val repo = SupabaseFamilyRepository.getInstance()

                LoginScreen(
                    isLoading = loading,
                    errorMessage = error,
                    onLoginClick = { email, password ->
                        when {
                            !NetworkUtils.isOnline(this@LoginActivity) -> {
                                error = "No internet connection. Please check your network and try again."
                            }
                            email.isBlank() || password.isBlank() -> {
                                error = "Please enter email and password."
                            }
                            else -> {
                                scope.launch {
                                    loading = true
                                    error = null
                                    val result = repo.login(email, password)
                                    loading = false
                                    result.onSuccess { member ->
                                        SessionManager.setUser(member)
                                        startActivity(
                                            Intent(this@LoginActivity, DashboardActivity::class.java)
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        )
                                        finish()
                                    }.onFailure {
                                        error = it.message ?: "Login failed"
                                    }
                                }
                            }
                        }
                    },
                    onSignUpClick = {
                        startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
                    },
                    onForgotPasswordClick = {
                        startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
                    }
                )
            }
        }
    }
}
