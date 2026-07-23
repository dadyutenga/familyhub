package com.biglitecode.familyhub.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown
import com.biglitecode.familyhub.util.UsageStatsHelper

@Composable
fun SettingsScreen(
    viewModel: TasksViewModel
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val group by viewModel.familyGroup.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT

    var pushNotifs by remember { mutableStateOf(true) }
    var smsReminders by remember { mutableStateOf(true) }
    var familyName by remember(group?.name) { mutableStateOf(group?.name.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isParent) {
            Text("Family Settings", style = MaterialTheme.typography.titleLarge, color = TextBrown)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = familyName,
                        onValueChange = { familyName = it },
                        label = { Text("Family Group Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        if (familyName.isNotBlank()) {
                            viewModel.updateFamilyGroupName(familyName.trim())
                        }
                    }) {
                        Text("Save family name", color = ForestGreen)
                    }
                    Text(
                        "Manage members from the Account screen",
                        color = TextMutedBrown,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Text("General", style = MaterialTheme.typography.titleLarge, color = TextBrown)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(Modifier.padding(8.dp)) {
                SettingSwitch("Push Notifications", pushNotifs) { pushNotifs = it }
                HorizontalDivider()
                SettingSwitch("SMS Reminders", smsReminders) { smsReminders = it }
                HorizontalDivider()
                SettingRow("Language", "English")
                HorizontalDivider()
                SettingRow("App Version", "1.0")
            }
        }

        // ── App Usage Monitoring ────────────────────────────────────────
        if (isParent) {
            Text("App Usage Monitoring", style = MaterialTheme.typography.titleLarge, color = TextBrown)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "View which apps your children use most",
                        color = TextMutedBrown,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Ask your children to enable Usage Access from their device's Settings screen to share app usage data.",
                        color = TextMutedBrown.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (!isParent) {
            Text("Parental Monitoring", style = MaterialTheme.typography.titleLarge, color = TextBrown)

            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            // Re-check permission when returning from settings (on resume)
            var hasPermission by remember { mutableStateOf(UsageStatsHelper.hasUsageAccessPermission(context)) }
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        hasPermission = UsageStatsHelper.hasUsageAccessPermission(context)
                        // Schedule usage collection if permission was just granted
                        if (hasPermission) {
                            UsageStatsHelper.scheduleUsageCollection(context)
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Info banner
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (hasPermission) ForestGreenLight.copy(alpha = 0.5f)
                                else GoldYellow.copy(alpha = 0.3f)
                            )
                            .padding(12.dp)
                    ) {
                        Icon(
                            if (hasPermission) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (hasPermission) ForestGreen else CoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (hasPermission) "Your parent can see your app usage"
                            else "Usage data sharing is disabled",
                            color = TextBrown,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Usage Access",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextBrown,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (hasPermission) "Enabled — your parent can see your screen time"
                                else "Disabled — enable to share usage data with your parent",
                                color = TextMutedBrown,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Action button
                    if (!hasPermission) {
                        Button(
                            onClick = {
                                UsageStatsHelper.requestUsageAccessPermission(context)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Enable Usage Access", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                UsageStatsHelper.requestUsageAccessPermission(context)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ForestGreenLight,
                                contentColor = ForestGreenDark
                            )
                        ) {
                            Text("Manage Usage Access Settings")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = TextBrown)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                checkedTrackColor = ForestGreen
            )
        )
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), color = TextBrown)
        Text(value, color = TextMutedBrown)
    }
}

@Preview(name = "Settings – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun SettingsScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        SettingsScreen(viewModel = vm)
    }
}

@Preview(name = "Settings – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun SettingsScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        SettingsScreen(viewModel = vm)
    }
}
