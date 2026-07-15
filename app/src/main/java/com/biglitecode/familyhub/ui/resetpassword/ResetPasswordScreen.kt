package com.biglitecode.familyhub.ui.resetpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.ui.components.ErrorBanner
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    success: Boolean,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Reset password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                "Enter your account email and we’ll send a reset link.",
                color = TextMutedBrown
            )
            Spacer(Modifier.height(20.dp))
            error?.let {
                ErrorBanner(it)
                Spacer(Modifier.height(12.dp))
            }
            if (success) {
                Text(
                    "Reset link sent! Check your inbox (demo: success).",
                    color = ForestGreen,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Send Reset Link",
                loading = loading,
                onClick = onSend
            )
        }
    }
}

@Preview(name = "Reset Password", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ResetPasswordScreenPreview() {
    var email by remember { mutableStateOf("amina@familyhub.test") }
    FamilyHubThemePreview {
        ResetPasswordScreen(
            email = email,
            onEmailChange = { email = it },
            loading = false,
            error = null,
            success = false,
            onSend = {},
            onBack = {}
        )
    }
}

@Preview(name = "Reset Password – Success", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ResetPasswordSuccessPreview() {
    FamilyHubThemePreview {
        ResetPasswordScreen(
            email = "amina@familyhub.test",
            onEmailChange = {},
            loading = false,
            error = null,
            success = true,
            onSend = {},
            onBack = {}
        )
    }
}
