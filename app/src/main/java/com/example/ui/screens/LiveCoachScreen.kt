package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.catalog.BoxingCatalog
import com.example.data.model.AppLanguage
import com.example.i18n.AppStrings
import com.example.pose.PoseAnalyzer
import com.example.ui.components.HunterGlowCard
import com.example.ui.components.PoseSkeletonCanvas
import com.example.ui.components.SlowMotionReplayDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel
import java.io.File
import java.util.concurrent.Executors

@Composable
fun LiveCoachScreen(
    viewModel: BoxingCoachViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val state by viewModel.liveCoachState.collectAsStateWithLifecycle()
    val isHi = prefs.language == AppLanguage.HINGLISH
    val s = AppStrings.get(prefs.language)

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPerm = granted }

    var showTimerConfig by remember { mutableStateOf(false) }
    var showReplayDialog by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCaptureRef by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val poseAnalyzer = remember {
        PoseAnalyzer(
            isFrontCamera = { prefs.useFrontCamera },
            onResult = { res -> viewModel.onPoseFrameAnalyzed(res) }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            activeRecording?.stop()
            poseAnalyzer.close()
            analysisExecutor.shutdown()
        }
    }

    if (!hasCameraPerm) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_shadow_coach),
                contentDescription = "Shadow Coach Illustration",
                modifier = Modifier
                    .height(210.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.height(18.dp))
            Text(s.cameraPermissionTitle, style = MaterialTheme.typography.headlineMedium, color = SystemCyan)
            Spacer(Modifier.height(8.dp))
            Text(s.cameraPermissionDesc, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_camera_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SystemCyan, contentColor = ObsidianBg)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(s.grantPermission, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // CameraX Preview + Pose Analyzer + VideoCapture
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val resolutionSelector = ResolutionSelector.Builder()
                        .setResolutionStrategy(
                            ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                        )
                        .build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { it.setAnalyzer(analysisExecutor, poseAnalyzer) }

                    val recorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.SD))
                        .build()
                    val videoCapture = VideoCapture.withOutput(recorder)
                    videoCaptureRef = videoCapture

                    val selector = if (prefs.useFrontCamera) {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    } else {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    }

                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageAnalysis,
                            videoCapture
                        )
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Live Skeleton Overlay
        PoseSkeletonCanvas(
            joints = state.livePose.normalizedJoints,
            isGuardGood = state.livePose.guardOk && state.livePose.chinTucked,
            activePunchSide = state.livePose.activePunchArmSide,
            modifier = Modifier.fillMaxSize()
        )

        // Top HUD: Timer, Round, Mode, Angles
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = ObsidianBg.copy(alpha = 0.78f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, SystemCyan.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val phaseTitle = if (state.isRestPhase) s.restLabel else "${s.roundLabel} ${state.currentRound}/${state.totalRounds}"
                        Text(phaseTitle, style = MaterialTheme.typography.labelSmall, color = if (state.isRestPhase) WarningAmber else SystemCyan)
                        val mins = state.timerSecondsLeft / 60
                        val secs = state.timerSecondsLeft % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = state.comboMode,
                            onClick = { viewModel.setComboMode(!state.comboMode) },
                            label = { Text(if (state.comboMode) state.selectedCombo.code else s.freeMode, style = MaterialTheme.typography.labelSmall) }
                        )
                        IconButton(
                            onClick = { showTimerConfig = true },
                            modifier = Modifier.testTag("timer_config_button")
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = s.roundTimer, tint = SystemCyan)
                        }
                        IconButton(
                            onClick = { viewModel.toggleCameraFacing() },
                            modifier = Modifier.testTag("flip_camera_button")
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = s.switchCamera, tint = TextPrimary)
                        }
                    }
                }
            }

            // Joint Angles & Footwork HUD
            Surface(
                color = ObsidianBg.copy(alpha = 0.72f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    val a = state.livePose.angles
                    Text(
                        text = "Elbow L:${a.leftElbow}° R:${a.rightElbow}° | Knee L:${a.leftKnee}° R:${a.rightKnee}° | Spine:${a.spineTilt}° | ${state.livePose.fps} FPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SystemCyan
                    )
                    Text(
                        text = if (isHi) state.livePose.footworkFeedbackHi else state.livePose.footworkFeedbackEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.livePose.footworkScore >= 75) SahiEmerald else WarningAmber
                    )
                }
            }

            if (state.comboMode) {
                Surface(
                    color = MonarchPurple.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MonarchPurpleGlow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.selectedCombo.code} • Step ${state.comboStepIndex + 1}/${state.selectedCombo.sequence.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Done: ${state.combosCompleted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SahiEmerald
                        )
                    }
                }
            }
        }

        // Bottom HUD: Live Punch Score + Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val punch = state.lastPunch
            val tagColor = if (punch == null) SystemCyan else if (punch.isSahi) SahiEmerald else GalatCrimson
            Surface(
                color = ObsidianBg.copy(alpha = 0.85f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, tagColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHi) state.liveBannerHi else state.liveBannerEn,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Punches: ${state.sessionPunches.size} • Guard: ${if (state.livePose.guardOk) "OK" else "DROP!"} • Chin: ${if (state.livePose.chinTucked) "TUCK" else "UP!"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    if (punch != null) {
                        Surface(
                            color = tagColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${if (punch.isSahi) s.sahiTag else s.galatTag} ${punch.score}",
                                style = MaterialTheme.typography.labelLarge,
                                color = tagColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (state.sessionActive) {
                            if (state.isRecording) {
                                activeRecording?.stop()
                                activeRecording = null
                                viewModel.toggleRecordingState(false)
                            }
                            viewModel.endLiveSession()
                        } else {
                            poseAnalyzer.resetEngine()
                            viewModel.startLiveSession()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("start_stop_session_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.sessionActive) GalatCrimson else SystemCyan,
                        contentColor = if (state.sessionActive) TextPrimary else ObsidianBg
                    )
                ) {
                    Icon(if (state.sessionActive) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.sessionActive) s.stopSession else s.startRound, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        if (state.isRecording) {
                            activeRecording?.stop()
                            activeRecording = null
                            viewModel.toggleRecordingState(false)
                        } else {
                            val dir = File(context.filesDir, "recordings").apply { mkdirs() }
                            val file = File(dir, "box_${System.currentTimeMillis()}.mp4")
                            val output = FileOutputOptions.Builder(file).build()
                            val capture = videoCaptureRef
                            if (capture != null) {
                                activeRecording = capture.output
                                    .prepareRecording(context, output)
                                    .start(ContextCompat.getMainExecutor(context)) { event ->
                                        if (event is VideoRecordEvent.Finalize && !event.hasError()) {
                                            viewModel.toggleRecordingState(false, file.toURI().toString())
                                        }
                                    }
                                viewModel.toggleRecordingState(true, file.toURI().toString())
                            }
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("record_video_button"),
                    border = BorderStroke(1.dp, if (state.isRecording) GalatCrimson else MonarchPurpleGlow)
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (state.isRecording) GalatCrimson else MonarchPurpleGlow
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (state.isRecording) s.stopRecording else s.recordVideo)
                }
            }
        }
    }

    // Timer Config Dialog
    if (showTimerConfig) {
        var rMin by remember { mutableIntStateOf(state.roundDurationSec / 60) }
        var restSec by remember { mutableIntStateOf(state.restDurationSec) }
        var totalR by remember { mutableIntStateOf(state.totalRounds) }
        AlertDialog(
            onDismissRequest = { showTimerConfig = false },
            containerColor = ObsidianCard,
            title = { Text(s.roundTimer, color = SystemCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${s.roundLabel}: $rMin min")
                    Slider(value = rMin.toFloat(), onValueChange = { rMin = it.toInt() }, valueRange = 1f..5f, steps = 3)
                    Text("${s.restLabel}: $restSec sec")
                    Slider(value = restSec.toFloat(), onValueChange = { restSec = (it.toInt() / 15) * 15 }, valueRange = 15f..120f)
                    Text("Rounds: $totalR")
                    Slider(value = totalR.toFloat(), onValueChange = { totalR = it.toInt() }, valueRange = 1f..12f, steps = 10)
                    Text("Combo:", color = MonarchPurpleGlow)
                    BoxingCatalog.presetCombos.take(4).forEach { c ->
                        FilterChip(
                            selected = state.selectedCombo.id == c.id,
                            onClick = { viewModel.setComboMode(true, c) },
                            label = { Text(c.nameHi) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.configureTimer(rMin * 60, restSec, totalR)
                    showTimerConfig = false
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Session End Summary Dialog
    val summary = state.summary
    if (summary != null) {
        Dialog(onDismissRequest = { viewModel.dismissSessionSummary() }) {
            HunterGlowCard(accentColor = SahiEmerald) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(s.sessionSummary, style = MaterialTheme.typography.headlineMedium, color = SystemCyan)
                    if (summary.leveledUpTo != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "⚡ LEVEL UP! Hunter Level ${summary.leveledUpTo}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = RankGold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("${s.totalPunches}: ${summary.totalPunches}", color = TextPrimary)
                    Text("${s.avgScore}: ${summary.avgScore}/100 (+${summary.xpEarned} XP)", color = SahiEmerald)
                    Text("Guard Discipline: ${summary.guardPct}% • Footwork: ${summary.footworkPct}%", color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Text("${s.bestPunch}: ${summary.bestPunch}", color = SystemCyan)
                    Text("${s.worstPunch}: ${summary.worstPunch}", color = WarningAmber)
                    Spacer(Modifier.height(8.dp))
                    Text(s.commonMistakes, style = MaterialTheme.typography.titleMedium, color = GalatCrimson)
                    if (summary.topMistakes.isEmpty()) {
                        Text(if (isHi) "Koi badi galti nahi!" else "No major mistakes!", color = SahiEmerald)
                    } else {
                        summary.topMistakes.forEach { Text("• $it", color = TextSecondary) }
                    }
                    Spacer(Modifier.height(14.dp))
                    if (summary.videoPath != null) {
                        Button(
                            onClick = { showReplayDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MonarchPurple)
                        ) {
                            Icon(Icons.Default.SlowMotionVideo, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(s.watchReplay)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = { viewModel.dismissSessionSummary() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(s.closeLabel)
                    }
                }
            }
        }
    }

    if (showReplayDialog && summary?.videoPath != null) {
        SlowMotionReplayDialog(
            videoPath = summary.videoPath,
            poseTimelineJson = summary.poseTimelineJson,
            onDismiss = { showReplayDialog = false }
        )
    }
}
