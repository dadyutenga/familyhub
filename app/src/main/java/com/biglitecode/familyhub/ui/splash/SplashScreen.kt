package com.biglitecode.familyhub.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.GoldYellow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    animate: Boolean = true,
    onFinished: () -> Unit = {}
) {
    val scale = remember { Animatable(if (animate) 0.6f else 1f) }
    LaunchedEffect(animate) {
        if (animate) {
            scale.animateTo(1f, animationSpec = tween(900))
            delay(600)
            onFinished()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(ForestGreen, ForestGreen.copy(alpha = 0.85f), GoldYellow.copy(alpha = 0.35f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = CardCream,
                shadowElevation = 8.dp,
                modifier = Modifier.size(110.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "FamilyHub",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Tasks · Care · Together",
                color = CardCream,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(name = "Splash", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun SplashScreenPreview() {
    FamilyHubTheme {
        SplashScreen(animate = false)
    }
}
