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

package com.experiment.jetpackxr.sportscompanion.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconButtonColors
import androidx.compose.ui.graphics.Color

val Gray = Color(0xFFA4ADA4)
val MediumGray = Color(0xFF1C211C)
val DarkGray = Color(0xFF101510)

val EndExperienceButtonColors = ButtonColors(
    containerColor = Gray,
    contentColor = Color.White,
    disabledContainerColor = Color.Gray,
    disabledContentColor = Color.White
)

val HomeSpaceButtonColors = IconButtonColors(
    containerColor = DarkGray,
    contentColor = Color.White,
    disabledContainerColor = Color.Gray,
    disabledContentColor = Color.White
)
