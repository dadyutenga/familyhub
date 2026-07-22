package com.biglitecode.familyhub.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

// STUB: This screen shows placeholder legal text until a real privacy policy
// is provided by the product owner or fetched from a remote config/CMS endpoint.
// TODO(supabase): replace static sections with real privacy policy content.

private data class PolicySection(val title: String, val body: String)

private val sections = listOf(
    PolicySection(
        "Introduction",
        "This is a placeholder privacy policy. The final version will describe how FamilyHub collects, uses, and protects your family data."
    ),
    PolicySection(
        "Information We Collect",
        "Placeholder: account details, task records, feedback, and complaints you enter into the app."
    ),
    PolicySection(
        "How We Use Your Information",
        "Placeholder: to provide family task management, rewards tracking, and communication features."
    ),
    PolicySection(
        "Data Sharing",
        "Placeholder: information is shared only within your family group. We do not sell personal data."
    ),
    PolicySection(
        "Data Security",
        "Placeholder: data is stored securely. Use strong passwords and do not share sensitive secrets."
    ),
    PolicySection(
        "Your Rights",
        "Placeholder: you may request deletion of your account data from a family admin or clear app data from system settings."
    ),
    PolicySection(
        "Contact Us",
        "Placeholder: contact the project author for privacy questions."
    ),
    PolicySection(
        "Last Updated",
        "Placeholder date. Replace with the actual effective date of the production privacy policy."
    )
)

@Composable
fun PrivacyPolicyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        PrivacyPolicyScreen()
    }
}
