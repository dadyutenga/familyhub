# FamilyHub — Full Rebuild Specification
**BEng23 COE — Individual Project Assignment (20 Marks), DIT**
**Deadline: July 20th–24th, 2026**

> This is a complete, self-contained build spec. Feed this entire document to the coding agent as the task. It consolidates the full app: architecture, theme, all 14 screens, role-based logic, and integration features. Build in the phase order given at the bottom — do not skip phases.

---

## 0. IMPORTANT FIRST STEP
Before writing any code: run `git init`, create a `.gitignore` for Android Studio/Kotlin, make an initial commit, and push to a GitHub repo (create one if none exists) after every completed phase below. This project was lost once already due to no version control — do not let that happen again.

---

## 1. Project Overview

**Title:** FamilyHub
**Concept:** An Android app for managing family relationships — Parents/Guardians assign tasks to Children/Members, track completion, log activities, leave feedback, raise complaints, and communicate within the family unit. Two roles: **Parent/Guardian** (full management access) and **Child/Member** (limited to own tasks/profile).

---

## 2. Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Local DB | Room (SQLite) |
| Cloud DB | Supabase (Postgres + Auth + Realtime) |
| Navigation | Compose Navigation + Bottom Navigation + Navigation Drawer |
| Architecture | MVVM (ViewModel + StateFlow) |
| Min SDK | API 24 (Android 7.0) |
| Package name | `com.biglitecode.familyhub` |

### Core Gradle dependencies
```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation(platform("io.github.jan-tennert.supabase:bom:2.6.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.ktor:ktor-client-android:2.3.12")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
}
```
*(Verify latest stable versions in Android Studio before syncing.)*

---

## 3. Theme (warm/playful family style — NOT default Material blue)

### `ui/theme/Color.kt`
```kotlin
val CreamBackground = Color(0xFFFBF3E0)
val CardCream = Color(0xFFFFFDF8)
val ForestGreen = Color(0xFF2F6B44)
val ForestGreenLight = Color(0xFFDCEEE0)
val GoldYellow = Color(0xFFF5C242)
val GoldYellowLight = Color(0xFFFDF0CE)
val CoralRed = Color(0xFFE05C5C)
val TextBrown = Color(0xFF3A2E22)
val TextMutedBrown = Color(0xFF8A7A64)
val BorderGreen = Color(0xFF2F6B44)
```

### Color scheme (`Theme.kt`)
```kotlin
lightColorScheme(
    primary = ForestGreen, onPrimary = Color.White,
    secondary = GoldYellow, onSecondary = TextBrown,
    background = CreamBackground, onBackground = TextBrown,
    surface = CardCream, onSurface = TextBrown,
    error = CoralRed
)
```

### Shapes (`ui/theme/Shape.kt`)
```kotlin
val FamilyHubShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)
```

### Visual language rules (apply everywhere)
- Rounded cards (`MaterialTheme.shapes.medium`), colored 1.5–2dp borders: **green border = done/active**, **gold border = pending**
- Circular avatar chips with colored ring border
- Primary buttons: ForestGreen, `shapes.large`, full width
- Secondary/accent: GoldYellow
- Destructive actions (logout, delete): CoralRed
- Body text: TextBrown; muted/hint text: TextMutedBrown
- Optional: rounded "Baloo 2" or "Fredoka" Google Font for headings (nice-to-have, skip if it slows things down)

---

## 4. Data Models

```kotlin
enum class FamilyRole { PARENT, CHILD }
enum class TaskStatus { PENDING, DONE, OVERDUE }
enum class FamilyGroupOption { CREATE, JOIN }

data class User(
    val id: String, val name: String, val email: String,
    val role: FamilyRole, val familyGroupId: String
)

data class FamilyMember(
    val id: String, val name: String, val role: FamilyRole,
    val avatarColor: String? = null, val phoneNumber: String = ""
)

data class FamilyGroup(val id: String, val name: String, val createdBy: String)

data class Task(
    val id: String, val title: String, val description: String,
    val assignedTo: String, val assignedToName: String, val assignedBy: String,
    val dueDate: Long, val status: TaskStatus, val createdAt: Long
)

data class Feedback(
    val id: String, val taskId: String, val userId: String,
    val comment: String, val rating: Int
)

data class Complaint(
    val id: String, val userId: String, val subject: String,
    val description: String, val createdAt: Long, val resolved: Boolean = false
)
```

