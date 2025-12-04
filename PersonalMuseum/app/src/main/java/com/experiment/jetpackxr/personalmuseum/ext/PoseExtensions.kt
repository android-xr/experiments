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
package com.experiment.jetpackxr.personalmuseum.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import kotlin.Float

private object Constants {
    const val X_TRANSLATION = 0f
    const val X_ROTATION = 0f
    const val Z_ROTATION = 0f
}

/**
 * Converts an array of [Float] values into a [Pose].
 *
 * This method expects an array with at least three elements. The elements are used as follows:
 * - Element 0: Y-translation for the resulting [Pose].
 * - Element 1: Z-translation for the resulting [Pose].
 * - Element 2: Yaw rotation (around the Y-axis) for the resulting [Pose].
 *
 * @return A [Pose] constructed from the provided float values.
 * @throws IllegalArgumentException if the array contains fewer than 3 elements.
 */
fun Array<Float>.getPose(): Pose {
    if (this.size < 3) {
        throw IllegalArgumentException("Provide Y-translation, Z-translation & initYaw")
    }
    val modelTranslation = Vector3(Constants.X_TRANSLATION, this[0], this[1])
    val modelOrientation = Quaternion.fromEulerAngles(Constants.X_ROTATION, this[2], Constants.Z_ROTATION)
    return Pose(modelTranslation, modelOrientation)
}

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}
