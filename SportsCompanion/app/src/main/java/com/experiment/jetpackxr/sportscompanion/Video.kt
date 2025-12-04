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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

data class VideoData(
    val player: ExoPlayer,
    val view: PlayerView,
    val mediaSession: MediaSession
) {
    fun dispose() {
        player.release()
        mediaSession.release()
    }
}

@OptIn(UnstableApi::class)
fun createVideoPlayerView(context: Context): VideoData {
    val player = ExoPlayer.Builder(context).build()
    val mediaSession = MediaSession.Builder(context, player).build()
    val playerView = PlayerView(context)
    playerView.player = player
    playerView.player?.playWhenReady = true
    playerView.useController = false
    playerView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.MATCH_PARENT // Match parent height
    )
    playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

    return VideoData(player, playerView, mediaSession)
}

fun loadAndPlayVideo(player: ExoPlayer, resourceId: Int) {
    player.prepare()
    val fileUri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .path(resourceId.toString()).build()
    val mediaItem = MediaItem.fromUri(fileUri)
    player.setMediaItem(mediaItem)
    player.play()
}

@Composable
fun VideoPlayerView(playerView: PlayerView, modifier: Modifier) {
    val parent = playerView.parent
    if (parent != null)
        (parent as ViewGroup).removeView(playerView)

    Box(modifier = modifier.background(Color.White)) {
        AndroidView(
            modifier = Modifier.fillMaxHeight(),
            factory = {
                playerView
            }
        )
        LiveLabel(
            Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
        )
    }
}

@Composable
fun LiveLabel(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(68.dp)
            .height(37.dp)
            .border(width = 2.27.dp, color = Color.White, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    )
    {
        Text(
            text = "LIVE",
            fontSize = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}