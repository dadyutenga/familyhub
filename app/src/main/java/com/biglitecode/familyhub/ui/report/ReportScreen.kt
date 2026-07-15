package com.biglitecode.familyhub.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

data class LeaderboardEntry(
    val memberId: String,
    val name: String,
    val doneCount: Int,
    val points: Int
)

@Composable
fun ReportScreen(viewModel: TasksViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()

    val leaderboard = remember(tasks, members) {
        members.map { member ->
            val memberTasks = tasks.filter { it.assignedTo == member.id && it.status == TaskStatus.DONE }
            LeaderboardEntry(
                memberId = member.id,
                name = member.name,
                doneCount = memberTasks.size,
                points = memberTasks.sumOf { it.rewardPoints }
            )
        }.sortedByDescending { it.doneCount }
    }
    val totalDone = leaderboard.sumOf { it.doneCount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PillHeader("Family Leaderboard")
            Spacer(Modifier.height(6.dp))
            Text("This week · all family tasks", color = TextMutedBrown)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = ForestGreenLight)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total tasks completed", color = TextMutedBrown)
                    Text(
                        text = "$totalDone",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                }
            }
        }

        itemsIndexed(leaderboard) { index, entry ->
            val member = members.find { it.id == entry.memberId }
            val rankBg = if (index == 0) GoldYellow else ForestGreenLight
            val rankFg = if (index == 0) TextBrown else ForestGreen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.5.dp,
                        if (index == 0) GoldYellow else ForestGreen.copy(alpha = 0.3f),
                        MaterialTheme.shapes.medium
                    ),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = if (index == 0) GoldYellowLight else CardCream
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(rankBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = rankFg
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    if (member != null) {
                        MemberAvatar(member = member, size = 44)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.name, fontWeight = FontWeight.SemiBold, color = TextBrown)
                        Text("${entry.doneCount} tasks done", color = TextMutedBrown, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(GoldYellowLight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("★ ${entry.points}", color = TextBrown, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(name = "Report / Leaderboard", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ReportScreenPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        ReportScreen(viewModel = vm)
    }
}
