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
package com.experiment.jetpackxr.soundexplorer.ui

import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEvent.Action
import java.util.function.Consumer

class SoundEntityMovementHandler(
    // sound object whose position will be tracked to determine if the sound should change
    val soundObject: SoundObjectComponent,
    // difference in height relative to the initial location at which the sound changes
    val heightToChangeSound: Float
) : Consumer<InputEvent> {

    private val initialHeight = soundObject.entity.getPose().translation.y

    override fun accept(inputEvent: InputEvent) {
        if (inputEvent.action != Action.ACTION_MOVE) {
            return
        }

        val relativeHeight = this.soundObject.entity.getPose().translation.y - initialHeight

        if (relativeHeight < -heightToChangeSound) {
            this.soundObject.setVolume(1.0f, 0.0f)
        } else if (relativeHeight > heightToChangeSound) {
            this.soundObject.setVolume(0.0f, 1.0f)
        } else {
            val highSoundVolume = (relativeHeight + heightToChangeSound) / (2.0f * heightToChangeSound)
            this.soundObject.setVolume(1.0f - highSoundVolume, highSoundVolume)
        }
    }
}
