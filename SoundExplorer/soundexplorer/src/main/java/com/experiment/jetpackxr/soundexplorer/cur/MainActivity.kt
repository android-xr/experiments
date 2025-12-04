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
package com.experiment.jetpackxr.soundexplorer.cur

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.util.fastForEachIndexed
import androidx.xr.compose.subspace.layout.alpha
import androidx.xr.runtime.Config
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.ui.ArrowPanelController
import com.experiment.jetpackxr.soundexplorer.ui.RestartDialogContent
import com.experiment.jetpackxr.soundexplorer.ui.SoundObjectComponent
import com.experiment.jetpackxr.soundexplorer.ui.SplashScreen
import com.experiment.jetpackxr.soundexplorer.ui.theme.LocalSpacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var modelRepository: GlbModelRepository

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var arrowPanelController: ArrowPanelController
    private val viewModel: MainViewModel by viewModels()
    private var soundObjects: Array<SoundObjectComponent>? = null
    private var playOnResume: Boolean = false
    private var soundObjectsReady: Boolean by mutableStateOf(false)


    fun createSoundObjects(
        glbModels: Array<GlbModel>
    ): Array<SoundObjectComponent> {
        val soundObjs = Array<SoundObjectComponent?>(checkNotNull(glbModels).size) { null }
        for (i in soundObjs.indices) {
            soundObjs[i] = SoundObjectComponent.createSoundObject(
                session,
                modelRepository,
                glbModels[i],
                viewModel.soundComposition,
                lifecycleScope
            )
        }
        return soundObjs.map { o -> checkNotNull(o) }.toTypedArray()
    }

    suspend fun initializeSoundsAndCreateObjects() {
        if (this.soundObjectsReady) {
            return
        }

        this.soundObjects = createSoundObjects(GlbModel.allGlbAnimatedModels.toTypedArray())

        val loadSound = { i: Int ->
            val model = GlbModel.allGlbAnimatedModels[i]
            soundObjects?.get(i)?.let { soundObject ->
                soundObject.lowSoundId = checkNotNull(
                    viewModel.soundManager.loadSound(
                        session,
                        soundObject.entity,
                        model.lowSoundResourceId
                    )
                )
                soundObject.highSoundId = checkNotNull(
                    viewModel.soundManager.loadSound(
                        session,
                        soundObject.entity,
                        model.highSoundResourceId
                    )
                )
            }
        }

        // Sounds are loaded and played in a specific order to prioritize the relative
        // synchronization of more syncopated sounds.
        GlbModel.allGlbAnimatedModels.fastForEachIndexed { index, glbModel ->
            loadSound(index)
        }

        val startTimeToPlaySounds = System.nanoTime()

        // Start playing all sounds at the same time.
        this.viewModel.soundManager.playAllSounds()

        // Log the time spent on calls to play sounds. Sounds are intended to start playback at
        // precisely the same time and any delay will negatively impact how well they align.
        val timeToPlaySounds = (System.nanoTime() - startTimeToPlaySounds) * 0.000000001
        Log.d("SOUNDEXPLOG", "Time to play sounds - $timeToPlaySounds seconds.")

        for (soundObj in checkNotNull(soundObjects)) {
            soundObj.initializeModelAndBehaviors()
            this.viewModel.soundComposition.registerSoundObject(soundObj)
        }

        // play the composition by default
        this.viewModel.soundComposition.play()

        this.soundObjectsReady = true
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session.scene.mainPanelEntity.setEnabled(false)
        session.configure(Config(headTracking = Config.HeadTrackingMode.LAST_KNOWN))

        lifecycleScope.launch { initializeSoundsAndCreateObjects() }

        setContent {

            Subspace {
                val isDialogHidden = viewModel.isDialogHidden.collectAsState()
                val alpha = remember { Animatable(0f) }
                var startFadeIn by remember { mutableStateOf(false) }

                LaunchedEffect(startFadeIn && soundObjectsReady) {
                    if (startFadeIn && soundObjectsReady) {
                        alpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 900)
                        )
                    }
                }

                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(200.dp)
                        .height(200.dp)
                        .offset(z = 200.dp)
                ) {
                    SplashScreen(
                        onFadeOut = { startFadeIn = true },
                        onFinished = { },
                        contentLoaded = soundObjectsReady
                    )
                }

                SpatialPanel(
                    modifier = SubspaceModifier
                        .width(1000.dp)
                        .height(190.dp)
                        .offset(z = 200.dp, y = (-200).dp)
                        .rotate(-20f, 0f, 0f)
                        .alpha(alpha = alpha.value)
                ) {
                    ShapeAppScreen()
                }

                if (!isDialogHidden.value) {
                    SpatialPanel(
                        modifier = SubspaceModifier
                            .width(400.dp)
                            .height(290.dp)
                            .offset(y = 250.dp)
                            .movable()
                    ) {
                        RestartDialogContent(
                            modifier = Modifier
                                .width(400.dp)
                                .height(300.dp)
                                .padding(top = LocalSpacing.current.xxl)
                        )
                    }
                }
            }
        }

        viewModel.menuListener = object : MainViewModel.MenuListener {
            override fun onShapeClick(shapeIndex: Int) {
                val initialLocation = checkNotNull(session.scene.spatialUser.head).transformPoseTo(
                    Pose(Vector3.Forward * 1.0f, Quaternion.Identity),
                    session.scene.activitySpace
                )

                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.setPose(initialLocation)
                soundObject.hidden = false
                soundObject.play()

                if (viewModel.isArrowVisible) {
                    viewModel.isArrowVisible = false
                    arrowPanelController.showArrows(soundObject.entity) {
                        // This callback can be used if SoundObjectComponent needs to inform ArrowPanelController about movement.
                        // For now, direct hiding from ArrowPanelController's timeout or other logic.
                    }
                    // The onMovementStarted logic from the original code is now partially handled within showArrows (timeout)
                    // and via direct calls to hideArrows if needed from other parts of the app.
                    // If SoundObjectComponent has specific movement detection, it could call arrowPanelController.hideArrows()
                    soundObject.onMovementStarted =
                        { // This is if the sound object itself detects movement and needs to hide arrows
                            arrowPanelController.hideArrows()
                        }
                }

            }

            override fun onRecallClick(shapeIndex: Int) {
                val soundObject = checkNotNull(soundObjects)[shapeIndex]
                soundObject.stop()
                soundObject.hidden = true
            }
        }

        lifecycleScope.launch {
            viewModel.deleteAll.collect { event ->
                if (event.value) {
                    soundObjects?.forEach {
                        it.stop()
                        it.hidden = true
                    }
                    viewModel.toggleDialogVisibility() // switch visibility
                    viewModel.restartShapes()
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()

        playOnResume = viewModel.soundComposition.stop()
    }

    override fun onResume() {
        super.onResume()

        if (playOnResume) {
            viewModel.soundComposition.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        arrowPanelController.destroy()
    }
}