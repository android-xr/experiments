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

package com.experiment.jetpackxr.sportscompanion

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.concurrent.futures.await
import androidx.xr.compose.material3.ExperimentalMaterial3XrApi
import androidx.xr.compose.platform.LocalSession
import androidx.xr.compose.platform.LocalSpatialCapabilities
import androidx.xr.compose.spatial.ContentEdge
import androidx.xr.compose.spatial.Orbiter
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.ExperimentalSubspaceVolumeApi
import androidx.xr.compose.subspace.SpatialColumn
import androidx.xr.compose.subspace.SpatialCurvedRow
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.Volume
import androidx.xr.compose.subspace.layout.SpatialRoundedCornerShape
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.movable
import androidx.xr.compose.subspace.layout.padding
import androidx.xr.compose.subspace.layout.requiredWidth
import androidx.xr.compose.subspace.layout.resizable
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.sportscompanion.ui.theme.DarkGray
import com.experiment.jetpackxr.sportscompanion.ui.theme.HomeSpaceButtonColors
import com.experiment.jetpackxr.sportscompanion.ui.theme.MediumGray
import com.experiment.jetpackxr.sportscompanion.ui.theme.SportsCompanionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.xr.compose.spatial.OrbiterOffsetType
import androidx.xr.scenecore.AnchorPlacement
import androidx.xr.scenecore.MovableComponent
import androidx.xr.scenecore.PlaneOrientation
import androidx.xr.scenecore.PlaneSemanticType

private object Constants {
    const val SHOW_SECOND_CARD_TIME = 15
    const val SHOW_THIRD_CARD_TIME = 30
    const val END_TIME = 45
}

class MainActivity : ComponentActivity() {
    private lateinit var video: VideoData
    private val time: MutableState<Int> = mutableIntStateOf(0)
    private val showEndDialog: MutableState<Boolean> = mutableStateOf(false)
    private var terrainEntity: Entity? = null

