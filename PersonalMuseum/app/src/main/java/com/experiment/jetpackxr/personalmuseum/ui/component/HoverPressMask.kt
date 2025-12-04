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
package com.experiment.jetpackxr.personalmuseum.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultFSMItemHoverColor
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultFSMItemPressedColor

/**
 * A Composable that applies a visual mask to its [content] based on hover and pressed
 * interaction states.
 *
 * This component observes an [InteractionSource] to determine if the content
 * is being hovered over or pressed, and displays a colored overlay accordingly.
 * It's useful for providing custom visual feedback for interactive elements
 * when `indication = null` is used in a `clickable` modifier.
 *
 * @param interactionSource The [InteractionSource] to observe for hover and pressed states.
 * @param hoverColor The [Color] to use for the overlay when the content is hovered.
 *                   Defaults to [defaultFSMItemHoverColor] with an alpha of 0.3f.
 * @param pressedColor The [Color] to use for the overlay when the content is pressed.
 *                     Defaults to [defaultFSMItemPressedColor] with an alpha of 0.6f.
 * @param content The Composable content to display beneath the mask.
 */
@Composable
fun HoverPressedMask(
    interactionSource: InteractionSource,
    hoverColor: Color = defaultFSMItemHoverColor.copy(alpha = 0.3f),
    pressedColor: Color = defaultFSMItemPressedColor.copy(alpha = 0.6f),
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    Box {
        // Draw the content first
        content()

        // Draw the mask on top based on interaction state
        if (isPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pressedColor)
            )
        } else if (isHovered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(hoverColor)
            )
        }
    }
}