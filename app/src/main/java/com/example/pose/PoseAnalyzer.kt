package com.example.pose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

class PoseAnalyzer(
    private val isFrontCamera: () -> Boolean,
    private val onResult: (BoxingBiomechanicsEngine.FrameAnalysisResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector: PoseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private val engine = BoxingBiomechanicsEngine()
    private var lastProcessedMs = 0L
    private var fpsCounter = 0
    private var fpsWindowStartMs = 0L
    private var currentFps = 0

    fun resetEngine() {
        engine.reset()
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        // Cap around ~18 fps for low-end phone battery and thermal stability
        if (now - lastProcessedMs < 55L) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        lastProcessedMs = now
        val rotation = imageProxy.imageInfo.rotationDegrees
        val uprightWidth = if (rotation == 90 || rotation == 270) imageProxy.height else imageProxy.width
        val uprightHeight = if (rotation == 90 || rotation == 270) imageProxy.width else imageProxy.height

        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
        detector.process(inputImage)
            .addOnSuccessListener { pose: Pose ->
                fpsCounter++
                if (fpsWindowStartMs == 0L) fpsWindowStartMs = now
                if (now - fpsWindowStartMs >= 1000L) {
                    currentFps = fpsCounter
                    fpsCounter = 0
                    fpsWindowStartMs = now
                }
                val result = engine.analyzePose(
                    pose = pose,
                    frameWidth = uprightWidth,
                    frameHeight = uprightHeight,
                    mirrorX = isFrontCamera(),
                    nowMs = now,
                    fps = currentFps
                )
                onResult(result)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    fun close() {
        detector.close()
    }
}
