package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: BoxingCoachViewModel,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val context = LocalContext.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val photos by viewModel.progressPhotos.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()

    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    val orderedSessions = remember(sessions) { sessions.reversed().takeLast(12) }
    val scoreSeries = remember(orderedSessions) { orderedSessions.map { it.avgScore.toFloat() } }
    val punchSeries = remember(orderedSessions) { orderedSessions.map { it.totalPunches } }
    val guardSeries = remember(orderedSessions) { orderedSessions.map { it.guardDisciplinePct.toFloat() } }
    val footSeries = remember(orderedSessions) { orderedSessions.map { it.footworkScorePct.toFloat() } }
    val weightSeries = remember(weightLogs) { weightLogs.takeLast(12).map { it.weightKg } }

    val weeklyReport = remember(sessions) { viewModel.computeWeeklyReport(sessions) }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var beforeIdx by remember { mutableIntStateOf(0) }
    var afterIdx by remember { mutableIntStateOf(0) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addProgressPhoto(uri.toString(), "Gallery • ${prefs.weightKg.toInt()} kg")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingPhotoUri
        if (success && uri != null) {
            viewModel.addProgressPhoto(uri.toString(), "Camera • ${prefs.weightKg.toInt()} kg")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Personal Records
        item {
            HunterGlowCard(accentColor = RankGold) {
                Text(s.personalRecords, style = MaterialTheme.typography.titleLarge, color = RankGold)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Best Round Punches", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${prefs.mostPunchesOneRound}", style = MaterialTheme.typography.headlineMedium, color = SystemCyan)
                    }
                    Column {
                        Text("Longest Streak", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${prefs.longestStreak} days", style = MaterialTheme.typography.headlineMedium, color = SahiEmerald)
                    }
                    Column {
                        Text("Sessions", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${sessions.size}", style = MaterialTheme.typography.headlineMedium, color = MonarchPurpleGlow)
                    }
                }
            }
        }

        // Weekly Report
        item {
            HunterGlowCard(accentColor = SystemCyan) {
                Text(s.weeklyReport, style = MaterialTheme.typography.titleLarge, color = SystemCyan)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isHi) weeklyReport.improvedTextHi else weeklyReport.improvedTextEn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SahiEmerald
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isHi) weeklyReport.weakPointHi else weeklyReport.weakPointEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarningAmber
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isHi) weeklyReport.nextFocusHi else weeklyReport.nextFocusEn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        // Form Score Chart
        item {
            HunterGlowCard {
                Text(s.progressScoreChart, style = MaterialTheme.typography.titleMedium, color = SystemCyan)
                Spacer(Modifier.height(8.dp))
                if (scoreSeries.isEmpty()) {
                    Text(
                        text = if (isHi) "Live Coach session complete karo — graph yahan dikhega!" else "Complete a Live Coach session to see your trend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                } else {
                    CustomLineChart(values = scoreSeries, lineColor = SystemCyan)
                }
            }
        }

        // Punches Per Session Bar Chart
        item {
            HunterGlowCard {
                Text(s.punchesChart, style = MaterialTheme.typography.titleMedium, color = MonarchPurpleGlow)
                Spacer(Modifier.height(8.dp))
                if (punchSeries.isEmpty()) {
                    Text("-", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                } else {
                    CustomBarChart(values = punchSeries)
                }
            }
        }

        // Hand vs Leg Progress Graph
        item {
            HunterGlowCard {
                Text(s.handVsLegChart, style = MaterialTheme.typography.titleMedium, color = SahiEmerald)
                Text(
                    text = "Cyan = Guard (Hand) • Purple = Footwork (Leg)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                if (guardSeries.isEmpty()) {
                    Text("-", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                } else {
                    CustomLineChart(
                        values = guardSeries,
                        lineColor = SystemCyan,
                        secondaryValues = footSeries,
                        secondaryColor = MonarchPurpleGlow
                    )
                }
            }
        }

        // Body Weight Chart
        item {
            HunterGlowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${s.weightConsistencyChart} (${prefs.weightKg.toInt()} kg)", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    OutlinedButton(onClick = { viewModel.updateWeight(prefs.weightKg) }) {
                        Text("Log", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (weightSeries.isEmpty()) {
                    CustomLineChart(values = listOf(prefs.weightKg), lineColor = WarningAmber, maxValue = 130f)
                } else {
                    CustomLineChart(values = weightSeries, lineColor = WarningAmber, maxValue = 130f)
                }
            }
        }

        // Progress Photos & Before-After
        item {
            HunterGlowCard(accentColor = MonarchPurple) {
                Text(s.progressPhotos, style = MaterialTheme.typography.titleLarge, color = MonarchPurpleGlow)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f).testTag("pick_gallery_photo_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(s.addPhotoGallery, style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = {
                            val dir = File(context.filesDir, "photos").apply { mkdirs() }
                            val file = File(dir, "prog_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            pendingPhotoUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f).testTag("take_progress_photo_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(s.takePhotoCamera, style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (photos.size >= 2) {
                    Spacer(Modifier.height(12.dp))
                    Text(s.beforeAfterTitle, style = MaterialTheme.typography.titleMedium, color = SystemCyan)
                    Spacer(Modifier.height(6.dp))
                    val bPhoto = photos[beforeIdx.coerceIn(0, photos.lastIndex)]
                    val aPhoto = photos[afterIdx.coerceIn(0, photos.lastIndex)]
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = bPhoto.uriString,
                                contentDescription = "Before",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text("Before (${bPhoto.weightKg.toInt()}kg)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            AsyncImage(
                                model = aPhoto.uriString,
                                contentDescription = "After",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text("After (${aPhoto.weightKg.toInt()}kg)", style = MaterialTheme.typography.labelSmall, color = SahiEmerald)
                        }
                    }
                }

                if (photos.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(photos.size) { idx ->
                            val p = photos[idx]
                            val fmt = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(p.timestamp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        if (idx != afterIdx) beforeIdx = afterIdx
                                        afterIdx = idx
                                    }
                            ) {
                                AsyncImage(
                                    model = p.uriString,
                                    contentDescription = p.note,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, SystemCyan, RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(fmt, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        item { FooterCredit() }
    }
}
