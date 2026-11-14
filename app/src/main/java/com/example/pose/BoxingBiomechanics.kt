package com.example.pose

import com.example.data.model.PosePointNorm
import com.example.data.model.PunchFeedbackEvent
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.*

data class JointAngles(
    val leftElbow: Int = 0,
    val rightElbow: Int = 0,
    val leftShoulder: Int = 0,
    val rightShoulder: Int = 0,
    val leftHip: Int = 0,
    val rightHip: Int = 0,
    val leftKnee: Int = 0,
    val rightKnee: Int = 0,
    val leftAnkle: Int = 0,
    val rightAnkle: Int = 0,
    val spineTilt: Int = 0
)

data class LivePoseState(
    val normalizedJoints: Map<Int, PosePointNorm> = emptyMap(),
    val angles: JointAngles = JointAngles(),
    val guardOk: Boolean = true,
    val chinTucked: Boolean = true,
    val footworkFeedbackHi: String = "Stance ready — balls of feet par bounce rakho",
    val footworkFeedbackEn: String = "Stance ready — stay light on balls of feet",
    val footworkScore: Int = 85,
    val stepDirection: String = "READY",
    val activePunchArmSide: Int = 0, // -1 left, 1 right, 0 none
    val fps: Int = 0,
    val hasPerson: Boolean = false
)

class BoxingBiomechanicsEngine {

    private var prevLeftWrist: PosePointNorm? = null
    private var prevRightWrist: PosePointNorm? = null
    private var prevHipCenter: PosePointNorm? = null
    private var prevShoulderSpan: Float = 0.2f
    private var prevAnkleY: Float = 0f
    private var prevTimestampMs: Long = 0L

    private var lastPunchTimestampMs: Long = 0L
    private var activeExtensionSide: Int = 0 // -1 left, 1 right
    private var peakExtensionTimeMs: Long = 0L
    private var pendingRetractionCheck: Boolean = false
    private var lastPunchEvent: PunchFeedbackEvent? = null

    fun reset() {
        prevLeftWrist = null
        prevRightWrist = null
        prevHipCenter = null
        prevTimestampMs = 0L
        lastPunchTimestampMs = 0L
        activeExtensionSide = 0
        pendingRetractionCheck = false
        lastPunchEvent = null
    }

    data class FrameAnalysisResult(
        val livePoseState: LivePoseState,
        val newPunchEvent: PunchFeedbackEvent? = null,
        val retractionWarningHi: String? = null,
        val retractionWarningEn: String? = null
    )

