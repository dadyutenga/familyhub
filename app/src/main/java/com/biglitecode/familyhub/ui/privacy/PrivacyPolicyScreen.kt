package com.biglitecode.familyhub.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

private data class PolicySection(val title: String, val body: String)

private val sections = listOf(
    PolicySection(
        "Introduction",
        "FamilyHub is a student demonstration app for managing family tasks and communication. This policy describes how sample account and task data may be handled in the demo."
    ),
    PolicySection(
        "Information We Collect",
        "We may collect account details such as name, email, role, family group membership, task records, feedback, and complaints that you enter into the app."
    ),
    PolicySection(
        "How We Use Your Information",
        "Data is used to provide core features: assigning tasks, tracking completion, leaderboards, notifications, and family communication tools."
    ),
    PolicySection(
        "Data Sharing",
        "Task and complaint information is shared only within your family group so parents and members can coordinate. We do not sell personal data."
    ),
    PolicySection(
        "Data Security",
        "Demo data may be stored locally on the device and optionally synced via a student Supabase project. Use strong passwords and do not enter real sensitive secrets."
    ),
    PolicySection(
        "Your Rights",
        "You may request deletion of your demo account data from a parent admin, or clear app data from system settings. Contact the project author for assignment-related questions."
    ),
    PolicySection(
        "Contact Us",
        "For privacy questions about this student project, email support@familyhub.app or contact the course instructor / project author (BIG LITE CODE)."
    ),
    PolicySection(
        "Last Updated",
        "July 15, 2026. This is placeholder legal text for a student coursework demo, not a production privacy policy."
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ForestGreen)
            }
        }
        Text("Privacy Policy", style = MaterialTheme.typography.headlineMedium, color = TextBrown)
        sections.forEach { section ->
            Column {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextBrown
                )
                Spacer(Modifier.height(4.dp))
                Text(section.body, color = TextMutedBrown, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(name = "Privacy Policy", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun PrivacyPolicyScreenPreview() {
    FamilyHubThemePreview {
        PrivacyPolicyScreen(onBack = {})
    }
}
