package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val weight: Double,     // in kg
    val note: String = ""
)

@Entity(tableName = "weight_goal")
data class WeightGoal(
    @PrimaryKey val id: Int = 1, // Wir brauchen nur einen Ziel-Eintrag
    val goalWeight: Double,      // Wunschgewicht in kg
    val startWeight: Double,     // Startgewicht in kg
    val startDateString: String  // Startdatum: YYYY-MM-DD
)
