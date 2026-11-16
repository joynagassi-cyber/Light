package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "FocusAudioEngine"
private const val SAMPLE_RATE = 22050
private const val CHUNK_SAMPLES = 2048

enum class AudioCategory(val label: String) {
    ALL("Tous"),
    LOFI("Lo-Fi"),
    AMBIENT("Ambiance"),
    WHITE_NOISE("Bruit Blanc")
}

data class FocusAudioTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: AudioCategory
)

object FocusAudioCatalog {
    val tracks: List<FocusAudioTrack> = listOf(
        FocusAudioTrack(
            id = "lofi_chill",
            title = "Lo-Fi Chill 75 BPM",
            subtitle = "Rhodes doux & vinyle chaud",
            category = AudioCategory.LOFI
        ),
        FocusAudioTrack(
            id = "lofi_synth",
            title = "Lo-Fi Deep Code",
            subtitle = "Accords nocturnes & nappes douces",
            category = AudioCategory.LOFI
        ),
        FocusAudioTrack(
            id = "rain_ambient",
            title = "Pluie d'Étude",
            subtitle = "Averse apaisante & gouttes fines",
            category = AudioCategory.AMBIENT
        ),
        FocusAudioTrack(
            id = "warm_cafe",
            title = "Atelier & Feu Doux",
            subtitle = "Chaleur grave & crépitement léger",
            category = AudioCategory.AMBIENT
        ),
        FocusAudioTrack(
            id = "alpha_waves",
            title = "Ondes Alpha 10 Hz",
            subtitle = "Concentration profonde pure",
            category = AudioCategory.AMBIENT
        ),
        FocusAudioTrack(
            id = "white_noise",
            title = "Bruit Blanc Doux",
            subtitle = "Masque acoustique uniforme",
            category = AudioCategory.WHITE_NOISE
        )
    )

    fun byId(id: String?): FocusAudioTrack =
        tracks.firstOrNull { it.id == id } ?: tracks.first()
}

class FocusAudioEngine {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null

    @Volatile
    private var targetVolume: Float = 0.65f

    @Volatile
    private var currentTrackId: String = FocusAudioCatalog.tracks.first().id

    @Volatile
    private var playing: Boolean = false

    fun setVolume(volume: Float) {
        targetVolume = volume.coerceIn(0f, 1f)
    }

    fun start(trackId: String, volume: Float): Boolean {
        targetVolume = volume.coerceIn(0f, 1f)
        if (playing && currentTrackId == trackId && playbackJob?.isActive == true) {
            return true
        }
        stop()
        currentTrackId = trackId
        playing = true
        playbackJob = scope.launch {
            runSynthesisLoop(trackId)
        }
        return true
    }

    fun stop() {
        playing = false
        playbackJob?.cancel()
        playbackJob = null
    }

    fun release() {
        stop()
        scope.cancel()
    }

    private fun CoroutineScope.runSynthesisLoop(trackId: String) {
        var audioTrack: AudioTrack? = null
        try {
            val minBufBytes = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(CHUNK_SAMPLES * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                playing = false
                return
            }

            audioTrack = track
            track.play()

            val pcmBuffer = ShortArray(CHUNK_SAMPLES)
            val generator = createGenerator(trackId)
            var smoothVol = 0f

            while (isActive && playing) {
                val desiredVol = targetVolume
                for (i in pcmBuffer.indices) {
                    smoothVol += (desiredVol - smoothVol) * 0.0025f
                    val sample = (generator.nextSample() * smoothVol).coerceIn(-0.98f, 0.98f)
                    pcmBuffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                }
                val written = track.write(pcmBuffer, 0, pcmBuffer.size)
                if (written < 0) break
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Audio playback stopped or unavailable on this device: ${t.message}")
        } finally {
            playing = false
            runCatching {
                audioTrack?.pause()
                audioTrack?.flush()
                audioTrack?.stop()
            }
            runCatching { audioTrack?.release() }
        }
    }

    private fun createGenerator(trackId: String): SampleGenerator = when (trackId) {
        "white_noise" -> FilteredWhiteNoiseGenerator()
        "rain_ambient" -> RainAmbientGenerator()
        "warm_cafe" -> WarmCafeGenerator()
        "alpha_waves" -> AlphaWavesGenerator()
        "lofi_synth" -> LofiSynthPadGenerator()
        else -> LofiChillBeatGenerator()
    }
}

private interface SampleGenerator {
    fun nextSample(): Float
}

private class FilteredWhiteNoiseGenerator : SampleGenerator {
    private val rng = Random(42)
    private var lp1 = 0f
    private var lp2 = 0f

