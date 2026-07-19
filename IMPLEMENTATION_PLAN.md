# FamilyHub Implementation Plan

> This document records what was implemented in the current `familyhub` Android project (v1) and provides a roadmap for building **v2** with a fresh repository.

---

## 1. Project Overview

| Item | Value |
|------|-------|
| **Project Name** | FamilyHub |
| **Package** | `com.biglitecode.familyhub` |
| **Language** | Kotlin |
| **UI Toolkit** | Jetpack Compose |
| **Local Database** | Room (scaffolded, not wired to production) |
| **Backend** | Supabase (Postgres + Auth + Realtime) |
| **Build System** | Gradle Kotlin DSL |

### Current Entry Point
- `SplashActivity` is the launcher activity.
- Authentication, dashboard, tasks, and settings are separate activities.
- The repository currently in use is `FakeTaskRepository` (in-memory demo data).

---

## 2. What Was Implemented in v1

### 2.1 Supabase Dependencies
Updated `app/build.gradle.kts` to use Supabase v3 BOM and modules:

```kotlin
implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
implementation("io.github.jan-tennert.supabase:postgrest-kt")
implementation("io.github.jan-tennert.supabase:auth-kt")
implementation("io.github.jan-tennert.supabase:realtime-kt")
implementation("io.github.jan-tennert.supabase:storage-kt")
implementation("io.ktor:ktor-client-android:2.3.11")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
```

Also added the Kotlin serialization plugin (`org.jetbrains.kotlin.plugin.serialization`) and `kotlinx-serialization-json` because `@Serializable` models are used.

### 2.2 Supabase Client Providers
Created two client providers during the setup:

1. **`core/SupabaseClient.kt`** — new `SupabaseClientProvider` object requested by the user.
2. **`data/remote/SupabaseClient.kt`** — existing `SupabaseProvider` object updated from Supabase v2 (`gotrue`) to v3 (`auth`).

Both now read credentials from `BuildConfig`:

```kotlin
val client by lazy {
    createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
```

### 2.3 Supabase Credentials (Secure via local.properties)
Credentials are stored in `local.properties` (which is `.gitignore`d) and injected into `BuildConfig` at build time:

```properties
# local.properties
SUPABASE_URL=https://fjsftwxvfkxalkdqqmqg.supabase.co
SUPABASE_ANON_KEY=sb_publishable_5J7yW4emAP8nMtQCwZzKWw_hWYrC9j5
```

```kotlin
// app/build.gradle.kts
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""}\"")
    }
}
```

> ✅ **Security note:** The real credentials are no longer in the committed source code. They live only in `local.properties` on the local machine.

### 2.4 Demo MainActivity + TodoItem
Created `MainActivity.kt` and `TodoItem.kt` as a quick Supabase Compose demo:

- `MainActivity.kt` fetches rows from the `todos` table and renders them in a `LazyColumn`.
- `TodoItem.kt` is a `@Serializable` data class: `data class TodoItem(val id: Int, val name: String)`.
- `MainActivity` was registered in `AndroidManifest.xml` as a non-launcher activity.

### 2.5 Demo Data Removal Sweep
All hardcoded demo, sample, mock, and placeholder data was removed from the runtime code:

- **Created `SupabaseFamilyRepository`** — a stub repository that returns `emptyList()` and `Result.failure` for every call, with `// TODO(supabase):` comments tracking each real Supabase wire-up.
- **Wired real app entry points** to `SupabaseFamilyRepository`:
  - `FamilyHubApp`
  - `LoginActivity`
  - `SignUpActivity`
  - `ResetPasswordActivity`
- **Kept `FakeTaskRepository`** but marked it **PREVIEW-ONLY**; it is now only used by `@Preview` composables via `PreviewUtils.kt`.
- **Removed demo files**:
  - `MainActivity.kt`
  - `TodoItem.kt`
- **Removed demo hints from auth screens**:
  - Login screen demo account hints
  - Reset password "(demo: success)" message
  - Sign-up "try MWNG2026" invite-code hint
- **Redesigned `LoginScreen.kt`** with a branded header gradient, overlapping form card,
  custom gradient button, error banner, and footer. It now uses the hoisted signature:
  ```kotlin
  fun LoginScreen(
      isLoading: Boolean,
      errorMessage: String?,
      onLoginClick: (email: String, password: String) -> Unit,
      onSignUpClick: () -> Unit,
      onForgotPasswordClick: () -> Unit
  )
  ```
  All colors resolve to existing theme tokens; new semantic tokens (`ForestGreenDark`,
  `ErrorContainer`, `OnErrorContainer`) were added to the theme files.
- **Marked stub screens** with `// STUB:` and `// TODO(supabase):` comments:
  - `HelpScreen.kt` — placeholder FAQs
  - `PrivacyPolicyScreen.kt` — placeholder legal text
- **Removed SQL seed inserts** from `supabase_schema.sql` — table definitions, RLS, indexes, and functions remain.

### 2.6 Supabase Database Schema
A complete SQL schema was written and saved to the Desktop:

```text
C:\Users\dadyu\OneDrive\Desktop\supabase_schema.sql
```

The schema includes:

| Table | Purpose |
|-------|---------|
| `family_groups` | Family units (name, invite code, creator) |
| `family_members` | User profiles linked to `auth.users(id)` |
| `tasks` | Chores/tasks assigned to children |
| `feedback` | Ratings and comments on completed tasks |
| `complaints` | Family complaints submitted by children |

Also included:
- Foreign keys and `CHECK` constraints.
- Row Level Security (RLS) policies.
- Helper functions: `current_user_family_group_id()` and `current_user_is_parent()`.
- Indexes for performance.
- **No seed/sample data** — all rows must be created through the app.

