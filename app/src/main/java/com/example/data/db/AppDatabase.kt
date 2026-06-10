package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CallRecord::class, MemoryItem::class, AutomationRoutine::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callRecordDao(): CallRecordDao
    abstract fun memoryItemDao(): MemoryItemDao
    abstract fun automationRoutineDao(): AutomationRoutineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_phone_assistant_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
