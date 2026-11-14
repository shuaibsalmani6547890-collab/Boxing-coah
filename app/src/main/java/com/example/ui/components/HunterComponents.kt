package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.AppLanguage
import com.example.data.model.HunterRank
import com.example.data.model.PoseFrameSnapshot
import com.example.data.model.TechniqueAnimType
import com.example.ui.theme.*
import com.google.mlkit.vision.pose.PoseLandmark
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.math.sin

@Composable
fun HunterGlowCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val arena = LocalArenaColors.current
    val resolvedAccent = accentColor ?: arena.primary
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = arena.card,
        tonalElevation = 4.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(resolvedAccent.copy(alpha = 0.72f), arena.secondary.copy(alpha = 0.48f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun RankBadge(rank: HunterRank, modifier: Modifier = Modifier) {
    val arena = LocalArenaColors.current
    val badgeColor = when (rank) {
        HunterRank.S -> RankGold
        HunterRank.A -> arena.primary
        HunterRank.B -> arena.secondary
        HunterRank.C -> SahiEmerald
        HunterRank.D -> Color(0xFF4CC9F0)
        HunterRank.E -> TextSecondary
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(badgeColor.copy(alpha = 0.16f))
            .border(1.5.dp, badgeColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.letter,
            style = MaterialTheme.typography.headlineMedium,
            color = badgeColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LanguageSwitchPill(
    language: AppLanguage,
    onToggle: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(arena.cardElevated)
            .border(1.dp, arena.primary.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .minimumInteractiveComponentSize()
            .clickable {
                onToggle(
                    if (language == AppLanguage.HINGLISH) AppLanguage.ENGLISH else AppLanguage.HINGLISH
                )
            }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("language_switch_pill"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (language == AppLanguage.HINGLISH) "HI" else "EN",
            style = MaterialTheme.typography.labelLarge,
            color = arena.primary
        )
    }
}

@Composable
fun FooterCredit(modifier: Modifier = Modifier) {
    val arena = LocalArenaColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = 10.dp),
            color = arena.outline.copy(alpha = 0.6f)
        )
        Text(
            text = "Made by Shuaib Salmani",
            style = MaterialTheme.typography.labelLarge,
            color = arena.primary
        )
        Text(
            text = "100% OFFLINE • PRO BOXING ARENA COACH",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun TechniqueStickFigureCanvas(
    animType: TechniqueAnimType,
    isPlaying: Boolean,
    slowMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stick_fig")
    val duration = if (slowMotion) 2400 else 1100
    val rawPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )
    val phase = if (isPlaying) rawPhase else 0.5f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ObsidianBg)
            .border(1.dp, ObsidianOutline, RoundedCornerShape(14.dp))
    ) {
        val cx = size.width * 0.45f
        val cy = size.height * 0.52f
        val s = size.minDimension * 0.38f

        var headX = cx
        var headY = cy - s * 0.95f
        var hipX = cx
        var hipY = cy + s * 0.1f
        var lFoot = Offset(cx - s * 0.45f, cy + s * 0.95f)
        var rFoot = Offset(cx + s * 0.45f, cy + s * 0.95f)
        var lHand = Offset(cx + s * 0.25f, cy - s * 0.65f)
        var rHand = Offset(cx - s * 0.1f, cy - s * 0.62f)
        var trajectoryStart: Offset? = null
        var trajectoryEnd: Offset? = null

        when (animType) {
            TechniqueAnimType.STANCE_ORTHODOX -> {
                val bounce = sin(phase * Math.PI).toFloat() * 6f
                headY -= bounce
                hipY -= bounce
                lFoot = Offset(cx + s * 0.35f, cy + s * 0.95f)
                rFoot = Offset(cx - s * 0.45f, cy + s * 0.95f)
            }
            TechniqueAnimType.STANCE_SOUTHPAW -> {
                val bounce = sin(phase * Math.PI).toFloat() * 6f
                headY -= bounce
                hipY -= bounce
                lFoot = Offset(cx - s * 0.35f, cy + s * 0.95f)
                rFoot = Offset(cx + s * 0.45f, cy + s * 0.95f)
            }
            TechniqueAnimType.JAB -> {
                trajectoryStart = Offset(cx + s * 0.25f, cy - s * 0.65f)
                trajectoryEnd = Offset(cx + s * 1.15f, cy - s * 0.68f)
                lHand = Offset(
                    trajectoryStart.x + (trajectoryEnd.x - trajectoryStart.x) * phase,
                    trajectoryStart.y
                )
            }
            TechniqueAnimType.CROSS -> {
                hipX += phase * s * 0.15f
                trajectoryStart = Offset(cx - s * 0.1f, cy - s * 0.62f)
                trajectoryEnd = Offset(cx + s * 1.25f, cy - s * 0.65f)
                rHand = Offset(
                    trajectoryStart.x + (trajectoryEnd.x - trajectoryStart.x) * phase,
                    trajectoryStart.y
                )
            }
            TechniqueAnimType.HOOK -> {
                hipX += phase * s * 0.12f
                val arcX = cx + s * (0.25f + phase * 0.65f)
                val arcY = cy - s * (0.65f + sin(phase * Math.PI).toFloat() * 0.18f)
                lHand = Offset(arcX, arcY)
                trajectoryStart = Offset(cx + s * 0.25f, cy - s * 0.65f)
                trajectoryEnd = Offset(cx + s * 0.9f, cy - s * 0.72f)
            }
            TechniqueAnimType.UPPERCUT -> {
                val dip = (1f - phase) * s * 0.15f
                hipY += dip
                headY += dip * 0.7f
                trajectoryStart = Offset(cx + s * 0.1f, cy - s * 0.2f)
                trajectoryEnd = Offset(cx + s * 0.75f, cy - s * 0.9f)
                rHand = Offset(
                    trajectoryStart.x + (trajectoryEnd.x - trajectoryStart.x) * phase,
                    trajectoryStart.y + (trajectoryEnd.y - trajectoryStart.y) * phase
                )
            }
            TechniqueAnimType.SLIP -> {
                headX += (phase - 0.5f) * s * 0.45f
                headY += sin(phase * Math.PI).toFloat() * s * 0.15f
            }
            TechniqueAnimType.BOB_WEAVE -> {
                headX += (phase - 0.5f) * s * 0.65f
                headY += sin(phase * Math.PI).toFloat() * s * 0.42f
                hipY += sin(phase * Math.PI).toFloat() * s * 0.25f
            }
            TechniqueAnimType.PARRY -> {
                rHand = Offset(cx + s * (0.15f + phase * 0.35f), cy - s * 0.65f)
            }
            TechniqueAnimType.STEP_DRAG, TechniqueAnimType.PIVOT, TechniqueAnimType.CIRCLE_STEP -> {
                val shift = (phase - 0.5f) * s * 0.55f
                headX += shift
                hipX += shift
                lFoot = Offset(cx + s * 0.35f + shift, cy + s * 0.95f)
                rFoot = Offset(cx - s * 0.45f + shift, cy + s * 0.95f)
                lHand = Offset(cx + s * 0.25f + shift, cy - s * 0.65f)
                rHand = Offset(cx - s * 0.1f + shift, cy - s * 0.62f)
            }
        }

        val shoulder = Offset((headX + hipX) / 2f, headY + s * 0.32f)
        val hip = Offset(hipX, hipY)
        val head = Offset(headX, headY)

        // Floor line
        drawLine(
            color = ObsidianOutline,
            start = Offset(20f, cy + s * 0.98f),
            end = Offset(size.width - 20f, cy + s * 0.98f),
            strokeWidth = 2f
        )

        // Trajectory arrow
        if (trajectoryStart != null && trajectoryEnd != null) {
            drawLine(
                color = MonarchPurpleGlow.copy(alpha = 0.7f),
                start = trajectoryStart,
                end = trajectoryEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = SahiEmerald,
                radius = 7f,
                center = trajectoryEnd
            )
        }

        // Torso & legs
        drawLine(SystemCyan, shoulder, hip, strokeWidth = 6f, cap = StrokeCap.Round)
        drawLine(SystemCyan, hip, lFoot, strokeWidth = 5.5f, cap = StrokeCap.Round)
        drawLine(SystemCyan, hip, rFoot, strokeWidth = 5.5f, cap = StrokeCap.Round)

        // Arms
        drawLine(MonarchPurpleGlow, shoulder, lHand, strokeWidth = 5.5f, cap = StrokeCap.Round)
        drawLine(SystemCyan, shoulder, rHand, strokeWidth = 5.5f, cap = StrokeCap.Round)

        // Gloves
        drawCircle(SahiEmerald, radius = 9f, center = lHand)
        drawCircle(SystemCyan, radius = 9f, center = rHand)

        // Head
        drawCircle(
            color = SystemCyan,
            radius = s * 0.22f,
            center = head,
            style = Stroke(width = 5f)
        )
    }
}

@Composable
fun CustomLineChart(
    values: List<Float>,
    lineColor: Color = SystemCyan,
    secondaryValues: List<Float>? = null,
    secondaryColor: Color = MonarchPurpleGlow,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBg)
            .padding(12.dp)
    ) {
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = size.height * (i / gridLines.toFloat())
            drawLine(
                color = ObsidianOutline.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.5f
            )
        }

        fun drawSeries(series: List<Float>, color: Color) {
            if (series.isEmpty()) return
            val stepX = if (series.size == 1) 0f else size.width / (series.size - 1)
            val path = Path()
            series.forEachIndexed { idx, v ->
                val x = if (series.size == 1) size.width / 2f else idx * stepX
                val y = size.height - ((v / maxValue).coerceIn(0f, 1f) * size.height)
                if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                drawCircle(color = color, radius = 6f, center = Offset(x, y))
            }
            if (series.size > 1) {
                drawPath(path = path, color = color, style = Stroke(width = 4f, cap = StrokeCap.Round))
            }
        }

        drawSeries(values, lineColor)
        if (secondaryValues != null) {
            drawSeries(secondaryValues, secondaryColor)
        }
    }
}

