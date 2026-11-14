package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.catalog.BoxingCatalog
import com.example.data.model.*
import com.example.i18n.AppStrings
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel

@Composable
fun ExercisesScreen(
    viewModel: BoxingCoachViewModel,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    var weightSlider by remember(prefs.weightKg) { mutableFloatStateOf(prefs.weightKg) }
    var selectedSection by remember { mutableStateOf<ExerciseCategory?>(null) }
    var selectedExercise by remember { mutableStateOf<BoxingExercise?>(null) }

    val filteredExercises = remember(prefs.ownedEquipment, selectedSection) {
        BoxingCatalog.exercises.filter { ex ->
            prefs.ownedEquipment.contains(ex.equipment) &&
                (selectedSection == null || ex.category == selectedSection)
        }
    }

    val groupedSections = remember(filteredExercises) {
        ExerciseCategory.entries.mapNotNull { cat ->
            val list = filteredExercises.filter { it.category == cat }
            if (list.isEmpty()) null else cat to list
        }
    }

    val (dailyCalories, dailyProtein) = remember(prefs.weightKg, prefs.goal) {
        BoxingCatalog.calculateNutrition(prefs.weightKg, prefs.goal)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(arena.card)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_boxing_drills_banner),
                    contentDescription = "Boxing Drills Banner",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.55f
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(arena.bg.copy(alpha = 0.92f), arena.bg.copy(alpha = 0.35f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (isHi) "PRO BOXING & GYM ARSENAL" else "PRO BOXING & GYM ARSENAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = arena.secondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isHi) "19 Exercises • 4 Alag Sections" else "19 Exercises • 4 Training Sections",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isHi) "Kisi bhi exercise par tap karo — Video + Full Details!" else "Tap any exercise for animated coaching video & form breakdown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section Selector Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedSection == null,
                        onClick = { selectedSection = null },
                        label = { Text(if (isHi) "All Sections (19)" else "All Sections (19)") }
                    )
                }
                items(ExerciseCategory.entries) { cat ->
                    FilterChip(
                        selected = selectedSection == cat,
                        onClick = { selectedSection = cat },
                        label = { Text(if (isHi) cat.labelHi else cat.labelEn) },
                        modifier = Modifier.testTag("section_chip_${cat.name}")
                    )
                }
            }
        }

        // Weight & Goal Calculator
        item {
            HunterGlowCard(accentColor = arena.primary) {
                Text(s.weightGoalPlan, style = MaterialTheme.typography.titleLarge, color = arena.primary)
                Spacer(Modifier.height(4.dp))
                Text("${s.bodyWeightKg}: ${weightSlider.toInt()} kg", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Slider(
                    value = weightSlider,
                    onValueChange = { weightSlider = it },
                    onValueChangeFinished = { viewModel.updateWeight(weightSlider) },
                    valueRange = 40f..120f
                )
                Text(s.goalLabel, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    items(TrainingGoal.entries) { g ->
                        FilterChip(
                            selected = prefs.goal == g,
                            onClick = { viewModel.setGoal(g) },
                            label = { Text(if (isHi) g.labelHi else g.labelEn) }
                        )
                    }
                }
            }
        }

        // Equipment Filter
        item {
            HunterGlowCard(accentColor = arena.secondary) {
                Text(s.equipmentFilter, style = MaterialTheme.typography.titleMedium, color = arena.secondary)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Equipment.entries) { eq ->
                        val selected = prefs.ownedEquipment.contains(eq)
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleEquipment(eq) },
                            label = { Text(if (isHi) eq.labelHi else eq.labelEn) },
                            modifier = Modifier.testTag("equip_chip_${eq.name}")
                        )
                    }
                }
            }
        }

        // Separate Section Headers + Cards with Animated Video Thumbnails
        groupedSections.forEach { (category, list) ->
            item(key = "header_${category.name}") {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = arena.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, arena.primary.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = (if (isHi) category.labelHi else category.labelEn).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = arena.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isHi) category.descHi else category.descEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = arena.secondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${list.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = arena.secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            items(list, key = { it.id }) { ex ->
                val (planText, _) = BoxingCatalog.calculateExercisePlan(ex, prefs.weightKg, prefs.goal)
                HunterGlowCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedExercise = ex }
                        .testTag("exercise_card_${ex.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(122.dp)) {
                            ProMotionVideoPlayer(
                                clip = MotionClip.Exercise(ex.motionType),
                                title = ex.nameEn,
                                height = 96.dp,
                                compactPreview = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHi) ex.nameHi else ex.nameEn,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = planText,
                                style = MaterialTheme.typography.labelLarge,
                                color = SahiEmerald
                            )
                            Text(
                                text = if (isHi) ex.muscleHi else ex.muscleEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = arena.secondary
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = arena.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isHi) "Video & Full Details →" else "Video & Full Details →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = arena.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Indian Diet & Protein Bonus Card
        item {
            HunterGlowCard(accentColor = SahiEmerald) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = SahiEmerald)
                    Spacer(Modifier.width(8.dp))
                    Text(s.indianDietTitle, style = MaterialTheme.typography.titleLarge, color = SahiEmerald)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${s.caloriesTarget}: ~$dailyCalories kcal • ${s.proteinTarget}: ${dailyProtein}g",
                    style = MaterialTheme.typography.titleMedium,
                    color = arena.primary
                )
                Spacer(Modifier.height(8.dp))
                BoxingCatalog.indianFoods.forEach { food ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isHi) food.nameHi else food.nameEn, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                            Text(if (isHi) food.servingHi else food.servingEn, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Text(
                            text = "${food.proteinG}g P • ${food.calories} kcal",
                            style = MaterialTheme.typography.labelLarge,
                            color = SahiEmerald
                        )
                    }
                }
            }
        }

        item { FooterCredit() }
    }

    // Full-Detail Video & Form Coaching Modal
    val activeEx = selectedExercise
    if (activeEx != null) {
        ExerciseDetailDialog(
            exercise = activeEx,
            weightKg = prefs.weightKg,
            goal = prefs.goal,
            isHi = isHi,
            onDismiss = { selectedExercise = null },
            onLogQuickSet = {
                viewModel.logQuickSkipping(if (activeEx.id == "skipping") 5 else 2)
            }
        )
    }
}

