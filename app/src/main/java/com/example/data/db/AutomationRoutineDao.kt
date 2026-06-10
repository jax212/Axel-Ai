package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationRoutineDao {
    @Query("SELECT * FROM automation_routines")
    fun getAllRoutines(): Flow<List<AutomationRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: AutomationRoutine): Long

    @Update
    suspend fun updateRoutine(routine: AutomationRoutine)

    @Delete
    suspend fun deleteRoutine(routine: AutomationRoutine)

    @Query("DELETE FROM automation_routines")
    suspend fun clearAll()
}
