package com.biglitecode.familyhub.ui.appusage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.AppUsageEntry
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.components.MemberAvatar
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(viewModel: AppUsageViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val children by viewModel.children.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT

    if (!isParent) {
        // Children don't see this screen
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("App usage is viewable by parents only.", color = TextMutedBrown)
        }
        return
    }

    if (children.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.ChildCare,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = TextMutedBrown.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(16.dp))
                Text("No children in your family yet.", color = TextMutedBrown)
            }
        }
        return
    }

    // Selected child tab index
    var selectedChildIndex by remember { mutableIntStateOf(0) }
    val selectedChild = children.getOrNull(selectedChildIndex) ?: children.first()

    // Date filter
    val availableDates by viewModel.availableDates(selectedChild.id).collectAsStateWithLifecycle()
    var selectedDateFilter by remember { mutableStateOf("today") } // "today", "week", or specific date

    // Usage data for selected child
    val allUsageForChild by viewModel.usageForChild(selectedChild.id).collectAsStateWithLifecycle()

    // Apply date filter
    val filteredUsage = remember(allUsageForChild, selectedDateFilter) {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -7) }
        val weekAgo = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(cal.time)

        when (selectedDateFilter) {
            "today" -> allUsageForChild.filter { it.date == today }
            "week" -> allUsageForChild.filter { it.date >= weekAgo }
            else -> allUsageForChild.filter { it.date == selectedDateFilter }
        }.sortedByDescending { it.usageMinutes }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Child tabs
        if (children.size > 1) {
            PrimaryTabRow(
                selectedTabIndex = selectedChildIndex.coerceIn(0, children.lastIndex),
                containerColor = ForestGreenLight,
                contentColor = ForestGreenDark
            ) {
                children.forEachIndexed { index, child ->
                    Tab(
                        selected = selectedChildIndex == index,
                        onClick = { selectedChildIndex = index },
                        text = { Text(child.name) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        } else {
            Text(
                "${selectedChild.name}'s App Usage",
                style = MaterialTheme.typography.headlineMedium,
                color = TextBrown
            )
            Spacer(Modifier.height(12.dp))
        }

        // Date filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DateChip("Today", selectedDateFilter == "today") { selectedDateFilter = "today" }
            DateChip("This Week", selectedDateFilter == "week") { selectedDateFilter = "week" }
            if (availableDates.isNotEmpty()) {
                DateChip("All Time", selectedDateFilter == "all") { selectedDateFilter = "all" }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (filteredUsage.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Apps,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = TextMutedBrown.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No usage data available",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMutedBrown
                    )
                    Text(
                        "Ask ${selectedChild.name} to enable Usage Access in Settings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedBrown.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Total screen time summary
            val totalMinutes = filteredUsage.sumOf { it.usageMinutes }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = ForestGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Total Screen Time",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            formatMinutes(totalMinutes),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // App list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(filteredUsage) { index, entry ->
                    AppUsageCard(
                        rank = index + 1,
                        entry = entry,
                        maxMinutes = filteredUsage.firstOrNull()?.usageMinutes ?: 1
                    )
                }
            }
        }
    }
}

@Composable
private fun DateChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ForestGreen,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun AppUsageCard(
    rank: Int,
    entry: AppUsageEntry,
    maxMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = CardCream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> GoldYellow
                            2 -> ForestGreenLight
                            3 -> ForestGreenLight.copy(alpha = 0.6f)
                            else -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    fontWeight = FontWeight.Bold,
                    color = if (rank <= 3) ForestGreenDark else TextMutedBrown,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // App icon placeholder (colored circle with initial)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ForestGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.appName.firstOrNull()?.uppercase() ?: "?",
                    color = ForestGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // App name + usage bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextBrown,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                // Usage bar
                val barFraction = if (maxMinutes > 0) entry.usageMinutes.toFloat() / maxMinutes else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ForestGreenLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = barFraction.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ForestGreen)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Usage time
            Text(
                formatMinutes(entry.usageMinutes),
                style = MaterialTheme.typography.bodyLarge,
                color = ForestGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> {
            val h = minutes / 60
            val m = minutes % 60
            if (m > 0) "${h}h ${m}m" else "${h}h"
        }
        else -> {
            val d = minutes / 1440
            val h = (minutes % 1440) / 60
            if (h > 0) "${d}d ${h}h" else "${d}d"
        }
    }
}

@Preview(name = "App Usage – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun AppUsageScreenPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { _ ->
        Text("App Usage Preview")
    }
}
