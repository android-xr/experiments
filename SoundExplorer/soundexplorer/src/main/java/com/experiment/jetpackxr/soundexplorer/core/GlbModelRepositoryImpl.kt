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

import androidx.xr.runtime.Session
import androidx.xr.scenecore.GltfModel
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.Result


class GlbModelRepositoryImpl @Inject constructor(
    private val session: Session
) : GlbModelRepository {

    companion object {
        private const val TAG = "ModelRepImpl"
    }

    // Cache for ongoing loading jobs (Deferred ensures only one load per identifier)
    private val loadingJobs = ConcurrentHashMap<GlbModel, Deferred<Result<GltfModel>>>()
    // Cache for successfully loaded models
    private val loadedModels = ConcurrentHashMap<GlbModel, GltfModel>()

    override suspend fun getOrLoadModel(scope: CoroutineScope, modelIdentifier: GlbModel): Result<GltfModel> {
        // 1. Check memory cache for already loaded models
        loadedModels[modelIdentifier]?.let {
            return Result.success(it)
        }

        // 2. Check cache for ongoing loading jobs
        // computeIfAbsent ensures atomic creation and retrieval of the Deferred job
        val loadingJob = loadingJobs.computeIfAbsent(modelIdentifier) { identifier ->
            scope.async {
                try {
                    val model = GltfModel.create(session, identifier.assetPath)
                    // Cache the successfully loaded model
                    loadedModels[identifier] = model
                    Result.success(model)
                } catch (e: Exception) {
                    Result.failure(e) // Propagate the error
                } finally {
                    // Remove the job from the map once it's completed (success or failure)
                    loadingJobs.remove(identifier)
                }
            }
        }

        // 3. Await the result of the (potentially new) loading job
        return loadingJob.await()
    }

    override fun clear() {
        // Cancel any ongoing loading jobs
        loadingJobs.values.forEach { it.cancel("Repository cleared") }
        loadingJobs.clear()

        // Release resources associated with loaded models (if required by SceneCore API)
        loadedModels.values.forEach { model ->
            try {
                // // TODO: Check SceneCore API - Is explicit release needed for Model objects?
                // model.release()
            } catch (e: Exception) {
            }
        }
        loadedModels.clear()
    }
}