Start with a `FakeTaskRepository` / in-memory `SessionManager` (StateFlow<FamilyMember?> currentUser) behind repository **interfaces**, so swapping to Room/Supabase later doesn't touch UI code. Sample data: 4+ family members (mix of PARENT/CHILD), 6–8 tasks with mixed statuses.

---

## 5. Package Structure

```
com.biglitecode.familyhub/
├── data/
│   ├── model/           → Task, FamilyMember, User, Feedback, Complaint, enums
│   ├── local/            → Room entities, DAOs, Database (Phase 5)
│   ├── remote/           → Supabase client (Phase 5)
│   ├── repository/       → interfaces + FakeTaskRepository impl
│   └── session/           → SessionManager (current logged-in user)
├── ui/
│   ├── theme/             → Color.kt, Theme.kt, Shape.kt, Type.kt
│   ├── splash/, login/, signup/, resetpassword/
│   ├── dashboard/, tasks/, report/
│   ├── account/, settings/, feedback/, complains/, help/, contact/, privacy/
├── navigation/            → BottomNavItem.kt, FamilyHubNavGraph.kt (+ drawer)
├── util/                  → NotificationHelper.kt, SmsHelper.kt
└── [Activity].kt files at root: SplashActivity, LoginActivity, SignUpActivity,
    ResetPasswordActivity, DashboardActivity (hosts NavGraph/bottom-nav shell),
    PrivacyPolicyActivity (only screens that aren't NavGraph routes get their own Activity —
    keep bottom-nav tabs as NavGraph routes inside DashboardActivity, not separate Activities)
```

---

## 6. All 14 Screens — Full Spec

### 1. SplashActivity
Gradient/branded splash (logo icon in a rounded surface, app name, tagline), ~1.5s animated scale-in, then routes to Login (or Dashboard if session exists — add session check once SessionManager exists).

### 2. LoginActivity — `checks DB + internet connection before login`
Email + password fields (Material icons, password visibility toggle), "Forgot password?" link, error banner (styled card, shows "No internet connection..." if `ConnectivityManager` check fails before attempting login), Login button (loading spinner state), "Create an account" link to SignUp.

### 3. Sign_up_Activity
Full Name, Email, Password, Confirm Password fields. **Role selector**: Parent/Guardian vs Child/Member (two selectable cards or SegmentedButton). **Family group selector**: "Create a new family group" (name field) OR "Join existing family group" (invite code field). Sign Up button disabled until valid + passwords match. Link back to Login.

### 4. Reset_password_Activity
Back arrow, heading, email field, "Send Reset Link" button, success message state (green text) after send, error state.

### 5. DashboardActivity — hosts the Bottom Nav + Drawer app shell
This becomes the single post-login entry point, hosting `FamilyHubNavGraph` (NavHost + Scaffold + NavigationBar with 4 tabs: Home/Tasks/Report/Settings, wrapped in a `ModalNavigationDrawer` for secondary items: Account, Settings, Help, Contact, Complains, Privacy Policy, Logout).

**DashboardScreen (Home tab) content:**
- "My Family" pill-style header + greeting
- Horizontal scroll row of family member avatar chips (colored ring)
- "Today's Tasks" (or "My Tasks" for CHILD role — see §7) with count badge, list of TaskCards
- "Weekly Rewards" summary card: big point number, gold progress bar, motivational subtext

### 6. MainActivity → Tasks tab (`TasksScreen` + `TaskDetailScreen`)
**TasksScreen:** Top bar "Tasks" + FAB "+" (PARENT only, see §7) to add task (dialog: title, description, assignee dropdown, due date, reward points). Filter chips: All/Pending/Done. LazyColumn of TaskCards → tap opens detail.

**TaskDetailScreen:** Back arrow + title, "Assigned To" card, due date + reward point cards side by side, description block, action buttons (role-dependent, see §7): Mark Complete, Edit Task, Send Reminder (SMS), Delete Task, Share Task Nearby (Bluetooth, secondary/optional).

