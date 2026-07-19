package com.biglitecode.familyhub.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.ui.components.ErrorBanner
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    loading: Boolean,
    error: String?,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit
) {
    Scaffold(containerColor = CreamBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                color = TextBrown,
                fontWeight = FontWeight.Bold
            )
            Text("Sign in to FamilyHub", color = TextMutedBrown)
            Spacer(Modifier.height(24.dp))

            error?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot password?", color = ForestGreen)
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Login",
                loading = loading,
                onClick = onLogin
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onCreateAccount) {
                Text("Create an account", color = ForestGreen)
            }
        }
    }
}

@Preview(name = "Login", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun LoginScreenPreview() {
    var email by remember { mutableStateOf("amina@familyhub.test") }
    var password by remember { mutableStateOf("parent123") }
    var showPassword by remember { mutableStateOf(false) }
    FamilyHubThemePreview {
        LoginScreen(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            showPassword = showPassword,
            onTogglePassword = { showPassword = !showPassword },
            loading = false,
            error = null,
            onLogin = {},
            onForgotPassword = {},
            onCreateAccount = {}
        )
    }
}

@Preview(name = "Login – Offline error", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun LoginScreenErrorPreview() {
    FamilyHubThemePreview {
        LoginScreen(
            email = "test@test.com",
            onEmailChange = {},
            password = "secret",
            onPasswordChange = {},
            showPassword = false,
            onTogglePassword = {},
            loading = false,
            error = "No internet connection. Please check your network and try again.",
            onLogin = {},
            onForgotPassword = {},
            onCreateAccount = {}
        )
    }
}
