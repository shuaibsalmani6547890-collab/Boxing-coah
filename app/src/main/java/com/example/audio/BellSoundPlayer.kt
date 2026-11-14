package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object BellSoundPlayer {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playBoxingBell() {
        scope.launch {
            playHarmonicBell(durationMs = 1100, baseFreq = 880.0)
        }
    }

    fun playClapperWarning() {
        scope.launch {
            playClack()
            kotlinx.coroutines.delay(140)
            playClack()
            kotlinx.coroutines.delay(140)
            playClack()
        }
    }

    private fun playHarmonicBell(durationMs: Int, baseFreq: Double) {
        runCatching {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 3.8)
                val wave = 0.55 * sin(2 * PI * baseFreq * t) +
                    0.28 * sin(2 * PI * (baseFreq * 2.01) * t) +
                    0.17 * sin(2 * PI * (baseFreq * 2.98) * t)
                buffer[i] = (wave * env * Short.MAX_VALUE * 0.7).toInt().toShort()
            }
            writePcm(sampleRate, buffer)
        }
    }

    private fun playClack() {
        runCatching {
            val sampleRate = 22050
            val numSamples = (sampleRate * 80) / 1000
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 45.0)
                val wave = sin(2 * PI * 1450.0 * t) + 0.5 * sin(2 * PI * 2200.0 * t)
                buffer[i] = (wave * env * Short.MAX_VALUE * 0.65).toInt().toShort()
            }
            writePcm(sampleRate, buffer)
        }
    }

    private fun writePcm(sampleRate: Int, buffer: ShortArray) {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        track.play()
        Thread.sleep((buffer.size * 1000L) / sampleRate + 60)
        track.release()
    }
}
