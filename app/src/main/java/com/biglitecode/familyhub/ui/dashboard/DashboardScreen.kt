package com.biglitecode.familyhub.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.ui.components.MemberAvatar
import com.biglitecode.familyhub.ui.components.PillHeader
import com.biglitecode.familyhub.ui.components.TaskCard
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.GoldYellowLight
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun DashboardScreen(
    viewModel: TasksViewModel,
    onTaskClick: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val visibleTasks by viewModel.visibleTasks.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT
    val tasksHeader = if (isParent) "Today's Tasks" else "My Tasks"
    val doneCount = visibleTasks.count { it.status == TaskStatus.DONE }
    val totalPoints = visibleTasks.filter { it.status == TaskStatus.DONE }.sumOf { it.rewardPoints }
    val progress = if (visibleTasks.isEmpty()) 0f else doneCount.toFloat() / visibleTasks.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            PillHeader("My Family")
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hi, ${user?.name?.substringBefore(" ") ?: "there"} 👋",
                style = MaterialTheme.typography.headlineMedium,
                color = TextBrown
            )
            Text(
                text = if (isParent) "Here's how your family is doing today"
                else "Here are your tasks for today",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedBrown
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                members.forEach { member ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MemberAvatar(member = member, size = 56)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = member.name.substringBefore(" "),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextBrown
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, GoldYellow, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = GoldYellowLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Weekly Rewards", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$totalPoints",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text("points earned this week", color = TextMutedBrown)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = GoldYellow,
                        trackColor = CardCream
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (progress >= 0.7f) "Amazing work — keep it up! 🌟"
                        else "Complete more tasks to climb the board!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedBrown
                    )
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tasksHeader,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextBrown,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ForestGreenLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${visibleTasks.size}",
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (visibleTasks.isEmpty()) {
            item {
                Text(
                    "No tasks yet. ${if (isParent) "Tap Tasks and add one!" else "Ask a parent to assign tasks."}",
                    color = TextMutedBrown
                )
            }
        } else {
            items(visibleTasks.take(6), key = { it.id }) { task ->
                TaskCard(task = task, onClick = { onTaskClick(task.id) })
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Preview(name = "Dashboard – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun DashboardScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        DashboardScreen(viewModel = vm, onTaskClick = {})
    }
}

@Preview(name = "Dashboard – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun DashboardScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        DashboardScreen(viewModel = vm, onTaskClick = {})
    }
}
