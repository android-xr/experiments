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

import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Sound Composition - Represents a musical composition created by a SoundExplorer user.
 *
 * To build a composition, a user may add sound objects to their environment.
 * Each sound object has a low and a high sound sample associated with it.
 * Users may play / pause individual sound objects or the entire composition at any time.
 *
 * All sound samples are loaded as separate instances of SpatialAudioTrack. Once loaded, all sound
 * samples are played at the same time and simply allowed to loop forever after that.
 * When a user plays and stops sounds, we just toggle the volume of it's respective audio track
 * between 0.0 and 1.0. We did this for simplicity and to maintain synchronization between different
 * sounds.
 */
class SoundComposition  @Inject constructor(
    val soundManager: SoundManager
) {
    enum class State {
        READY,
        PLAYING,
        STOPPED
    }

    private val _state = MutableStateFlow<State>(State.READY)
    val state: StateFlow<State> = _state.asStateFlow()

    private var soundObjects = mutableListOf<SoundObjectComponent>()
    private var soundsInitialized = false

    fun updateSoundObjectPlayback(soundObject: SoundObjectComponent) {
        synchronized(this) {
            initializeSounds() // ensure sounds are initialized

            if (soundObject.isPlaying && this._state.value == State.PLAYING) {
                this.soundManager.setVolume(soundObject.lowSoundId, soundObject.lowSoundVolume)
                this.soundManager.setVolume(soundObject.highSoundId, soundObject.highSoundVolume)
            } else {
                this.soundManager.setVolume(soundObject.lowSoundId, 0.0f)
                this.soundManager.setVolume(soundObject.highSoundId, 0.0f)
            }
        }
    }

    fun registerSoundObject(soundObject: SoundObjectComponent) {
        synchronized(this) {
            if (this.state.value == State.PLAYING || this.state.value == State.STOPPED) {
                throw IllegalStateException("Tried to add an component after play() was called.")
            }

            this.soundObjects.add(soundObject)
        }
    }

    private fun initializeSounds() {
        if (this.soundsInitialized) {
            return
        }

        for (soundObject in soundObjects) {
            val soundObjectPlaying = this._state.value == State.PLAYING && soundObject.isPlaying

            soundManager.setVolume(soundObject.lowSoundId,
                if (soundObjectPlaying) soundObject.lowSoundVolume else 0.0f)
            soundManager.setVolume(soundObject.highSoundId,
                if (soundObjectPlaying) soundObject.highSoundVolume else 0.0f)
        }

        this.soundsInitialized = true
    }

    fun play(): Boolean {
        synchronized(this) {
            if (this._state.value != State.READY && this._state.value != State.STOPPED) {
                return false
            }

            this._state.value = State.PLAYING

            if (!this.soundsInitialized) {
                initializeSounds()
                return true // calling initialize sounds will set volumes appropriately, no need to continue.
            }

            for (soundObject in soundObjects) {
                this.soundManager.setVolume(soundObject.lowSoundId,
                    if (soundObject.isPlaying) soundObject.lowSoundVolume else 0.0f)
                this.soundManager.setVolume(soundObject.highSoundId,
                    if (soundObject.isPlaying) soundObject.highSoundVolume else 0.0f)
                soundObject.onPropertyChanged?.invoke()
            }

            return true
        }
    }

    fun stop(): Boolean {
        synchronized(this) {
            if (this._state.value != State.PLAYING) {
                return false
            }

            this._state.value = State.STOPPED

            for (soundObject in this.soundObjects) {
                this.soundManager.setVolume(soundObject.lowSoundId, 0.0f)
                this.soundManager.setVolume(soundObject.highSoundId, 0.0f)
                soundObject.onPropertyChanged?.invoke()
            }

            return true
        }
    }

    fun stopAllSoundComponents(): Boolean {
        synchronized(this) {
            if (this._state.value == State.READY) {
                return false
            }

            for (soundObject in this.soundObjects) {
                soundObject.stop()
            }

            return true
        }
    }
}