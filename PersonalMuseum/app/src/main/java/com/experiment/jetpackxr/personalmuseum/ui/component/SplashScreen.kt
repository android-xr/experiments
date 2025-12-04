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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import com.experiment.jetpackxr.personalmuseum.R
import com.experiment.jetpackxr.personalmuseum.ui.theme.splashAnimationDelay
import com.experiment.jetpackxr.personalmuseum.ui.theme.splashAnimationSize
import com.experiment.jetpackxr.personalmuseum.ui.theme.splashFadeoutDuration
import com.experiment.jetpackxr.personalmuseum.ui.theme.splashScreenBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var shouldStartAnimation by remember { mutableStateOf(false) }
    var startFadeOut by remember { mutableStateOf(false) }
    var alpha = remember { Animatable(1f) }

    LaunchedEffect(startFadeOut) {
        if (startFadeOut) {
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
            .background(color = splashScreenBackground),
        contentAlignment = Alignment.Center
    ) {
        if (shouldStartAnimation) {
            AndroidView(
                modifier = Modifier
                    .size(splashAnimationSize),
                factory = { context ->
                    ImageView(context).apply {
                        val animation =
                            context.getDrawable(R.drawable.personal_museum_splash_animation) as? AnimatedVectorDrawable
                        setImageDrawable(animation)
                        animation?.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                postDelayed(
                                    {
                                        startFadeOut = true
                                    }, splashAnimationDelay)
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
            delay(splashAnimationDelay)
            shouldStartAnimation = true
        }
    }
}