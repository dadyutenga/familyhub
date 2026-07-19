package com.biglitecode.familyhub.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (email: String, password: String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primaryGradient = remember {
        Brush.verticalGradient(
            listOf(ForestGreenDark, ForestGreen, ForestGreenLight)
        )
    }

    val disabledGradient = remember {
        Brush.verticalGradient(
            listOf(
                ForestGreenDark.copy(alpha = 0.45f),
                ForestGreen.copy(alpha = 0.45f),
                ForestGreenLight.copy(alpha = 0.45f)
            )
        )
    }

    val isFormValid = email.isNotBlank() && password.isNotBlank()
    val canLogin = !isLoading && isFormValid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand header block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(primaryGradient)
                .statusBarsPadding()
        ) {
            // Decorative soft circles
            val circleColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .offset(x = (-40).dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 40.dp, y = 30.dp)
                    .clip(CircleShape)
                    .background(circleColor)
            )

            // Header content
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "FamilyHub logo",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "FamilyHub",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Organize tasks, rewards & family life",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Form card overlapping the header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-28).dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome back",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sign in to your family account",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(20.dp))

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle password",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f),
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onForgotPasswordClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Forgot password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(4.dp))

                // Error banner
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                } else {
                    Spacer(Modifier.height(8.dp))
                }

                // Login gradient button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (canLogin) primaryGradient else disabledGradient)
                        .clickable(enabled = canLogin) {
                            onLoginClick(email, password)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Login",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Divider row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                    Text(
                        text = "New here?",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                        fontSize = 12.sp
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Create account button
                OutlinedButton(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Create an account",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Footer
        Spacer(Modifier.height(12.dp))
        Text(
            text = "© FamilyHub — Built for families",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
            fontSize = 12.sp,
            modifier = Modifier.navigationBarsPadding()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(name = "Login", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun LoginScreenPreview() {
    FamilyHubThemePreview {
        LoginScreen(
            isLoading = false,
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onSignUpClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(name = "Login – Error", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun LoginScreenErrorPreview() {
    FamilyHubThemePreview {
        LoginScreen(
            isLoading = false,
            errorMessage = "No internet connection. Please check your network and try again.",
            onLoginClick = { _, _ -> },
            onSignUpClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(name = "Login – Loading", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun LoginScreenLoadingPreview() {
    FamilyHubThemePreview {
        LoginScreen(
            isLoading = true,
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onSignUpClick = {},
            onForgotPasswordClick = {}
        )
    }
}
