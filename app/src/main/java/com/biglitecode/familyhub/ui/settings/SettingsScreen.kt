package com.biglitecode.familyhub.ui.settings

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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun SettingsScreen(
    viewModel: TasksViewModel,
    onLogout: () -> Unit
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

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", color = CoralRed, fontWeight = FontWeight.Bold)
        }
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
        SettingsScreen(viewModel = vm, onLogout = {})
    }
}

@Preview(name = "Settings – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun SettingsScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        SettingsScreen(viewModel = vm, onLogout = {})
    }
}