    fun analyzePose(
        pose: Pose,
        frameWidth: Int,
        frameHeight: Int,
        mirrorX: Boolean,
        nowMs: Long,
        fps: Int
    ): FrameAnalysisResult {
        val allLandmarks = pose.allPoseLandmarks
        if (allLandmarks.isEmpty() || frameWidth <= 0 || frameHeight <= 0) {
            return FrameAnalysisResult(LivePoseState(fps = fps, hasPerson = false))
        }

        val joints = HashMap<Int, PosePointNorm>(33)
        for (lm in allLandmarks) {
            if (lm.inFrameLikelihood >= 0.45f) {
                var nx = (lm.position.x / frameWidth.toFloat()).coerceIn(0f, 1f)
                val ny = (lm.position.y / frameHeight.toFloat()).coerceIn(0f, 1f)
                if (mirrorX) nx = 1f - nx
                joints[lm.landmarkType] = PosePointNorm(nx, ny)
            }
        }

        val lShoulder = joints[PoseLandmark.LEFT_SHOULDER]
        val rShoulder = joints[PoseLandmark.RIGHT_SHOULDER]
        val lElbow = joints[PoseLandmark.LEFT_ELBOW]
        val rElbow = joints[PoseLandmark.RIGHT_ELBOW]
        val lWrist = joints[PoseLandmark.LEFT_WRIST]
        val rWrist = joints[PoseLandmark.RIGHT_WRIST]
        val lHip = joints[PoseLandmark.LEFT_HIP]
        val rHip = joints[PoseLandmark.RIGHT_HIP]
        val lKnee = joints[PoseLandmark.LEFT_KNEE]
        val rKnee = joints[PoseLandmark.RIGHT_KNEE]
        val lAnkle = joints[PoseLandmark.LEFT_ANKLE]
        val rAnkle = joints[PoseLandmark.RIGHT_ANKLE]
        val lHeel = joints[PoseLandmark.LEFT_HEEL]
        val rHeel = joints[PoseLandmark.RIGHT_HEEL]
        val lToe = joints[PoseLandmark.LEFT_FOOT_INDEX]
        val rToe = joints[PoseLandmark.RIGHT_FOOT_INDEX]
        val nose = joints[PoseLandmark.NOSE]

        val leftElbowAngle = angleDeg(lShoulder, lElbow, lWrist)
        val rightElbowAngle = angleDeg(rShoulder, rElbow, rWrist)
        val leftShoulderAngle = angleDeg(lHip, lShoulder, lElbow)
        val rightShoulderAngle = angleDeg(rHip, rShoulder, rElbow)
        val leftHipAngle = angleDeg(lShoulder, lHip, lKnee)
        val rightHipAngle = angleDeg(rShoulder, rHip, rKnee)
        val leftKneeAngle = angleDeg(lHip, lKnee, lAnkle)
        val rightKneeAngle = angleDeg(rHip, rKnee, rAnkle)
        val leftAnkleAngle = angleDeg(lKnee, lAnkle, lToe)
        val rightAnkleAngle = angleDeg(rKnee, rAnkle, rToe)

        val midShoulder = midpoint(lShoulder, rShoulder)
        val midHip = midpoint(lHip, rHip)
        val spineTilt = if (midShoulder != null && midHip != null) {
            val dx = abs(midShoulder.x - midHip.x)
            val dy = abs(midHip.y - midShoulder.y).coerceAtLeast(0.001f)
            Math.toDegrees(atan2(dx.toDouble(), dy.toDouble())).toInt()
        } else 0

        val angles = JointAngles(
            leftElbow = leftElbowAngle,
            rightElbow = rightElbowAngle,
            leftShoulder = leftShoulderAngle,
            rightShoulder = rightShoulderAngle,
            leftHip = leftHipAngle,
            rightHip = rightHipAngle,
            leftKnee = leftKneeAngle,
            rightKnee = rightKneeAngle,
            leftAnkle = leftAnkleAngle,
            rightAnkle = rightAnkleAngle,
            spineTilt = spineTilt
        )

        val shoulderSpan = if (lShoulder != null && rShoulder != null) {
            dist(lShoulder, rShoulder).coerceAtLeast(0.08f)
        } else prevShoulderSpan

        // Chin tuck check: nose vertical clearance above shoulder line
        val chinTucked = if (nose != null && midShoulder != null) {
            val chinGap = (midShoulder.y - nose.y) / shoulderSpan
            chinGap < 0.78f
        } else true

        // Guard check for both wrists near nose/shoulder head box
        val leftGuardOk = isHandInGuard(lWrist, nose, lShoulder, shoulderSpan)
        val rightGuardOk = isHandInGuard(rWrist, nose, rShoulder, shoulderSpan)

        // Footwork analysis
        var fwScore = 90
        var fwHi = "Footwork badhiya — balance aur bounce tight hai"
        var fwEn = "Solid footwork — balanced stance & bounce"
        var stepDir = "BALANCED"

        if (lAnkle != null && rAnkle != null && lShoulder != null && rShoulder != null) {
            val ankleSpread = abs(lAnkle.x - rAnkle.x)
            val ratio = ankleSpread / shoulderSpan
            val crossing = (lShoulder.x < rShoulder.x && lAnkle.x > rAnkle.x + 0.02f) ||
                (lShoulder.x > rShoulder.x && lAnkle.x < rAnkle.x - 0.02f)

            if (crossing) {
                fwScore -= 30
                fwHi = "Pair cross mat karo! Stance khul ke rakho"
                fwEn = "Don't cross your feet! Keep base wide"
            } else if (ratio < 0.55f) {
                fwScore -= 22
                fwHi = "Pair bahut paas hain — shoulder-width kholo"
                fwEn = "Feet too narrow — widen stance"
            }

            if (midHip != null) {
                val minX = min(lAnkle.x, rAnkle.x)
                val maxX = max(lAnkle.x, rAnkle.x)
                val span = (maxX - minX).coerceAtLeast(0.05f)
                val balancePos = (midHip.x - minX) / span
                if (balancePos < 0.25f || balancePos > 0.75f) {
                    fwScore -= 15
                    fwHi = "Weight ek pair pe gira hai — center balance rakho"
                    fwEn = "Over-shifted weight — stay centered"
                }
            }

            if (leftKneeAngle > 172 && rightKneeAngle > 172 && leftKneeAngle != 0) {
                fwScore -= 12
                fwHi = "Ghutne bilkul seedhe hain — halka bend aur toes pe bounce karo"
                fwEn = "Knees locked — bend slightly & bounce on toes"
            } else if (lHeel != null && lToe != null && rHeel != null && rToe != null) {
                val heelOnGround = abs(lHeel.y - lToe.y) < 0.01f && abs(rHeel.y - rToe.y) < 0.01f
                if (heelOnGround && prevAnkleY > 0f && abs(lAnkle.y - prevAnkleY) < 0.002f) {
                    fwScore -= 8
                    fwHi = "Heel zameen pe chipki hai — balls of feet par bounce karo"
                    fwEn = "Flat-footed — stay light on the balls of your feet"
                }
            }

            if (prevHipCenter != null && midHip != null) {
                val dx = midHip.x - prevHipCenter!!.x
                val dy = midHip.y - prevHipCenter!!.y
                stepDir = when {
                    abs(dx) > 0.018f -> if (dx > 0) "SIDE RIGHT" else "SIDE LEFT"
                    dy > 0.015f -> "STEP IN"
                    dy < -0.015f -> "STEP BACK"
                    else -> "BOUNCE"
                }
            }
            prevAnkleY = lAnkle.y
        }
        fwScore = fwScore.coerceIn(35, 100)

        // Punch detection & retraction check
        var punchEvent: PunchFeedbackEvent? = null
        var retractWarnHi: String? = null
        var retractWarnEn: String? = null

        val dtSec = ((nowMs - prevTimestampMs).coerceAtLeast(16L)) / 1000f
        if (prevTimestampMs > 0L) {
            if (pendingRetractionCheck && nowMs - peakExtensionTimeMs > 520L) {
                pendingRetractionCheck = false
                val activeGuardBack = if (activeExtensionSide == -1) leftGuardOk else rightGuardOk
                if (!activeGuardBack) {
                    retractWarnHi = "Punch ke baad haath wapas guard pe nahi aaya!"
                    retractWarnEn = "Snap your hand back to guard after punching!"
                }
                activeExtensionSide = 0
            }

            val canTriggerNewPunch = (nowMs - lastPunchTimestampMs) > 480L
            if (canTriggerNewPunch) {
                val leftVel = velocity(prevLeftWrist, lWrist, dtSec)
                val rightVel = velocity(prevRightWrist, rWrist, dtSec)
                val leftDy = if (prevLeftWrist != null && lWrist != null) (prevLeftWrist!!.y - lWrist.y) / dtSec else 0f
                val rightDy = if (prevRightWrist != null && rWrist != null) (prevRightWrist!!.y - rWrist.y) / dtSec else 0f
                val leftDx = if (prevLeftWrist != null && lWrist != null) abs(lWrist.x - prevLeftWrist!!.x) / dtSec else 0f
                val rightDx = if (prevRightWrist != null && rWrist != null) abs(rWrist.x - prevRightWrist!!.x) / dtSec else 0f

                val leftPunching = leftVel > 1.25f && (leftElbowAngle > 135 || leftDx > 0.9f || leftDy > 0.9f)
                val rightPunching = rightVel > 1.25f && (rightElbowAngle > 135 || rightDx > 0.9f || rightDy > 0.9f)

                if (leftPunching || rightPunching) {
                    val isLeft = leftVel >= rightVel
                    val pElbow = if (isLeft) leftElbowAngle else rightElbowAngle
                    val oppGuardOk = if (isLeft) rightGuardOk else leftGuardOk
                    val dy = if (isLeft) leftDy else rightDy
                    val dx = if (isLeft) leftDx else rightDx

                    val rotationHappened = abs(shoulderSpan - prevShoulderSpan) > 0.028f ||
                        (lHip != null && rHip != null && dist(lHip, rHip) < shoulderSpan * 0.92f)

                    val punchType = classifyPunch(
                        isLeadSide = isLeft,
                        elbowAngle = pElbow,
                        dy = dy,
                        dx = dx,
                        shoulderRotated = rotationHappened
                    )

                    val evaluation = evaluatePunchForm(
                        punchType = punchType,
                        elbowAngle = pElbow,
                        oppGuardOk = oppGuardOk,
                        chinTucked = chinTucked,
                        spineTilt = spineTilt,
                        rotationHappened = rotationHappened,
                        fwScore = fwScore
                    )

                    punchEvent = PunchFeedbackEvent(
                        punchType = punchType,
                        score = evaluation.first,
                        isSahi = evaluation.first >= 72,
                        messageHi = evaluation.second,
                        messageEn = evaluation.third,
                        timestampMs = nowMs
                    )
                    lastPunchEvent = punchEvent
                    lastPunchTimestampMs = nowMs
                    activeExtensionSide = if (isLeft) -1 else 1
                    peakExtensionTimeMs = nowMs
                    pendingRetractionCheck = true
                }
            }
        }

        prevLeftWrist = lWrist
        prevRightWrist = rWrist
        prevHipCenter = midHip
        prevShoulderSpan = shoulderSpan
        prevTimestampMs = nowMs

        val guardBothOk = if (activeExtensionSide == 0) (leftGuardOk && rightGuardOk)
        else if (activeExtensionSide == -1) rightGuardOk else leftGuardOk

        return FrameAnalysisResult(
            livePoseState = LivePoseState(
                normalizedJoints = joints,
                angles = angles,
                guardOk = guardBothOk,
                chinTucked = chinTucked,
                footworkFeedbackHi = fwHi,
                footworkFeedbackEn = fwEn,
                footworkScore = fwScore,
                stepDirection = stepDir,
                activePunchArmSide = activeExtensionSide,
                fps = fps,
                hasPerson = true
            ),
            newPunchEvent = punchEvent,
            retractionWarningHi = retractWarnHi,
            retractionWarningEn = retractWarnEn
        )
    }

