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

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.experiment.jetpackxr.soundexplorer.R

@Composable
fun SplashScreen(
    onFadeOut: () -> Unit,
    onFinished: () -> Unit,
    contentLoaded: Boolean
) {
    var shouldStartAnimation by remember { mutableStateOf(false) }
    var startFadeOut by remember { mutableStateOf(false) }
    var alpha = remember { Animatable(1f) }
    val splashFadeoutDuration = 900

    LaunchedEffect(startFadeOut && contentLoaded) {
        if (startFadeOut && contentLoaded) {
            onFadeOut()
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = splashFadeoutDuration)
            )
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = alpha.value)
            .background(color = Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (shouldStartAnimation) {
            AndroidView(
                modifier = Modifier.size(512.dp),
                factory = { context ->
                    ImageView(context).apply {
                        val animation =
                            context.getDrawable(R.drawable.loader_3_sound_explorer) as? AnimatedVectorDrawable
                        setImageDrawable(animation)
                        animation?.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                animation.start()
                                // Set startFadeOut on animation end to ensure that the animation plays at least once.
                                post { startFadeOut = true }
                            }
                        })
                        animation?.start()
                    }
                }
            )
        }

        // Delay animation until first frame is rendered
        LaunchedEffect(Unit) {
            withFrameNanos {}
            shouldStartAnimation = true
        }
    }
}