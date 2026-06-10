package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CallRecord): Long

    @Update
    suspend fun updateRecord(record: CallRecord)

    @Delete
    suspend fun deleteRecord(record: CallRecord)

    @Query("DELETE FROM call_records")
    suspend fun clearAll()
}
