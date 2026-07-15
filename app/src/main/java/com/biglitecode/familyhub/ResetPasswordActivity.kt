package com.biglitecode.familyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.biglitecode.familyhub.data.repository.FakeTaskRepository
import com.biglitecode.familyhub.ui.resetpassword.ResetPasswordScreen
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import kotlinx.coroutines.launch

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                var email by remember { mutableStateOf("") }
                var loading by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }
                var success by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                val repo = FakeTaskRepository.getInstance()

                ResetPasswordScreen(
                    email = email,
                    onEmailChange = {
                        email = it
                        error = null
                        success = false
                    },
                    loading = loading,
                    error = error,
                    success = success,
                    onSend = {
                        if (email.isBlank()) {
                            error = "Please enter your email."
                        } else {
                            scope.launch {
                                loading = true
                                val result = repo.sendPasswordReset(email)
                                loading = false
                                result.onSuccess {
                                    success = true
                                    error = null
                                }.onFailure {
                                    success = false
                                    error = it.message
                                }
                            }
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
