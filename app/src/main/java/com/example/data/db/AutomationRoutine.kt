package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_routines")
data class AutomationRoutine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val triggerSource: String, // "7:00 AM", "Battery < 20%", "Hey Axel phrase"
    val actionSequence: String, // Comma separated: "Read Weather, Turn on power saver, Reduce Brightness"
    val isActive: Boolean = true
)
