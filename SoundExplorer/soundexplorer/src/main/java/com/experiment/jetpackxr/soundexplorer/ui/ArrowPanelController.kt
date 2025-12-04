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

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.IntSize2d
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.PanelEntity
import com.experiment.jetpackxr.soundexplorer.R
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class ArrowPanelController @Inject constructor(
    private val session: Session,
    @ActivityContext private val context: Context
) {

    private lateinit var arrowTopPanel: PanelEntity
    private lateinit var topArrowAnimation: AnimatorSet
    private lateinit var arrowBottomPanel: PanelEntity
    private lateinit var bottomArrowAnimation: AnimatorSet
    private lateinit var timeoutHandler: Handler

    init {
        initArrowPanel()
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    private fun initArrowPanel() {
        val layoutInflater = LayoutInflater.from(context)
        val topArrowView = layoutInflater.inflate(R.layout.top_arrow, null, false)
        val bottomArrowView = layoutInflater.inflate(R.layout.bottom_arrow, null, false)

        arrowTopPanel = PanelEntity.create(
            session = session,
            view = topArrowView,
            pixelDimensions = IntSize2d(220, 350),
            name = "Top Arrow",
            pose = Pose(Vector3(0f, 0f, 0f))
        )
        arrowTopPanel.setEnabled(false)

        arrowBottomPanel = PanelEntity.create(
            session = session,
            view = bottomArrowView,
            pixelDimensions = IntSize2d(220, 350),
            name = "Bottom Arrow",
            pose = Pose(Vector3(0f, 0f, 0f))
        )
        arrowBottomPanel.setEnabled(false)

        topArrowAnimation = AnimatorInflater.loadAnimator(context, R.animator.top_arrow_animation) as AnimatorSet
        topArrowAnimation.setTarget(topArrowView.findViewById(R.id.animated_image))

        bottomArrowAnimation = AnimatorInflater.loadAnimator(context, R.animator.bottom_arrow_animation) as AnimatorSet
        bottomArrowAnimation.setTarget(bottomArrowView.findViewById(R.id.animated_image))

        topArrowAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                topArrowAnimation.start()
            }
        })

        bottomArrowAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                bottomArrowAnimation.start()
            }
        })

        timeoutHandler = Handler(Looper.getMainLooper())
    }

    @SuppressLint("RestrictedApi")
    @Suppress("unused")
    fun showArrows(targetEntity: Entity, onMovementStarted: () -> Unit) {
        arrowTopPanel.setEnabled(true)
        arrowBottomPanel.setEnabled(true)

        arrowTopPanel.parent = targetEntity
        arrowTopPanel.setScale(0.5f)
        arrowTopPanel.setPose(arrowTopPanel.getPose().translate(Vector3.Up * 0.1f))

        arrowBottomPanel.parent = targetEntity
        arrowBottomPanel.setScale(0.5f)
        arrowBottomPanel.setPose(arrowBottomPanel.getPose().translate(Vector3.Down * 0.1f))

        // Call onMovementStarted when appropriate (this might need adjustment based on actual movement detection)
        // For now, let's assume it's called when arrows are shown or some interaction happens.
        // This part might need to be triggered from the SoundObjectComponent or MainActivity
        // depending on how movement is detected.

        timeoutHandler.removeCallbacksAndMessages(null) // Clear previous timeouts
        timeoutHandler.postDelayed({
            hideArrows()
        }, 10000) // Hide after 10 seconds

        // Start the animation with a delay
        timeoutHandler.postDelayed({
            if (!topArrowAnimation.isStarted) topArrowAnimation.start()
            if (!bottomArrowAnimation.isStarted) bottomArrowAnimation.start()
        }, 700)
    }

    @SuppressLint("RestrictedApi")
    fun hideArrows() {
        arrowTopPanel.setEnabled(false)
        arrowBottomPanel.setEnabled(false)
        if (topArrowAnimation.isStarted) topArrowAnimation.pause()
        if (bottomArrowAnimation.isStarted) bottomArrowAnimation.pause()
    }

    @SuppressLint("RestrictedApi")
    fun destroy() {
        timeoutHandler.removeCallbacksAndMessages(null)
        topArrowAnimation.removeAllListeners()
        topArrowAnimation.cancel()
        bottomArrowAnimation.removeAllListeners()
        bottomArrowAnimation.cancel()
        // Potentially detach panels from parent if they are still attached
        if (arrowTopPanel.parent != null) arrowTopPanel.parent = null
        if (arrowBottomPanel.parent != null) arrowBottomPanel.parent = null
    }
}