package com.biglitecode.familyhub.ui.tasks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.components.StatusPill
import com.biglitecode.familyhub.ui.components.formatDate
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown
import com.biglitecode.familyhub.util.SmsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    viewModel: TasksViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val task = tasks.find { it.id == taskId }
    val context = LocalContext.current
    val isParent = user?.role == FamilyRole.PARENT
    val isAssignee = task?.assignedTo == user?.id
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && task != null) {
            val member = members.find { it.id == task.assignedTo }
            SmsHelper.sendTaskReminder(
                context,
                member?.phoneNumber.orEmpty(),
                task.title,
                task.assignedToName
            )
        } else {
            Toast.makeText(context, "SMS permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    if (task == null) {
        Column(Modifier.padding(16.dp)) {
            Text("Task not found.", color = TextMutedBrown)
            TextButton(onClick = onBack) { Text("Go back") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ForestGreen)
            }
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextBrown,
                modifier = Modifier.weight(1f)
            )
            StatusPill(task.status)
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ForestGreen, MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Assigned To", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                Text(task.assignedToName, style = MaterialTheme.typography.titleLarge, color = TextBrown)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, GoldYellow, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Due date", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                    Text(formatDate(task.dueDate), fontWeight = FontWeight.SemiBold, color = TextBrown)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, GoldYellow, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Reward", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                    Text("${task.rewardPoints} pts", fontWeight = FontWeight.SemiBold, color = ForestGreen)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Description", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(task.description, color = TextBrown, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Role-based actions
        if (task.status != TaskStatus.DONE && (isParent || isAssignee)) {
            PrimaryButton(
                text = "Mark Complete",
                onClick = {
                    viewModel.markComplete(task)
                    Toast.makeText(context, "Task marked complete!", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(Modifier.height(10.dp))
        }

        if (isParent) {
            PrimaryButton(text = "Edit Task", onClick = { showEdit = true })
            Spacer(Modifier.height(10.dp))

            if (task.status != TaskStatus.DONE) {
                PrimaryButton(
                    text = "Send Reminder (SMS)",
                    onClick = {
                        val permission = Manifest.permission.SEND_SMS
                        if (ContextCompat.checkSelfPermission(context, permission) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            val member = members.find { it.id == task.assignedTo }
                            SmsHelper.sendTaskReminder(
                                context,
                                member?.phoneNumber.orEmpty(),
                                task.title,
                                task.assignedToName
                            )
                        } else {
                            smsPermissionLauncher.launch(permission)
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            androidx.compose.material3.Button(
                onClick = { showDelete = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Text("Delete Task", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
        } else if (!isAssignee) {
            Text(
                "This task is not assigned to you (read-only).",
                color = TextMutedBrown,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
        }

        // Lightweight Bluetooth demo — opens system BT settings (full P2P out of scope)
        TextButton(
            onClick = {
                try {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                } catch (_: Exception) {
                    Toast.makeText(context, "Bluetooth settings unavailable", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share Task Nearby (Bluetooth)", color = ForestGreen)
        }
    }

    if (showEdit) {
        var newTitle by remember { mutableStateOf(task.title) }
        var newDesc by remember { mutableStateOf(task.description) }
        AlertDialog(
            onDismissRequest = { showEdit = false },
            title = { Text("Edit Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTask(task.copy(title = newTitle, description = newDesc))
                    showEdit = false
                }) { Text("Save", color = ForestGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showEdit = false }) { Text("Cancel") }
            }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete task?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task.id)
                    showDelete = false
                    onBack()
                }) { Text("Delete", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Preview(name = "Task Detail – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun TaskDetailParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        TaskDetailScreen(taskId = "t1", viewModel = vm, onBack = {})
    }
}

@Preview(name = "Task Detail – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun TaskDetailChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        TaskDetailScreen(taskId = "t1", viewModel = vm, onBack = {})
    }
}
