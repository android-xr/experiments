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
package com.experiment.jetpackxr.personalmuseum.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.xr.compose.spatial.Subspace
import androidx.xr.compose.subspace.SpatialPanel
import androidx.xr.compose.subspace.layout.SubspaceModifier
import androidx.xr.compose.subspace.layout.height
import androidx.xr.compose.subspace.layout.offset
import androidx.xr.compose.subspace.layout.rotate
import androidx.xr.compose.subspace.layout.width
import androidx.xr.runtime.Session
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.Scene
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.personalmuseum.R
import com.experiment.jetpackxr.personalmuseum.data.ArtDataRepository
import com.experiment.jetpackxr.personalmuseum.environment.EnvironmentController
import com.experiment.jetpackxr.personalmuseum.ext.createModel
import com.experiment.jetpackxr.personalmuseum.ext.customEnvironmentEnabled
import com.experiment.jetpackxr.personalmuseum.ext.getPose
import com.experiment.jetpackxr.personalmuseum.models.ArtItem
import com.experiment.jetpackxr.personalmuseum.models.findById
import com.experiment.jetpackxr.personalmuseum.ui.component.ArtsLazyRow
import com.experiment.jetpackxr.personalmuseum.ui.component.CrossFadeText
import com.experiment.jetpackxr.personalmuseum.ui.theme.PersonalMuseumTheme
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultHeaderBackground
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultPanelBackground
import com.experiment.jetpackxr.personalmuseum.ui.theme.fullscreenPanelYOffset
import com.experiment.jetpackxr.personalmuseum.ui.theme.fullscreenPanelZOffset
import com.experiment.jetpackxr.personalmuseum.ui.theme.pedestalDefaultScale
import com.experiment.jetpackxr.personalmuseum.ui.theme.pedestalYOffset
import com.experiment.jetpackxr.personalmuseum.util.EntityYAxisRotationHandler
import com.experiment.jetpackxr.personalmuseum.util.RunTimeSessionHelper
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FullSpaceActivity: ComponentActivity() {

    companion object {
        const val EXTRA_ART_ID = "art_id"
        private const val ENVIRONMENT_FILES_PATH = "environment"
        private const val PEDESTAL_FILE_NAME = "museum_pedestal.glb"
        private const val ENVIRONMENT_MODEL_NAME = "museum_environment.glb"
        private const val TAG = "ArtModelContent"
    }

    private var artModelEntity: GltfModelEntity? = null
    private var pedestalEntity: GltfModelEntity? = null
    private val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionHelper = RunTimeSessionHelper(
            onCreateCallback = { runtimeSession ->
                setContent {
                    PersonalMuseumTheme {
                        val artId = intent.getStringExtra(EXTRA_ART_ID)
                        artId?.let { id ->
                            ArtDataRepository.artItems.findById(id) }?.let { artItem ->
                            FullSpaceContent(artItem, runtimeSession)
                        }
                    }
                }
            }
        )
        lifecycle.addObserver(sessionHelper)
    }

    @Composable
    fun FullSpaceContent(art: ArtItem, runTimeSession: Session) {
        var selectedArt by remember { mutableStateOf<ArtItem?>(art) }

        ArtModelContent(selectedArt, runTimeSession)

        InfoPanelContent(selectedArt, onItemClick = {
            if (it != selectedArt) {
                selectedArt = it
            }
        })
        IconPanel(runTimeSession.scene)

        LaunchedEffect(Unit) {
            EnvironmentController.loadModelAssetAsync(runTimeSession, ENVIRONMENT_FILES_PATH, ENVIRONMENT_MODEL_NAME)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    fun ArtModelContent(art: ArtItem?, runTimeSession: Session) {
        val modelRoot: Entity = runTimeSession.scene.activitySpace
        var isLoading by remember { mutableStateOf(true)}
        Log.d(TAG, "invoked with art: ${art?.modelName}")

        Subspace {
            LaunchedEffect(art?.modelName) {
                art?.let {
                    artModelEntity?.setEnabled(false)
                    pedestalEntity?.setEnabled(false)
                    // Create the art entity
                    isLoading = true
                    Log.d(TAG, "Start create gltf model ${art.modelName}")
                    val artGltfModel = runTimeSession.createModel(art.path)
                    artModelEntity = artGltfModel?.let {
                        artModel ->
                            Log.d(TAG, "Start create model entity ${art.modelName}")
                            GltfModelEntity.create(runTimeSession, artModel)
                    }

                    artModelEntity?.apply {
                        setPose(art.pose)
                        setScale(art.displayScale)
                        val modelInteractable = InteractableComponent.create(
                            runTimeSession, executor,
                            EntityYAxisRotationHandler(
                                this,
                                linearToAngularMovementScalar = 200.0f
                            ) { entity, yRotation ->
                            })
                        addComponent(modelInteractable)
                        parent = modelRoot
                    }

                    artModelEntity?.setEnabled(true)

                    // Create museum pedestal entity
                    pedestalEntity = pedestalEntity ?: run {
                        val pedGlbModel = runTimeSession.createModel(Path.of(ENVIRONMENT_FILES_PATH, PEDESTAL_FILE_NAME))
                        val entity: GltfModelEntity? = pedGlbModel?.let {
                            GltfModelEntity.create(runTimeSession, it)
                        }?.apply {
                            setScale(pedestalDefaultScale)
                        }
                        entity
                    }
                    pedestalEntity?.let {
                        it.setEnabled(!art.isLarge)
                        it.setPose(arrayOf(pedestalYOffset, art.translateZ, 0f).getPose())
                    }
                }

                isLoading = false
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }

    @Composable
    fun LoadingOverlay(isLoading: Boolean) {
        Subspace {
            SpatialPanel(
                SubspaceModifier
                    .height(50.dp)
                    .width(50.dp)
                    .offset(x = 0.dp, y = 280.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .clickable(enabled = false) {}
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun IconPanel(scene: Scene) {
        val activity = LocalActivity.current
        var isCustomEnvironmentEnabled by remember {
            mutableStateOf(scene.customEnvironmentEnabled())
        }

        Subspace {
            SpatialPanel(
                modifier = SubspaceModifier
                    .height(156.dp)
                    .width(96.dp)
                    .rotate(-15f, 0f, 0f)
                    .offset(x = (-300).dp, y = fullscreenPanelYOffset, z = fullscreenPanelZOffset),
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(30.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .background(color = defaultPanelBackground),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                val environmentModelName =
                                    if (isCustomEnvironmentEnabled) {
                                        // Turn off custom environment
                                        null
                                    } else {
                                        // Turn on custom environment
                                        ENVIRONMENT_MODEL_NAME
                                    }
                                EnvironmentController.requestCustomEnvironment(
                                    scene,
                                    environmentModelName
                                )
                                isCustomEnvironmentEnabled = !isCustomEnvironmentEnabled
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isCustomEnvironmentEnabled) {
                                    R.drawable.ic_environment_enabled
                                } else {
                                    R.drawable.ic_environment_disabled
                                }),
                                contentDescription = "Museum Mode"
                            )
                        }

                        IconButton(
                            onClick = {
                                EnvironmentController.clearCustomEnvironment(scene)
                                activity?.finish()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_homespace),
                                contentDescription = "Home Space"
                            )
                        }
                    }
                }
            }
        }

    }

    @Composable
    fun InfoPanelContent(art: ArtItem?,
                         onItemClick: (ArtItem) -> Unit) {
        Subspace {
            SpatialPanel(
                SubspaceModifier
                    .height(285.dp)
                    .width(500.dp)
                    .rotate(-15f, 0f, 0f)
                    .offset(x = 0.dp, y = fullscreenPanelYOffset, z = fullscreenPanelZOffset)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Column(
                        modifier = Modifier.background(color = defaultHeaderBackground),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        ArtsLazyRow(
                            items = ArtDataRepository.artItems,
                            selectedItem = art,
                            onItemClick = onItemClick,
                            modifier = Modifier.fillMaxWidth()
                                    .height(80.dp)
                        )

                        art?.let {
                            Card (modifier = Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = defaultPanelBackground)
                            ) {
                                Column(
                                    modifier = Modifier.background(color = defaultPanelBackground).padding(start = 36.dp, end = 36.dp, top = 30.dp),
                                    horizontalAlignment = Alignment.Start,
                                ) {
                                    CrossFadeText(
                                        text = it.title,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    CrossFadeText(
                                        text = it.summary,
                                        style = MaterialTheme.typography.labelSmall
                                    )

                                    Spacer(modifier = Modifier.height(22.dp))

                                    CrossFadeText(
                                        text = it.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        duration = 1000,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}