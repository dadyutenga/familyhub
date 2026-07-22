package com.biglitecode.familyhub.core

import com.biglitecode.familyhub.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

/**
 * Single source of truth for the Supabase client used by the whole app.
 *
 * Student setup:
 * 1. Create a project at https://supabase.com
 * 2. Put URL + anon key into local.properties (SUPABASE_URL, SUPABASE_ANON_KEY).
 *    They are injected at build time via BuildConfig — never commit them.
 * 3. Run supabase/schema.sql against your project (Supabase Dashboard → SQL Editor).
 * 4. Enable "Email" auth provider in Supabase Dashboard → Authentication → Providers.
 */
object SupabaseClientProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }

    /**
     * True when real credentials have been provided. If false, the app should
     * avoid making Supabase calls and show a configuration error instead.
     */
    fun isConfigured(): Boolean {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        return url.isNotBlank() &&
            !url.contains("YOUR_PROJECT") &&
            !url.contains("PLACEHOLDER") &&
            key.isNotBlank() &&
            !key.contains("YOUR_SUPABASE") &&
            !key.contains("PLACEHOLDER")
    }
}
