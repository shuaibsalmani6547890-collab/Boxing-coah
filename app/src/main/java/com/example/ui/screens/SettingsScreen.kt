package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeStyle
import com.example.i18n.AppStrings
import com.example.ui.components.FooterCredit
import com.example.ui.components.HunterGlowCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: BoxingCoachViewModel,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    var showResetDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val jsonText = viewModel.exportJsonString()
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonText.toByteArray())
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (!raw.isNullOrBlank()) {
                    viewModel.importJsonString(raw)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme Selector
        item {
            HunterGlowCard(accentColor = arena.primary) {
                Text(
                    text = if (isHi) "App Theme Style" else "Arena Theme Style",
                    style = MaterialTheme.typography.titleLarge,
                    color = arena.primary
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AppThemeStyle.entries) { style ->
                        FilterChip(
                            selected = prefs.themeStyle == style,
                            onClick = { viewModel.setThemeStyle(style) },
                            label = { Text(if (isHi) style.labelHi else style.labelEn) },
                            modifier = Modifier.testTag("theme_chip_${style.name}")
                        )
                    }
                }
            }
        }

        item {
            HunterGlowCard(accentColor = arena.secondary) {
                Text(s.settingsLanguage, style = MaterialTheme.typography.titleLarge, color = arena.secondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = isHi,
                        onClick = { viewModel.setLanguage(AppLanguage.HINGLISH) },
                        label = { Text("Hinglish (Default)") },
                        modifier = Modifier.testTag("lang_hinglish_chip")
                    )
                    FilterChip(
                        selected = !isHi,
                        onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                        label = { Text("English") },
                        modifier = Modifier.testTag("lang_english_chip")
                    )
                }
            }
        }

        item {
            HunterGlowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.settingsVoice, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Switch(
                        checked = prefs.voiceEnabled,
                        onCheckedChange = { viewModel.setVoiceEnabled(it) },
                        modifier = Modifier.testTag("voice_switch")
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(s.settingsCamera, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
                    FilterChip(
                        selected = prefs.useFrontCamera,
                        onClick = { viewModel.setUseFrontCamera(true) },
                        label = { Text("Front Camera") }
                    )
                    FilterChip(
                        selected = !prefs.useFrontCamera,
                        onClick = { viewModel.setUseFrontCamera(false) },
                        label = { Text("Back Camera") }
                    )
                }
            }
        }

        item {
            HunterGlowCard(accentColor = arena.secondary) {
                Text("Offline Data Backup", style = MaterialTheme.typography.titleLarge, color = arena.secondary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("shadow_boxing_backup.json") },
                        modifier = Modifier.weight(1f).testTag("export_json_button")
                    ) {
                        Text(s.exportJson, style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f).testTag("import_json_button")
                    ) {
                        Text(s.importJson, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("reset_data_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GalatCrimson)
                ) {
                    Text(s.resetAllData)
                }
            }
        }

        item { FooterCredit() }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = arena.card,
            title = { Text(s.resetAllData, color = GalatCrimson) },
            text = { Text(s.confirmReset) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GalatCrimson)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}
