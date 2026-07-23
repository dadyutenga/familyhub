-- ============================================================================
-- FamilyHub Supabase Database Schema
-- Run this SQL in the Supabase Dashboard (SQL Editor -> New query)
-- ============================================================================
-- This schema mirrors the Room entities and Kotlin data models used in the app:
--   FamilyGroup, FamilyMember, Task, Feedback, Complaint, plus a demo todos table
-- ============================================================================

-- ============================================================================
-- 1. ENUMS / DOMAINS (stored as TEXT with CHECK constraints for max portability)
-- ============================================================================

-- ============================================================================
-- 2. CORE TABLES
-- ============================================================================

-- Family groups
CREATE TABLE IF NOT EXISTS family_groups (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    created_by      TEXT NOT NULL,              -- family_members.id of the creator
    invite_code     TEXT UNIQUE NOT NULL,
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- Family members (linked to Supabase Auth users)
CREATE TABLE IF NOT EXISTS family_members (
    id              TEXT PRIMARY KEY,
    user_id         UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    name            TEXT NOT NULL,
    role            TEXT NOT NULL CHECK (role IN ('PARENT', 'CHILD')),
    avatar_color    TEXT,
    phone_number    TEXT DEFAULT '',
    email           TEXT NOT NULL,
    family_group_id TEXT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE,
    UNIQUE (user_id, family_group_id)
);

-- Tasks assigned to family members
CREATE TABLE IF NOT EXISTS tasks (
    id              TEXT PRIMARY KEY,
    title           TEXT NOT NULL,
    description     TEXT NOT NULL DEFAULT '',
    assigned_to     TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    assigned_to_name TEXT NOT NULL,             -- denormalized display name
    assigned_by     TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    due_date        BIGINT NOT NULL,
    status          TEXT NOT NULL CHECK (status IN ('PENDING', 'DONE', 'OVERDUE')) DEFAULT 'PENDING',
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    reward_points   INTEGER NOT NULL DEFAULT 10,
    family_group_id TEXT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE
);

-- Feedback on completed tasks
CREATE TABLE IF NOT EXISTS feedback (
    id              TEXT PRIMARY KEY,
    task_id         TEXT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id         TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    comment         TEXT NOT NULL DEFAULT '',
    rating          INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    UNIQUE (task_id, user_id)
);

-- Family complaints
CREATE TABLE IF NOT EXISTS complaints (
    id              TEXT PRIMARY KEY,
    user_id         TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    user_name       TEXT NOT NULL,
    subject         TEXT NOT NULL,
    description     TEXT NOT NULL,
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    resolved        BOOLEAN NOT NULL DEFAULT FALSE,
    family_group_id TEXT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE
);

-- Family reminders (recurring daily/weekly notifications)
CREATE TABLE IF NOT EXISTS family_reminders (
    id              TEXT PRIMARY KEY,
    family_group_id TEXT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE,
    title           TEXT NOT NULL,
    reminder_time   TEXT NOT NULL,              -- "HH:mm" format
    repeat_type     TEXT NOT NULL CHECK (repeat_type IN ('DAILY', 'WEEKLY', 'SPECIFIC_DAYS')) DEFAULT 'DAILY',
    days_of_week    TEXT,                       -- nullable, comma-separated "MON,WED,FRI"
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_by      TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- App usage logs (child device usage data, uploaded periodically)
CREATE TABLE IF NOT EXISTS app_usage_logs (
    id              TEXT PRIMARY KEY,
    child_id        TEXT NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    child_name      TEXT NOT NULL DEFAULT '',
    app_package     TEXT NOT NULL,
    app_name        TEXT NOT NULL,
    usage_minutes   INTEGER NOT NULL DEFAULT 0,
    date            TEXT NOT NULL,              -- "YYYY-MM-DD" format
    created_at      BIGINT NOT NULL DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    family_group_id TEXT NOT NULL REFERENCES family_groups(id) ON DELETE CASCADE
);

-- Demo table for the MainActivity TodoList example
CREATE TABLE IF NOT EXISTS todos (
    id              INTEGER PRIMARY KEY,
    name            TEXT NOT NULL
);

-- ============================================================================
-- 3. ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE family_groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE family_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE complaints ENABLE ROW LEVEL SECURITY;
ALTER TABLE family_reminders ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_usage_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE todos ENABLE ROW LEVEL SECURITY;

-- Helper function: get current user's family_group_id
CREATE OR REPLACE FUNCTION public.current_user_family_group_id()
RETURNS TEXT AS $$
DECLARE
    group_id TEXT;
BEGIN
    SELECT family_group_id INTO group_id
    FROM family_members
    WHERE user_id = auth.uid()
    LIMIT 1;
    RETURN group_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Helper function: check if current user is a PARENT in their family
CREATE OR REPLACE FUNCTION public.current_user_is_parent()
RETURNS BOOLEAN AS $$
DECLARE
    is_parent BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM family_members
        WHERE user_id = auth.uid() AND role = 'PARENT'
    ) INTO is_parent;
    RETURN is_parent;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Helper function: look up a family group by invite code.
-- SECURITY DEFINER so it bypasses RLS — needed during sign-up when the user
-- has an auth session but no family_members row yet, so the normal
-- family_groups_select_members policy would block the SELECT.
CREATE OR REPLACE FUNCTION public.lookup_family_by_invite_code(code TEXT)
RETURNS TABLE(id TEXT, name TEXT, created_by TEXT, invite_code TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT fg.id, fg.name, fg.created_by, fg.invite_code
    FROM family_groups fg
    WHERE fg.invite_code = code;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Helper function: check whether the current auth user has a family_members row.
-- SECURITY DEFINER so it bypasses the normal SELECT policy (which would return
-- nothing for an orphaned user who has no family_members row yet).
-- Used by the app during session restore and sign-up resume.
CREATE OR REPLACE FUNCTION public.check_my_member_record()
RETURNS TABLE(id TEXT, name TEXT, role TEXT, family_group_id TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT fm.id, fm.name, fm.role, fm.family_group_id
    FROM family_members fm
    WHERE fm.user_id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- family_groups policies
CREATE POLICY "family_groups_select_members"
    ON family_groups FOR SELECT
    USING (id = public.current_user_family_group_id());

-- Allow any authenticated user to create a family group during sign-up.
-- The app will link the creator as a PARENT in family_members immediately after.
CREATE POLICY "family_groups_insert_authenticated"
    ON family_groups FOR INSERT
    TO authenticated
    WITH CHECK (true);

CREATE POLICY "family_groups_update_parents"
    ON family_groups FOR UPDATE
    USING (public.current_user_is_parent())
    WITH CHECK (public.current_user_is_parent());

-- family_members policies
CREATE POLICY "family_members_select_group"
    ON family_members FOR SELECT
    USING (family_group_id = public.current_user_family_group_id());

-- Allow authenticated users to insert themselves (sign-up) OR parents to add others.
CREATE POLICY "family_members_insert_self_or_parent"
    ON family_members FOR INSERT
    TO authenticated
    WITH CHECK (
        user_id = auth.uid()           -- sign-up: inserting yourself
        OR public.current_user_is_parent()  -- parent adding a child
    );

CREATE POLICY "family_members_update_self_or_parent"
    ON family_members FOR UPDATE
    USING (user_id = auth.uid() OR public.current_user_is_parent())
    WITH CHECK (family_group_id = public.current_user_family_group_id());

CREATE POLICY "family_members_delete_parent"
    ON family_members FOR DELETE
    USING (public.current_user_is_parent() AND user_id <> auth.uid());

-- tasks policies
CREATE POLICY "tasks_select_group"
    ON tasks FOR SELECT
    USING (family_group_id = public.current_user_family_group_id());

CREATE POLICY "tasks_insert_parents"
    ON tasks FOR INSERT
    WITH CHECK (public.current_user_is_parent() AND family_group_id = public.current_user_family_group_id());

CREATE POLICY "tasks_update_parents_or_assignee"
    ON tasks FOR UPDATE
    USING (
        public.current_user_is_parent()
        OR assigned_to IN (
            SELECT id FROM family_members WHERE user_id = auth.uid()
        )
    )
    WITH CHECK (family_group_id = public.current_user_family_group_id());

CREATE POLICY "tasks_delete_parents"
    ON tasks FOR DELETE
    USING (public.current_user_is_parent());

-- feedback policies
CREATE POLICY "feedback_select_group"
    ON feedback FOR SELECT
    USING (
        task_id IN (
            SELECT id FROM tasks WHERE family_group_id = public.current_user_family_group_id()
        )
    );

CREATE POLICY "feedback_insert_self"
    ON feedback FOR INSERT
    WITH CHECK (
        user_id IN (
            SELECT id FROM family_members WHERE user_id = auth.uid()
        )
        AND task_id IN (
            SELECT id FROM tasks WHERE family_group_id = public.current_user_family_group_id()
        )
    );

CREATE POLICY "feedback_update_own"
    ON feedback FOR UPDATE
    USING (user_id IN (SELECT id FROM family_members WHERE user_id = auth.uid()));

CREATE POLICY "feedback_delete_own"
    ON feedback FOR DELETE
    USING (user_id IN (SELECT id FROM family_members WHERE user_id = auth.uid()));

-- complaints policies
CREATE POLICY "complaints_select_group"
    ON complaints FOR SELECT
    USING (family_group_id = public.current_user_family_group_id());

CREATE POLICY "complaints_insert_self"
    ON complaints FOR INSERT
    WITH CHECK (
        user_id IN (SELECT id FROM family_members WHERE user_id = auth.uid())
        AND family_group_id = public.current_user_family_group_id()
    );

CREATE POLICY "complaints_update_parents"
    ON complaints FOR UPDATE
    USING (public.current_user_is_parent())
    WITH CHECK (family_group_id = public.current_user_family_group_id());

CREATE POLICY "complaints_delete_parents"
    ON complaints FOR DELETE
    USING (public.current_user_is_parent());

-- family_reminders policies
CREATE POLICY "family_reminders_select_group"
    ON family_reminders FOR SELECT
    TO authenticated
    USING (family_group_id = public.current_user_family_group_id());

CREATE POLICY "family_reminders_insert_parents"
    ON family_reminders FOR INSERT
    TO authenticated
    WITH CHECK (public.current_user_is_parent() AND family_group_id = public.current_user_family_group_id());

CREATE POLICY "family_reminders_update_parents"
    ON family_reminders FOR UPDATE
    TO authenticated
    USING (public.current_user_is_parent())
    WITH CHECK (family_group_id = public.current_user_family_group_id());

CREATE POLICY "family_reminders_delete_parents"
    ON family_reminders FOR DELETE
    TO authenticated
    USING (public.current_user_is_parent());

-- app_usage_logs policies
-- Parents can read usage data for children in their family
CREATE POLICY "app_usage_select_parents"
    ON app_usage_logs FOR SELECT
    USING (
        public.current_user_is_parent()
        AND family_group_id = public.current_user_family_group_id()
    );

-- Children can also see their own usage data
CREATE POLICY "app_usage_select_own"
    ON app_usage_logs FOR SELECT
    USING (
        child_id IN (
            SELECT id FROM family_members WHERE user_id = auth.uid()
        )
    );

-- Children can insert their own usage data
CREATE POLICY "app_usage_insert_children"
    ON app_usage_logs FOR INSERT
    WITH CHECK (
        child_id IN (
            SELECT id FROM family_members WHERE user_id = auth.uid()
        )
        AND family_group_id = public.current_user_family_group_id()
    );

-- No one can update usage logs (append-only)
-- Parents can delete old usage data
CREATE POLICY "app_usage_delete_parents"
    ON app_usage_logs FOR DELETE
    USING (public.current_user_is_parent());

-- todos policy: allow all authenticated users to read demo todos
CREATE POLICY "todos_select_all"
    ON todos FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "todos_insert_all"
    ON todos FOR INSERT
    TO authenticated
    WITH CHECK (true);

-- ============================================================================
-- 4. INDEXES (performance)
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_family_members_group  ON family_members(family_group_id);
CREATE INDEX IF NOT EXISTS idx_family_members_user     ON family_members(user_id);
CREATE INDEX IF NOT EXISTS idx_family_members_email    ON family_members(email);

CREATE INDEX IF NOT EXISTS idx_tasks_group             ON tasks(family_group_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to       ON tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_tasks_status            ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at        ON tasks(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_feedback_task           ON feedback(task_id);
CREATE INDEX IF NOT EXISTS idx_feedback_user           ON feedback(user_id);

CREATE INDEX IF NOT EXISTS idx_complaints_group        ON complaints(family_group_id);
CREATE INDEX IF NOT EXISTS idx_complaints_user         ON complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_created_at   ON complaints(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_family_groups_invite    ON family_groups(invite_code);

CREATE INDEX IF NOT EXISTS idx_family_reminders_group ON family_reminders(family_group_id);

CREATE INDEX IF NOT EXISTS idx_app_usage_child        ON app_usage_logs(child_id);
CREATE INDEX IF NOT EXISTS idx_app_usage_group         ON app_usage_logs(family_group_id);
CREATE INDEX IF NOT EXISTS idx_app_usage_date          ON app_usage_logs(date);

-- ============================================================================
-- 5. SAMPLE / DEMO DATA (REMOVED)
-- ============================================================================
-- No seed data is committed. All rows should be created through the app via
-- Supabase Auth sign-ups and authenticated API calls. Keep this section empty
-- in production to avoid leaking demo accounts or fake tasks.
-- ============================================================================

-- ============================================================================
-- 6. REALTIME (optional — enable if you want live updates)
-- ============================================================================
-- In Supabase Dashboard, go to Database -> Publications and add tables
-- to the 'supabase_realtime' publication, or run:
--
-- BEGIN;
--   DROP PUBLICATION IF EXISTS supabase_realtime;
--   CREATE PUBLICATION supabase_realtime FOR TABLE family_groups, family_members, tasks, feedback, complaints, family_reminders, app_usage_logs, todos;
-- COMMIT;
-- ============================================================================

-- ============================================================================
-- 7. IMPORTANT NOTES
-- ============================================================================
-- 1. Enable "Email" provider in Supabase Auth -> Providers.
-- 2. Update app/build.gradle.kts BuildConfig fields with your real project URL + anon key.
-- 3. The sample members use placeholder UUIDs for user_id. In production, those
--    MUST match real UUIDs from auth.users(id) after users sign up.
-- 4. For sign-up flow, create the auth user first, then insert the family_member
--    row with the returned auth.uid().
-- 5. The 'passwordHash' field from the old Room UserEntity is intentionally NOT
--    stored here because Supabase Auth handles password hashing securely.
-- ============================================================================
