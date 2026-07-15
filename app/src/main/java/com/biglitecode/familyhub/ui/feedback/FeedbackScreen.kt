package com.biglitecode.familyhub.ui.feedback

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun FeedbackScreen(viewModel: TasksViewModel) {
    val tasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val doneTasks = tasks.filter { it.status == TaskStatus.DONE }
    val ratings = remember { mutableStateMapOf<String, Int>() }
    val comments = remember { mutableStateMapOf<String, String>() }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Task Feedback", style = MaterialTheme.typography.headlineMedium, color = TextBrown)
            Text("Rate completed tasks", color = TextMutedBrown)
        }

        if (doneTasks.isEmpty()) {
            item {
                Text("No completed tasks yet.", color = TextMutedBrown)
            }
        }

        items(doneTasks, key = { it.id }) { task ->
            var rating by remember(task.id) { mutableIntStateOf(ratings[task.id] ?: 0) }
            var comment by remember(task.id) { mutableStateOf(comments[task.id].orEmpty()) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, color = TextBrown)
                    Text("by ${task.assignedToName}", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "$star stars",
                                tint = GoldYellow,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .clickable {
                                        rating = star
                                        ratings[task.id] = star
                                    }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = {
                            comment = it
                            comments[task.id] = it
                        },
                        label = { Text("Optional comment") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = "Submit feedback",
                        onClick = {
                            if (rating == 0) {
                                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.submitFeedback(task.id, rating, comment)
                                Toast.makeText(context, "Feedback saved", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = rating > 0
                    )
                }
            }
        }
    }
}

@Preview(name = "Task Feedback", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun FeedbackScreenPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        FeedbackScreen(viewModel = vm)
    }
}
