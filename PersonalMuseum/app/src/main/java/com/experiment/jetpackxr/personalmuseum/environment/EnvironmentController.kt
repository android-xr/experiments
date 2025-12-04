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
package com.experiment.jetpackxr.personalmuseum.environment

import android.util.Log
import androidx.xr.scenecore.ExrImage
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.SpatialEnvironment
import androidx.xr.scenecore.Scene
import androidx.xr.runtime.Session
import java.nio.file.Path

object EnvironmentController {
    private const val TAG = "EnvironmentController"
    private val assetCache: HashMap<String, Any> = HashMap()
    private var activeEnvironmentModelName: String? = null

    /**
     * Request the system load a custom Environment
     */
    fun requestCustomEnvironment(
        xrScene: Scene,
        environmentModelName: String? = null,
        environmentImageName: String? = null
    ) {
        try {
            if (activeEnvironmentModelName != environmentModelName) {
                val environmentModel = environmentModelName?.let {assetCache[environmentModelName] as GltfModel}
                val environmentImage: ExrImage? = environmentImageName?.let { assetCache[it] as ExrImage? }

                SpatialEnvironment.SpatialEnvironmentPreference(
                    skybox = environmentImage,
                    geometry = environmentModel
                ).let {
                    xrScene.spatialEnvironment.preferredSpatialEnvironment = it
                }
                activeEnvironmentModelName = environmentModelName
            }
            xrScene.spatialEnvironment.preferredPassthroughOpacity = 0f
        } catch (e: Exception){
            Log.e(TAG, "Failed to update Environment Preference for $environmentModelName: $e")
        }
    }

    fun clearCustomEnvironment(xrScene: Scene) {
        activeEnvironmentModelName = null
        xrScene.spatialEnvironment.preferredSpatialEnvironment = null
    }

    suspend fun loadModelAssetAsync(runTimeSession: Session, envModelPath: String, envModelName: String) {
        if (!assetCache.containsKey(envModelName)) {
            try {
                val gltfModel = GltfModel.create(runTimeSession, Path.of(envModelPath, envModelName))
                assetCache[envModelName] = gltfModel
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load $envModelPath: $e")
            }
        }
    }
}