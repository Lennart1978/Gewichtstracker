package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WeightEntry
import com.example.data.model.WeightGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY dateString ASC")
    fun getAllWeightEntriesFlow(): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightEntry(entry: WeightEntry)

    @Delete
    suspend fun deleteWeightEntry(entry: WeightEntry)

    @Query("SELECT * FROM weight_entries WHERE dateString = :dateString LIMIT 1")
    suspend fun getEntryByDate(dateString: String): WeightEntry?

    @Query("SELECT * FROM weight_goal WHERE id = 1 LIMIT 1")
    fun getWeightGoalFlow(): Flow<WeightGoal?>

    @Query("SELECT * FROM weight_goal WHERE id = 1 LIMIT 1")
    suspend fun getWeightGoal(): WeightGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightGoal(goal: WeightGoal)

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAllWeightEntries()

    @Query("DELETE FROM weight_goal")
    suspend fun deleteWeightGoal()
}
