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
package com.experiment.jetpackxr.personalmuseum.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.experiment.jetpackxr.personalmuseum.R
import com.experiment.jetpackxr.personalmuseum.data.ArtDataRepository
import com.experiment.jetpackxr.personalmuseum.ext.toPx
import com.experiment.jetpackxr.personalmuseum.models.ArtItem
import com.experiment.jetpackxr.personalmuseum.ui.component.ArtCard
import com.experiment.jetpackxr.personalmuseum.ui.component.SplashScreen
import com.experiment.jetpackxr.personalmuseum.ui.theme.PersonalMuseumTheme
import com.experiment.jetpackxr.personalmuseum.ui.theme.defaultPanelBackground
import com.experiment.jetpackxr.personalmuseum.ui.theme.listPadding
import com.experiment.jetpackxr.personalmuseum.ui.theme.topBarHeight

class HomeSpaceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalMuseumTheme {
                MainContentWithSplash()
            }
        }
    }
}

@Composable
fun MainContentWithSplash() {
    var showSplash by remember {mutableStateOf(true)}

    HomeSpaceContent(arts = ArtDataRepository.artItems)
    if (showSplash) {
        SplashScreen({ showSplash = false })
    }
}

@Composable
fun HomeSpaceContent(arts: List<ArtItem>) {
    val topBarHeightPx = topBarHeight.toPx()
    var topBarOffsetY by remember { mutableStateOf(0f) }
    val topBarVisibleFraction = 1f + (topBarOffsetY / topBarHeightPx)
    val nestedScrollConnection = remember {
        object: NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                val newOffset = topBarOffsetY + delta
                topBarOffsetY = newOffset.coerceIn(-topBarHeightPx, 0f)
                return Offset.Zero

            }
        }
    }

    Surface {
        Column(
            modifier = Modifier.fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .background(color = defaultPanelBackground),
            verticalArrangement = Arrangement.Top,
        ) {
            // Appbar for home screen
            Box (
                modifier = Modifier.fillMaxWidth()
                    .height(topBarHeight * topBarVisibleFraction)
                    .graphicsLayer {
                        translationY = topBarOffsetY
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.home_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = listPadding,
                    end = listPadding,
                    bottom = listPadding),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(arts.size) { art ->
                    ArtCard(artItem = arts.get(art))
                }
            }
        }
    }
}