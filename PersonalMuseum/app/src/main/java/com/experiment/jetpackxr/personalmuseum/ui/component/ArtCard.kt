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
package com.experiment.jetpackxr.personalmuseum.ui.component

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.experiment.jetpackxr.personalmuseum.activities.FullSpaceActivity
import com.experiment.jetpackxr.personalmuseum.models.ArtItem
import com.experiment.jetpackxr.personalmuseum.ui.theme.cardViewCorner
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultHSMItemHoverColor
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultHSMItemPressedColor
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultPanelBackground

@SuppressLint("DiscouragedApi")
@Composable
fun ArtCard(artItem: ArtItem) {

    val context = LocalContext.current
    val drawableId = artItem.getThumbnailLargeRedId(context)
    // Collect the interaction states
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .size(280.dp, 270.dp)
            .clip(RoundedCornerShape(cardViewCorner))
            .clickable(
                interactionSource = interactionSource,
                /* Disable the default ripple effect */
                indication = null,
                onClick = {
                    val intent = Intent(context, FullSpaceActivity::class.java)
                        .putExtra(FullSpaceActivity.EXTRA_ART_ID, artItem.id)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }),
        colors = CardDefaults.cardColors(containerColor = defaultPanelBackground),
    ) {
        HoverPressedMask (
            interactionSource = interactionSource,
            hoverColor = defaultHSMItemHoverColor.copy(alpha = 0.3f),
            pressedColor = defaultHSMItemPressedColor.copy(alpha = 0.6f),
        ) {
            Column {
                Card (
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ){
                    Image(
                        painter = painterResource(id = drawableId),
                        contentDescription = artItem.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color.Transparent)) {
                    Text(artItem.title, style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(artItem.summary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}