    private fun classifyPunch(
        isLeadSide: Boolean,
        elbowAngle: Int,
        dy: Float,
        dx: Float,
        shoulderRotated: Boolean
    ): String {
        return when {
            dy > 1.05f && elbowAngle in 60..130 -> "UPPERCUT"
            dx > 1.0f && elbowAngle in 65..135 -> "LEAD_HOOK"
            !isLeadSide || (shoulderRotated && !isLeadSide) -> "CROSS"
            else -> "JAB"
        }
    }

    private fun evaluatePunchForm(
        punchType: String,
        elbowAngle: Int,
        oppGuardOk: Boolean,
        chinTucked: Boolean,
        spineTilt: Int,
        rotationHappened: Boolean,
        fwScore: Int
    ): Triple<Int, String, String> {
        var score = 96
        val issuesHi = mutableListOf<String>()
        val issuesEn = mutableListOf<String>()

        if (!oppGuardOk) {
            score -= 22
            issuesHi.add("Doosra haath guard se neeche gir gaya!")
            issuesEn.add("Opposite guard hand dropped!")
        }
        if (!chinTucked) {
            score -= 14
            issuesHi.add("Chin upar uth rahi hai, tuck rakho!")
            issuesEn.add("Chin is up — tuck it down!")
        }
        if (spineTilt > 22) {
            score -= 14
            issuesHi.add("Aage zyada mat jhuko, spine seedhi rakho!")
            issuesEn.add("Over-leaning forward — keep spine upright!")
        }
        if ((punchType == "JAB" || punchType == "CROSS") && elbowAngle < 148) {
            score -= 15
            issuesHi.add("Elbow poora extend karo!")
            issuesEn.add("Extend your elbow fully!")
        }
        if ((punchType == "CROSS" || punchType == "LEAD_HOOK") && !rotationHappened) {
            score -= 12
            issuesHi.add("Hip aur shoulder rotate karo!")
            issuesEn.add("Rotate your hips and shoulders!")
        }
        if (fwScore < 65) {
            score -= 8
        }

        val finalScore = score.coerceIn(35, 100)
        val msgHi = issuesHi.firstOrNull() ?: "Shabash! Zabardast form aur crisp guard!"
        val msgEn = issuesEn.firstOrNull() ?: "Clean punch! Tight guard and sharp extension!"
        return Triple(finalScore, msgHi, msgEn)
    }

