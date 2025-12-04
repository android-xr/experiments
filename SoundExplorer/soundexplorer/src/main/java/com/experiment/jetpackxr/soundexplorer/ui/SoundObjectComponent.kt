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

import android.annotation.SuppressLint
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Component
import androidx.xr.scenecore.GroupEntity
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.runtime.Session
import androidx.xr.scenecore.InputEvent.Action
import androidx.xr.scenecore.scene
import com.experiment.jetpackxr.soundexplorer.core.GlbModel
import com.experiment.jetpackxr.soundexplorer.core.GlbModelRepository
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import kotlinx.coroutines.CoroutineScope
import java.util.function.Consumer

class SoundObjectComponent(
    val session : Session,
    val modelRepository : GlbModelRepository,
    val glbModel : GlbModel,
    val composition: SoundComposition,
    val coroutineScope: CoroutineScope
) : Component {

    companion object {
        fun createSoundObject(
            session : Session,
            modelRepository : GlbModelRepository,
            glbModel : GlbModel,
            composition: SoundComposition,
            coroutineScope: CoroutineScope
        ): SoundObjectComponent {
            // Create contentless wrapper entities for the sound object.
            // Entities must be created before spatial audio tracks are initialized. So, we defer
            // object initialization to initializeModelAndBehaviors().

            val manipulationEntity = GroupEntity.create(session, "ObjectManipEntity", Pose.Identity)

            manipulationEntity.parent = session.scene.activitySpace

            val soc = SoundObjectComponent(
                session, modelRepository, glbModel, composition, coroutineScope)

            manipulationEntity.addComponent(soc)

            soc.hidden = true

            return soc
        }
    }

    var onPropertyChanged: (() -> Unit)? = null

    var lowSoundId: Int? = null
    var highSoundId: Int? = null

    var lowSoundVolume: Float = 0.5f
        private set
    var highSoundVolume: Float = 0.5f
        private set

    private var isInitialized = false
    private var _entity: Entity? = null

    val entity: Entity get() {
        val e = this._entity
        if (e == null) {
            throw IllegalStateException("Tried to get hidden state on sound object when entity was detached!")
        }

        return e
    }

    var hidden : Boolean
        get() {
            val e = this._entity
            if (e == null) {
                throw IllegalStateException("Tried to get hidden state on sound object when entity was detached!")
            }

            return e.isEnabled()
        }
        set(value) {
            val e = this._entity
            if (e == null) {
                throw IllegalStateException("Tried to set hidden state on sound object when entity was detached!")
            }

            if (value == !e.isEnabled()){
                return
            }

            e.setEnabled(!value)
        }

    var isPlaying: Boolean = false
        private set(value) {
            if (field == value) {
                return
            }

            field = value

            this.onPropertyChanged?.invoke()
        }

    var onMovementStarted : (() -> Unit)? = null

    fun play() {
        this.isPlaying = true
        this.composition.updateSoundObjectPlayback(this)
    }

    fun stop() {
        this.isPlaying = false
        this.composition.updateSoundObjectPlayback(this)
    }

    fun setVolume(lowSoundVolume: Float, highSoundVolume: Float) {
        this.lowSoundVolume = lowSoundVolume.coerceIn(0.0f, 1.0f)
        this.highSoundVolume = highSoundVolume.coerceIn(0.0f, 1.0f)
        this.composition.updateSoundObjectPlayback(this)
    }

    @SuppressLint("RestrictedApi")
    override fun onAttach(entity: Entity): Boolean {
        this._entity = entity
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onDetach(entity: Entity) {
        this._entity = null
    }

    fun setPose(pose: Pose) {
        val e = this._entity
        if (e == null) {
            throw IllegalStateException("Tried to set translation on sound object when entity was detached!")
        }

        e.setPose(pose)
    }

    @SuppressLint("RestrictedApi")
    suspend fun initializeModelAndBehaviors() {
        if (this.isInitialized) {
            return
        }

        val e = checkNotNull(this._entity)

        val gltfModel = modelRepository
            .getOrLoadModel(coroutineScope, glbModel)
            .getOrNull()
        if (gltfModel == null) {
            throw IllegalArgumentException("Failed to load model " + glbModel.assetPath)
        }

        // Object Manipulation -> Local Programmatic Animation -> Model

        val animationEntity = GroupEntity.create(session, "AnimationEntity", Pose.Identity)
        val gltfModelEntity = GltfModelEntity.create(session, gltfModel)
        animationEntity.parent = e
        gltfModelEntity.parent = animationEntity

        val tapHandler = object : Consumer<InputEvent> {
            override fun accept(ie: InputEvent) {
                when (ie.action) {
                    Action.ACTION_UP -> {
                        gltfModelEntity.startAnimation(loop = false)

                        if (composition.state.value == SoundComposition.State.STOPPED) {
                            composition.stopAllSoundComponents()
                            play()
                            composition.play()
                        } else if (!isPlaying) {
                            play()
                        } else {
                            stop()
                        }
                    }
                }
            }
        }

        val simComponent = SimpleSimulationComponent(coroutineScope, {
                    e: Entity, dT: Double ->
                e.setPose(Pose(
                    e.getPose().translation,
                    e.getPose().rotation
                            * Quaternion.fromAxisAngle(Vector3.One, 40.0f * dT.toFloat() * lowSoundVolume)
                            * Quaternion.fromAxisAngle(Vector3.Right, 70.0f * dT.toFloat() * highSoundVolume)
                ))
            })

        gltfModelEntity.addComponent(simComponent)

        this.onPropertyChanged = {
            simComponent.paused = !isPlaying || composition.state.value != SoundComposition.State.PLAYING
        }

        gltfModelEntity.addComponent(InteractableComponent.create(
            session,
            session.activity.mainExecutor,
            SoundEntityMovementHandler(
                this,
                heightToChangeSound = 0.3f)))

        gltfModelEntity.addComponent(InteractableComponent.create(
            session,
            session.activity.mainExecutor,
            EntityMoveInteractionHandler(
                e,
                linearAcceleration = 2.0f,
                deadZone = 0.02f,
                onInputEventBubble = tapHandler,
                onMovementStarted =  { onMovementStarted?.invoke() } )))

        this.isInitialized = true
    }
}
