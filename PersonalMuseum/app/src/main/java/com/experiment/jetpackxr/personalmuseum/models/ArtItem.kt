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
package com.experiment.jetpackxr.personalmuseum.models

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.xr.runtime.math.Pose
import com.experiment.jetpackxr.personalmuseum.ext.getPose
import kotlinx.parcelize.Parcelize
import java.nio.file.Path

@Parcelize
data class ArtItem(
    val id: String,
    val title: String,
    val description: String,
    val modelPath: String,
    val modelName: String,
    val summary: String,
    val displayScale: Float,
    val translateY: Float,
    val translateZ: Float,
    val initYaw: Float,
    val thumbnailLarge: String,
    val thumbnailSmall: String,
    val collection: String,
    val isLarge: Boolean = false,
    @DrawableRes private var thumbnailLargeRedId: Int = 0,
    @DrawableRes private var thumbnailSmallRedId: Int = 0,
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is ArtItem && id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    val pose: Pose
        get() = arrayOf(translateY, translateZ, initYaw).getPose()

    val path: Path
        get() = Path.of(modelPath, modelName)

    fun getThumbnailLargeRedId(context: Context) =
        getResId(context, thumbnailLarge, thumbnailLargeRedId) { thumbnailLargeRedId = it }

    fun getThumbnailSmallRedId(context: Context) =
        getResId(context, thumbnailSmall, thumbnailSmallRedId) { thumbnailSmallRedId = it }
}

data class ArtCollection(
    val items: List<ArtItem>
)

@SuppressLint("DiscouragedApi")
private fun getResId(context: Context, name: String, cacheId: Int, setter: (Int) -> Unit): Int {
    if (cacheId == 0) {
        val drawableId = context.resources.getIdentifier(name, "drawable", context.packageName)
        setter(drawableId)
        return drawableId
    }

    return cacheId
}

fun List<ArtItem>.findById(id: String): ArtItem? = find { it.id == id }
fun List<ArtItem>.findIndexById(id: String): Int = indexOfFirst { it.id == id }