@Composable
private fun ExerciseDetailDialog(
    exercise: BoxingExercise,
    weightKg: Float,
    goal: TrainingGoal,
    isHi: Boolean,
    onDismiss: () -> Unit,
    onLogQuickSet: () -> Unit
) {
    val arena = LocalArenaColors.current
    val (planText, _) = BoxingCatalog.calculateExercisePlan(exercise, weightKg, goal)

    BackHandler(onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(22.dp),
            color = arena.surface,
            border = BorderStroke(1.dp, arena.primary.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = (if (isHi) exercise.category.labelHi else exercise.category.labelEn).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = arena.secondary
                        )
                        Text(
                            text = if (isHi) exercise.nameHi else exercise.nameEn,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                // Full Motion Video Player
                ProMotionVideoPlayer(
                    clip = MotionClip.Exercise(exercise.motionType),
                    title = if (isHi) exercise.nameHi else exercise.nameEn,
                    height = 230.dp
                )

                // Prescription Card
                HunterGlowCard(accentColor = SahiEmerald) {
                    Text(
                        text = if (isHi) "TERA PERSONAL PLAN (${weightKg.toInt()} kg)" else "YOUR PRESCRIPTION (${weightKg.toInt()} kg)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SahiEmerald
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(planText, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tempo: ${exercise.tempoCue} • Rest: ${exercise.restSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = arena.secondary
                    )
                }

                // Muscles & Boxing Ring Benefit
                HunterGlowCard(accentColor = arena.primary) {
                    Text(
                        text = if (isHi) "Muscles & Equipment" else "Target Muscles & Equipment",
                        style = MaterialTheme.typography.titleMedium,
                        color = arena.primary
                    )
                    Text(
                        text = "${if (isHi) exercise.muscleHi else exercise.muscleEn} • ${if (isHi) exercise.equipment.labelHi else exercise.equipment.labelEn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isHi) "Boxing Ring Me Fayda:" else "Boxing Ring Benefit:",
                        style = MaterialTheme.typography.titleMedium,
                        color = arena.secondary
                    )
                    Text(
                        text = if (isHi) exercise.boxingBenefitHi else exercise.boxingBenefitEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Step-by-Step Instructions
                HunterGlowCard {
                    Text(
                        text = if (isHi) "Step-by-Step Kaise Karein" else "Step-by-Step Form",
                        style = MaterialTheme.typography.titleMedium,
                        color = arena.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    (if (isHi) exercise.stepsHi else exercise.stepsEn).forEachIndexed { i, step ->
                        Text("${i + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isHi) "Common Galtiyan (Bacho)" else "Common Mistakes",
                        style = MaterialTheme.typography.titleMedium,
                        color = GalatCrimson
                    )
                    Spacer(Modifier.height(4.dp))
                    (if (isHi) exercise.mistakesHi else exercise.mistakesEn).forEach { m ->
                        Text("• $m", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isHi) "Band Karo" else "Close")
                    }
                    Button(
                        onClick = {
                            onLogQuickSet()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = arena.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (isHi) "Set Complete" else "Log Done")
                    }
                }
            }
        }
    }
}
