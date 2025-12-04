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

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.experiment.jetpackxr.personalmuseum.models.ArtItem
import com.experiment.jetpackxr.personalmuseum.ui.theme.ItemBorderSelected

@Composable
fun ArtsLazyRow(
    items: List<ArtItem>,
    selectedItem: ArtItem?,
    onItemClick: (ArtItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedItem) {
        selectedItem?.let {
            val index = items.indexOf(it)
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyRow (
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = true
    ) {
        items(items.count()) { index ->
            val art = items[index]
            CarouselItem(art, art == selectedItem, onItemClick)
        }
    }
}

@Composable
fun CarouselItem(art: ArtItem, isSelected: Boolean, onItemClick: (ArtItem) -> Unit) {
    // Collect the interaction states
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .width(99.dp)
            .height(45.dp)
            .clip(RoundedCornerShape(35.dp))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) ItemBorderSelected else Color.Transparent,
                shape = RoundedCornerShape(35.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                /*Disable the default ripple effect*/
                indication = null,
                onClick = { onItemClick(art) }
            ),
    ) {
        HoverPressedMask (
            interactionSource = interactionSource,
        ) {
            val drawableId = art.getThumbnailSmallRedId(LocalContext.current)
            Image(
                painter = painterResource(id = drawableId),
                contentDescription = art.title,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}