package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ScheduleDayEntity
import com.example.data.model.AppLanguage
import com.example.data.model.SchedulePlanType
import com.example.data.model.ScheduledExercise
import com.example.i18n.AppStrings
import com.example.ui.components.FooterCredit
import com.example.ui.components.HunterGlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel

@Composable
fun ScheduleScreen(
    viewModel: BoxingCoachViewModel,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val days by viewModel.weeklySchedule.collectAsStateWithLifecycle()
    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    var editingDay by remember { mutableStateOf<ScheduleDayEntity?>(null) }
    var addingToDay by remember { mutableStateOf<ScheduleDayEntity?>(null) }
    var editingExercisePair by remember { mutableStateOf<Pair<ScheduleDayEntity, ScheduledExercise>?>(null) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Plan Preset Selector
        item {
            HunterGlowCard(accentColor = MonarchPurple) {
                Text(s.navSchedule, style = MaterialTheme.typography.titleLarge, color = SystemCyan)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SchedulePlanType.entries) { plan ->
                        FilterChip(
                            selected = prefs.activePlan == plan,
                            onClick = { viewModel.selectSchedulePlan(plan) },
                            label = { Text(if (isHi) plan.labelHi else plan.labelEn) },
                            modifier = Modifier.testTag("plan_chip_${plan.name}")
                        )
                    }
                }
            }
        }

        items(days, key = { it.dayOfWeek }) { day ->
            val exercises = remember(day.exercisesJson) {
                viewModel.repo.decodeExercises(day.exercisesJson)
            }
            HunterGlowCard(
                accentColor = if (day.isRestDay) SahiEmerald else if (day.isRecoveryDay) WarningAmber else SystemCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(day.dayTitle, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        val timeText = "%02d:%02d".format(day.reminderHour, day.reminderMinute)
                        Text(
                            text = "${s.reminderLabel}: $timeText • ${if (day.reminderEnabled) "ON" else "OFF"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SystemCyan
                        )
                    }
                    IconButton(onClick = { editingDay = day }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit day", tint = SystemCyan)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                    FilterChip(
                        selected = day.isRestDay,
                        onClick = { viewModel.toggleDayRest(day) },
                        label = { Text(s.restDay) }
                    )
                    FilterChip(
                        selected = day.isRecoveryDay,
                        onClick = { viewModel.toggleDayRecovery(day) },
                        label = { Text(s.recoveryDay) }
                    )
                }

                if (!day.isRestDay) {
                    exercises.forEachIndexed { idx, ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ex.name, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                Text("${ex.sets} sets × ${ex.reps} • ${ex.equipment}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            IconButton(
                                onClick = { viewModel.moveExerciseInDay(day, idx, -1) },
                                enabled = idx > 0
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = TextSecondary)
                            }
                            IconButton(
                                onClick = { viewModel.moveExerciseInDay(day, idx, 1) },
                                enabled = idx < exercises.lastIndex
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = TextSecondary)
                            }
                            IconButton(onClick = { editingExercisePair = day to ex }) {
                                Icon(Icons.Default.Tune, contentDescription = "Edit Exercise", tint = SystemCyan)
                            }
                            IconButton(onClick = { viewModel.deleteExerciseFromDay(day, ex.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = GalatCrimson)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { addingToDay = day },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.addExercise)
                    }
                }
            }
        }

        item { FooterCredit() }
    }

    // Edit Day Title & Reminder Time Dialog
    val dEdit = editingDay
    if (dEdit != null) {
        var title by remember(dEdit) { mutableStateOf(dEdit.dayTitle) }
        var hour by remember(dEdit) { mutableIntStateOf(dEdit.reminderHour) }
        var min by remember(dEdit) { mutableIntStateOf(dEdit.reminderMinute) }
        var remOn by remember(dEdit) { mutableStateOf(dEdit.reminderEnabled) }

        AlertDialog(
            onDismissRequest = { editingDay = null },
            containerColor = ObsidianCard,
            title = { Text("Edit Day", color = SystemCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.reminderLabel, modifier = Modifier.weight(1f))
                        Switch(
                            checked = remOn,
                            onCheckedChange = {
                                remOn = it
                                if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                    }
                    Text("Hour: $hour • Min: $min")
                    Slider(value = hour.toFloat(), onValueChange = { hour = it.toInt() }, valueRange = 4f..22f)
                    Slider(value = min.toFloat(), onValueChange = { min = (it.toInt() / 5) * 5 }, valueRange = 0f..55f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateDayTitleAndTime(dEdit, title, hour, min, remOn)
                    editingDay = null
                }) { Text("Save") }
            }
        )
    }

    // Add Exercise Dialog
    val dAdd = addingToDay
    if (dAdd != null) {
        var name by remember { mutableStateOf("Shadow Boxing") }
        var sets by remember { mutableIntStateOf(4) }
        var reps by remember { mutableStateOf("3 min") }
        var eq by remember { mutableStateOf("Bodyweight") }

        AlertDialog(
            onDismissRequest = { addingToDay = null },
            containerColor = ObsidianCard,
            title = { Text(s.addExercise, color = SystemCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise") })
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps / Duration") })
                    OutlinedTextField(value = eq, onValueChange = { eq = it }, label = { Text("Equipment") })
                    Text("Sets: $sets")
                    Slider(value = sets.toFloat(), onValueChange = { sets = it.toInt() }, valueRange = 1f..8f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addExerciseToDay(dAdd, name.trim(), sets, reps.trim(), eq.trim())
                    }
                    addingToDay = null
                }) { Text("Add") }
            }
        )
    }

    // Edit Exercise Dialog
    val pair = editingExercisePair
    if (pair != null) {
        val (day, ex) = pair
        var name by remember(ex) { mutableStateOf(ex.name) }
        var sets by remember(ex) { mutableIntStateOf(ex.sets) }
        var reps by remember(ex) { mutableStateOf(ex.reps) }
        AlertDialog(
            onDismissRequest = { editingExercisePair = null },
            containerColor = ObsidianCard,
            title = { Text("Edit Exercise", color = SystemCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps / Time") })
                    Text("Sets: $sets")
                    Slider(value = sets.toFloat(), onValueChange = { sets = it.toInt() }, valueRange = 1f..10f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateExerciseInDay(day, ex.copy(name = name, sets = sets, reps = reps))
                    editingExercisePair = null
                }) { Text("Save") }
            }
        )
    }
}
