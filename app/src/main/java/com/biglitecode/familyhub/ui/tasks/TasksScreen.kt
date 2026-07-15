package com.biglitecode.familyhub.ui.tasks

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.ui.components.TaskCard
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown
import com.biglitecode.familyhub.util.NotificationHelper
import java.util.concurrent.TimeUnit

enum class TaskFilter { ALL, PENDING, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    onTaskClick: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val tasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filtered = when (filter) {
        TaskFilter.ALL -> tasks
        TaskFilter.PENDING -> tasks.filter {
            it.status == TaskStatus.PENDING || it.status == TaskStatus.OVERDUE
        }
        TaskFilter.DONE -> tasks.filter { it.status == TaskStatus.DONE }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            if (isParent) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = ForestGreen,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (filtered.isEmpty()) {
                Text("No tasks in this filter.", color = TextMutedBrown)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered, key = { it.id }) { task ->
                        TaskCard(task = task, onClick = { onTaskClick(task.id) })
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            members = members.filter { it.role == FamilyRole.CHILD || it.id != user?.id },
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, assignee, due, points ->
                viewModel.addTask(title, desc, assignee, due, points) { task ->
                    NotificationHelper.showTaskAssignedNotification(
                        context, task.title, task.assignedToName
                    )
                    Toast.makeText(context, "Task assigned to ${task.assignedToName}", Toast.LENGTH_SHORT).show()
                }
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDialog(
    members: List<FamilyMember>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, FamilyMember, Long, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(members.firstOrNull()) }
    var reward by remember { mutableIntStateOf(10) }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task", color = TextBrown) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selected?.name ?: "Select assignee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign to") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.name) },
                                onClick = {
                                    selected = member
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reward.toString(),
                    onValueChange = { reward = it.toIntOrNull() ?: 10 },
                    label = { Text("Reward points") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Due: ${java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(dueDate))}")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val assignee = selected ?: return@TextButton
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), description.trim(), assignee, dueDate, reward)
                    }
                },
                enabled = title.isNotBlank() && selected != null
            ) {
                Text("Create", color = ForestGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text("OK", color = ForestGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(name = "Tasks – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun TasksScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        TasksScreen(viewModel = vm, onTaskClick = {})
    }
}

@Preview(name = "Tasks – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun TasksScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        TasksScreen(viewModel = vm, onTaskClick = {})
    }
}
