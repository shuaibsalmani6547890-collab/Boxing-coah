package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.catalog.BoxingCatalog
import com.example.data.local.UserPreferencesManager
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeStyle
import com.example.data.model.HunterRank
import com.example.i18n.AppStrings
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: BoxingCoachViewModel,
    onNavigateToCoach: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val schedule by viewModel.weeklySchedule.collectAsStateWithLifecycle()
    val checkIns by viewModel.checkIns.collectAsStateWithLifecycle()
    val quoteIdx by viewModel.quoteIndex.collectAsStateWithLifecycle()

    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)
    val rank = HunterRank.fromLevel(prefs.level)
    val nextLevelXp = UserPreferencesManager.xpForNextLevel(prefs.level)
    val xpProgress = (prefs.xp.toFloat() / nextLevelXp.toFloat()).coerceIn(0f, 1f)

    val todayKey = UserPreferencesManager.todayKey()
    val yesterdayKey = UserPreferencesManager.yesterdayKey()
    val streakBroken = prefs.lastWorkoutDate.isNotBlank() &&
        prefs.lastWorkoutDate != todayKey &&
        prefs.lastWorkoutDate != yesterdayKey

    val quotes = if (isHi) AppStrings.quotesHinglish else AppStrings.quotesEnglish
    val currentQuote = quotes[quoteIdx % quotes.size]

    val calDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val mappedDay = if (calDay == Calendar.SUNDAY) 7 else calDay - 1
    val todaySchedule = schedule.find { it.dayOfWeek == mappedDay }

    val disciplineScore = remember(checkIns, prefs.streak) {
        val base = if (checkIns.isEmpty()) 70 else (checkIns.count { it.disciplineFollowed } * 100 / checkIns.size)
        ((base + prefs.streak * 4).coerceIn(0, 100))
    }

    var mood by remember { mutableIntStateOf(4) }
    var sleepHrs by remember { mutableFloatStateOf(7f) }
    var soreness by remember { mutableIntStateOf(2) }
    val todayCheckIn = checkIns.find { it.dateKey == todayKey }

    val quests = viewModel.dailyQuests()
    val bannerRes = if (prefs.themeStyle == AppThemeStyle.HUNTER_BLUE) {
        R.drawable.img_hunter_banner
    } else {
        R.drawable.img_arena_crimson_hero
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(arena.bg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Arena Status Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(arena.card)
            ) {
                Image(
                    painter = painterResource(id = bannerRes),
                    contentDescription = "Championship Boxing Arena",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.58f
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(arena.bg.copy(alpha = 0.94f), arena.bg.copy(alpha = 0.35f))
                            )
                        )
                )
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FIGHT NIGHT ARENA // OFFLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = arena.secondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${s.levelLabel} ${prefs.level} • ${s.rankLabel} ${rank.letter}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary
                            )
                        }
                        RankBadge(rank = rank)
                    }

                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = arena.primary,
                        trackColor = arena.bg
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "XP: ${prefs.xp} / $nextLevelXp",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "🔥 ${s.streakLabel}: ${prefs.streak}d • ${s.disciplineScore}: $disciplineScore%",
                            style = MaterialTheme.typography.labelSmall,
                            color = arena.secondary
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateToCoach,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("home_start_coach_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = arena.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(s.startLiveCoach, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (streakBroken) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GalatCrimson.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GalatCrimson)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = GalatCrimson)
                        Spacer(Modifier.width(10.dp))
                        Text(s.streakBrokenWarning, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }
            }
        }

        // Motivational Quote Card
        item {
            HunterGlowCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.nextQuote() }
                    .testTag("quote_card"),
                accentColor = arena.secondary
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = arena.secondary)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isHi) "CHAMPION MINDSET" else "CHAMPION QUOTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = arena.secondary
                        )
                    }
                    Icon(Icons.Default.Refresh, contentDescription = "Next quote", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "\"$currentQuote\"",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }

        // Today's Scheduled Plan
        item {
            HunterGlowCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.todaysPlan, style = MaterialTheme.typography.titleLarge, color = arena.primary)
                    TextButton(
                        onClick = onNavigateToSchedule,
                        modifier = Modifier.testTag("home_open_schedule_button")
                    ) {
                        Text(s.openSchedule, color = arena.secondary)
                    }
                }
                if (todaySchedule != null) {
                    Text(
                        text = todaySchedule.dayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    if (todaySchedule.isRestDay) {
                        Text(
                            text = if (isHi) "Aaj Rest Day hai — paani piyo, stretch karo aur recovery lo." else "Rest Day today — hydrate, stretch, and recover.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SahiEmerald
                        )
                    } else {
                        val list = viewModel.repo.decodeExercises(todaySchedule.exercisesJson)
                        list.take(3).forEach { ex ->
                            Text(
                                text = "• ${ex.name} (${ex.sets} × ${ex.reps})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Daily Quests
        item {
            HunterGlowCard(modifier = Modifier.fillMaxWidth(), accentColor = SahiEmerald) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.dailyQuests, style = MaterialTheme.typography.titleLarge, color = SahiEmerald)
                    OutlinedButton(
                        onClick = { viewModel.logQuickSkipping(5) },
                        modifier = Modifier.testTag("log_skipping_button")
                    ) {
                        Text("+5m Skip", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(10.dp))
                quests.forEach { q ->
                    val complete = q.current >= q.target
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHi) q.titleHi else q.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${q.current} / ${q.target} • +${q.xpReward} XP",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { viewModel.claimQuest(q) },
                                enabled = complete && !q.claimed,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (q.claimed) arena.cardElevated else SahiEmerald,
                                    contentColor = arena.bg
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(if (q.claimed) s.claimed else s.claimXp, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (q.current.toFloat() / q.target.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (complete) SahiEmerald else arena.primary,
                            trackColor = arena.bg
                        )
                    }
                }
            }
        }

        // Daily Check-in
        item {
            HunterGlowCard(modifier = Modifier.fillMaxWidth()) {
                Text(s.dailyCheckIn, style = MaterialTheme.typography.titleLarge, color = arena.primary)
                Spacer(Modifier.height(8.dp))
                if (todayCheckIn != null) {
                    val recText = when (todayCheckIn.intensityRecommendation) {
                        "LIGHT" -> s.recommendedLight
                        "HEAVY" -> s.recommendedHeavy
                        else -> s.recommendedModerate
                    }
                    Surface(
                        color = arena.primary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "✓ ${todayCheckIn.intensityRecommendation}: $recText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = arena.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text("${s.moodLabel}: $mood / 5", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Slider(value = mood.toFloat(), onValueChange = { mood = it.toInt() }, valueRange = 1f..5f, steps = 3)
                Text("${s.sleepLabel}: ${"%.1f".format(sleepHrs)}h", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Slider(value = sleepHrs, onValueChange = { sleepHrs = it }, valueRange = 3f..10f)
                Text("${s.sorenessLabel}: $soreness / 5", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Slider(value = soreness.toFloat(), onValueChange = { soreness = it.toInt() }, valueRange = 1f..5f, steps = 3)
                Button(
                    onClick = { viewModel.saveDailyCheckIn(mood, sleepHrs, soreness) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_checkin_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = arena.secondary, contentColor = arena.bg)
                ) {
                    Text(s.saveCheckIn, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Badges
        item {
            Text(s.badgesTitle, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(BoxingCatalog.badges) { badge ->
                    val unlocked = prefs.unlockedBadges.contains(badge.id)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (unlocked) RankGold.copy(alpha = 0.14f) else arena.card,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (unlocked) RankGold else arena.outline
                        ),
                        modifier = Modifier.width(156.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = if (unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (unlocked) RankGold else TextMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isHi) badge.titleHi else badge.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (unlocked) RankGold else TextSecondary
                            )
                            Text(
                                text = if (isHi) badge.descHi else badge.descEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        item {
            FooterCredit()
        }
    }
}
