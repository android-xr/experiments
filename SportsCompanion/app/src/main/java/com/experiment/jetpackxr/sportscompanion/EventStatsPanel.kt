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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.experiment.jetpackxr.sportscompanion.ui.theme.Gray
import com.experiment.jetpackxr.sportscompanion.ui.theme.MediumGray

@Composable
fun Leaderboard(modifier: Modifier = Modifier, riders: List<RiderData>) {
    Row(
        modifier = modifier
            .width(280.dp)
            .background(MediumGray)
    )
    {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        )
        {
            for (i in riders.indices) {
                RiderEventRow(rider = riders[i], isFirstEntry = i == 0)
            }
        }
    }
}

@Composable
private fun RiderEventRow(rider: RiderData, isFirstEntry: Boolean) {
    Row(
        modifier = Modifier.height(60.dp),
        verticalAlignment = Alignment.Top
    )
    {
        Text(
            text = (rider.index).toString(),
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .offset(y = 7.dp)
                .background(rider.color, CircleShape)
        )
        Spacer(modifier = Modifier.width(11.3.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy((-6).dp)
        ) {
            Text(
                text = "${rider.name} ${rider.lastname}",
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Start,
            )
            Text(
                text = rider.location,
                fontSize = 11.sp,
                color = Gray,
                textAlign = TextAlign.Start,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        val timeColor = if (isFirstEntry) Color.White else Gray
        Text(
            text = rider.time,
            fontSize = 12.sp,
            color = timeColor,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun EventStatsTop(modifier: Modifier = Modifier, timer: MutableState<Int>) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .height(121.dp)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    )
    {
        Column(modifier = Modifier.weight(1f)) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center)
            {
                Icon(
                    modifier = Modifier
                        .width(18.dp)
                        .height(16.dp)
                        .offset(y = 3.dp),
                    painter = painterResource(id = R.drawable.icon_loop),
                    contentDescription = null,
                    tint = Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            )
            {
                Text(
                    text = "2",
                    fontSize = 36.sp,
                    color = Color.White,
                    textAlign = TextAlign.Start
                )
                Text(
                    modifier = Modifier.offset(y = (-2).dp),
                    text = "/6",
                    fontSize = 24.sp,
                    color = Gray,
                    textAlign = TextAlign.Start
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center)
            {
                Icon(
                    modifier = Modifier
                        .width(16.dp)
                        .height(19.dp),
                    painter = painterResource(id = R.drawable.icon_stopwatch),
                    contentDescription = null,
                    tint = Gray
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = timer.value.formatString(),
                fontSize = 36.sp,
                color = Color.White,
                textAlign = TextAlign.Start
            )
        }
    }
}

private fun Int.formatString(): String {
    // Adding 924 to the timer value for this demo.
    // This makes the timer readout start at 15:24
    val adjusted = 924 + this
    val minutes = (adjusted % 3600) / 60
    val remainingSeconds = adjusted % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}