Shared `TasksViewModel` (StateFlow<List<Task>>) used by Dashboard, Tasks, Report, Feedback screens.

### 7. ReportActivity → Report tab (`ReportScreen`)
"Family Leaderboard" pill heading + date range subtext. Summary card: total tasks completed this period. Ranked list by tasks completed: numbered badge (gold circle for #1), avatar, name, "{X} tasks done", star/point badge. Derived from `TasksViewModel` grouped by `assignedTo`.

### 8. AccountActivity
Large avatar (colored ring), name, email, role pill badge. Family group card: name, member count, invite code with copy button. Family members list. "Edit Profile" (can be placeholder). **PARENT only:** "Manage Family" — Remove Member action per member (confirm dialog).

### 9. SettingsActivity
Grouped card sections: Dark Mode switch, Push Notifications switch, SMS Reminders switch, Language (static "English"), App Version (static). **PARENT only:** "Family Settings" section above general settings — Manage Family Members link, Family Group Name field. Logout row (CoralRed) at bottom → clears back stack → LoginActivity.

### 10. UserFeedbackActivity
"Task Feedback" heading. List of completed tasks (status == DONE), each with 5-star rating (tap to set, gold when filled) + optional comment field. Submit stores to `Feedback` in fake repo.

### 11. ComplainsActivity
"Family Complaints" heading + form (Subject, multiline Description, Submit button). Below: list of prior complaints as cards (subject, preview, date, status pill: "Open" CoralRed-tint / "Resolved" ForestGreenLight-tint). **PARENT sees all complaints; CHILD sees only their own** (see §7).

### 12. HelpActivity
"Help & FAQ" heading, visual search bar (non-functional is fine), 6+ expandable FAQ cards (chevron rotates, AnimatedVisibility reveals answer) covering: adding members, assigning tasks, points/rewards, offline use, password reset, changing role.

### 13. ContactActivity
"Contact Family Admin" card: admin (PARENT) name + avatar, "Call" button (`Intent.ACTION_DIAL`, no runtime permission needed), "Message" button (`Intent.ACTION_SENDTO` for SMS — satisfies assignment's SMS requirement). Support section: static support email + "Send Feedback" button (`Intent.ACTION_SEND` email).

### 14. PrivacyPolicyActivity
Back arrow + title. Scrollable sections (bold subheading + muted body, 2–4 sentences each): Introduction, Information We Collect, How We Use Your Information, Data Sharing, Data Security, Your Rights, Contact Us, Last Updated. Generic placeholder legal text — this is a student demo, not a real policy. Reached via Drawer.

---

## 7. Role-Based Behavior (Parent vs Child) — CRITICAL, do not skip

**Do NOT create separate screens per role.** Use conditional rendering on the SAME 14 screens based on `SessionManager.currentUser.role`.

| Screen | PARENT sees | CHILD sees |
|---|---|---|
| Dashboard | All members' tasks, "Today's Tasks" | Only own tasks, header becomes "My Tasks" |
| Tasks | FAB "+" to add/assign tasks, all tasks | No FAB, only own assigned tasks |
| Task Detail | Mark Complete, Edit, Delete, Send Reminder (SMS) | Only Mark Complete (if assigned to them); read-only note if not their task |
| Account | + "Manage Family" (remove member) | Own profile + read-only member list |
| Complains | Sees ALL complaints | Sees only own submitted complaints |
| Settings | + "Family Settings" section | General settings only |
| Report | Same (full leaderboard, both roles) | Same |

`SessionManager` (StateFlow<FamilyMember?>) is set on successful Login/SignUp and read by every screen/ViewModel needing role checks. Keep the pattern simple: `if (currentUser?.role == FamilyRole.PARENT) { ... } else { ... }`.

---

## 8. Communication & Device Features (assignment-required)

| Requirement | Implementation |
|---|---|
| **Notifications** | `NotificationHelper` object — channel creation (API 26+), `showTaskAssignedNotification()` fired when a PARENT creates/assigns a task. Request `POST_NOTIFICATIONS` runtime permission (API 33+) once on first Dashboard launch. |
| **SMS API** | `SmsHelper` object using `SmsManager` — "Send Reminder" button on TaskDetailScreen (PARENT only, PENDING/OVERDUE tasks) sends a reminder SMS to the assignee's `phoneNumber`. Request `SEND_SMS` runtime permission at point of use. |
| **Phone calls** | ContactActivity "Call" button via `Intent.ACTION_DIAL` (no runtime permission required for DIAL). |
| **Network connection check** | LoginActivity checks `ConnectivityManager` before attempting login; shows error banner if offline. |
| **Bluetooth** | Lightweight: "Share Task Nearby" button on TaskDetailScreen opens `Intent(Settings.ACTION_BLUETOOTH_SETTINGS)` as a demonstrated integration point (full P2P transfer is out of scope — note this simplification in a code comment). |
| **Launcher icon** | Custom adaptive icon: ForestGreen background + simple people/house vector glyph in cream/white. Update app label to "FamilyHub". |

---

## 9. Database Wiring (final phase)

### Room (local/offline)
- Entities mirroring the data models in §4 (`@Entity` on Task, FamilyMember, User, Feedback, Complaint)
- DAOs with suspend functions / Flow queries for CRUD
- `AppDatabase` (RoomDatabase) singleton
- Replace `FakeTaskRepository` internals with Room calls, keep the same repository interface so UI doesn't change

### Supabase (cloud sync)
- Supabase client setup (URL + anon key — use placeholders/BuildConfig fields, note where the student should insert their real project keys)
- GoTrue for Auth (real signUp/signIn replacing LoginActivity/SignUpActivity's TODO placeholders)
- Postgrest for Task/FamilyMember/Complaint tables (mirror the Room schema)
- Optional Realtime subscription so task updates sync live across family members' devices
- Repository layer should try Supabase first (if online), fall back to Room cache (if offline) — simple online/offline strategy, not full conflict resolution (out of scope for a student project)

---

## 10. Build Order (do in this sequence, commit to git after each phase)

1. **Phase 1 — Foundation:** Project setup, Gradle deps, theme (Color/Theme/Shape.kt), package structure, data models, FakeTaskRepository, SessionManager
2. **Phase 2 — Auth flow:** SplashActivity, LoginActivity, Sign_up_Activity, Reset_password_Activity
3. **Phase 3 — App shell:** BottomNavItem, FamilyHubNavGraph (+ Drawer), DashboardActivity/DashboardScreen
4. **Phase 4 — Core feature:** TasksScreen, TaskDetailScreen, ReportScreen (shared TasksViewModel)
5. **Phase 5 — Secondary screens:** AccountActivity, SettingsActivity, UserFeedbackActivity, ComplainsActivity, HelpActivity, ContactActivity
6. **Phase 6 — Final screen:** PrivacyPolicyActivity, wire into Drawer
7. **Phase 7 — Role-based logic:** Apply §7 conditionals across all screens
8. **Phase 8 — Device features:** Notifications, SMS, Bluetooth (lightweight), launcher icon (§8)
9. **Phase 9 — Real database:** Room wiring, then Supabase wiring (§9)
10. **Phase 10 — Polish:** Test both roles end-to-end, fix bugs, screenshots for submission report

After every phase: `git add . && git commit -m "Phase X: ..."` and `git push`. Confirm `BUILD SUCCESSFUL` before moving to the next phase.

---

## 11. Constraints (apply throughout)

- Launcher activity in `AndroidManifest.xml` must remain `SplashActivity`.
- Keep bottom-nav tabs (Dashboard/Tasks/Report/Settings-shortcut) as NavGraph routes inside one Activity (DashboardActivity), not separate Activities — matches how the assignment's "Activities" list maps to screens conceptually even though some are Compose destinations under one host Activity. Drawer-only items not part of the bottom nav (Account, Help, Contact, Complains, Privacy) can be either NavGraph routes or their own lightweight Activities — pick one pattern and stay consistent.
- Do not reintroduce the default blue Material theme — always use the warm palette from §3.
- Every runtime permission request must handle both granted and denied cases with clear user feedback (Toast/Snackbar), no crashes.
- Total screens must be ≥ 7 per assignment minimum (this spec has 14, comfortably exceeding it).

---
*Prepared for Dady (BIG LITE CODE) — DIT BEng Computing, BENG23COE. Rebuild after local data loss — push to GitHub after every phase this time.*
