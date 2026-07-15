package com.biglitecode.familyhub.ui.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

private data class Faq(val question: String, val answer: String)

private val faqs = listOf(
    Faq(
        "How do I add family members?",
        "Parents can share the family invite code from the Account screen. New members enter that code during Sign Up under “Join existing family group”."
    ),
    Faq(
        "How do I assign tasks?",
        "As a Parent/Guardian, open the Tasks tab and tap the + button. Fill in title, description, assignee, due date, and reward points."
    ),
    Faq(
        "How do points and rewards work?",
        "Each task has reward points. When a task is marked Done, points count toward the Weekly Rewards card and the Family Leaderboard on Report."
    ),
    Faq(
        "Can I use FamilyHub offline?",
        "Yes for local demo data. After Room/Supabase wiring, the app caches tasks offline and syncs when you’re back online."
    ),
    Faq(
        "How do I reset my password?",
        "On the Login screen, tap “Forgot password?”, enter your email, and tap Send Reset Link. You’ll see a confirmation message."
    ),
    Faq(
        "Can I change my role later?",
        "Roles are set at sign-up (Parent/Guardian or Child/Member). Contact a parent admin if you need a role change for a demo account."
    ),
    Faq(
        "How do SMS reminders work?",
        "Parents can open a task and tap Send Reminder (SMS). The app asks for SMS permission and texts the assignee’s saved phone number."
    )
)

@Composable
fun HelpScreen() {
    var query by remember { mutableStateOf("") }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    val filtered = faqs.filter {
        query.isBlank() ||
            it.question.contains(query, ignoreCase = true) ||
            it.answer.contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Help & FAQ", style = MaterialTheme.typography.headlineMedium, color = TextBrown)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search help…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = ForestGreen) },
                singleLine = true
            )
        }

        itemsIndexed(filtered) { index, faq ->
            val expanded = expandedIndex == index
            val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (expanded) null else index },
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            faq.question,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextBrown,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = faq.answer,
                            color = TextMutedBrown,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Help & FAQ", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun HelpScreenPreview() {
    FamilyHubThemePreview {
        HelpScreen()
    }
}
