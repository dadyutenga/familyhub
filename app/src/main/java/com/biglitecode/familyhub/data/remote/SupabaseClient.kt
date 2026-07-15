package com.biglitecode.familyhub.data.remote

import com.biglitecode.familyhub.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Supabase cloud client.
 *
 * Student setup:
 * 1. Create a project at https://supabase.com
 * 2. Put URL + anon key into app/build.gradle.kts BuildConfig fields
 *    (SUPABASE_URL, SUPABASE_ANON_KEY) — or local.properties / secrets.
 * 3. Create tables: tasks, family_members, complaints, feedback (mirror Room schema).
 * 4. Enable Email auth in Supabase Auth settings.
 *
 * The UI still talks to FamilyRepository. Wire a real OnlineFirstRepository
 * that tries Supabase when online and falls back to Room when offline.
 */
object SupabaseProvider {
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

    fun isConfigured(): Boolean {
        return !BuildConfig.SUPABASE_URL.contains("YOUR_PROJECT") &&
            !BuildConfig.SUPABASE_ANON_KEY.contains("YOUR_SUPABASE")
    }
}
