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

import androidx.xr.scenecore.Component
import androidx.xr.scenecore.Entity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class SimpleSimulationComponent (
    val coroutineScope: CoroutineScope,
    val updateFn: (entity: Entity, deltaTime: Double) -> Unit) : Component {

    // must be accessed on same thread that executes simulationLoop
    var paused: Boolean = false
        set(value) {
            if (field == value) {
                return
            }

            if (!value) {
                this.lastUpdateTimeNs = -1
            }

            field = value
        }

    private var entity: Entity? = null
    private var enabled = AtomicBoolean(false)
    private var lastUpdateTimeNs: Long = -1
    private var simulationCr: Job? = null

    override fun onAttach(entity: Entity): Boolean {
        this.entity = entity
        this.enabled.set(true)
        this.simulationCr = coroutineScope.launch (Dispatchers.Main) { simulationLoop() }
        return true
    }

    override fun onDetach(entity: Entity) {
        this.enabled.set(false)
        val cr = this.simulationCr
        if (cr != null) {
            runBlocking { cr.cancelAndJoin() }
        }
        lastUpdateTimeNs = -1
        this.simulationCr = null
        this.entity = null
    }

    suspend fun simulationLoop() {
        while (this.enabled.get()) {
            if (this.paused) {
                delay(16)
                continue
            }

            val currentTimeNs = System.nanoTime()

            if (lastUpdateTimeNs < 0) {
                lastUpdateTimeNs = currentTimeNs
                delay(16)
                continue
            }

            val deltaTimeNs = currentTimeNs - lastUpdateTimeNs
            val deltaTimeS = deltaTimeNs.toDouble() * 0.000000001

            this.updateFn(checkNotNull(this.entity), deltaTimeS)

            lastUpdateTimeNs = currentTimeNs

            delay(16) // todo - implement as a catchup or do something more than just sleep 16ms
        }
    }
}