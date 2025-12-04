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
package com.experiment.jetpackxr.personalmuseum.data

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.google.gson.Gson
import com.experiment.jetpackxr.personalmuseum.R
import com.experiment.jetpackxr.personalmuseum.models.ArtCollection
import com.experiment.jetpackxr.personalmuseum.models.ArtItem
import java.io.InputStreamReader

object ArtDataRepository {
    private const val TAG = "ArtDataRepository"
    private val _artItems: MutableList<ArtItem> = mutableListOf()
    val artItems: List<ArtItem> = _artItems

    fun init(context: Context) {
        val gson = Gson()
        val type = object : com.google.gson.reflect.TypeToken<ArtCollection>() {}.type

        try {
            context.resources.openRawResource(R.raw.items).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    gson.fromJson<ArtCollection>(reader, type).items.let { items ->
                    _artItems.clear()
                    _artItems.addAll(items)
                }}
            }
        } catch (ignore: Resources.NotFoundException) {
            Log.e(TAG, "Failed to load art data.", ignore)
        }
    }
}