package com.example.data.repository

import com.example.data.local.WeightDao
import com.example.data.model.WeightEntry
import com.example.data.model.WeightGoal
import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {

    val allWeightEntries: Flow<List<WeightEntry>> = weightDao.getAllWeightEntriesFlow()
    val weightGoal: Flow<WeightGoal?> = weightDao.getWeightGoalFlow()

    suspend fun insertWeightEntry(entry: WeightEntry) {
        // Falls bereits ein Eintrag für diesen Tag existiert, stellen wir sicher, dass wir ihn überschreiben oder korrigieren.
        val existing = weightDao.getEntryByDate(entry.dateString)
        if (existing != null) {
            val updated = entry.copy(id = existing.id)
            weightDao.insertWeightEntry(updated)
        } else {
            weightDao.insertWeightEntry(entry)
        }
    }

    suspend fun deleteWeightEntry(entry: WeightEntry) {
        weightDao.deleteWeightEntry(entry)
    }

    suspend fun insertWeightGoal(goal: WeightGoal) {
        weightDao.insertWeightGoal(goal)
    }

    suspend fun getWeightGoalDirect(): WeightGoal? {
        return weightDao.getWeightGoal()
    }

    suspend fun clearAllData() {
        weightDao.deleteAllWeightEntries()
        weightDao.deleteWeightGoal()
    }
}