    override fun nextSample(): Float {
        // Box-like warm noise averaging two uniform draws + gentle 2-pole lowpass
        val raw = ((rng.nextFloat() * 2f - 1f) + (rng.nextFloat() * 2f - 1f)) * 0.5f
        lp1 += (raw - lp1) * 0.24f
        lp2 += (lp1 - lp2) * 0.24f
        return lp2 * 0.65f
    }
}

private class RainAmbientGenerator : SampleGenerator {
    private val rng = Random(108)
    private var pinkB0 = 0f
    private var pinkB1 = 0f
    private var pinkB2 = 0f
    private var dropAmp = 0f
    private var dropPhase = 0.0
    private var dropFreq = 900.0
    private var sampleIndex = 0L

    override fun nextSample(): Float {
        sampleIndex++
        val white = rng.nextFloat() * 2f - 1f
        pinkB0 = 0.997f * pinkB0 + white * 0.029f
        pinkB1 = 0.985f * pinkB1 + white * 0.032f
        pinkB2 = 0.950f * pinkB2 + white * 0.048f
        val baseRain = (pinkB0 + pinkB1 + pinkB2 + white * 0.12f) * 0.42f

        val swell = 0.85f + 0.15f * sin(2.0 * PI * sampleIndex / (SAMPLE_RATE * 11.0)).toFloat()

        // Occasional gentle raindrop pitter-patter
        if (dropAmp < 0.002f && rng.nextInt(3200) == 0) {
            dropAmp = 0.12f + rng.nextFloat() * 0.12f
            dropFreq = 700.0 + rng.nextDouble() * 900.0
            dropPhase = 0.0
        }
        val drop = if (dropAmp > 0.001f) {
            dropPhase += 2.0 * PI * dropFreq / SAMPLE_RATE
            dropAmp *= 0.994f
            sin(dropPhase).toFloat() * dropAmp
        } else {
            0f
        }
        return (baseRain * swell + drop) * 0.85f
    }
}

private class WarmCafeGenerator : SampleGenerator {
    private val rng = Random(256)
    private var brown = 0f
    private var crackleEnv = 0f
    private var sampleIndex = 0L

    override fun nextSample(): Float {
        sampleIndex++
        val white = rng.nextFloat() * 2f - 1f
        brown = (brown + white * 0.03f).coerceIn(-1f, 1f) * 0.994f

        val t = sampleIndex.toDouble() / SAMPLE_RATE
        val warmDrone = (sin(2.0 * PI * 110.0 * t) * 0.06 + sin(2.0 * PI * 165.0 * t) * 0.04).toFloat()

        if (crackleEnv < 0.003f && rng.nextInt(4200) == 0) {
            crackleEnv = 0.16f + rng.nextFloat() * 0.12f
        }
        val crackle = if (crackleEnv > 0.002f) {
            crackleEnv *= 0.965f
            (rng.nextFloat() * 2f - 1f) * crackleEnv
        } else {
            0f
        }

        return (brown * 1.15f + warmDrone + crackle) * 0.75f
    }
}

private class AlphaWavesGenerator : SampleGenerator {
    private var sampleIndex = 0L

    override fun nextSample(): Float {
        val t = sampleIndex++.toDouble() / SAMPLE_RATE
        // Soothing harmonic drone modulated at 10 Hz alpha rhythm
        val alphaMod = 0.72 + 0.28 * sin(2.0 * PI * 10.0 * t)
        val fundamental = sin(2.0 * PI * 196.0 * t) * 0.28
        val fifth = sin(2.0 * PI * 294.0 * t) * 0.14
        val octave = sin(2.0 * PI * 392.0 * t) * 0.07
        val slowBreath = 0.85 + 0.15 * sin(2.0 * PI * 0.09 * t)
        return ((fundamental + fifth + octave) * alphaMod * slowBreath).toFloat()
    }
}

private class LofiChillBeatGenerator : SampleGenerator {
    private val rng = Random(777)
    private var sampleIndex = 0L
    private var crackleLp = 0f

