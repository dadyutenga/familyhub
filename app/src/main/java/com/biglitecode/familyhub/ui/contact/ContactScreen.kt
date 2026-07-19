package com.biglitecode.familyhub.ui.contact

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.components.MemberAvatar
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.GoldYellow
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun ContactScreen(viewModel: TasksViewModel) {
    val members by viewModel.members.collectAsStateWithLifecycle()
    val admin = remember(members) { members.find { it.role == FamilyRole.PARENT } }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Contact Family Admin", style = MaterialTheme.typography.headlineMedium, color = TextBrown)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ForestGreen, MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (admin != null) {
                    MemberAvatar(member = admin, size = 72)
                    Spacer(Modifier.height(12.dp))
                    Text(admin.name, style = MaterialTheme.typography.titleLarge, color = TextBrown)
                    Text("Parent / Family Admin", color = TextMutedBrown)
                    if (admin.phoneNumber.isNotBlank()) {
                        Text(admin.phoneNumber, color = ForestGreen)
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Call",
                        enabled = admin.phoneNumber.isNotBlank(),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${admin.phoneNumber}"))
                            context.startActivity(intent)
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${admin.phoneNumber}")).apply {
                                putExtra("sms_body", "Hi ${admin.name}, message from FamilyHub.")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "No SMS app available", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = admin.phoneNumber.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = GoldYellow,
                            contentColor = TextBrown
                        )
                    ) {
                        Text("Message", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                } else {
                    Text("No family admin found.", color = TextMutedBrown)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = CardCream)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Support", style = MaterialTheme.typography.titleLarge, color = TextBrown)
                Text("support@familyhub.app", color = ForestGreen)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Send Feedback",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@familyhub.app"))
                            putExtra(Intent.EXTRA_SUBJECT, "FamilyHub feedback")
                        }
                        try {
                            context.startActivity(Intent.createChooser(intent, "Send email"))
                        } catch (_: Exception) {
                            Toast.makeText(context, "No email app available", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Preview(name = "Contact", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ContactScreenPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        ContactScreen(viewModel = vm)
    }
}
