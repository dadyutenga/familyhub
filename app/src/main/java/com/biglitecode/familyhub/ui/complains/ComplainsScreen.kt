package com.biglitecode.familyhub.ui.complains

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.components.formatDate
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.ForestGreenLight
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun ComplainsScreen(viewModel: TasksViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val allComplaints by viewModel.complaints.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT
    val complaints = if (isParent) {
        allComplaints
    } else {
        allComplaints.filter { it.userId == user?.id }
    }

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Family Complaints", style = MaterialTheme.typography.headlineMedium, color = TextBrown)
            Text(
                if (isParent) "View and manage all family complaints"
                else "Submit and track your complaints",
                color = TextMutedBrown
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    PrimaryButton(
                        text = "Submit",
                        onClick = {
                            if (subject.isBlank() || description.isBlank()) {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.submitComplaint(subject.trim(), description.trim())
                                subject = ""
                                description = ""
                                Toast.makeText(context, "Complaint submitted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = subject.isNotBlank() && description.isNotBlank()
                    )
                }
            }
        }

        item {
            Text("Previous complaints", style = MaterialTheme.typography.titleLarge, color = TextBrown)
        }

        if (complaints.isEmpty()) {
            item { Text("No complaints yet.", color = TextMutedBrown) }
        }

        items(complaints, key = { it.id }) { complaint ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text(
                            complaint.subject,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextBrown,
                            modifier = Modifier.weight(1f)
                        )
                        val (bg, fg, label) = if (complaint.resolved) {
                            Triple(ForestGreenLight, ForestGreen, "Resolved")
                        } else {
                            Triple(CoralRed.copy(alpha = 0.15f), CoralRed, "Open")
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(bg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        complaint.description,
                        color = TextMutedBrown,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${complaint.userName} · ${formatDate(complaint.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMutedBrown
                    )
                    if (isParent && !complaint.resolved) {
                        TextButton(onClick = {
                            viewModel.resolveComplaint(complaint)
                            Toast.makeText(context, "Marked resolved", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Mark resolved", color = ForestGreen)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Complaints – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ComplainsScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        ComplainsScreen(viewModel = vm)
    }
}

@Preview(name = "Complaints – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun ComplainsScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        ComplainsScreen(viewModel = vm)
    }
}