@Composable
fun CustomBarChart(
    values: List<Int>,
    barColor: Color = MonarchPurpleGlow,
    modifier: Modifier = Modifier
) {
    val maxVal = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBg)
            .padding(12.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        val slotWidth = size.width / values.size
        val barWidth = (slotWidth * 0.55f).coerceAtMost(36f)
        values.forEachIndexed { idx, v ->
            val ratio = (v.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
            val barH = size.height * ratio
            val x = idx * slotWidth + (slotWidth - barWidth) / 2f
            val y = size.height - barH
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(SystemCyan, barColor)),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}

@Composable
fun SlowMotionReplayDialog(
    videoPath: String,
    poseTimelineJson: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var playbackSpeed by remember { mutableFloatStateOf(0.5f) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableLongStateOf(0L) }

    val timeline = remember(poseTimelineJson) {
        if (poseTimelineJson.isNullOrBlank()) emptyList()
        else runCatching { Json.decodeFromString<List<PoseFrameSnapshot>>(poseTimelineJson) }.getOrDefault(emptyList())
    }

    val exoPlayer = remember(videoPath) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoPath)))
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
            prepare()
            playWhenReady = true
            playbackParameters = PlaybackParameters(0.5f)
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    LaunchedEffect(playbackSpeed) {
        exoPlayer.playbackParameters = PlaybackParameters(playbackSpeed)
    }

    LaunchedEffect(isPlaying) {
        while (true) {
            currentPosMs = exoPlayer.currentPosition
            delay(40L)
        }
    }

    val activeSnapshot = remember(currentPosMs, timeline) {
        timeline.minByOrNull { kotlin.math.abs(it.timeMs - currentPosMs) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ObsidianCard,
            border = BorderStroke(1.dp, SystemCyan)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Slow-Mo Replay (${playbackSpeed}x)",
                        style = MaterialTheme.typography.titleLarge,
                        color = SystemCyan
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ObsidianBg)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (activeSnapshot != null) {
                        PoseSkeletonCanvas(
                            joints = activeSnapshot.joints,
                            isGuardGood = activeSnapshot.isGoodForm,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(onClick = {
                        if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        isPlaying = !isPlaying
                    }) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Pause"
                        )
                    }

                    listOf(0.25f, 0.5f, 1.0f).forEach { spd ->
                        FilterChip(
                            selected = playbackSpeed == spd,
                            onClick = { playbackSpeed = spd },
                            label = { Text("${spd}x") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PoseSkeletonCanvas(
    joints: Map<Int, com.example.data.model.PosePointNorm>,
    isGuardGood: Boolean,
    activePunchSide: Int = 0,
    modifier: Modifier = Modifier
) {
    val connections = listOf(
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW,
        PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE,
        PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE,
        PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE
    )

    Canvas(modifier = modifier) {
        if (joints.isEmpty()) return@Canvas
        val w = size.width
        val h = size.height

        for ((a, b) in connections) {
            val p1 = joints[a] ?: continue
            val p2 = joints[b] ?: continue
            val isArm = (a == PoseLandmark.LEFT_ELBOW || b == PoseLandmark.LEFT_WRIST) && activePunchSide == -1 ||
                (a == PoseLandmark.RIGHT_ELBOW || b == PoseLandmark.RIGHT_WRIST) && activePunchSide == 1
            val lineColor = when {
                isArm -> SahiEmerald
                !isGuardGood && (a == PoseLandmark.LEFT_WRIST || b == PoseLandmark.RIGHT_WRIST || a == PoseLandmark.LEFT_ELBOW || a == PoseLandmark.RIGHT_ELBOW) -> GalatCrimson
                else -> SystemCyan
            }
            drawLine(
                color = lineColor,
                start = Offset(p1.x * w, p1.y * h),
                end = Offset(p2.x * w, p2.y * h),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        joints.forEach { (type, pt) ->
            val c = when (type) {
                PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST -> if (isGuardGood) SahiEmerald else GalatCrimson
                PoseLandmark.NOSE -> MonarchPurpleGlow
                else -> SystemCyan
            }
            drawCircle(
                color = c,
                radius = 7f,
                center = Offset(pt.x * w, pt.y * h)
            )
        }
    }
}
