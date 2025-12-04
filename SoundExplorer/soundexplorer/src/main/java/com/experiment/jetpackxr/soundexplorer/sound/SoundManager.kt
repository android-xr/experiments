/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.experiment.jetpackxr.soundexplorer.sound

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.PointSourceParams
import androidx.xr.runtime.Session
import androidx.xr.scenecore.SpatialAudioTrackBuilder
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor() : Closeable {

    private var audioTracks: MutableList<AudioTrack> = mutableListOf()

    private fun parseWAVFile(session: Session, soundResourceId: Int): ByteArray? {
        var inputStream: InputStream? = null
        try {
            inputStream = session.activity.resources.openRawResource(soundResourceId)

            // wav file header checks
            val riffId = String(inputStream.readNBytes(4))
            if (riffId != "RIFF") throw IOException("Invalid WAV file - Missing 'RIFF'")
            val waveId = String(inputStream.readNBytes(8), 4, 4)
            if (waveId != "WAVE") throw IOException("Invalid WAV file - Missing 'WAVE'")

            var dataId = String(inputStream.readNBytes(4))
            while (dataId != "data") {
                val nextChar = String(inputStream.readNBytes(1))
                if (nextChar.length != 1) {
                    throw IOException("Invalid WAV file - No data section found!")
                }
                dataId = dataId.substring(1) + nextChar
            }

            val bufferSize = ByteBuffer
                .wrap(inputStream.readNBytes(4))
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt(0)

            return inputStream.readNBytes(bufferSize)
        } finally {
            inputStream?.close()
        }

        return null
    }

    // Note! - loadSound is purposefully not a suspend function and is allowed to execute
    // on the callers thread. It is important to minimize the time for playAllSounds() to
    // execute as much as possible so all sounds play in sync.
    // By playing immediately after loading on the same thread, we maximize chances for
    // exploiting locality. (yielding a ~2x perf improvement vs calling loadSound in separate
    // coroutines and scheduling using the IO coroutine context)
    // Be careful and check for regressions in the time to play all the sounds if refactors
    // are made to this implementation or the way these functions are called from MainActivity.
    fun loadSound(session: Session, entity: Entity, soundResourceId: Int): Int? {
        var audioTrackInitialized = false
        var audioTrack: AudioTrack? = null
        var soundId: Int? = null

        try {
            val audioBuffer = parseWAVFile(session, soundResourceId)

            if (audioBuffer == null) {
                return null
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormatConfig = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrackBuilder = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormatConfig)
                .setBufferSizeInBytes(audioBuffer.size)
                .setTransferMode(AudioTrack.MODE_STATIC)

            val pointSourceAttributes = PointSourceParams(entity)

            audioTrack = SpatialAudioTrackBuilder
                .setPointSourceParams(
                    session,
                    audioTrackBuilder,
                    pointSourceAttributes
                )
                .build()

            audioTrack.write(audioBuffer, 0, audioBuffer.size)

            audioTrack.setLoopPoints(0, audioTrack.bufferSizeInFrames, -1)
            audioTrack.setVolume(0.0f)

            // Audio tracks are created before any are played so synchronization is not necessary elsewhere.
            // (It may be desirable to assert that audiotracks is not mutated later, but these calls are highly
            //  latency sensitive, so leaving additional synchronization out for now. If added later, use lock-free
            //  synchronization. (atomic flag or something))
            synchronized(audioTracks) {
                audioTracks.add(audioTrack)
                soundId = audioTracks.size - 1
            }

            audioTrackInitialized = true
        } finally {
            if (audioTrack != null && !audioTrackInitialized && audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
            }
        }

        return soundId
    }

    // Note - playing all sounds in order seems to execute faster than starting sounds in an
    // arbitrary order. Measure and exercise caution if you consider refactoring this call to play
    // individual sounds instead of all of them.
    fun playAllSounds() {
        if (this.audioTracks.isEmpty()) {
            return
        }

        for (i in 0..<this.audioTracks.size) {
            this.audioTracks[i].play()
        }
    }

    fun setVolume(soundId: Int?, volume: Float) {
        soundId?.let {
            this.audioTracks[it].setVolume(volume)
        }
    }

    override fun close() {
        for (audioTrack in audioTracks) {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop()
            }
            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.release()
            }
        }
    }
}