package com.xenogenics.app.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

class AudioController(context: Context) {
    private val soundPool: SoundPool
    private val sounds: Map<SoundEvent, Int>

    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(6)
            .build()

        sounds = SoundEvent.entries.associateWith { event ->
            val file = generateToneFile(context, event.frequency, event.durationMs, event.fileName)
            soundPool.load(file.absolutePath, 1)
        }
    }

    fun play(event: SoundEvent, enabled: Boolean) {
        if (!enabled) return
        val soundId = sounds[event] ?: return
        soundPool.play(soundId, 0.9f, 0.9f, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }

    private fun generateToneFile(context: Context, frequency: Int, durationMs: Int, name: String): File {
        val sampleRate = 22050
        val samples = (durationMs / 1000.0 * sampleRate).toInt()
        val data = ByteArray(samples * 2)
        for (i in 0 until samples) {
            val value = (sin(2.0 * PI * frequency * i / sampleRate) * 0.5 * Short.MAX_VALUE).toInt()
            data[i * 2] = (value and 0xff).toByte()
            data[i * 2 + 1] = ((value shr 8) and 0xff).toByte()
        }

        val file = File(context.cacheDir, name)
        if (!file.exists()) {
            FileOutputStream(file).use { out ->
                writeWavHeader(out, data.size, sampleRate)
                out.write(data)
            }
        }
        return file
    }

    private fun writeWavHeader(out: FileOutputStream, dataSize: Int, sampleRate: Int) {
        val totalDataLen = dataSize + 36
        val byteRate = sampleRate * 2
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        writeInt(header, 4, totalDataLen)
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        writeInt(header, 16, 16)
        writeShort(header, 20, 1)
        writeShort(header, 22, 1)
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, 2)
        writeShort(header, 34, 16)
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        writeInt(header, 40, dataSize)
        out.write(header, 0, 44)
    }

    private fun writeInt(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xff).toByte()
        array[offset + 1] = ((value shr 8) and 0xff).toByte()
        array[offset + 2] = ((value shr 16) and 0xff).toByte()
        array[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(array: ByteArray, offset: Int, value: Int) {
        array[offset] = (value and 0xff).toByte()
        array[offset + 1] = ((value shr 8) and 0xff).toByte()
    }
}

enum class SoundEvent(val frequency: Int, val durationMs: Int, val fileName: String) {
    SPIN(420, 120, "spin.wav"),
    REEL_STOP(520, 90, "reel_stop.wav"),
    WIN_TICK(640, 110, "win_tick.wav"),
    BONUS_TRIGGER(320, 280, "bonus_trigger.wav"),
    ORB_LAND(740, 90, "orb_land.wav"),
    JACKPOT(260, 360, "jackpot.wav")
}
