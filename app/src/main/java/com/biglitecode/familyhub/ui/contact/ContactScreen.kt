package com.biglitecode.familyhub.ui.contact

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT
    val context = LocalContext.current

    // Parents whose contact info is shown to children
    val parents = remember(members) { members.filter { it.role == FamilyRole.PARENT } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── PARENT: Edit own phone number ───────────────────────────────
        if (isParent) {
            Text("Your Contact Number", style = MaterialTheme.typography.headlineMedium, color = TextBrown)

            var phoneInput by remember(user?.phoneNumber) {
                mutableStateOf(user?.phoneNumber.orEmpty())
            }
            var phoneError by remember { mutableStateOf<String?>(null) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = {
                            phoneInput = it
                            phoneError = null
                        },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+255 7XX XXX XXX") },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = ForestGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrimaryButton(
                        text = "Save",
                        onClick = {
                            val cleaned = phoneInput.replace(Regex("[\\s\\-()]"), "")
                            when {
                                cleaned.isBlank() -> phoneError = "Phone number is required"
                                !cleaned.matches(Regex("^\\+?\\d{7,15}$")) -> phoneError = "Enter a valid phone number (7-15 digits)"
                                else -> {
                                    viewModel.updatePhoneNumber(phoneInput.trim())
                                    phoneError = null
                                    Toast.makeText(context, "Phone number saved", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        // ── CHILD: Show all parents' contact cards ──────────────────────
        if (!isParent) {
            Text("Contact Your Parents", style = MaterialTheme.typography.headlineMedium, color = TextBrown)

            val parentsWithPhone = parents.filter { it.phoneNumber.isNotBlank() }

            if (parentsWithPhone.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = CardCream)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Your parent hasn't added a contact number yet",
                            color = TextMutedBrown,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                parentsWithPhone.forEach { parent ->
                    ParentContactCard(
                        parentName = parent.name,
                        phoneNumber = parent.phoneNumber,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${parent.phoneNumber}"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        // ── Support (visible to all) ────────────────────────────────────
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

@Composable
private fun ParentContactCard(
    parentName: String,
    phoneNumber: String,
    onCall: () -> Unit
) {
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
            // Avatar initial
            val initials = parentName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2)
                .joinToString("")

            MemberAvatar(
                member = com.biglitecode.familyhub.data.model.FamilyMember(
                    id = "",
                    name = parentName,
                    role = FamilyRole.PARENT
                ),
                size = 72
            )
            Spacer(Modifier.height(12.dp))
            Text(parentName, style = MaterialTheme.typography.titleLarge, color = TextBrown)
            Text("Parent / Guardian", color = TextMutedBrown)
            Spacer(Modifier.height(4.dp))
            Text(
                phoneNumber,
                color = ForestGreen,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(Icons.Filled.Call, contentDescription = "Call")
                Spacer(Modifier.width(8.dp))
                Text("Call", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(name = "Contact – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ContactScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        ContactScreen(viewModel = vm)
    }
}

@Preview(name = "Contact – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ContactScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        ContactScreen(viewModel = vm)
    }
}
