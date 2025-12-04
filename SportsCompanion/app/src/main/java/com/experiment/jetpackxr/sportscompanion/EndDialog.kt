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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.xr.compose.spatial.SpatialDialog
import androidx.xr.compose.spatial.SpatialDialogProperties
import com.experiment.jetpackxr.sportscompanion.ui.theme.DarkGray
import com.experiment.jetpackxr.sportscompanion.ui.theme.EndExperienceButtonColors

@Composable
fun EndDialog(
    onConfirmation: () -> Unit
) {
    SpatialDialog(
        onDismissRequest = { },
        SpatialDialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
    {
        Box(
            Modifier
                .width(312.dp)
                .height(240.dp)
                .clip(shape = RoundedCornerShape(28.dp))
                .background(DarkGray)
        )
        {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            )
            {
                Text(
                    text = stringResource(R.string.end_of_experience),
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                FilledTonalButton(onClick = onConfirmation, colors = EndExperienceButtonColors) {
                    Text(
                        text = stringResource(R.string.restart),
                        color = Color.Black
                    )
                }
            }
        }
    }
}