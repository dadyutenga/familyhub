package com.biglitecode.familyhub.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.biglitecode.familyhub.data.model.FamilyMember
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.repository.SupabaseFamilyRepository
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme

/** Shared device specs for Android Studio previews */
object PreviewDevices {
    const val PHONE = "spec:width=411dp,height=891dp,dpi=420"
}

/** Generic preview users with no demo data. Phone/email are intentionally blank. */
val PreviewParent = FamilyMember(
    id = "preview_parent",
    name = "Parent User",
    role = FamilyRole.PARENT,
    avatarColor = "0xFF2F6B44",
    phoneNumber = "",
    email = "",
    familyGroupId = "preview_group"
)

val PreviewChild = FamilyMember(
    id = "preview_child",
    name = "Child User",
    role = FamilyRole.CHILD,
    avatarColor = "0xFFE05C5C",
    phoneNumber = "",
    email = "",
    familyGroupId = "preview_group"
)

/**
 * Wraps any screen in FamilyHub theme + cream background for previews.
 * Seeds [SessionManager] with a generic preview user and builds a [TasksViewModel]
 * on the empty-data SupabaseFamilyRepository. This is intentionally for @Preview tooling only.
 */
@Composable
fun FamilyHubPreview(
    role: FamilyRole = FamilyRole.PARENT,
    content: @Composable (TasksViewModel) -> Unit
) {
    val viewModel = remember { TasksViewModel(SupabaseFamilyRepository.getInstance()) }
    val user = if (role == FamilyRole.PARENT) PreviewParent else PreviewChild

    DisposableEffect(user.id) {
        SessionManager.setUser(user)
        onDispose { /* keep session for other previews */ }
    }

    FamilyHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CreamBackground
        ) {
            content(viewModel)
        }
    }
}

@Composable
fun FamilyHubThemePreview(content: @Composable () -> Unit) {
    FamilyHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = CreamBackground
        ) {
            content()
        }
    }
}

@Preview(name = "Phone", showBackground = true, device = PreviewDevices.PHONE)
annotation class PhonePreview
