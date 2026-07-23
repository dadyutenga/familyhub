package com.biglitecode.familyhub.util

import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.biglitecode.familyhub.FamilyHubApp
import com.biglitecode.familyhub.data.model.AppUsageEntry
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Periodic WorkManager worker that collects app usage stats from the device
 * and uploads daily aggregates to Supabase.
 *
 * Only runs on child devices that have granted PACKAGE_USAGE_STATS permission.
 * Queries today's usage, filters system apps, maps to human-readable names,
 * and inserts aggregated rows into `app_usage_logs`.
 */
class AppUsageCollectorWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        // Only collect on child devices with usage access permission
        if (!UsageStatsHelper.hasUsageAccessPermission(context)) return Result.success()
        val user = SessionManager.currentUser.value ?: return Result.success()
        if (user.role != FamilyRole.CHILD) return Result.success()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE)
            as? UsageStatsManager ?: return Result.failure()

        // Query today's usage (midnight to now)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } catch (_: SecurityException) {
            return Result.success()
        }

        if (usageStats.isNullOrEmpty()) return Result.success()

        // Filter system apps and aggregate by package
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = dateFormat.format(calendar.time)

        val entries = usageStats
            .filter { stat ->
                stat.totalTimeInForeground > 0 &&
                !UsageStatsHelper.isSystemApp(context, stat.packageName)
            }
            .map { stat ->
                val minutes = (stat.totalTimeInForeground / 60_000).toInt().coerceAtLeast(1)
                val appName = UsageStatsHelper.getAppName(context, stat.packageName)
                AppUsageEntry(
                    id = "au_${UUID.randomUUID().toString().take(8)}",
                    childId = user.id,
                    childName = user.name,
                    appPackage = stat.packageName,
                    appName = appName,
                    usageMinutes = minutes,
                    date = today,
                    createdAt = System.currentTimeMillis()
                )
            }
            .sortedByDescending { it.usageMinutes }
            .take(20) // Top 20 apps max

        if (entries.isEmpty()) return Result.success()

        // Upload to Supabase
        return try {
            val app = context.applicationContext as FamilyHubApp
            app.repository.addAppUsageLogs(entries)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
