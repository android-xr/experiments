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
import com.google.common.truth.Truth.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.Result

@ExperimentalCoroutinesApi
class GlbModelRepositoryImplTest {

    @Mock
    private lateinit var mockSession: Session

    @Mock
    private lateinit var mockModel: GltfModel // A generic mock model

    private lateinit var repository: GlbModelRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = GlbModelRepositoryImpl(mockSession)
    }


    @Test
    fun `getOrLoadModel when model is already cached should return cached model`() = runTest {
        val modelIdentifier = GlbModel.Bloomspire

        // Use reflection to access and populate the private loadedModels cache
        val loadedModelsField = GlbModelRepositoryImpl::class.java.getDeclaredField("loadedModels")
        loadedModelsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val loadedModelsMap = loadedModelsField.get(repository) as java.util.concurrent.ConcurrentHashMap<GlbModel, GltfModel>
        loadedModelsMap[modelIdentifier] = mockModel

        val result = repository.getOrLoadModel(this, modelIdentifier)

        assertWithMessage("Result should be success").that(result.isSuccess).isTrue()
        assertThat(mockModel).isEqualTo(result.getOrNull())

        // Verify that no new loading job was created for this model
        val loadingJobsField = GlbModelRepositoryImpl::class.java.getDeclaredField("loadingJobs")
        loadingJobsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val loadingJobsMap = loadingJobsField.get(repository) as java.util.concurrent.ConcurrentHashMap<GlbModel, kotlinx.coroutines.Deferred<Result<GltfModel>>>

        assertWithMessage("No loading job should be present for this identifier")
            .that(loadingJobsMap)
            .doesNotContainKey(modelIdentifier)
    }

    @Test
    fun `clear should clear the loadedModels cache`() { // No runTest needed if no coroutines are directly launched by the test logic itself
        val modelIdentifier = GlbModel.Bloomspire

        // Use reflection to directly add a model to the loadedModels cache
        val loadedModelsField = GlbModelRepositoryImpl::class.java.getDeclaredField("loadedModels")
        loadedModelsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val loadedModelsMap = loadedModelsField.get(repository) as java.util.concurrent.ConcurrentHashMap<GlbModel, GltfModel>
        loadedModelsMap[modelIdentifier] = mockModel // mockModel is from setUp()

        assertWithMessage("Loaded models map should not be empty before clear")
            .that(loadedModelsMap)
            .isNotEmpty()

        repository.clear()

        assertWithMessage("Loaded models map should be empty after clear")
            .that(loadedModelsMap)
            .isEmpty()
    }
}