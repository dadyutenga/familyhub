package com.biglitecode.familyhub.ui.signup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.data.model.FamilyGroupOption
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.components.ErrorBanner
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun SignUpScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirm: String,
    onConfirmChange: (String) -> Unit,
    role: FamilyRole,
    onRoleChange: (FamilyRole) -> Unit,
    groupOption: FamilyGroupOption,
    onGroupOptionChange: (FamilyGroupOption) -> Unit,
    groupField: String,
    onGroupFieldChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    onSignUp: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val valid = name.isNotBlank() &&
        email.isNotBlank() &&
        password.length >= 6 &&
        password == confirm &&
        groupField.isNotBlank()

    Scaffold(containerColor = CreamBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Create account", style = MaterialTheme.typography.headlineMedium, color = TextBrown)
            Text("Join your family on FamilyHub", color = TextMutedBrown)

            error?.let { ErrorBanner(it) }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = onConfirmChange,
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (password.isNotEmpty() && confirm.isNotEmpty() && password != confirm) {
                Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
            }

            Text("I am a…", fontWeight = FontWeight.SemiBold, color = TextBrown)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectCard(
                    title = "Parent/Guardian",
                    selected = role == FamilyRole.PARENT,
                    modifier = Modifier.weight(1f)
                ) { onRoleChange(FamilyRole.PARENT) }
                SelectCard(
                    title = "Child/Member",
                    selected = role == FamilyRole.CHILD,
                    modifier = Modifier.weight(1f)
                ) { onRoleChange(FamilyRole.CHILD) }
            }

            Text("Family group", fontWeight = FontWeight.SemiBold, color = TextBrown)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectCard(
                    title = "Create new",
                    selected = groupOption == FamilyGroupOption.CREATE,
                    modifier = Modifier.weight(1f)
                ) { onGroupOptionChange(FamilyGroupOption.CREATE) }
                SelectCard(
                    title = "Join existing",
                    selected = groupOption == FamilyGroupOption.JOIN,
                    modifier = Modifier.weight(1f)
                ) { onGroupOptionChange(FamilyGroupOption.JOIN) }
            }
            OutlinedTextField(
                value = groupField,
                onValueChange = onGroupFieldChange,
                label = {
                    Text(
                        if (groupOption == FamilyGroupOption.CREATE) "Family group name"
                        else "Family name to join"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Sign Up",
                enabled = valid,
                loading = loading,
                onClick = onSignUp
            )
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Already have an account? Login", color = ForestGreen)
            }
        }
    }
}

@Composable
private fun SelectCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                2.dp,
                if (selected) ForestGreen else GoldYellow.copy(alpha = 0.4f),
                MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) ForestGreen.copy(alpha = 0.1f) else CardCream
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(14.dp),
            color = TextBrown,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(name = "Sign Up", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun SignUpScreenPreview() {
    var name by remember { mutableStateOf("New User") }
    var email by remember { mutableStateOf("new@familyhub.test") }
    var password by remember { mutableStateOf("secret1") }
    var confirm by remember { mutableStateOf("secret1") }
    var role by remember { mutableStateOf(FamilyRole.PARENT) }
    var groupOption by remember { mutableStateOf(FamilyGroupOption.CREATE) }
    var groupField by remember { mutableStateOf("Our Family") }

    FamilyHubThemePreview {
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
            loading = false,
            error = null,
            onSignUp = {},
            onBackToLogin = {}
        )
    }
}
