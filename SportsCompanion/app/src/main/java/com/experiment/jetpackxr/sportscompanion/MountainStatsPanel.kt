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

package com.experiment.jetpackxr.sportscompanion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.experiment.jetpackxr.sportscompanion.ui.theme.DarkGray
import com.experiment.jetpackxr.sportscompanion.ui.theme.Gray
import com.experiment.jetpackxr.sportscompanion.ui.theme.MediumGray

@Composable
fun MountainStatsPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier
            .fillMaxHeight()
            .width(280.dp)
    )
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MediumGray)
        )
        {
            Image(
                painter = painterResource(R.drawable.mountain),
                contentDescription = null,
                modifier = Modifier
                    .width(148.dp)
                    .height(200.dp)
                    .offset(y = 111.dp, x = 108.dp),
            )
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start
            )
            {
                Text(
                    text = stringResource(R.string.gravity_mountain),
                    fontSize = 24.sp,
                    color = Color.White,
                )
                Text(
                    text = "C3-C4",
                    fontSize = 24.sp,
                    color = Gray
                )

                Spacer(modifier = Modifier.height(110.dp))
                CheckpointDataRow(stringResource(R.string.distance), "9.3", " km")
                CheckpointDataRow(stringResource(R.string.climb), "202", " m")
                CheckpointDataRow(stringResource(R.string.high_point), "1,584", " m")
            }
        }
    }
}

@Composable
private fun CheckpointDataRow(label: String, value: String, unit: String) {
    Text(
        text = label,
        fontSize = 13.sp,
        color = Gray
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.Bottom
    )
    {
        Text(
            text = value,
            fontSize = 25.2.sp,
            color = Color.White
        )
        Text(
            text = unit,
            fontSize = 16.8.sp,
            color = Color.White
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
}