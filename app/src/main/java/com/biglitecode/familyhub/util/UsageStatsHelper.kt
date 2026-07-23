package com.biglitecode.familyhub.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Helper for Android's UsageStatsManager (PACKAGE_USAGE_STATS).
 *
 * IMPORTANT: This is a special permission — it cannot be requested via
 * ActivityCompat.requestPermissions(). The user must navigate manually to
 * Settings > Apps > Special app access > Usage access, or be directed there
 * via [requestUsageAccessPermission].
 */
object UsageStatsHelper {

    private const val USAGE_WORK_NAME = "app_usage_collector"

    /**
     * Check whether the app has been granted PACKAGE_USAGE_STATS permission.
     */
    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Open the system "Usage Access" settings screen so the user can grant
     * PACKAGE_USAGE_STATS to this app.
     */
    fun requestUsageAccessPermission(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    /**
     * Schedule the periodic app-usage collection worker.
     * Runs every 6 hours on child devices that have granted usage access.
     */
    fun scheduleUsageCollection(context: Context) {
        val request = PeriodicWorkRequestBuilder<AppUsageCollectorWorker>(
            6, TimeUnit.HOURS
        ).addTag(USAGE_WORK_NAME).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            USAGE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Cancel the periodic app-usage collection worker.
     */
    fun cancelUsageCollection(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(USAGE_WORK_NAME)
    }

    /**
     * Resolve a package name to a human-readable app label.
     * Returns the package name itself if the app is not found.
     */
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * Resolve a package name to the app's icon drawable.
     * Returns null if the app is not found.
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(info)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Check whether a package belongs to a system app (not user-facing).
     */
    fun isSystemApp(context: Context, packageName: String): Boolean {
        return try {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (_: PackageManager.NameNotFoundException) {
            true // If we can't resolve it, treat as system
        }
    }
}
