package com.biglitecode.familyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.biglitecode.familyhub.ui.privacy.PrivacyPolicyScreen
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = CreamBackground) {
                    PrivacyPolicyScreen(onBack = { finish() })
                }
            }
        }
    }
}
