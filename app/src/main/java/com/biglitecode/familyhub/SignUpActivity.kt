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
import com.biglitecode.familyhub.data.model.FamilyGroupOption
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.repository.SupabaseFamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.ui.signup.SignUpScreen
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                var name by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var confirm by remember { mutableStateOf("") }
                var role by remember { mutableStateOf(FamilyRole.PARENT) }
                var groupOption by remember { mutableStateOf(FamilyGroupOption.CREATE) }
                var groupField by remember { mutableStateOf("") }
                var loading by remember { mutableStateOf(false) }
                var error by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                val repo = SupabaseFamilyRepository.getInstance()

                SignUpScreen(
                    name = name,
                    onNameChange = { name = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    confirm = confirm,
                    onConfirmChange = { confirm = it },
                    role = role,
                    onRoleChange = { role = it },
                    groupOption = groupOption,
                    onGroupOptionChange = { groupOption = it },
                    groupField = groupField,
                    onGroupFieldChange = { groupField = it },
                    loading = loading,
                    error = error,
                    onSignUp = {
                        scope.launch {
                            loading = true
                            val result = repo.signUp(
                                name = name,
                                email = email,
                                password = password,
                                role = role,
                                createGroup = groupOption == FamilyGroupOption.CREATE,
                                groupNameOrCode = groupField
                            )
                            loading = false
                            result.onSuccess { member ->
                                SessionManager.setUser(member)
                                startActivity(
                                    Intent(this@SignUpActivity, DashboardActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                )
                                finish()
                            }.onFailure {
                                error = it.message
                            }
                        }
                    },
                    onBackToLogin = { finish() }
                )
            }
        }
    }
}
