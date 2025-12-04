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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CrossFadeText(
    text: String,
    duration: Int = 500,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier
) {
    var hasAppeared by remember { mutableStateOf(false) }

    //Animate alpha for initial fade-in
    val alpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(
            durationMillis = duration
        )
    )

    //Mark as appeared when first set
    LaunchedEffect(text) {
        if (!hasAppeared && text.isNotBlank()) {
            hasAppeared = true
        }
    }

    AnimatedContent(
        targetState = text,
        transitionSpec = {
            fadeIn(animationSpec = tween(durationMillis = duration)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = duration))
        },
        label = "CrossFadeText"
    ) { targetText ->

        Text(
            text = targetText,
            style = style,
            overflow = overflow,
            maxLines = maxLines,
            modifier = modifier.alpha(alpha)
        )
    }
}