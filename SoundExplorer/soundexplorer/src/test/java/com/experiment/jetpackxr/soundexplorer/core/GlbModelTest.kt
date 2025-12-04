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
package com.experiment.jetpackxr.soundexplorer.core

import com.experiment.jetpackxr.soundexplorer.R
import com.google.common.truth.Truth.*
import org.junit.Test

class GlbModelTest {

    @Test
    fun pumpod_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pumpod
        assertWithMessage("Pumpod low sound ID does not match").that(R.raw.inst01_high).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Pumpod high sound ID does not match").that(R.raw.inst01_low).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun pluff_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pluff
        assertWithMessage("Pluff low sound ID does not match").that(R.raw.inst02_mid).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Pluff high sound ID does not match").that(R.raw.inst02_high).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun pillowtri_hasCorrectSoundResourceIds() {
        val model = GlbModel.Pillowtri
        assertWithMessage("Pillowtri low sound ID does not match").that(R.raw.inst03_high).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Pillowtri high sound ID does not match").that(R.raw.inst03_low).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun swirlnut_hasCorrectSoundResourceIds() {
        val model = GlbModel.Swirlnut
        assertWithMessage("Swirlnut low sound ID does not match").that(R.raw.inst04_low).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Swirlnut high sound ID does not match").that(R.raw.inst04_high).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun twistbud_hasCorrectSoundResourceIds() {
        val model = GlbModel.Twistbud
        assertWithMessage("Twistbud low sound ID does not match").that(R.raw.inst05_high).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Twistbud high sound ID does not match").that(R.raw.inst05_mid).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun squube_hasCorrectSoundResourceIds() {
        val model = GlbModel.Squube

        assertWithMessage("Squube low sound ID does not match").that(R.raw.inst06_high).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Squube high sound ID does not match").that(R.raw.inst06_low).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun bloomspire_hasCorrectSoundResourceIds() {
        val model = GlbModel.Bloomspire
        assertWithMessage("Bloomspire low sound ID does not match").that(R.raw.inst07_low).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Bloomspire high sound ID does not match").that(R.raw.inst07_mid).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun cello_hasCorrectSoundResourceIds() {
        val model = GlbModel.Cello
        assertWithMessage("Cello low sound ID does not match").that(R.raw.inst08_high).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Cello high sound ID does not match").that(R.raw.inst08_mid).isEqualTo(model.highSoundResourceId)
    }

    @Test
    fun munchkin_hasCorrectSoundResourceIds() {
        val model = GlbModel.Munchkin
        assertWithMessage("Munchkin low sound ID does not match").that(R.raw.inst09_low).isEqualTo(model.lowSoundResourceId)
        assertWithMessage("Munchkin high sound ID does not match").that(R.raw.inst09_high).isEqualTo(model.highSoundResourceId)
    }

}