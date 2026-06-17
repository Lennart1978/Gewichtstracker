package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.WeightEntry
import com.example.data.model.WeightGoal
import com.example.data.repository.WeightRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeightViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WeightRepository

    val allEntries: StateFlow<List<WeightEntry>>
    val weightGoal: StateFlow<WeightGoal?>

    // Form inputs for log addition
    val inputWeight = MutableStateFlow("")
    val inputDate = MutableStateFlow(getCurrentDateString())
    val inputNote = MutableStateFlow("")

    // Form inputs for Goal Configuration
    val goalInputWeight = MutableStateFlow("")
    val goalInputStartWeight = MutableStateFlow("")
    val goalInputStartDate = MutableStateFlow(getCurrentDateString())

    // Optional user height to compute BMI
    private val prefs = application.getSharedPreferences("weight_tracker_prefs", Application.MODE_PRIVATE)
    val userHeight = MutableStateFlow(prefs.getFloat("user_height_cm", 175f)) // Default values

    init {
        val database = AppDatabase.getDatabase(application)
        repository = WeightRepository(database.weightDao())

        allEntries = repository.allWeightEntries
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        weightGoal = repository.weightGoal
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
            
        // Pre-fill goal input values if they already exist
        viewModelScope.launch {
            repository.weightGoal.collect { goal ->
                if (goal != null) {
                    if (goalInputWeight.value.isEmpty()) {
                        goalInputWeight.value = goal.goalWeight.toString()
                    }
                    if (goalInputStartWeight.value.isEmpty()) {
                        goalInputStartWeight.value = goal.startWeight.toString()
                    }
                    if (goalInputStartDate.value.isEmpty()) {
                        goalInputStartDate.value = goal.startDateString
                    }
                }
            }
        }
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun saveHeight(heightCm: Float) {
        prefs.edit().putFloat("user_height_cm", heightCm).apply()
        userHeight.value = heightCm
    }

    fun addWeightEntry(weight: Double, dateString: String, note: String): Boolean {
        if (weight <= 0.0 || dateString.isEmpty()) return false
        viewModelScope.launch {
            val entry = WeightEntry(dateString = dateString, weight = weight, note = note)
            repository.insertWeightEntry(entry)
        }
        return true
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeightEntry(entry)
        }
    }

    fun setWeightGoal(goalWeight: Double, startWeight: Double, startDateString: String): Boolean {
        if (goalWeight <= 0.0 || startWeight <= 0.0 || startDateString.isEmpty()) return false
        viewModelScope.launch {
            val goal = WeightGoal(
                goalWeight = goalWeight,
                startWeight = startWeight,
                startDateString = startDateString
            )
            repository.insertWeightGoal(goal)

            // Falls es noch keinen Eintrag an diesem Tag gibt, füge ihn als Startpunkt hinzu
            val currentList = allEntries.value
            val existing = currentList.find { it.dateString == startDateString }
            if (existing == null) {
                repository.insertWeightEntry(
                    WeightEntry(
                        dateString = startDateString,
                        weight = startWeight,
                        note = "Anfangs-Gewichts-Eintrag"
                    )
                )
            }
        }
        return true
    }
}
