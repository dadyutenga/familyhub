package com.biglitecode.familyhub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database scaffold for Phase 9.
 *
 * KSP + room-compiler is temporarily disabled due to AGP 9 / built-in Kotlin
 * compatibility. When re-enabled, uncomment ksp(libs.androidx.room.compiler)
 * in app/build.gradle.kts and use [getInstance] from a Room-backed repository.
 * Until then the app uses [com.biglitecode.familyhub.data.repository.SupabaseFamilyRepository]
 * as a stub that returns empty data; real Supabase wiring is tracked with TODOs.
 */
@Database(
    entities = [
        FamilyMemberEntity::class,
        TaskEntity::class,
        FeedbackEntity::class,
        ComplaintEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun memberDao(): MemberDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * Requires Room annotation processing (KSP) to generate AppDatabase_Impl.
         * Do not call until KSP is re-enabled.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "familyhub.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { instance = it }
            }
        }
    }
}
