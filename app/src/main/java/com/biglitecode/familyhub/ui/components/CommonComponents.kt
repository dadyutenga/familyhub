package com.biglitecode.familyhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
// Column used in component previews
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.model.Task
import com.biglitecode.familyhub.data.model.TaskStatus
import com.biglitecode.familyhub.ui.preview.FamilyHubThemePreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.GoldYellowLight
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun parseAvatarColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return ForestGreen
    return try {
        val cleaned = hex.removePrefix("0x").removePrefix("#")
        Color(cleaned.toLong(16) or 0xFF000000)
    } catch (_: Exception) {
        ForestGreen
    }
}

fun formatDate(millis: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
}

@Composable
fun MemberAvatar(
    member: FamilyMember,
    modifier: Modifier = Modifier,
    size: Int = 48,
    onClick: (() -> Unit)? = null
) {
    val color = parseAvatarColor(member.avatarColor)
    val initials = member.name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(2.5.dp, color, CircleShape)
            .background(color.copy(alpha = 0.15f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = (size / 2.8).sp
        )
    }
}

@Composable
fun PillHeader(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(ForestGreenLight)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = ForestGreen,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun StatusPill(status: TaskStatus) {
    val (bg, fg, label) = when (status) {
        TaskStatus.DONE -> Triple(ForestGreenLight, ForestGreen, "Done")
        TaskStatus.PENDING -> Triple(GoldYellowLight, TextBrown, "Pending")
        TaskStatus.OVERDUE -> Triple(CoralRed.copy(alpha = 0.15f), CoralRed, "Overdue")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RolePill(role: FamilyRole) {
    val (bg, fg, label) = if (role == FamilyRole.PARENT) {
        Triple(ForestGreenLight, ForestGreen, "Parent/Guardian")
    } else {
        Triple(GoldYellowLight, TextBrown, "Child/Member")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (task.status) {
        TaskStatus.DONE -> ForestGreen
        TaskStatus.PENDING -> GoldYellow
        TaskStatus.OVERDUE -> CoralRed
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CardCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                StatusPill(task.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedBrown,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "→ ${task.assignedToName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestGreen,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${task.rewardPoints} pts · ${formatDate(task.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedBrown
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.12f)),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = message,
            color = CoralRed,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large,
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = ForestGreen,
            contentColor = Color.White,
            disabledContainerColor = ForestGreen.copy(alpha = 0.4f)
        )
    ) {
        if (loading) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}

private val sampleMember = FamilyMember(
    id = "m1",
    name = "Amina Mwangi",
    role = FamilyRole.PARENT,
    avatarColor = "0xFF2F6B44"
)

private val sampleTask = Task(
    id = "t1",
    title = "Wash the dishes",
    description = "Clean all plates and utensils after dinner.",
    assignedTo = "m3",
    assignedToName = "Wanjiku",
    assignedBy = "m1",
    dueDate = System.currentTimeMillis(),
    status = TaskStatus.PENDING,
    createdAt = System.currentTimeMillis(),
    rewardPoints = 15
)

@Preview(name = "Components – TaskCard", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun TaskCardPreview() {
    FamilyHubThemePreview {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PillHeader("My Family")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MemberAvatar(member = sampleMember, size = 56)
                RolePill(FamilyRole.PARENT)
                StatusPill(TaskStatus.DONE)
                StatusPill(TaskStatus.PENDING)
            }
            TaskCard(task = sampleTask, onClick = {})
            ErrorBanner("No internet connection…")
            PrimaryButton(text = "Login", onClick = {})
        }
    }
}