### 2.7 Supabase Agent Skills
Installed via `npx skills add supabase/agent-skills --yes`:

- `supabase`
- `supabase-postgres-best-practices`

These are saved in `.agents/skills/` for AI coding assistance.

### 2.8 Build Verification
The project compiles successfully:

```bash
./gradlew :app:compileDebugKotlin
```

Output: `BUILD SUCCESSFUL`

---

## 3. Current Architecture

```text
familyhub/
├── app/
│   ├── build.gradle.kts              # Supabase deps + BuildConfig credentials (local.properties)
│   └── src/main/java/com/biglitecode/familyhub/
│       ├── core/
│       │   └── SupabaseClient.kt     # SupabaseClientProvider
│       ├── data/
│       │   ├── local/                # Room entities/DAOs/database (scaffold)
│       │   ├── model/Models.kt       # Domain models
│       │   ├── remote/
│       │   │   └── SupabaseClient.kt # SupabaseProvider (v3)
│       │   ├── repository/
│       │   │   ├── FamilyRepository.kt          # Repository interface
│       │   │   ├── SupabaseFamilyRepository.kt    # Runtime stub (empty data + TODOs)
│       │   │   └── FakeTaskRepository.kt          # PREVIEW-ONLY sample data
│       │   └── session/SessionManager.kt
│       └── ui/...                    # Compose screens
├── gradle/libs.versions.toml         # Version catalog
├── local.properties                  # Supabase credentials (NOT committed)
└── supabase_schema.sql (on Desktop)   # SQL schema without seed data
```

---

## 4. What Is NOT Done Yet

The following work remains for a fully functional v2:

1. **Real Supabase Repository**
   - Replace the stub methods in `SupabaseFamilyRepository` with real Supabase Postgrest/Auth calls.
   - `FakeTaskRepository` is now preview-only and should be removed once all screens are fully wired.

2. **Authentication with Supabase Auth**
   - Replace the fake `login`/`signUp` logic with `Supabase.auth.signInWithEmail()` and `signUp()`.
   - Handle email confirmation and password reset.
   - Map `auth.users` to `family_members` rows on sign-up.

3. **Data Mapping**
   - Map Kotlin domain models to/from Supabase Postgrest responses.
   - Ensure `id` generation matches Supabase expectations (or use UUIDs).

4. **Offline-First / Sync**
   - Decide if Room should be the source of truth with Supabase sync, or if Supabase is the only source.
   - Handle network errors gracefully.

5. **UI/UX Polish**
   - Remove or integrate the demo `MainActivity`.
   - Add loading states, empty states, and error messages.
   - Add pull-to-refresh.

6. **Security Hardening**
   - Move Supabase credentials out of `BuildConfig` and into `local.properties` or environment variables.
   - Review RLS policies before production.
   - Enable email confirmation and rate limiting in Supabase Auth.

7. **Testing**
   - Add unit tests for repositories.
   - Add UI tests for critical flows.

8. **Realtime (Optional)**
   - Enable Supabase Realtime for live task/member updates.

---

## 5. v2 Recommendations

### 5.1 Use a Single Supabase Client
Remove the duplicate `SupabaseClientProvider` and `SupabaseProvider`. Keep one `SupabaseClient` in a clean package, e.g.:

```text
data/remote/SupabaseClient.kt
```

### 5.2 Dependency Injection
Introduce **Hilt** or **Koin** for injecting `FamilyRepository` and `SupabaseClient` into ViewModels and Activities.

### 5.3 Repository Pattern
Create a concrete `SupabaseFamilyRepository`:

```kotlin
class SupabaseFamilyRepository(
    private val client: SupabaseClient
) : FamilyRepository {
    // observeTasks(), addTask(), updateTask(), etc.
}
```

### 5.4 Single Activity + Navigation
Consider migrating to a single `MainActivity` with Compose Navigation instead of multiple activities.

### 5.5 Secrets Management (Done in v1)
Credentials now live in `local.properties` and are injected into `BuildConfig` at build time. This file is `.gitignore`d and will not be committed. New team members must create their own `local.properties` file with the project credentials.

### 5.6 Consistent ID Strategy
Use `UUID` (or Supabase-generated `uuid`) for all IDs. Update Kotlin models from `String` to `UUID` if desired, or keep `String` and generate UUIDs client-side.

### 5.7 Error Handling & Loading States
Use sealed classes for UI state:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 6. Quick Reference: Important Files

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Supabase dependencies + BuildConfig credentials |
| `app/src/main/java/.../core/SupabaseClient.kt` | New Supabase client provider |
| `app/src/main/java/.../data/remote/SupabaseClient.kt` | Existing Supabase provider (v3) |
| `app/src/main/java/.../data/repository/SupabaseFamilyRepository.kt` | Runtime stub returning empty data |
| `app/src/main/java/.../data/repository/FakeTaskRepository.kt` | PREVIEW-ONLY sample data |
| `Desktop/supabase_schema.sql` | Full SQL schema for Supabase (no seed data) |
| `gradle/libs.versions.toml` | Version catalog |

---

## 7. Next Steps for v2

1. Create a new repository/branch.
2. Copy the Supabase setup from this plan (including `local.properties` setup).
3. Implement `SupabaseFamilyRepository` against the schema.
4. Wire authentication to Supabase Auth.
5. Replace `FakeTaskRepository` with the new repository.
6. Add dependency injection.
7. Test all flows end-to-end.

---

**Prepared by OpenCode** for the FamilyHub project.
