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
package com.experiment.jetpackxr.soundexplorer.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("SpellCheckingInspection")
data class Spacing(
    val xxs : Dp    = 2.dp,
    val xs : Dp     = 4.dp,
    val s : Dp      = 8.dp,
    val m : Dp      = 16.dp,
    val l : Dp      = 24.dp,
    val xl : Dp     = 32.dp,
    val xxl : Dp    = 48.dp,
    val xxxl : Dp   = 72.dp,
    val xxxxl : Dp  = 96.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }