package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_records")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val factKey: String,
    val content: String,
    val category: String, // "HABIT", "PREFERENCE", "CONTACT", "WORK"
    val timestamp: Long
)