    // Warm Rhodes jazz progression (Cmaj9 -> Am9 -> Fmaj9 -> G6)
    private val chords = arrayOf(
        doubleArrayOf(261.63, 329.63, 392.00, 493.88, 587.33),
        doubleArrayOf(220.00, 261.63, 329.63, 392.00, 493.88),
        doubleArrayOf(174.61, 220.00, 261.63, 329.63, 392.00),
        doubleArrayOf(196.00, 246.94, 293.66, 329.63, 440.00)
    )

    override fun nextSample(): Float {
        val idx = sampleIndex++
        val t = idx.toDouble() / SAMPLE_RATE
        // 75 BPM -> 0.8s per beat, 4 beats per chord (3.2s)
        val beatDuration = 0.8
        val totalChordDur = beatDuration * 4.0
        val loopPos = t % (totalChordDur * chords.size)
        val chordIdx = (loopPos / totalChordDur).toInt().coerceIn(0, chords.lastIndex)
        val chordLocalT = loopPos % totalChordDur

        // Gentle tape wow/flutter
        val wow = 1.0 + 0.0018 * sin(2.0 * PI * 0.45 * t)

        // Warm electric piano envelope per bar
        val attack = (chordLocalT / 0.08).coerceIn(0.0, 1.0)
        val decay = kotlin.math.exp(-chordLocalT * 0.55)
        val chordEnv = attack * (0.35 + 0.65 * decay)

        var keys = 0.0
        val notes = chords[chordIdx]
        for (n in notes.indices) {
            val freq = notes[n] * wow
            keys += sin(2.0 * PI * freq * t) * 0.10
            keys += sin(2.0 * PI * (freq * 2.0) * t) * 0.022
        }

        // Muted soft lo-fi kick on beat 0 & beat 2, soft brush on beat 1 & 3
        val beatIdx = ((chordLocalT / beatDuration).toInt()) % 4
        val beatLocalT = chordLocalT % beatDuration
        var perc = 0.0
        if (beatIdx == 0 || beatIdx == 2) {
            if (beatLocalT < 0.18) {
                val kickEnv = kotlin.math.exp(-beatLocalT * 24.0)
                val kickFreq = 52.0 + 70.0 * kotlin.math.exp(-beatLocalT * 35.0)
                perc += sin(2.0 * PI * kickFreq * beatLocalT) * kickEnv * 0.24
            }
        } else {
            if (beatLocalT < 0.07) {
                val brushEnv = kotlin.math.exp(-beatLocalT * 45.0)
                perc += (rng.nextFloat() * 2f - 1f) * brushEnv * 0.07
            }
        }

        // Cozy vinyl texture
        val rawDust = rng.nextFloat() * 2f - 1f
        crackleLp += (rawDust - crackleLp) * 0.14f
        val vinyl = crackleLp * 0.035f

        return (keys * chordEnv + perc + vinyl).toFloat() * 0.88f
    }
}

private class LofiSynthPadGenerator : SampleGenerator {
    private val rng = Random(909)
    private var sampleIndex = 0L
    private var hissLp = 0f

    // Dreamy minor progression (Dm9 -> Bbmaj7 -> Gm7 -> Asus4)
    private val chords = arrayOf(
        doubleArrayOf(146.83, 220.00, 261.63, 329.63),
        doubleArrayOf(116.54, 174.61, 220.00, 293.66),
        doubleArrayOf(196.00, 233.08, 293.66, 349.23),
        doubleArrayOf(110.00, 164.81, 220.00, 293.66)
    )

    override fun nextSample(): Float {
        val idx = sampleIndex++
        val t = idx.toDouble() / SAMPLE_RATE
        val barDur = 3.6
        val loopPos = t % (barDur * chords.size)
        val chordIdx = (loopPos / barDur).toInt().coerceIn(0, chords.lastIndex)
        val localT = loopPos % barDur

        val env = (localT / 0.45).coerceIn(0.0, 1.0) *
            ((barDur - localT) / 0.45).coerceIn(0.15, 1.0)
        val drift = 1.0 + 0.002 * sin(2.0 * PI * 0.3 * t)

        var pad = 0.0
        for (freq in chords[chordIdx]) {
            pad += sin(2.0 * PI * freq * drift * t) * 0.13
            pad += sin(2.0 * PI * (freq * 1.5) * t) * 0.03
        }

        val noise = rng.nextFloat() * 2f - 1f
        hissLp += (noise - hissLp) * 0.08f
        return (pad * env + hissLp * 0.035).toFloat()
    }
}
