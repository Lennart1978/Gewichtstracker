package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WeightEntry
import com.example.data.model.WeightGoal
import com.example.ui.components.WeightChart
import com.example.ui.components.formatGermanDate
import com.example.ui.viewmodel.WeightViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WeightViewModel) {
    val entries by viewModel.allEntries.collectAsState()
    val goal by viewModel.weightGoal.collectAsState()
    val heightCm by viewModel.userHeight.collectAsState()

    var showAddLogDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gewichtstracker",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = "Behalte deine Fitness im Blick",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (goal != null) {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Einstellungen öffnen"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("main_top_app_bar")
            )
        },
        floatingActionButton = {
            if (goal != null) {
                Button(
                    onClick = { showAddLogDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .testTag("fab_add_log")
                        .padding(bottom = 16.dp, end = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gewicht loggen", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (goal == null) {
                // Onboarding State: Set Goal and Start Weights
                OnboardingView(
                    onSaveGoal = { gW, sW, dateVal ->
                        viewModel.setWeightGoal(gW, sW, dateVal)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Active Dashboard State
                DashboardView(
                    entries = entries,
                    goal = goal!!,
                    heightCm = heightCm,
                    onDeleteEntry = { viewModel.deleteWeightEntry(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dialog for logging weight
            if (showAddLogDialog) {
                AddLogDialog(
                    onDismiss = { showAddLogDialog = false },
                    onConfirm = { weight, date, note ->
                        val success = viewModel.addWeightEntry(weight, date, note)
                        if (success) {
                            showAddLogDialog = false
                        }
                    }
                )
            }

            // Dialog for editing goal and height
            if (showSettingsDialog && goal != null) {
                EditGoalDialog(
                    currentGoal = goal!!,
                    currentHeight = heightCm,
                    onDismiss = { showSettingsDialog = false },
                    onConfirmSetting = { goalWeight, startWeight, startDate, height ->
                        viewModel.setWeightGoal(goalWeight, startWeight, startDate)
                        viewModel.saveHeight(height)
                        showSettingsDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingView(
    onSaveGoal: (Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var goalWInput by remember { mutableStateOf("") }
    var startWInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getCurrentDateString()) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome and Aesthetic Title card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Willkommen! 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wir helfen dir, dein Wunschgewicht gesund und nachhaltig zu erreichen. Lass uns zuerst deine Ziele festlegen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Text(
            text = "Ziele einrichten",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Starting Weight In
        OutlinedTextField(
            value = startWInput,
            onValueChange = { startWInput = it.replace(',', '.') },
            label = { Text("Aktuelles Gewicht / Startgewicht (kg)") },
            placeholder = { Text("z.B. 82.5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_start_weight")
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Target Weight In
        OutlinedTextField(
            value = goalWInput,
            onValueChange = { goalWInput = it.replace(',', '.') },
            label = { Text("Wunschgewicht (kg)") },
            placeholder = { Text("z.B. 72.0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_goal_weight")
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Date selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .clickable {
                    val calendar = Calendar.getInstance()
                    val dpd = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    dpd.show()
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Startdatum",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatGermanDate(selectedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Error log
        if (inputError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = inputError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val startW = startWInput.toDoubleOrNull()
                val goalW = goalWInput.toDoubleOrNull()
                if (startW == null || startW <= 0.0) {
                    inputError = "Bitte gib ein gültiges Startgewicht ein!"
                } else if (goalW == null || goalW <= 0.0) {
                    inputError = "Bitte gib ein gültiges Wunschgewicht ein!"
                } else {
                    onSaveGoal(goalW, startW, selectedDate)
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboarding_save_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Fortfahren", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardView(
    entries: List<WeightEntry>,
    goal: WeightGoal,
    heightCm: Float,
    onDeleteEntry: (WeightEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Sort entries to find critical weights
    val sortedEntries = remember(entries) {
        entries.sortedByDescending { it.dateString }
    }

    val currentWeight = sortedEntries.firstOrNull()?.weight ?: goal.startWeight
    val currentWeightDate = sortedEntries.firstOrNull()?.dateString ?: goal.startDateString

    // Calculations
    val remainingToGoal = currentWeight - goal.goalWeight
    val targetReached = remainingToGoal <= 0.0

    // Lost so far compared to initial starting weight
    val weightLossTotal = goal.startWeight - currentWeight

    // BMI computation
    val bmi = if (heightCm > 0) {
        val heightM = heightCm / 100.0
        currentWeight / (heightM * heightM)
    } else 0.0

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- 1. Top Statistics Row/Cards ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Current Weight
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Aktuell",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", currentWeight)} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Eintrag: ${formatGermanDate(currentWeightDate).take(10)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Card 2: Wunschgewicht Goal Weight
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Wunschgewicht",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format(Locale.getDefault(), "%.1f", goal.goalWeight)} kg",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981) // emerald
                    )
                    Text(
                        text = if (targetReached) "Ziel erreicht! 🎉" else "Noch ${String.format(Locale.getDefault(), "%.1f", remainingToGoal)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (targetReached) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (targetReached) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar visual helper
        ProgressIndicatorCard(
            startWeight = goal.startWeight,
            goalWeight = goal.goalWeight,
            currentWeight = currentWeight,
            weightLossTotal = weightLossTotal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. Chart Section / Liniendiagramm ---
        Text(
            text = "Gewichtsverlauf",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        WeightChart(
            entries = entries,
            goalWeight = goal.goalWeight,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. Optional BMI Card ---
        if (heightCm > 0) {
            BMICard(bmi = bmi, heightCm = heightCm)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- 4. Historical Logs Title ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tagebuch Einträge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${entries.size} Logs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        // List of entries
        if (sortedEntries.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Keine Einträge",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Füge ein tägliches Gewicht hinzu, um den Fortschritt aufzuschreiben.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sortedEntries.forEach { entry ->
                    WeightEntryRow(
                        entry = entry,
                        isStartEntry = entry.dateString == goal.startDateString,
                        onDeleteEntry = { onDeleteEntry(entry) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(80.dp)) // Spacer so FAB doesn't overlay elements
    }
}

@Composable
fun ProgressIndicatorCard(
    startWeight: Double,
    goalWeight: Double,
    currentWeight: Double,
    weightLossTotal: Double
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val totalLossNeeded = startWeight - goalWeight
            val progressPercentage = if (totalLossNeeded > 0.0) {
                (weightLossTotal / totalLossNeeded).coerceIn(0.0, 1.0).toFloat()
            } else if (currentWeight <= goalWeight) {
                1.0f
            } else {
                0.0f
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ziel-Progress",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (weightLossTotal > 0.0) {
                        "Erfolgreich abgenommen: ${String.format(Locale.getDefault(), "%.1f", weightLossTotal)} kg"
                    } else if (weightLossTotal < 0.0) {
                        "Zugelegt: ${String.format(Locale.getDefault(), "%.1f", -weightLossTotal)} kg"
                    } else {
                        "Gewicht gehalten!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (weightLossTotal >= 0.0) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Beautiful linear visual progress
            LinearProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Start: ${String.format(Locale.getDefault(), "%.1f", startWeight)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(progressPercentage * 100).toInt()}% erreicht",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ziel: ${String.format(Locale.getDefault(), "%.1f", goalWeight)} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BMICard(bmi: Double, heightCm: Float) {
    val (category, color) = when {
        bmi < 18.5 -> Pair("Untergewicht ⚠️", Color(0xFF3B82F6))     // blue
        bmi < 25.0 -> Pair("Normalgewicht ✅", Color(0xFF10B981))    // green / emerald
        bmi < 30.0 -> Pair("Übergewicht ⚠️", Color(0xFFF59E0B))      // amber
        else -> Pair("Adipositas 🚨", Color(0xFFEF4444))             // red
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Dein Body-Mass-Index (BMI)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", bmi),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "kg/m²",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Kategorie: $category",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun WeightEntryRow(
    entry: WeightEntry,
    isStartEntry: Boolean,
    onDeleteEntry: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚖️",
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${entry.weight} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatGermanDate(entry.dateString),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.note.isNotEmpty()) {
                        Text(
                            text = entry.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isStartEntry) {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                } else {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_log_${entry.dateString}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Log löschen",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Eintrag löschen?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Möchtest du den Gewichtseintrag für den ${formatGermanDate(entry.dateString)} (${entry.weight} kg) wirklich entfernen?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Abbrechen")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onDeleteEntry()
                                showDeleteConfirm = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Löschen")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddLogDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getCurrentDateString()) }
    var noteInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Gewicht protokollieren 📅",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Weight log
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it.replace(',', '.') },
                    label = { Text("Gewicht (kg)") },
                    placeholder = { Text("z.B. 79.2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_weight_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Date selection triggers DatePickerDialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val parts = selectedDate.split("-")
                            if (parts.size == 3) {
                                calendar.set(Calendar.YEAR, parts[0].toInt())
                                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                                calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Datum wählen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Ausgewähltes Datum",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatGermanDate(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Optional note item
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Notiz (optional)") },
                    placeholder = { Text("z.B. Nach dem Training, nüchtern") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_note_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val weight = weightInput.toDoubleOrNull()
                            if (weight == null || weight <= 0.0) {
                                errorMessage = "Bitte gib ein korrektes Gewicht ein!"
                            } else {
                                onConfirm(weight, selectedDate, noteInput)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_confirm_button")
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

@Composable
fun EditGoalDialog(
    currentGoal: WeightGoal,
    currentHeight: Float,
    onDismiss: () -> Unit,
    onConfirmSetting: (Double, Double, String, Float) -> Unit
) {
    var goalWInput by remember { mutableStateOf(currentGoal.goalWeight.toString()) }
    var startWInput by remember { mutableStateOf(currentGoal.startWeight.toString()) }
    var heightInput by remember { mutableStateOf(if (currentHeight > 0) currentHeight.toString() else "") }
    var selectedDate by remember { mutableStateOf(currentGoal.startDateString) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Konfiguration / Einstellungen ⚙️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Wunschgewicht
                OutlinedTextField(
                    value = goalWInput,
                    onValueChange = { goalWInput = it.replace(',', '.') },
                    label = { Text("Wunschgewicht (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_goal_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Startgewicht
                OutlinedTextField(
                    value = startWInput,
                    onValueChange = { startWInput = it.replace(',', '.') },
                    label = { Text("Startgewicht (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_start_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Körpergröße (für BMI)
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it.replace(',', '.') },
                    label = { Text("Größe in cm (für BMI)") },
                    placeholder = { Text("z.B. 175") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_height_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Starting Date Configuration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable {
                            val calendar = Calendar.getInstance()
                            val parts = selectedDate.split("-")
                            if (parts.size == 3) {
                                calendar.set(Calendar.YEAR, parts[0].toInt())
                                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                                calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Startdatum",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Startdatum",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatGermanDate(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Abbrechen")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val goalW = goalWInput.toDoubleOrNull()
                            val startW = startWInput.toDoubleOrNull()
                            val height = heightInput.toFloatOrNull() ?: 0f
                            if (goalW == null || goalW <= 0.0) {
                                errorMessage = "Bitte gib ein Wunschgewicht ein!"
                            } else if (startW == null || startW <= 0.0) {
                                errorMessage = "Bitte gib ein Startgewicht ein!"
                            } else {
                                onConfirmSetting(goalW, startW, selectedDate, height)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("settings_save_button")
                    ) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

private fun getCurrentDateString(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date())
}
