package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val contactName: String,
    val phoneNumber: String,
    val direction: String,      // "INCOMING", "OUTGOING", "DELEGATED"
    val timestamp: Long,
    val transcript: String,     // Conversational script
    val summary: String,        // Summary generated or extracted
    val durationSeconds: Int = 12
)
