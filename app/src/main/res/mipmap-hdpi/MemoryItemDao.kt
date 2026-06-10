package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryItemDao {
    @Query("SELECT * FROM memory_records ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(item: MemoryItem): Long

    @Delete
    suspend fun deleteMemory(item: MemoryItem)

    @Query("DELETE FROM memory_records WHERE id = :itemId")
    suspend fun deleteMemoryById(itemId: Long)

    @Query("DELETE FROM memory_records")
    suspend fun clearAll()
}
