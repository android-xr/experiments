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
package com.experiment.jetpackxr.soundexplorer.core

import androidx.xr.scenecore.GltfModel
import kotlinx.coroutines.CoroutineScope


/**
 * Interface for accessing 3D model data.
 * Abstracts the data source (SceneCoreSession, network, etc.) and caching.
 */
interface GlbModelRepository {

    /**
     * Gets a model by its identifier, loading it if necessary.
     * Handles caching and concurrent requests.
     *
     * @param scope CoroutineScope to launch the loading coroutine.
     * @param modelIdentifier Unique identifier (e.g., asset path "glb/bloomspire_animated.glb").
     * @return Result<Model> containing the loaded Model on success or an exception on failure.
     * @throws RuntimeException on error.
     */
    suspend fun getOrLoadModel(scope: CoroutineScope, modelIdentifier: GlbModel): Result<GltfModel>

    /**
     * Clears caches and releases resources held by the repository.
     * Should be called when the repository is no longer needed (e.g., ViewModel onCleared).
     */
    fun clear()
}