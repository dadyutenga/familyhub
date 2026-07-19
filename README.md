# FamilyHub

An Android app for managing family relationships — parents/guardians assign tasks to children/members, track completion, leave feedback, raise complaints, and stay in touch within the family unit.

**Package:** `com.biglitecode.familyhub`  
**Student project:** DIT BEng Computing (BENG23COE)

---

## Features

- **Two roles** (same screens, conditional UI):
  - **Parent/Guardian** — assign tasks, manage family, view all complaints, send SMS reminders
  - **Child/Member** — own tasks, own profile, submit complaints/feedback
- **14 screens:** Splash, Login, Sign Up, Reset Password, Dashboard, Tasks, Task Detail, Report (leaderboard), Account, Settings, Feedback, Complaints, Help & FAQ, Privacy Policy
- **Warm family theme** — cream, forest green, and gold (not default Material blue)
- **Device integrations:** notifications (task assigned), SMS reminders, call/message intents, lightweight Bluetooth settings hook
- **Compose previews** on all major screens for design-time review

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Compose Navigation + Bottom Nav + Drawer |
| Local data (scaffold) | Room entities/DAOs |
| Cloud (scaffold) | Supabase client placeholders |
| Min SDK | API 24 (Android 7.0) |

Currently the app uses a stub **`SupabaseFamilyRepository`** that returns empty data; real Supabase auth and sync are tracked with TODOs. `FakeTaskRepository` is kept only for `@Preview` composables.

---

## Screenshots / previews

Open any screen file in Android Studio and use the **Preview** panel (e.g. `DashboardScreen.kt`, `LoginScreen.kt`, `TasksScreen.kt`). Parent and Child variants are provided where roles differ.

---

## Getting started

### Requirements

- Android Studio (recent stable)
- JDK 17+ (Android Studio JBR is fine)
- Android emulator or physical device (API 24+)

### Run

1. Clone the repo:
   ```bash
   git clone https://github.com/dadyutenga/familyhub.git
   cd familyhub
   ```
2. Open the project in **Android Studio**.
3. Wait for Gradle sync.
4. Run **app** on an emulator or device (launcher: `SplashActivity`).

### Build from CLI

```bash
# Windows (PowerShell) — set JAVA_HOME to Android Studio JBR if needed
.\gradlew.bat assembleDebug
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Project structure

```
com.biglitecode.familyhub/
├── data/
│   ├── model/          # Task, FamilyMember, Feedback, Complaint, enums
│   ├── local/          # Room entities, DAOs, AppDatabase (scaffold)
│   ├── remote/         # Supabase client placeholders
│   ├── repository/     # FamilyRepository + FakeTaskRepository
│   └── session/        # SessionManager (current user StateFlow)
├── ui/
│   ├── theme/          # Color, Theme, Shape, Type
│   ├── splash/, login/, signup/, resetpassword/
│   ├── dashboard/, tasks/, report/
│   ├── account/, settings/, feedback/, complains/
│   ├── help/, contact/, privacy/
│   ├── components/     # Shared UI (TaskCard, avatars, buttons)
│   └── preview/        # Shared preview helpers
├── navigation/         # Bottom nav + drawer NavGraph
└── util/               # NotificationHelper, SmsHelper, NetworkUtils
```

---

## Role-based behaviour (summary)

| Screen | Parent | Child |
|---|---|---|
| Dashboard | All family tasks | Own tasks only (“My Tasks”) |
| Tasks | FAB to add/assign | No FAB; own tasks |
| Task detail | Edit, delete, SMS reminder | Mark complete (if assignee) |
| Account | Manage family (remove member) | Profile + read-only list |
| Complaints | All complaints | Own only |
| Settings | Family settings section | General only |
| Report | Full leaderboard | Full leaderboard |

---

## Permissions

Declared in `AndroidManifest.xml`:

- `INTERNET` / `ACCESS_NETWORK_STATE` — login connectivity check
- `POST_NOTIFICATIONS` — task assignment alerts (API 33+)
- `SEND_SMS` — parent task reminders (requested at use)

---

## Supabase (optional next step)

Placeholders live in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://YOUR_PROJECT.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR_SUPABASE_ANON_KEY\"")
```

Replace with your project keys when wiring real auth and sync. See `data/remote/SupabaseClient.kt`.

---

## License / coursework note

Student coursework demo for DIT BEng Computing. Not a production privacy policy or production backend — sample data and placeholder legal text only.

---

## Author

**Dady** — [github.com/dadyutenga](https://github.com/dadyutenga)
