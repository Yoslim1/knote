/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [Task::class, Note::class, Expense::class, Category::class, AppSettings::class, Habit::class, HabitLog::class, GratitudeEntry::class, MoodEntry::class, CaffeineDose::class, RecurringCostHistory::class, AdditionalIncome::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun habitDao(): HabitDao
    abstract fun gratitudeDao(): GratitudeDao
    abstract fun moodDao(): MoodDao
    abstract fun caffeineDoseDao(): CaffeineDoseDao
    abstract fun recurringCostHistoryDao(): RecurringCostHistoryDao
    abstract fun additionalIncomeDao(): AdditionalIncomeDao
    abstract fun backupDao(): BackupDao

    companion object {
        /**
         * Everything this release adds to the schema, in one step: the note
         * colour and the two notes preferences. Written out rather than left to
         * the destructive fallback, which would wipe every note on update.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE notes ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
                connection.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN newNoteStartsWithTitle INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * The switch between lists and notes is gone, and with it its setting.
         * Its column only exists where the unreleased step above once wrote it,
         * so the drop is asked for rather than assumed.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                val hasColumn = connection.prepare(
                    "SELECT COUNT(*) FROM pragma_table_info('app_settings') WHERE name = 'noteTypeFilterEnabled'"
                ).use { it.step() && it.getLong(0) > 0 }
                if (hasColumn) {
                    connection.execSQL("ALTER TABLE app_settings DROP COLUMN noteTypeFilterEnabled")
                }
            }
        }

        /**
         * The pointer to the mindfulness view is meant for someone opening the
         * app for the first time, so everyone already here counts as having
         * seen it.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN mindfulnessHintSeen INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        /** Mindfulness can now be switched off, the way finance already could. */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN mindfulnessEnabled INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        fun build(context: Context, dek: ByteArray): AppDatabase {
            val factory = SupportOpenHelperFactory(SqlCipherKey.rawKeyBytes(dek))
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "knote.db")
                .openHelperFactory(factory)
                // No destructive fallback, intentionally - a missing migration must
                // fail loudly during development, not silently delete user data in
                // production. Every schema bump requires a hand-written, tested
                // Migration object.
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
        }

        fun getInstance(context: Context): AppDatabase = DatabaseProvider.requireDatabase()
    }
}