    private fun isHandInGuard(
        wrist: PosePointNorm?,
        nose: PosePointNorm?,
        shoulder: PosePointNorm?,
        shoulderSpan: Float
    ): Boolean {
        if (wrist == null) return true
        val ref = nose ?: shoulder ?: return true
        val d = dist(wrist, ref)
        return d < shoulderSpan * 1.45f && wrist.y <= (shoulder?.y ?: ref.y) + shoulderSpan * 0.65f
    }

    private fun velocity(p1: PosePointNorm?, p2: PosePointNorm?, dtSec: Float): Float {
        if (p1 == null || p2 == null) return 0f
        return dist(p1, p2) / dtSec
    }

    private fun dist(a: PosePointNorm, b: PosePointNorm): Float =
        hypot(a.x - b.x, a.y - b.y)

    private fun midpoint(a: PosePointNorm?, b: PosePointNorm?): PosePointNorm? {
        if (a == null || b == null) return null
        return PosePointNorm((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    }

    private fun angleDeg(a: PosePointNorm?, b: PosePointNorm?, c: PosePointNorm?): Int {
        if (a == null || b == null || c == null) return 0
        val baX = a.x - b.x
        val baY = a.y - b.y
        val bcX = c.x - b.x
        val bcY = c.y - b.y
        val dot = baX * bcX + baY * bcY
        val mag = hypot(baX, baY) * hypot(bcX, bcY)
        if (mag < 0.0001f) return 0
        val cosVal = (dot / mag).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cosVal.toDouble())).toInt()
    }
}
