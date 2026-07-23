package com.biglitecode.familyhub.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.TextBrown

// ── Colors ─────────────────────────────────────────────────────────────
private val DrawerGradientStart = Color(0xFF2E5339)
private val DrawerGradientEnd = Color(0xFF3F7350)
private val LogoutBg = Color(0xFFFDECEC)
private val IconTintBg = ForestGreenLight.copy(alpha = 0.6f)

// ── Main drawer content ────────────────────────────────────────────────
@Composable
fun FamilyHubDrawerContent(
    user: FamilyMember?,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardCream)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────
        DrawerHeader(user = user)

        Spacer(Modifier.height(16.dp))

        // ── Section 1: Home, Account & Settings ──────────────────────
        DrawerSection {
            DrawerMenuItem(
                icon = Icons.Filled.Home,
                label = "Home",
                isActive = currentRoute == Routes.HOME,
                onClick = { onItemClick(Routes.HOME) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.AccountCircle,
                label = "Account",
                isActive = currentRoute == Routes.ACCOUNT,
                onClick = { onItemClick(Routes.ACCOUNT) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.Settings,
                label = "Settings",
                isActive = currentRoute == Routes.SETTINGS,
                onClick = { onItemClick(Routes.SETTINGS) }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = ForestGreenLight
        )

        // ── Section 2: Info & Support ─────────────────────────────────
        DrawerSection {
            DrawerMenuItem(
                icon = Icons.Filled.Feedback,
                label = "Task Feedback",
                isActive = currentRoute == Routes.FEEDBACK,
                onClick = { onItemClick(Routes.FEEDBACK) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.ReportProblem,
                label = "Complaints",
                isActive = currentRoute == Routes.COMPLAINS,
                onClick = { onItemClick(Routes.COMPLAINS) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.Notifications,
                label = "Reminders",
                isActive = currentRoute == Routes.REMINDERS,
                onClick = { onItemClick(Routes.REMINDERS) }
            )
            // App Usage — parent only
            if (user?.role == FamilyRole.PARENT) {
                Spacer(Modifier.height(4.dp))
                DrawerMenuItem(
                    icon = Icons.Filled.BarChart,
                    label = "App Usage",
                    isActive = currentRoute == Routes.APP_USAGE,
                    onClick = { onItemClick(Routes.APP_USAGE) }
                )
            }
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.Help,
                label = "Help & FAQ",
                isActive = currentRoute == Routes.HELP,
                onClick = { onItemClick(Routes.HELP) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.ContactPhone,
                label = "Contact",
                isActive = currentRoute == Routes.CONTACT,
                onClick = { onItemClick(Routes.CONTACT) }
            )
            Spacer(Modifier.height(4.dp))
            DrawerMenuItem(
                icon = Icons.Filled.PrivacyTip,
                label = "Privacy Policy",
                isActive = currentRoute == Routes.PRIVACY,
                onClick = { onItemClick(Routes.PRIVACY) }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            color = ForestGreenLight
        )

        // ── Section 3: Logout ─────────────────────────────────────────
        DrawerSection {
            LogoutItem(onClick = onLogout)
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Header ─────────────────────────────────────────────────────────────
@Composable
private fun DrawerHeader(user: FamilyMember?) {
    val initials = user?.name
        ?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercase() }
        ?.take(2)
        ?.joinToString("")
        ?: "?"

    val roleLabel = when (user?.role) {
        FamilyRole.PARENT -> "Parent / Guardian"
        FamilyRole.CHILD -> "Child / Member"
        null -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(DrawerGradientStart, DrawerGradientEnd)
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // App name
            Text(
                text = "FamilyHub",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(4.dp))

            // Username
            Text(
                text = user?.name ?: "",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )

            if (roleLabel.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                // Role pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = roleLabel,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Section wrapper ────────────────────────────────────────────────────
@Composable
private fun DrawerSection(content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        content()
    }
}

// ── Single menu item ───────────────────────────────────────────────────
@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isActive) ForestGreenLight else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon inside tinted circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IconTintBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = ForestGreenDark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = TextBrown
        )
    }
}

// ── Logout item ────────────────────────────────────────────────────────
@Composable
private fun LogoutItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LogoutBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red circle with white icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CoralRed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Logout",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Text(
            text = "Logout",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = CoralRed
        )
    }
}