    @OptIn(ExperimentalMaterial3XrApi::class)
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        val videoContext = this
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DisposableEffect(Unit) {
                video = createVideoPlayerView(videoContext)
                loadAndPlayVideo(video.player, R.raw.footage)

                onDispose {
                    video.dispose()
                }
            }
            val scope = rememberCoroutineScope()
            SportsCompanionTheme {
                StartTimer()
                if (LocalSpatialCapabilities.current.isSpatialUiEnabled) {
                    FullSpaceContent(scope)
                } else {
                    HomeSpaceContent()
                }

            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun FullSpaceContent(
        scope: CoroutineScope
    ) {
        Subspace {
            SpatialCurvedRow(
                modifier = SubspaceModifier.requiredWidth(3000.dp),
                curveRadius = 1024.dp
            ) {
                SpatialPanel(SubspaceModifier.height(440.dp).width(280.dp).movable()) {
                    Column(modifier = Modifier.background(DarkGray)) {

                        EventStatsTop(Modifier, time)
                        Leaderboard(
                            Modifier
                                .fillMaxHeight()
                                .padding(10.dp)
                                .clip(shape = RoundedCornerShape(24.dp)), riders
                        )
                    }
                }
                SpatialPanel(
                    SubspaceModifier.width(1024.dp).height(576.dp).padding(24.dp).movable()
                        .resizable()
                ) {
                    TerrainModel(scope)

                    Surface {
                        VideoPlayerView(video.view, Modifier.fillMaxSize())
                    }

                    TopOrbiter()
                    ShowEndExperienceDialogIfApplicable()
                }
                SpatialPanel(SubspaceModifier.width(280.dp).height(440.dp).movable()) {
                    InfoPanel()
                }
            }
        }
    }

    @Composable
    private fun TopOrbiter() {
        val session = LocalSession.current
        if (session == null) return
        Orbiter(
            position = ContentEdge.Top,
            offset = 20.dp,
            offsetType = OrbiterOffsetType.InnerEdge,
            alignment = Alignment.CenterHorizontally,
            shape = SpatialRoundedCornerShape(CornerSize(80.dp))
        ) {
            Surface(Modifier.clip(CircleShape)) {
                Row(
                    Modifier
                        .background(color = DarkGray)
                        .height(72.dp)
                        .width(758.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(56.dp))
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.mountain_bike_national_championships),
                        fontSize = 22.sp,
                        color = Color.White

                    )
                    Spacer(modifier = Modifier.weight(1f))
                    HomeSpaceModeIconButton(
                        onClick = {
                            terrainEntity?.setEnabled(false)
                            terrainEntity?.parent = session.scene.activitySpace
                            session.scene.requestHomeSpaceMode()
                        }, modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalSubspaceVolumeApi::class)
    @Composable
    fun TerrainModel(scope: CoroutineScope) {
        val session = LocalSession.current
        if (session == null) return

        Subspace {
            SpatialColumn {
                Volume {
                    scope.launch {
                        if (terrainEntity == null) {
                            val gltfModel = GltfModel.create(session, "model.glb".toUri())
                            val pose = Pose(Vector3(0f, -0.6f, 0f), Quaternion(0f, 0f, 0f, 1f))
                            val gltfEntity = GltfModelEntity.create(session, gltfModel, pose)
                            // Null animationName means it will play the first animation in the file.
                            // Since named animations are not supported by all glb exporters this can be helpful if you only need a single animation.
                            gltfEntity.startAnimation(loop = false)
                            gltfEntity.setScale(0.7f)

                            val component = InteractableComponent.create(
                                session, mainExecutor, EntityMoveInteractionHandler(
                                    gltfEntity,
                                    linearAcceleration = 2.0f,
                                    deadZone = 0.02f,
                                    onInputEventBubble = { })
                            )

                            gltfEntity.addComponent(component)
                            terrainEntity = gltfEntity
                        }
                        terrainEntity?.parent = it
                        terrainEntity?.setEnabled(true)
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun HomeSpaceContent() {
        val session = LocalSession.current ?: return
        Surface {
            HomeSpaceBody(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkGray)
            )
            Row(
                modifier = Modifier
                    .height(80.dp)
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FullSpaceModeIconButton(
                    onClick = { session.scene.requestFullSpaceMode() },
                    modifier = Modifier.size(40.dp)
                )
            }
            ShowEndExperienceDialogIfApplicable()
        }
    }

    @Composable
    private fun HomeSpaceBody(modifier: Modifier = Modifier) {
        Column {
            Row(
                modifier = Modifier
                    .background(MediumGray)
                    .height(80.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.mountain_bike_national_championships),
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(16.dp))
                            .background(MediumGray)
                    ) {
                        EventStatsTop(modifier, time)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    InfoPanel(modifier = Modifier.clip(shape = RoundedCornerShape(16.dp)))
                }
                Spacer(modifier = Modifier.width(24.dp))

                Column(
                    modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.End
                ) {
                    VideoPlayerView(
                        video.view,
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(shape = RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Timeline(Modifier)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    @Composable
    private fun InfoPanel(modifier: Modifier = Modifier) {
        when {
            time.value < Constants.SHOW_SECOND_CARD_TIME -> RiderDataPanel(modifier, riders[1])
            time.value < Constants.SHOW_THIRD_CARD_TIME -> MountainStatsPanel(modifier)
            else -> RiderDataPanel(modifier, riders[0])
        }
    }

    @Composable
    private fun Timeline(modifier: Modifier = Modifier) {
        Box {
            Image(
                painter = painterResource(R.drawable.timeline),
                contentDescription = null,
                modifier = Modifier
                    .height(99.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillHeight
            )
            AnimatedRiderPin(R.drawable.pin_tv, 400)
            AnimatedRiderPin(R.drawable.pin_jl, 450)
            AnimatedRiderPin(R.drawable.pin_gb, 480)
            AnimatedRiderPin(R.drawable.pin_cr, 500)
        }
    }

    @Composable
    private fun AnimatedRiderPin(image: Int, offset: Int) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .width(22.dp)
                .height(48.dp)
                .offset(x = (time.value + offset).dp, y = 32.dp),
            contentScale = ContentScale.FillBounds
        )
    }

    private fun resetExperience() {
        time.value = 0
        video.player.seekTo(0)
        video.player.play()
    }

    @Composable
    private fun StartTimer() {
        time.value = 0
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                val t = time.value
                if (t == Constants.END_TIME - 1) showEndDialog.value = true

                if (t < Constants.END_TIME) {
                    time.value++
                }
            }
        }
    }

    @Composable
    private fun ShowEndExperienceDialogIfApplicable() {
        if (showEndDialog.value) {
            EndDialog(
                onConfirmation = {
                    showEndDialog.value = false
                    resetExperience()
                })
        }
    }
}

@Composable
fun FullSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.fs_icon),
            contentDescription = stringResource(R.string.switch_to_full_space_mode)
        )
    }
}

@Composable
fun HomeSpaceModeIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalIconButton(onClick = onClick, modifier = modifier, colors = HomeSpaceButtonColors) {
        Icon(
            painter = painterResource(id = R.drawable.hs_icon),
            contentDescription = stringResource(R.string.switch_to_home_space_mode)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullSpaceModeButtonPreview() {
    SportsCompanionTheme {
        FullSpaceModeIconButton(onClick = {})
    }
}

@PreviewLightDark
@Composable
fun HomeSpaceModeButtonPreview() {
    SportsCompanionTheme {
        HomeSpaceModeIconButton(onClick = {})
    }
}