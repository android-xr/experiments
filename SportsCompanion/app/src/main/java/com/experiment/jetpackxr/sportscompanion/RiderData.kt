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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.experiment.jetpackxr.sportscompanion.ui.theme.MediumGray

data class RiderData(
    val index: Int,
    val name: String,
    val lastname: String,
    val location: String,
    val time: String,
    val color: Color = Color.Black,
    val image: Int? = null,
)

val riders: List<RiderData> = listOf(
    RiderData(
        index = 1,
        name = "Carter",
        lastname = "Reyes",
        location = "Bend, OR",
        time = "31:26s",
        color = Color(0xFF62816B),
        image = R.drawable.carter
    ), RiderData(
        index = 2,
        name = "Grant",
        lastname = "Briggs",
        location = "Alger, WA",
        time = "+2.3s",
        color = Color(0xFF6D97A3),
        image = R.drawable.grant
    ), RiderData(
        index = 3,
        name = "Jay",
        lastname = "Laporte",
        location = "San Diego, CA",
        time = "+3.5s",
        color = Color(0xFF5F90E7),
    ), RiderData(
        index = 4,
        name = "Tyler",
        lastname = "Vaughn",
        location = "Spokane, WA",
        time = "+5.1s",
        color = Color(0xFFA87ED4)
    )
)

@Composable
fun RiderDataPanel(modifier: Modifier = Modifier, rider: RiderData) {
    // Should only display the two main riders that have images.
    if (rider.image == null) return

    Box(
        modifier = Modifier
            .width(280.dp)
            .background(MediumGray)
            .fillMaxSize()
    )
    {
        Column(
            modifier = Modifier.padding(22.4.dp)
        ) {
            Text(
                text = "${rider.name}\n${rider.lastname}",
                fontSize = 28.sp,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rider.location,
                fontSize = 14.sp,
                color = rider.color,
            )
        }
        Image(
            painter = painterResource(rider.image),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd),
            contentScale = ContentScale.FillWidth
        )
    }
}