package com.biglitecode.familyhub.ui.account

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.ui.components.MemberAvatar
import com.biglitecode.familyhub.ui.components.PrimaryButton
import com.biglitecode.familyhub.ui.components.RolePill
import com.biglitecode.familyhub.ui.preview.FamilyHubPreview
import com.biglitecode.familyhub.ui.preview.PreviewDevices
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CoralRed
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import com.biglitecode.familyhub.ui.theme.TextMutedBrown

@Composable
fun AccountScreen(viewModel: TasksViewModel) {
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val group by viewModel.familyGroup.collectAsStateWithLifecycle()
    val isParent = user?.role == FamilyRole.PARENT
    val context = LocalContext.current
    var memberToRemove by remember { mutableStateOf<FamilyMember?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            if (user != null) {
                MemberAvatar(member = user!!, size = 96)
                Spacer(Modifier.height(12.dp))
                Text(user!!.name, style = MaterialTheme.typography.headlineMedium, color = TextBrown)
                Text(user!!.email, color = TextMutedBrown)
                Spacer(Modifier.height(8.dp))
                RolePill(user!!.role)
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Family group", color = TextMutedBrown, style = MaterialTheme.typography.bodySmall)
                    Text(group?.name ?: "—", style = MaterialTheme.typography.titleLarge)
                    Text("${members.size} members", color = TextMutedBrown)
                }
            }
        }

        item {
            PrimaryButton(text = "Edit Profile", onClick = {
                Toast.makeText(context, "Profile editing coming soon", Toast.LENGTH_SHORT).show()
            })
        }

        item {
            Text(
                if (isParent) "Manage Family" else "Family members",
                style = MaterialTheme.typography.titleLarge,
                color = TextBrown,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(members, key = { it.id }) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = CardCream)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MemberAvatar(member = member, size = 44)
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(member.name, color = TextBrown, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        Text(
                            if (member.role == FamilyRole.PARENT) "Parent/Guardian" else "Child/Member",
                            color = TextMutedBrown,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (isParent && member.id != user?.id) {
                        IconButton(onClick = { memberToRemove = member }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = CoralRed)
                        }
                    }
                }
            }
        }
    }

    memberToRemove?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text("Remove ${member.name}?") },
            text = { Text("They will lose access to this family group.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeMember(member.id)
                    memberToRemove = null
                    Toast.makeText(context, "${member.name} removed", Toast.LENGTH_SHORT).show()
                }) { Text("Remove", color = CoralRed) }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) { Text("Cancel") }
            }
        )
    }
}

@Preview(name = "Account – Parent", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun AccountScreenParentPreview() {
    FamilyHubPreview(role = FamilyRole.PARENT) { vm ->
        AccountScreen(viewModel = vm)
    }
}

@Preview(name = "Account – Child", showBackground = true, device = PreviewDevices.PHONE)
@Composable
private fun AccountScreenChildPreview() {
    FamilyHubPreview(role = FamilyRole.CHILD) { vm ->
        AccountScreen(viewModel = vm)
    }
}
