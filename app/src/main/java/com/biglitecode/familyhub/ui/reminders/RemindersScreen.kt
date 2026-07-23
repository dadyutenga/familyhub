package com.biglitecode.familyhub.ui.reminders

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.RepeatType
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown
import kotlinx.coroutines.launch
import java.util.Calendar

// Day abbreviation constants (matching the DB format)
private val ALL_DAYS = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
private val DAY_LABELS = mapOf(
    "MON" to "Mon", "TUE" to "Tue", "WED" to "Wed",
    "THU" to "Thu", "FRI" to "Fri", "SAT" to "Sat", "SUN" to "Sun"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT

    var showAddSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (reminders.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextMutedBrown.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No reminders yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMutedBrown
                )
                if (isParent) {
                    Text(
                        "Tap + to create a family reminder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedBrown.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                reminders.sortedBy { it.reminderTime }.forEach { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        isParent = isParent,
                        onToggle = { isActive ->
                            viewModel.toggleReminder(reminder, isActive)
                        },
                        onDelete = {
                            viewModel.deleteReminder(reminder.id)
                        }
                    )
                }
                // Bottom spacer for FAB clearance
                Spacer(Modifier.height(72.dp))
            }
        }

        // FAB — PARENT only
        if (isParent) {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = ForestGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        }
    }

    // Add reminder bottom sheet
    if (showAddSheet) {
        AddReminderSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, time, repeatType, daysOfWeek ->
                viewModel.createReminder(title, time, repeatType, daysOfWeek)
                showAddSheet = false
            }
        )
    }
}

// ── Reminder list item ─────────────────────────────────────────────────────
@Composable
private fun ReminderCard(
    reminder: FamilyReminder,
    isParent: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ForestGreen.copy(alpha = 0.3f), MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CardCream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (reminder.isActive) ForestGreenLight else ForestGreenLight.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = if (reminder.isActive) ForestGreenDark else ForestGreenDark.copy(alpha = 0.4f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (reminder.isActive) TextBrown else TextMutedBrown,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    formatTime(reminder.reminderTime),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (reminder.isActive) ForestGreen else TextMutedBrown,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    formatRepeatLabel(reminder),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedBrown
                )
            }

            if (isParent) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = TextMutedBrown.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ForestGreen
                    )
                )
            }
        }
    }
}

// ── Add Reminder Bottom Sheet ──────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, time: String, repeatType: RepeatType, daysOfWeek: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var repeatTypeIndex by remember { mutableIntStateOf(0) } // 0=DAILY, 1=WEEKLY, 2=SPECIFIC_DAYS
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var titleError by remember { mutableStateOf<String?>(null) }

    val repeatOptions = listOf("Daily", "Weekly", "Specific Days")
    val currentRepeatType = when (repeatTypeIndex) {
        0 -> RepeatType.DAILY
        1 -> RepeatType.WEEKLY
        2 -> RepeatType.SPECIFIC_DAYS
        else -> RepeatType.DAILY
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Add Reminder",
                style = MaterialTheme.typography.headlineSmall,
                color = TextBrown,
                fontWeight = FontWeight.Bold
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = null
                },
                label = { Text("Reminder Title") },
                placeholder = { Text("e.g. Swala ya Alasiri") },
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Time picker button
            Column {
                Text("Time", style = MaterialTheme.typography.labelLarge, color = TextBrown)
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, h, m ->
                                    hour = h
                                    minute = m
                                },
                                hour,
                                minute,
                                true
                            ).show()
                        },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = ForestGreen)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            String.format("%02d:%02d", hour, minute),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextBrown,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Repeat type tabs
            Column {
                Text("Repeat", style = MaterialTheme.typography.labelLarge, color = TextBrown)
                Spacer(Modifier.height(4.dp))
                PrimaryTabRow(
                    selectedTabIndex = repeatTypeIndex,
                    containerColor = ForestGreenLight,
                    contentColor = ForestGreenDark
                ) {
                    repeatOptions.forEachIndexed { index, label ->
                        Tab(
                            selected = repeatTypeIndex == index,
                            onClick = { repeatTypeIndex = index },
                            text = { Text(label) }
                        )
                    }
                }
            }

            // Day chips — visible for WEEKLY and SPECIFIC_DAYS
            if (currentRepeatType == RepeatType.WEEKLY || currentRepeatType == RepeatType.SPECIFIC_DAYS) {
                Column {
                    Text("Select Days", style = MaterialTheme.typography.labelLarge, color = TextBrown)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ALL_DAYS.forEach { day ->
                            val isSelected = day in selectedDays
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) ForestGreen else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) ForestGreen else TextMutedBrown.copy(alpha = 0.3f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    DAY_LABELS[day] ?: day,
                                    color = if (isSelected) Color.White else TextBrown,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            PrimaryButton(
                text = "Save Reminder",
                onClick = {
                    when {
                        title.isBlank() -> titleError = "Title is required"
                        (currentRepeatType == RepeatType.WEEKLY || currentRepeatType == RepeatType.SPECIFIC_DAYS)
                                && selectedDays.isEmpty() -> {
                            // For weekly, default to all days if none selected
                        }
                        else -> {
                            val daysCsv = when (currentRepeatType) {
                                RepeatType.DAILY -> null
                                RepeatType.WEEKLY -> if (selectedDays.isEmpty()) "MON,TUE,WED,THU,FRI,SAT,SUN"
                                    else selectedDays.joinToString(",")
                                RepeatType.SPECIFIC_DAYS -> selectedDays.joinToString(",")
                            }
                            val timeStr = String.format("%02d:%02d", hour, minute)
                            onSave(title.trim(), timeStr, currentRepeatType, daysCsv)
                        }
                    }
                }
            )

            // Cancel
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = TextMutedBrown)
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────
private fun formatTime(time: String): String {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val period = if (h < 12) "AM" else "PM"
    val displayH = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return String.format("%d:%02d %s", displayH, m, period)
}

private fun formatRepeatLabel(reminder: FamilyReminder): String {
    return when (reminder.repeatType) {
        RepeatType.DAILY -> "Daily"
        RepeatType.WEEKLY -> {
            val days = reminder.daysOfWeek?.split(",")?.map { it.trim() }
            if (days.isNullOrEmpty() || days.size == 7) "Every day"
            else days.joinToString(", ") { DAY_LABELS[it] ?: it }
        }
        RepeatType.SPECIFIC_DAYS -> {
            val days = reminder.daysOfWeek?.split(",")?.map { it.trim() }
            if (days.isNullOrEmpty()) "No days set"
            else days.joinToString(", ") { DAY_LABELS[it] ?: it }
        }
    }
}

// ── Preview ────────────────────────────────────────────────────────────────
@Preview(name = "Reminders – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun RemindersScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        // Preview uses a dummy; real screen needs RemindersViewModel
        Text("Reminders Preview (Parent)")
    }
}

@Preview(name = "Reminders – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun RemindersScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        Text("Reminders Preview (Child)")
    }
}
