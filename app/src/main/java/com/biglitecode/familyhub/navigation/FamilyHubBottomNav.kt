package com.biglitecode.familyhub.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenDark
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

/**
 * A floating pill-shaped bottom navigation bar with a fixed brand circle
 * for the Home button on the left.
 */
@Composable
fun FamilyHubBottomNav(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onBrandClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        // ── The pill bar ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PILL_HEIGHT)
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = ForestGreen.copy(alpha = 0.15f),
                    spotColor = ForestGreen.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(50))
                .background(CardCream)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PILL_HEIGHT),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spacer for the fixed home circle
                Spacer(modifier = Modifier.size(BRAND_SIZE))
                items.forEachIndexed { index, item ->
                    val isActive = selectedIndex == index
                    NavItemCell(
                        item = item,
                        isActive = isActive,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Fixed brand circle (Home button) — always at the left ─────
        val isBrandActive = selectedIndex == -1
        Box(
            modifier = Modifier
                .size(BRAND_SIZE)
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp, y = (-8).dp)
                .shadow(
                    elevation = if (isBrandActive) 16.dp else 12.dp,
                    shape = CircleShape,
                    ambientColor = ForestGreen.copy(alpha = 0.25f),
                    spotColor = ForestGreen.copy(alpha = 0.25f)
                )
                .clip(CircleShape)
                .background(if (isBrandActive) ForestGreenDark else ForestGreen)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBrandClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            // Small gold accent dot
            Box(
                modifier = Modifier
                    .size(if (isBrandActive) 14.dp else 12.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-3).dp, y = (-3).dp)
                    .clip(CircleShape)
                    .background(if (isBrandActive) GoldYellow else GoldYellow.copy(alpha = 0.7f))
            )
        }
    }
}

// ── Sizing constants ───────────────────────────────────────────────────
private val BRAND_SIZE = 72.dp
private val PILL_HEIGHT = 68.dp

// ── Single nav item cell (icon + label) ───────────────────────────────
@Composable
private fun NavItemCell(
    item: BottomNavItem,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint = if (isActive) ForestGreenDark else TextMutedBrown
    val labelColor = if (isActive) ForestGreenDark else TextMutedBrown
    val icon = if (isActive) item.icon else item.iconOutlined
    val labelWeight = if (isActive) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = item.label,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = labelWeight,
            color = labelColor
        )
        if (isActive) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(ForestGreen)
            )
        }
    }
}
