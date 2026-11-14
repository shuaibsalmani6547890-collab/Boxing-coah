package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.catalog.BoxingCatalog
import com.example.data.model.AppLanguage
import com.example.data.model.BoxingTechnique
import com.example.i18n.AppStrings
import com.example.ui.components.FooterCredit
import com.example.ui.components.HunterGlowCard
import com.example.ui.components.MotionClip
import com.example.ui.components.ProMotionVideoPlayer
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel

@Composable
fun CombosTechniquesScreen(
    viewModel: BoxingCoachViewModel,
    onLaunchComboInCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val customSteps by viewModel.customCombo.collectAsStateWithLifecycle()
    val coachState by viewModel.liveCoachState.collectAsStateWithLifecycle()
    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedTechId by remember { mutableStateOf<String?>(BoxingCatalog.techniques.first().id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = arena.surface,
            contentColor = arena.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(s.techniquesTab) },
                modifier = Modifier.testTag("tab_techniques")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(s.combosTab) },
                modifier = Modifier.testTag("tab_combos")
            )
        }

        if (selectedTab == 0) {
            val categories = listOf("All", "Stance", "Punch", "Defense", "Footwork")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            val filtered = remember(selectedCategory) {
                if (selectedCategory == "All") BoxingCatalog.techniques
                else BoxingCatalog.techniques.filter { it.categoryEn == selectedCategory }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { tech ->
                    TechniqueCard(
                        tech = tech,
                        isHi = isHi,
                        expanded = expandedTechId == tech.id,
                        onToggleExpand = {
                            expandedTechId = if (expandedTechId == tech.id) null else tech.id
                        },
                        stepsLabel = s.stepsLabel,
                        mistakesLabel = s.mistakesLabel
                    )
                }
                item { FooterCredit() }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Custom Combo Builder
                item {
                    HunterGlowCard(accentColor = arena.secondary) {
                        Text(s.comboBuilder, style = MaterialTheme.typography.titleLarge, color = arena.secondary)
                        Spacer(Modifier.height(8.dp))
                        val customObj = viewModel.buildCustomComboObject()
                        Text(
                            text = if (customSteps.isEmpty()) "Tap moves below" else "${customObj.code} • ${customObj.voiceCallEn}",
                            style = MaterialTheme.typography.titleMedium,
                            color = arena.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("JAB" to "1 Jab", "CROSS" to "2 Cross", "LEAD_HOOK" to "3 Hook", "UPPERCUT" to "5 Upper").forEach { (key, lbl) ->
                                FilterChip(
                                    selected = false,
                                    onClick = { viewModel.addStepToCustomCombo(key) },
                                    label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { viewModel.clearCustomCombo() }) {
                                Text("Clear")
                            }
                            Button(
                                onClick = {
                                    viewModel.launchComboInCoach(viewModel.buildCustomComboObject())
                                    onLaunchComboInCoach()
                                },
                                modifier = Modifier.weight(1f).testTag("practice_custom_combo"),
                                colors = ButtonDefaults.buttonColors(containerColor = arena.primary)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text(s.practiceInCoach)
                            }
                        }
                    }
                }

                // Rounds Timer Quick Settings
                item {
                    HunterGlowCard {
                        Text(s.roundTimer, style = MaterialTheme.typography.titleLarge, color = arena.primary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${s.roundLabel}: ${coachState.roundDurationSec / 60}m • ${s.restLabel}: ${coachState.restDurationSec}s • Rounds: ${coachState.totalRounds}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            listOf(
                                Triple(120, 45, 3) to "3×2m",
                                Triple(180, 60, 4) to "4×3m",
                                Triple(180, 45, 6) to "6×3m"
                            ).forEach { (cfg, lbl) ->
                                FilterChip(
                                    selected = coachState.roundDurationSec == cfg.first && coachState.totalRounds == cfg.third,
                                    onClick = { viewModel.configureTimer(cfg.first, cfg.second, cfg.third) },
                                    label = { Text(lbl) }
                                )
                            }
                        }
                    }
                }

                // Preset Combos
                items(BoxingCatalog.presetCombos) { combo ->
                    HunterGlowCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(combo.code, style = MaterialTheme.typography.headlineMedium, color = arena.primary)
                                Text(if (isHi) combo.nameHi else combo.nameEn, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("${combo.difficulty} • Voice: \"${combo.voiceCallEn}\"", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    viewModel.launchComboInCoach(combo)
                                    onLaunchComboInCoach()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = arena.secondary, contentColor = arena.bg)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = s.practiceInCoach)
                            }
                        }
                    }
                }

                item { FooterCredit() }
            }
        }
    }
}

@Composable
private fun TechniqueCard(
    tech: BoxingTechnique,
    isHi: Boolean,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    stepsLabel: String,
    mistakesLabel: String
) {
    val arena = LocalArenaColors.current
    HunterGlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHi) tech.categoryHi else tech.categoryEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = arena.secondary
                )
                Text(
                    text = if (isHi) tech.nameHi else tech.nameEn,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = arena.primary
            )
        }

        if (expanded) {
            Spacer(Modifier.height(12.dp))
            ProMotionVideoPlayer(
                clip = MotionClip.Technique(tech.animType),
                title = if (isHi) tech.nameHi else tech.nameEn,
                height = 210.dp
            )

            Spacer(Modifier.height(10.dp))
            Text(stepsLabel, style = MaterialTheme.typography.titleMedium, color = arena.primary)
            (if (isHi) tech.stepsHi else tech.stepsEn).forEachIndexed { idx, step ->
                Text("${idx + 1}. $step", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            }

            Spacer(Modifier.height(8.dp))
            Text(mistakesLabel, style = MaterialTheme.typography.titleMedium, color = GalatCrimson)
            (if (isHi) tech.mistakesHi else tech.mistakesEn).forEach { m ->
                Text("• $m", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
