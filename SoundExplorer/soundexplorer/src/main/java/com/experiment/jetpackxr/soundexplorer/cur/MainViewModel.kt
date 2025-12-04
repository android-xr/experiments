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

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.experiment.jetpackxr.soundexplorer.R
import com.experiment.jetpackxr.soundexplorer.sound.SoundComposition
import com.experiment.jetpackxr.soundexplorer.sound.SoundManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val soundManager: SoundManager,
    val soundComposition: SoundComposition
) : ViewModel() {
    // UI state to track if the dialog is showing
    private val _isDialogHidden = MutableStateFlow(true)
    val isDialogHidden = _isDialogHidden.asStateFlow()

    class DeleteAll(val value: Boolean = false)
    private val _deleteAll = MutableStateFlow(DeleteAll())
    val deleteAll = _deleteAll.asStateFlow()

    // Track which shapes are spawned in the environment
    private val _spawnedShapes = mutableStateListOf<Int>()
    val spawnedShapes: List<Int> = _spawnedShapes

    // Are there any shapes in the environment
    val hasSpawnedShapes: Boolean
        get() = _spawnedShapes.isNotEmpty()

    // List of all available shapes
    val shapeList = listOf(
        R.drawable.pumpod,
        R.drawable.pluff,
        R.drawable.pillowtri,
        R.drawable.swirlnut,
        R.drawable.twistbud,
        R.drawable.squube,
        R.drawable.bloomspire,
        R.drawable.cello,
        R.drawable.munchkin
    )

    var menuListener: MenuListener? = null

    // show tutorial arrow only once
    var isArrowVisible: Boolean = true

    interface MenuListener {
        fun onShapeClick(shapeIndex: Int)
        fun onRecallClick(shapeIndex: Int)
    }


    /** Spawn a shape into the environment */
    fun spawnShape(shapeIndex: Int) {
        if (!_spawnedShapes.contains(shapeIndex)) {
            _spawnedShapes.add(shapeIndex)
            menuListener?.onShapeClick(shapeIndex)
        }
    }

    /** Recall a shape from the environment */
    fun recallShape(shapeIndex: Int) {
        menuListener?.onRecallClick(shapeIndex)
        _spawnedShapes.remove(shapeIndex)
    }

    /** Restart shapes (recall all shapes) */
    fun restartShapes() {
        _spawnedShapes.clear()
    }

    /** Action to show dialog */
    fun toggleDialogVisibility() {
        _isDialogHidden.value = !_isDialogHidden.value
    }

    fun deleteAll() {
        _deleteAll.value = DeleteAll(true)
    }

    override fun onCleared() {
        super.onCleared()
        this.soundManager.close()
    }
}