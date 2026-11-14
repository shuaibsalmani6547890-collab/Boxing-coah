package com.example.ui.components

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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.ExerciseMotionType
import com.example.data.model.TechniqueAnimType
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

sealed interface MotionClip {
    data class Technique(val type: TechniqueAnimType) : MotionClip
    data class Exercise(val type: ExerciseMotionType) : MotionClip
}

private data class FighterPose(
    val head: Offset,
    val neck: Offset,
    val lShoulder: Offset,
    val rShoulder: Offset,
    val lElbow: Offset,
    val rElbow: Offset,
    val lHand: Offset,
    val rHand: Offset,
    val hip: Offset,
    val lKnee: Offset,
    val rKnee: Offset,
    val lFoot: Offset,
    val rFoot: Offset,
    val phaseLabel: String = "READY",
    val impactPoint: Offset? = null,
    val trailStart: Offset? = null,
    val trailEnd: Offset? = null,
    val isBoxingGloves: Boolean = true
)

@Composable
fun ProMotionVideoPlayer(
    clip: MotionClip,
    title: String,
    height: Dp = 220.dp,
    compactPreview: Boolean = false,
    modifier: Modifier = Modifier
) {
    val arena = LocalArenaColors.current
    var isPlaying by remember { mutableStateOf(true) }
    var speed by remember { mutableFloatStateOf(1.0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pro_motion")
    val durationMs = when {
        speed <= 0.6f -> 2600
        speed >= 1.4f -> 900
        else -> 1450
    }
    val rawT by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )
    val t = if (isPlaying) rawT else 0.45f
    val pingPong = if (t < 0.5f) t * 2f else (1f - t) * 2f
    val smooth = (1f - cos(pingPong * PI.toFloat())) / 2f

    var currentPhase by remember { mutableStateOf("STANCE") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(arena.bg)
            .border(1.dp, arena.primary.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArenaBackdrop(arena.primary, arena.secondary)

                val cx = size.width * 0.44f
                val floorY = size.height * 0.84f
                val unit = size.minDimension * 0.36f

                val pose = computeClipPose(clip, cx, floorY, unit, t, smooth)
                currentPhase = pose.phaseLabel

                // Extra props behind/in front
                drawClipProps(clip, cx, floorY, unit, t, smooth, arena.primary, arena.secondary)

                // Motion trail & shockwave
                if (pose.trailStart != null && pose.trailEnd != null) {
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(arena.secondary.copy(alpha = 0.1f), arena.primary.copy(alpha = 0.85f))
                        ),
                        start = pose.trailStart,
                        end = pose.trailEnd,
                        strokeWidth = unit * 0.14f,
                        cap = StrokeCap.Round
                    )
                }
                pose.impactPoint?.let { imp ->
                    val pulse = (t * 2f) % 1f
                    drawCircle(
                        color = arena.secondary.copy(alpha = (1f - pulse) * 0.8f),
                        radius = unit * (0.14f + pulse * 0.32f),
                        center = imp,
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = arena.primary.copy(alpha = 0.8f),
                        radius = unit * 0.08f,
                        center = imp
                    )
                }

                // Ground shadow
                drawOval(
                    color = Color.Black.copy(alpha = 0.55f),
                    topLeft = Offset(pose.hip.x - unit * 0.55f, floorY - 6f),
                    size = Size(unit * 1.1f, 14f)
                )

                // Draw Anatomical Fighter
                drawAnatomicalAthlete(pose, unit, arena.primary, arena.secondary)
            }

            // Top Video Overlay HUD
            if (!compactPreview) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, arena.primary.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) arena.primary else TextMuted)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = currentPhase,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${speed}x DEMO",
                            style = MaterialTheme.typography.labelSmall,
                            color = arena.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom timeline bar inside video
                LinearProgressIndicator(
                    progress = { t },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp),
                    color = arena.primary,
                    trackColor = Color.White.copy(alpha = 0.14f)
                )
            }
        }

        if (!compactPreview) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(arena.surface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isPlaying = !isPlaying }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = arena.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.5f, 1.0f, 1.5f).forEach { spd ->
                        val active = speed == spd
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (active) arena.primary.copy(alpha = 0.22f) else arena.cardElevated,
                            border = BorderStroke(1.dp, if (active) arena.primary else arena.outline),
                            modifier = Modifier.clickable { speed = spd }
                        ) {
                            Text(
                                text = "${spd}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (active) arena.primary else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawArenaBackdrop(primary: Color, secondary: Color) {
    // Spotlight cone
    drawPath(
        path = Path().apply {
            moveTo(size.width * 0.25f, 0f)
            lineTo(size.width * 0.75f, 0f)
            lineTo(size.width * 0.92f, size.height * 0.85f)
            lineTo(size.width * 0.08f, size.height * 0.85f)
            close()
        },
        brush = Brush.verticalGradient(
            listOf(primary.copy(alpha = 0.14f), Color.Transparent)
        )
    )
    // Ring ropes
    listOf(0.32f, 0.48f, 0.64f).forEachIndexed { i, yf ->
        val y = size.height * yf
        drawLine(
            color = if (i == 1) secondary.copy(alpha = 0.28f) else primary.copy(alpha = 0.25f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.5f
        )
    }
    // Canvas mat floor
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFF141824), Color(0xFF0A0C12))
        ),
        topLeft = Offset(0f, size.height * 0.82f),
        size = Size(size.width, size.height * 0.18f)
    )
    drawLine(
        color = primary.copy(alpha = 0.45f),
        start = Offset(0f, size.height * 0.82f),
        end = Offset(size.width, size.height * 0.82f),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawAnatomicalAthlete(
    p: FighterPose,
    unit: Float,
    primary: Color,
    secondary: Color
) {
    val skin = Color(0xFFE0B294)
    val jersey = Color(0xFF262D42)
    val shortsColor = primary.copy(alpha = 0.88f)
    val shoeColor = Color(0xFFE8ECF5)

    // Legs (back leg then front leg)
    drawCapsuleLimb(p.hip, p.rKnee, unit * 0.16f, shortsColor)
    drawCapsuleLimb(p.rKnee, p.rFoot, unit * 0.12f, skin)
    drawShoe(p.rFoot, unit, shoeColor, secondary)

    drawCapsuleLimb(p.hip, p.lKnee, unit * 0.17f, shortsColor)
    drawCapsuleLimb(p.lKnee, p.lFoot, unit * 0.12f, skin)
    drawShoe(p.lFoot, unit, shoeColor, secondary)

    // Torso (V-taper athletic trunk)
    val torsoPath = Path().apply {
        moveTo(p.lShoulder.x, p.lShoulder.y)
        lineTo(p.rShoulder.x, p.rShoulder.y)
        lineTo(p.hip.x + unit * 0.16f, p.hip.y)
        lineTo(p.hip.x - unit * 0.16f, p.hip.y)
        close()
    }
    drawPath(torsoPath, color = jersey)
    // Waist belt
    drawLine(
        color = secondary,
        start = Offset(p.hip.x - unit * 0.17f, p.hip.y - unit * 0.04f),
        end = Offset(p.hip.x + unit * 0.17f, p.hip.y - unit * 0.04f),
        strokeWidth = unit * 0.09f,
        cap = StrokeCap.Round
    )

    // Rear arm
    drawCapsuleLimb(p.rShoulder, p.rElbow, unit * 0.11f, skin)
    drawCapsuleLimb(p.rElbow, p.rHand, unit * 0.10f, skin)

    // Neck & Head
    drawCapsuleLimb(p.neck, p.head, unit * 0.11f, skin)
    drawCircle(color = skin, radius = unit * 0.21f, center = p.head)
    // Hair/Headband
    drawArc(
        color = Color(0xFF1A1E2B),
        startAngle = 190f,
        sweepAngle = 165f,
        useCenter = true,
        topLeft = Offset(p.head.x - unit * 0.21f, p.head.y - unit * 0.22f),
        size = Size(unit * 0.42f, unit * 0.30f)
    )
    drawLine(
        color = primary,
        start = Offset(p.head.x - unit * 0.19f, p.head.y - unit * 0.06f),
        end = Offset(p.head.x + unit * 0.19f, p.head.y - unit * 0.06f),
        strokeWidth = unit * 0.05f
    )

    // Lead arm
    drawCapsuleLimb(p.lShoulder, p.lElbow, unit * 0.115f, skin)
    drawCapsuleLimb(p.lElbow, p.lHand, unit * 0.105f, skin)

    // Gloves or wrapped hands
    if (p.isBoxingGloves) {
        drawGlove(p.rHand, unit, primary, secondary)
        drawGlove(p.lHand, unit, primary, secondary)
    } else {
        drawCircle(color = primary, radius = unit * 0.10f, center = p.rHand)
        drawCircle(color = primary, radius = unit * 0.10f, center = p.lHand)
    }
}

private fun DrawScope.drawCapsuleLimb(a: Offset, b: Offset, width: Float, color: Color) {
    drawLine(color = color, start = a, end = b, strokeWidth = width, cap = StrokeCap.Round)
}

private fun DrawScope.drawGlove(center: Offset, unit: Float, primary: Color, secondary: Color) {
    drawCircle(color = Color.White, radius = unit * 0.135f, center = Offset(center.x - unit * 0.03f, center.y + unit * 0.04f))
    drawCircle(color = primary, radius = unit * 0.155f, center = center)
    drawCircle(color = secondary.copy(alpha = 0.65f), radius = unit * 0.065f, center = Offset(center.x + unit * 0.05f, center.y - unit * 0.04f))
}

private fun DrawScope.drawShoe(foot: Offset, unit: Float, shoeColor: Color, accent: Color) {
    drawRoundRect(
        color = shoeColor,
        topLeft = Offset(foot.x - unit * 0.08f, foot.y - unit * 0.11f),
        size = Size(unit * 0.28f, unit * 0.12f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawLine(
        color = accent,
        start = Offset(foot.x - unit * 0.06f, foot.y),
        end = Offset(foot.x + unit * 0.18f, foot.y),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawClipProps(
    clip: MotionClip,
    cx: Float,
    floorY: Float,
    unit: Float,
    t: Float,
    smooth: Float,
    primary: Color,
    secondary: Color
) {
    when (clip) {
        is MotionClip.Exercise -> when (clip.type) {
            ExerciseMotionType.HEAVY_BAG -> {
                val bagX = cx + unit * 1.15f + smooth * unit * 0.12f
                drawLine(Color(0xFF8892A8), Offset(bagX, 0f), Offset(bagX, floorY - unit * 1.45f), strokeWidth = 4f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF5A1622), primary.copy(alpha = 0.85f), Color(0xFF330C14))),
                    topLeft = Offset(bagX - unit * 0.28f, floorY - unit * 1.45f),
                    size = Size(unit * 0.56f, unit * 1.25f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
            }
            ExerciseMotionType.SPEED_BAG -> {
                val drumX = cx + unit * 0.9f
                val drumY = floorY - unit * 1.65f
                drawRect(Color(0xFF5A627A), Offset(drumX - unit * 0.35f, drumY), Size(unit * 0.7f, 10f))
                val sway = sin(t * 4f * PI.toFloat()) * unit * 0.12f
                drawOval(
                    color = secondary,
                    topLeft = Offset(drumX + sway - unit * 0.12f, drumY + 10f),
                    size = Size(unit * 0.24f, unit * 0.32f)
                )
            }
            ExerciseMotionType.DOUBLE_END_BAG -> {
                val bx = cx + unit * 0.95f + sin(t * 2f * PI.toFloat()) * unit * 0.08f
                val by = floorY - unit * 1.32f
                drawLine(Color.White.copy(alpha = 0.4f), Offset(bx, 0f), Offset(bx, floorY), strokeWidth = 2.5f)
                drawCircle(primary, radius = unit * 0.18f, center = Offset(bx, by))
            }
            ExerciseMotionType.SKIPPING -> {
                val arcY = floorY - unit * 0.8f + cos(t * 2f * PI.toFloat()) * unit * 0.95f
                val ropePath = Path().apply {
                    moveTo(cx - unit * 0.35f, floorY - unit * 0.85f)
                    quadraticTo(cx, arcY, cx + unit * 0.35f, floorY - unit * 0.85f)
                }
                drawPath(ropePath, color = secondary, style = Stroke(width = 3.5f))
            }
            ExerciseMotionType.PULL_UPS -> {
                val barY = floorY - unit * 2.0f
                drawLine(Color(0xFF9AA4BA), Offset(cx - unit * 0.9f, barY), Offset(cx + unit * 0.9f, barY), strokeWidth = 8f, cap = StrokeCap.Round)
            }
            ExerciseMotionType.MED_BALL_SLAM -> {
                val ballY = (floorY - unit * 1.9f) + smooth * (unit * 1.78f)
                drawCircle(
                    brush = Brush.radialGradient(listOf(secondary, primary)),
                    radius = unit * 0.18f,
                    center = Offset(cx + unit * 0.35f, ballY)
                )
            }
            ExerciseMotionType.WRIST_CURLS -> {
                drawRoundRect(
                    color = Color(0xFF333B52),
                    topLeft = Offset(cx - unit * 0.55f, floorY - unit * 0.42f),
                    size = Size(unit * 1.15f, unit * 0.18f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
            else -> {}
        }
        is MotionClip.Technique -> {
            if (clip.type == TechniqueAnimType.JAB || clip.type == TechniqueAnimType.CROSS ||
                clip.type == TechniqueAnimType.HOOK || clip.type == TechniqueAnimType.UPPERCUT
            ) {
                val targetCenter = Offset(cx + unit * 1.18f, floorY - unit * 1.42f)
                drawCircle(
                    color = secondary.copy(alpha = 0.25f),
                    radius = unit * 0.20f,
                    center = targetCenter,
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

private fun computeClipPose(
    clip: MotionClip,
    cx: Float,
    floorY: Float,
    unit: Float,
    t: Float,
    smooth: Float
): FighterPose {
    // Base orthodox stance proportions
    val bounce = sin(t * 4f * PI.toFloat()) * unit * 0.03f
    var hip = Offset(cx, floorY - unit * 0.88f - bounce)
    var neck = Offset(cx + unit * 0.04f, hip.y - unit * 0.56f)
    var head = Offset(cx + unit * 0.06f, neck.y - unit * 0.24f)
    var lShoulder = Offset(cx + unit * 0.18f, neck.y + unit * 0.06f)
    var rShoulder = Offset(cx - unit * 0.14f, neck.y + unit * 0.06f)
    var lElbow = Offset(cx + unit * 0.28f, hip.y - unit * 0.24f)
    var rElbow = Offset(cx - unit * 0.06f, hip.y - unit * 0.26f)
    var lHand = Offset(cx + unit * 0.36f, head.y + unit * 0.05f)
    var rHand = Offset(cx + unit * 0.10f, head.y + unit * 0.08f)
    var lKnee = Offset(cx + unit * 0.26f, floorY - unit * 0.44f)
    var rKnee = Offset(cx - unit * 0.24f, floorY - unit * 0.44f)
    var lFoot = Offset(cx + unit * 0.36f, floorY)
    var rFoot = Offset(cx - unit * 0.38f, floorY)

    var phase = "GUARD READY"
    var impact: Offset? = null
    var trailStart: Offset? = null
    var trailEnd: Offset? = null
    var gloves = true

    when (clip) {
        is MotionClip.Technique -> when (clip.type) {
            TechniqueAnimType.STANCE_ORTHODOX -> {
                phase = "ORTHODOX BOUNCE"
            }
            TechniqueAnimType.STANCE_SOUTHPAW -> {
                phase = "SOUTHPAW BOUNCE"
                lFoot = Offset(cx - unit * 0.36f, floorY)
                rFoot = Offset(cx + unit * 0.38f, floorY)
            }
            TechniqueAnimType.JAB -> {
                phase = if (smooth > 0.6f) "FULL EXTENSION + CHIN COVER" else "SNAP FROM CHEEK"
                val start = lHand
                val end = Offset(cx + unit * 1.15f, head.y + unit * 0.03f)
                lHand = lerpOffset(start, end, smooth)
                lElbow = lerpOffset(lElbow, Offset(cx + unit * 0.72f, head.y + unit * 0.04f), smooth)
                if (smooth > 0.75f) impact = end
                trailStart = start
                trailEnd = lHand
            }
            TechniqueAnimType.CROSS -> {
                phase = if (smooth > 0.6f) "HIP PIVOT + POWER CROSS" else "LOAD REAR HIP"
                hip = Offset(cx + smooth * unit * 0.12f, hip.y)
                rShoulder = Offset(rShoulder.x + smooth * unit * 0.26f, rShoulder.y)
                val start = rHand
                val end = Offset(cx + unit * 1.22f, head.y + unit * 0.04f)
                rHand = lerpOffset(start, end, smooth)
                rElbow = lerpOffset(rElbow, Offset(cx + unit * 0.68f, head.y + unit * 0.05f), smooth)
                if (smooth > 0.75f) impact = end
                trailStart = start
                trailEnd = rHand
            }
            TechniqueAnimType.HOOK -> {
                phase = if (smooth > 0.55f) "90° HOOK WHIP + PIVOT" else "ROTATE LEAD HIP"
                val arcX = cx + unit * (0.36f + smooth * 0.68f)
                val arcY = head.y + unit * (0.06f - sin(smooth * PI.toFloat()) * 0.14f)
                lElbow = Offset(cx + unit * (0.34f + smooth * 0.35f), head.y + unit * 0.10f)
                lHand = Offset(arcX, arcY)
                if (smooth > 0.75f) impact = lHand
                trailStart = Offset(cx + unit * 0.36f, head.y + unit * 0.06f)
                trailEnd = lHand
            }
            TechniqueAnimType.UPPERCUT -> {
                phase = if (smooth > 0.5f) "LEG DRIVE UPWARD" else "DIP KNEES & LOAD"
                val dip = (1f - smooth) * unit * 0.14f
                hip = hip.copy(y = hip.y + dip)
                neck = neck.copy(y = neck.y + dip)
                head = head.copy(y = head.y + dip)
                val start = Offset(cx + unit * 0.15f, hip.y - unit * 0.05f)
                val end = Offset(cx + unit * 0.92f, head.y - unit * 0.06f)
                rHand = lerpOffset(start, end, smooth)
                rElbow = Offset(cx + unit * (0.18f + smooth * 0.42f), hip.y - unit * 0.10f)
                if (smooth > 0.75f) impact = end
                trailStart = start
                trailEnd = rHand
            }
            TechniqueAnimType.SLIP -> {
                phase = "SLIP OFFLINE (KNEES BENT)"
                val shiftX = (smooth - 0.5f) * unit * 0.34f
                val dipY = sin(smooth * PI.toFloat()) * unit * 0.12f
                head = head.copy(x = head.x + shiftX, y = head.y + dipY)
                neck = neck.copy(x = neck.x + shiftX * 0.7f, y = neck.y + dipY)
                lHand = lHand.copy(x = lHand.x + shiftX, y = lHand.y + dipY)
                rHand = rHand.copy(x = rHand.x + shiftX, y = rHand.y + dipY)
            }
            TechniqueAnimType.BOB_WEAVE -> {
                phase = "U-SHAPE BOB & WEAVE"
                val shiftX = (smooth - 0.5f) * unit * 0.52f
                val dipY = sin(smooth * PI.toFloat()) * unit * 0.36f
                hip = hip.copy(x = hip.x + shiftX * 0.5f, y = hip.y + dipY * 0.7f)
                neck = neck.copy(x = neck.x + shiftX * 0.8f, y = neck.y + dipY)
                head = head.copy(x = head.x + shiftX, y = head.y + dipY)
                lHand = lHand.copy(x = lHand.x + shiftX, y = lHand.y + dipY)
                rHand = rHand.copy(x = rHand.x + shiftX, y = rHand.y + dipY)
            }
            TechniqueAnimType.PARRY -> {
                phase = "COMPACT PALM PARRY"
                val end = Offset(rHand.x + unit * 0.28f, rHand.y - unit * 0.06f)
                rHand = lerpOffset(rHand, end, smooth)
            }
            TechniqueAnimType.STEP_DRAG, TechniqueAnimType.PIVOT, TechniqueAnimType.CIRCLE_STEP -> {
                phase = "LIGHT FOOTWORK STEP-DRAG"
                val dx = (smooth - 0.5f) * unit * 0.45f
                hip = hip.copy(x = hip.x + dx)
                neck = neck.copy(x = neck.x + dx)
                head = head.copy(x = head.x + dx)
                lShoulder = lShoulder.copy(x = lShoulder.x + dx)
                rShoulder = rShoulder.copy(x = rShoulder.x + dx)
                lHand = lHand.copy(x = lHand.x + dx)
                rHand = rHand.copy(x = rHand.x + dx)
                lKnee = lKnee.copy(x = lKnee.x + dx)
                rKnee = rKnee.copy(x = rKnee.x + dx)
                lFoot = lFoot.copy(x = lFoot.x + dx)
                rFoot = rFoot.copy(x = rFoot.x + dx)
            }
        }
        is MotionClip.Exercise -> when (clip.type) {
            ExerciseMotionType.SHADOW_BOXING, ExerciseMotionType.DOUBLE_END_BAG -> {
                phase = if (t < 0.5f) "1-JAB + SLIP" else "2-CROSS + RESET"
                if (t < 0.5f) {
                    val p = sin(t * 2f * PI.toFloat())
                    lHand = lerpOffset(lHand, Offset(cx + unit * 1.1f, head.y), p)
                    lElbow = lerpOffset(lElbow, Offset(cx + unit * 0.7f, head.y), p)
                    trailEnd = lHand
                } else {
                    val p = sin((t - 0.5f) * 2f * PI.toFloat())
                    rHand = lerpOffset(rHand, Offset(cx + unit * 1.18f, head.y), p)
                    rElbow = lerpOffset(rElbow, Offset(cx + unit * 0.7f, head.y), p)
                    trailEnd = rHand
                }
            }
            ExerciseMotionType.HEAVY_BAG -> {
                phase = if (smooth > 0.6f) "BAG IMPACT + SNAP" else "POWER COMBINATION"
                val start = rHand
                val end = Offset(cx + unit * 0.92f, head.y + unit * 0.08f)
                rHand = lerpOffset(start, end, smooth)
                rElbow = lerpOffset(rElbow, Offset(cx + unit * 0.55f, head.y + unit * 0.08f), smooth)
                if (smooth > 0.75f) impact = end
                trailStart = start
                trailEnd = rHand
            }
            ExerciseMotionType.SPEED_BAG -> {
                phase = "HIGH ELBOW RHYTHM"
                val angle = t * 4f * PI.toFloat()
                lHand = Offset(cx + unit * 0.65f + cos(angle) * unit * 0.10f, head.y - unit * 0.08f + sin(angle) * unit * 0.10f)
                rHand = Offset(cx + unit * 0.65f - cos(angle) * unit * 0.10f, head.y - unit * 0.08f - sin(angle) * unit * 0.10f)
                lElbow = Offset(cx + unit * 0.42f, head.y + unit * 0.08f)
                rElbow = Offset(cx + unit * 0.25f, head.y + unit * 0.10f)
            }
            ExerciseMotionType.SKIPPING -> {
                gloves = false
                phase = "BOXER SHUFFLE JUMP"
                val hop = kotlin.math.abs(sin(t * 2f * PI.toFloat())) * unit * 0.18f
                hip = hip.copy(y = hip.y - hop)
                neck = neck.copy(y = neck.y - hop)
                head = head.copy(y = head.y - hop)
                lShoulder = lShoulder.copy(y = lShoulder.y - hop)
                rShoulder = rShoulder.copy(y = rShoulder.y - hop)
                lElbow = Offset(cx + unit * 0.25f, hip.y - unit * 0.15f)
                rElbow = Offset(cx - unit * 0.25f, hip.y - unit * 0.15f)
                lHand = Offset(cx + unit * 0.36f, hip.y - unit * 0.02f)
                rHand = Offset(cx - unit * 0.36f, hip.y - unit * 0.02f)
                lFoot = Offset(cx + unit * 0.18f, floorY - hop)
                rFoot = Offset(cx - unit * 0.18f, floorY - hop)
                lKnee = Offset(cx + unit * 0.14f, floorY - unit * 0.44f - hop)
                rKnee = Offset(cx - unit * 0.14f, floorY - unit * 0.44f - hop)
            }
            ExerciseMotionType.PUSH_UPS, ExerciseMotionType.PLANK_TAPS, ExerciseMotionType.MOUNTAIN_CLIMBERS -> {
                gloves = false
                val plankY = floorY - unit * 0.26f
                val pushOffset = if (clip.type == ExerciseMotionType.PUSH_UPS) smooth * unit * 0.16f else 0f
                phase = when (clip.type) {
                    ExerciseMotionType.PUSH_UPS -> if (smooth > 0.5f) "CHEST LOWER" else "EXPLOSIVE DRIVE"
                    ExerciseMotionType.PLANK_TAPS -> "TAP OPPOSITE SHOULDER"
                    else -> "FAST KNEE DRIVES"
                }
                lFoot = Offset(cx - unit * 0.85f, floorY)
                rFoot = Offset(cx - unit * 0.78f, floorY)
                lKnee = Offset(cx - unit * 0.42f, floorY - unit * 0.14f)
                rKnee = if (clip.type == ExerciseMotionType.MOUNTAIN_CLIMBERS) {
                    Offset(cx - unit * (0.42f - smooth * 0.42f), floorY - unit * (0.14f + smooth * 0.15f))
                } else Offset(cx - unit * 0.38f, floorY - unit * 0.14f)
                hip = Offset(cx - unit * 0.05f, plankY - unit * 0.10f + pushOffset * 0.5f)
                neck = Offset(cx + unit * 0.52f, plankY - unit * 0.22f + pushOffset)
                head = Offset(cx + unit * 0.72f, plankY - unit * 0.28f + pushOffset)
                lShoulder = Offset(cx + unit * 0.50f, plankY - unit * 0.16f + pushOffset)
                rShoulder = Offset(cx + unit * 0.44f, plankY - unit * 0.18f + pushOffset)
                lHand = Offset(cx + unit * 0.54f, floorY)
                rHand = if (clip.type == ExerciseMotionType.PLANK_TAPS && smooth > 0.45f) {
                    lShoulder
                } else Offset(cx + unit * 0.46f, floorY)
                lElbow = Offset(cx + unit * 0.52f, (lShoulder.y + floorY) / 2f)
                rElbow = Offset(cx + unit * 0.45f, (rShoulder.y + floorY) / 2f)
            }
            ExerciseMotionType.BURPEES -> {
                gloves = false
                if (t < 0.55f) {
                    phase = "PLANK + PUSH-UP"
                    val plankY = floorY - unit * 0.24f
                    lFoot = Offset(cx - unit * 0.82f, floorY)
                    rFoot = Offset(cx - unit * 0.76f, floorY)
                    lKnee = Offset(cx - unit * 0.40f, floorY - unit * 0.12f)
                    rKnee = Offset(cx - unit * 0.36f, floorY - unit * 0.12f)
                    hip = Offset(cx, plankY - unit * 0.08f)
                    neck = Offset(cx + unit * 0.50f, plankY - unit * 0.20f)
                    head = Offset(cx + unit * 0.70f, plankY - unit * 0.25f)
                    lShoulder = neck
                    rShoulder = neck
                    lHand = Offset(cx + unit * 0.52f, floorY)
                    rHand = Offset(cx + unit * 0.46f, floorY)
                    lElbow = Offset(cx + unit * 0.51f, floorY - unit * 0.14f)
                    rElbow = Offset(cx + unit * 0.47f, floorY - unit * 0.14f)
                } else {
                    phase = "EXPLOSIVE JUMP UP!"
                    val j = sin(((t - 0.55f) / 0.45f) * PI.toFloat()) * unit * 0.35f
                    hip = hip.copy(y = hip.y - j)
                    neck = neck.copy(y = neck.y - j)
                    head = head.copy(y = head.y - j)
                    lShoulder = lShoulder.copy(y = lShoulder.y - j)
                    rShoulder = rShoulder.copy(y = rShoulder.y - j)
                    lHand = Offset(cx + unit * 0.25f, head.y - unit * 0.22f)
                    rHand = Offset(cx - unit * 0.20f, head.y - unit * 0.22f)
                    lFoot = lFoot.copy(y = floorY - j)
                    rFoot = rFoot.copy(y = floorY - j)
                }
            }
            ExerciseMotionType.PULL_UPS -> {
                gloves = false
                phase = if (smooth > 0.5f) "CHIN OVER BAR" else "CONTROLLED LOWER"
                val pullY = (1f - smooth) * unit * 0.38f
                val barY = floorY - unit * 2.0f
                lHand = Offset(cx + unit * 0.28f, barY)
                rHand = Offset(cx - unit * 0.28f, barY)
                head = Offset(cx, barY + unit * 0.08f + pullY)
                neck = Offset(cx, head.y + unit * 0.22f)
                lShoulder = Offset(cx + unit * 0.18f, neck.y + unit * 0.05f)
                rShoulder = Offset(cx - unit * 0.18f, neck.y + unit * 0.05f)
                lElbow = Offset(cx + unit * 0.36f, (lShoulder.y + barY) / 2f + unit * 0.08f)
                rElbow = Offset(cx - unit * 0.36f, (rShoulder.y + barY) / 2f + unit * 0.08f)
                hip = Offset(cx, neck.y + unit * 0.55f)
                lKnee = Offset(cx + unit * 0.12f, hip.y + unit * 0.42f)
                rKnee = Offset(cx - unit * 0.12f, hip.y + unit * 0.42f)
                lFoot = Offset(cx + unit * 0.10f, lKnee.y + unit * 0.36f)
                rFoot = Offset(cx - unit * 0.10f, rKnee.y + unit * 0.36f)
            }
            ExerciseMotionType.SQUATS -> {
                gloves = false
                phase = if (smooth > 0.55f) "DEEP SQUAT + CHEST UP" else "EXPLOSIVE DRIVE"
                val drop = smooth * unit * 0.42f
                hip = Offset(cx - smooth * unit * 0.12f, hip.y + drop)
                neck = Offset(cx + unit * 0.04f, hip.y - unit * 0.54f)
                head = Offset(cx + unit * 0.06f, neck.y - unit * 0.22f)
                lShoulder = Offset(neck.x + unit * 0.14f, neck.y + unit * 0.05f)
                rShoulder = Offset(neck.x - unit * 0.14f, neck.y + unit * 0.05f)
                lHand = Offset(neck.x + unit * 0.22f, neck.y + unit * 0.12f)
                rHand = Offset(neck.x + unit * 0.18f, neck.y + unit * 0.14f)
                lKnee = Offset(cx + unit * 0.28f, floorY - unit * 0.44f + drop * 0.35f)
                rKnee = Offset(cx - unit * 0.22f, floorY - unit * 0.44f + drop * 0.35f)
            }
            ExerciseMotionType.LUNGES -> {
                gloves = false
                phase = "SPLIT STANCE LUNGE DROP"
                val drop = smooth * unit * 0.35f
                hip = hip.copy(y = hip.y + drop)
                neck = neck.copy(y = neck.y + drop)
                head = head.copy(y = head.y + drop)
                lShoulder = lShoulder.copy(y = lShoulder.y + drop)
                rShoulder = rShoulder.copy(y = rShoulder.y + drop)
                lHand = Offset(hip.x + unit * 0.18f, hip.y + unit * 0.10f)
                rHand = Offset(hip.x - unit * 0.18f, hip.y + unit * 0.10f)
                lFoot = Offset(cx + unit * 0.46f, floorY)
                rFoot = Offset(cx - unit * 0.48f, floorY)
                lKnee = Offset(cx + unit * 0.44f, floorY - unit * 0.42f + drop * 0.3f)
                rKnee = Offset(cx - unit * 0.30f, floorY - unit * 0.35f + drop * 0.65f)
            }
            ExerciseMotionType.DEADLIFT -> {
                gloves = false
                phase = if (smooth > 0.5f) "HIP HINGE BACK" else "SNAP HIPS FORWARD"
                val hinge = smooth * unit * 0.42f
                hip = Offset(cx - hinge * 0.45f, hip.y + hinge * 0.15f)
                neck = Offset(cx + hinge * 0.85f, hip.y - unit * (0.54f - smooth * 0.32f))
                head = Offset(neck.x + unit * 0.14f, neck.y - unit * 0.16f)
                lShoulder = neck
                rShoulder = neck
                lHand = Offset(neck.x + unit * 0.05f, hip.y + smooth * unit * 0.38f)
                rHand = Offset(neck.x - unit * 0.05f, hip.y + smooth * unit * 0.38f)
                lElbow = lerpOffset(lShoulder, lHand, 0.5f)
                rElbow = lerpOffset(rShoulder, rHand, 0.5f)
            }
            ExerciseMotionType.MED_BALL_SLAM -> {
                gloves = false
                phase = if (smooth > 0.6f) "CORE SLAM IMPACT!" else "EXTEND OVERHEAD"
                lHand = Offset(cx + unit * 0.32f, (head.y - unit * 0.2f) + smooth * unit * 0.7f)
                rHand = lHand
                if (smooth > 0.8f) impact = Offset(cx + unit * 0.35f, floorY - unit * 0.1f)
            }
            ExerciseMotionType.RUSSIAN_TWISTS -> {
                gloves = false
                phase = "TORSO LEFT-RIGHT ROTATION"
                hip = Offset(cx, floorY - unit * 0.22f)
                neck = Offset(cx - unit * 0.28f, floorY - unit * 0.68f)
                head = Offset(cx - unit * 0.34f, neck.y - unit * 0.22f)
                lFoot = Offset(cx + unit * 0.48f, floorY - unit * 0.26f)
                rFoot = Offset(cx + unit * 0.44f, floorY - unit * 0.22f)
                lKnee = Offset(cx + unit * 0.22f, floorY - unit * 0.48f)
                rKnee = Offset(cx + unit * 0.18f, floorY - unit * 0.45f)
                val twist = (smooth - 0.5f) * unit * 0.38f
                lHand = Offset(cx + twist, floorY - unit * 0.55f)
                rHand = lHand
            }
            ExerciseMotionType.NECK_ISOMETRIC -> {
                gloves = false
                phase = "STEADY ISOMETRIC RESISTANCE"
                rHand = Offset(head.x + unit * 0.16f, head.y)
                lHand = Offset(head.x - unit * 0.16f, head.y)
                impact = head
            }
            ExerciseMotionType.WRIST_CURLS -> {
                gloves = false
                phase = "FOREARM LOCKED • WRIST CURL"
                hip = Offset(cx - unit * 0.25f, floorY - unit * 0.45f)
                neck = Offset(cx - unit * 0.15f, hip.y - unit * 0.48f)
                head = Offset(cx - unit * 0.10f, neck.y - unit * 0.22f)
                lKnee = Offset(cx + unit * 0.22f, floorY - unit * 0.38f)
                rKnee = Offset(cx + unit * 0.15f, floorY - unit * 0.38f)
                lFoot = Offset(cx + unit * 0.25f, floorY)
                rFoot = Offset(cx + unit * 0.18f, floorY)
                lElbow = Offset(cx + unit * 0.15f, floorY - unit * 0.44f)
                lHand = Offset(cx + unit * 0.42f, floorY - unit * (0.44f + smooth * 0.14f))
            }
            ExerciseMotionType.RUNNING_SPRINTS, ExerciseMotionType.AGILITY_FOOTWORK -> {
                gloves = clip.type == ExerciseMotionType.AGILITY_FOOTWORK
                phase = if (clip.type == ExerciseMotionType.RUNNING_SPRINTS) "HIGH KNEE SPRINT" else "RING FOOTWORK LADDER"
                val stride = sin(t * 2f * PI.toFloat()) * unit * 0.34f
                lFoot = Offset(cx + stride, floorY - kotlin.math.max(0f, stride * 0.4f))
                rFoot = Offset(cx - stride, floorY - kotlin.math.max(0f, -stride * 0.4f))
                lKnee = Offset(cx + stride * 0.7f, floorY - unit * 0.44f)
                rKnee = Offset(cx - stride * 0.7f, floorY - unit * 0.44f)
            }
        }
    }

    return FighterPose(
        head = head,
        neck = neck,
        lShoulder = lShoulder,
        rShoulder = rShoulder,
        lElbow = lElbow,
        rElbow = rElbow,
        lHand = lHand,
        rHand = rHand,
        hip = hip,
        lKnee = lKnee,
        rKnee = rKnee,
        lFoot = lFoot,
        rFoot = rFoot,
        phaseLabel = phase,
        impactPoint = impact,
        trailStart = trailStart,
        trailEnd = trailEnd,
        isBoxingGloves = gloves
    )
}

private fun lerpOffset(a: Offset, b: Offset, t: Float): Offset =
    Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
