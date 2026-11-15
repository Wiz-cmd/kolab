package com.example.ui.frames

import android.os.Build
import android.view.LayoutInflater
import android.graphics.RenderEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.R

/**
 * Plays a Frame video (or a just-recorded local clip) and re-applies the
 * creator's look. Videos keep their original colours on disk, so the look is
 * never destructive and can be changed for future playback.
 */
@Composable
fun FrameVideoPlayer(
    url: String,
    playWhenReady: Boolean,
    lookKey: String = "clean",
    muted: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val look = remember(lookKey) { FrameLook.fromKey(lookKey) }
    val latestPlayWhenReady by rememberUpdatedState(playWhenReady)

    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = if (muted) 0f else 1f
            prepare()
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(playWhenReady) {
        player.playWhenReady = latestPlayWhenReady
        if (latestPlayWhenReady) player.play() else player.pause()
    }

    LaunchedEffect(muted) {
        player.volume = if (muted) 0f else 1f
    }

    Box(modifier = modifier.background(Color.Black)) {
        AndroidView(
            factory = {
                LayoutInflater.from(context)
                    .inflate(R.layout.view_frame_video, null) as PlayerView
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (view.player !== player) view.player = player
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    view.setRenderEffect(
                        RenderEffect.createColorFilterEffect(look.colorFilter())
                    )
                }
            }
        )
        // Android 11 and older cannot render filter effects on a TextureView.
        // A light tint keeps the look recognisable without touching the file.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && look.tint != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(look.tint),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}
