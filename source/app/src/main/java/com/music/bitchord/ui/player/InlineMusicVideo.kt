package com.music.bitchord.ui.player

import android.view.LayoutInflater
import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import com.music.bitchord.R
import com.music.bitchord.data.Http
import com.music.bitchord.data.innertube.VideoQuality
import com.music.bitchord.data.model.durationMillis
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.innertube.StreamResolver
import com.music.bitchord.data.model.Song
import com.music.bitchord.playback.ChunkedDataSource
import com.music.bitchord.playback.PlaybackPosition
import com.music.bitchord.playback.InlineVideoPolicy
import com.music.bitchord.ui.components.BrandText as Text
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

/** Service-owned A/V session shared by expanded, minimized and background transports. */
@Stable
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
class InlineMusicVideoSession internal constructor(
    internal val video: ExoPlayer,
    private val audioController: MediaController?,
    
    internal val bandwidthMeter: DefaultBandwidthMeter,
    fallbackDurationMs: Long,
) {
    val position = PlaybackPosition()
    var durationMs by mutableLongStateOf(audioController?.duration?.takeIf { it > 0L } ?: fallbackDurationMs.coerceAtLeast(0L))
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var loading by mutableStateOf(true)
        internal set
    var error by mutableStateOf<String?>(null)
        internal set
    var attempt by mutableIntStateOf(0)
        private set
    internal var playingId by mutableStateOf<String?>(null)
    internal var resolvedVideo by mutableStateOf<Song?>(null)
    internal var loadedAttempt = -1
    var aspectRatio by mutableFloatStateOf(16f / 9f)
        internal set
    internal val originalId = audioController?.currentMediaItem?.mediaId
    internal val originalPosition = audioController?.currentPosition ?: 0L
    internal var closed = false
    private var desiredPlay = true // Convert to video is an explicit play request.
    internal var onClosed: (() -> Unit)? = null
    internal var measuredBitrate = 0L
    internal var bufferingSinceMs = 0L
    internal var activeNetworkHandle: Long? = null
    internal var activeStreamKey: String? = null
    internal var preparingSource = true
    private var pendingSeekMs: Long? = null
    private var pendingFraction: Float? = null
    internal fun requestedPosition(fallback: Long): Long = pendingSeekMs ?: fallback
    internal fun updatePlaybackState(state: Int) {
        if (state == Player.STATE_BUFFERING && bufferingSinceMs == 0L) bufferingSinceMs = SystemClock.elapsedRealtime()
        if (state == Player.STATE_READY) {
            bufferingSinceMs = 0L
            preparingSource = false
            pendingFraction?.let { fraction ->
                if (video.duration > 0L) {
                    durationMs = video.duration
                    pendingFraction = null
                    seekFraction(fraction)
                }
            }
            val target = pendingSeekMs?.let { InlineVideoPolicy.seekTarget(it, video.duration) }
            if (target != null) {
                pendingSeekMs = target
                if (kotlin.math.abs(video.currentPosition - target) <= 1_000L) pendingSeekMs = null
                else video.seekTo(target) // Apply a drag queued while the source was preparing.
            }
        }
    }

    // One decoder/clock, one attached surface. The expanded player wins while
    // both it and the mini bar are composed during the sheet animation.
    private val videoViews = linkedMapOf<PlayerView, Boolean>()
    private var attachedView: PlayerView? = null
    internal fun bindVideoView(view: PlayerView, expanded: Boolean) {
        if (closed || videoViews[view] == expanded) return
        videoViews[view] = expanded
        updateVideoView()
    }
    internal fun unbindVideoView(view: PlayerView) {
        videoViews.remove(view)
        updateVideoView()
        view.player = null
        view.keepScreenOn = false
    }
    private fun updateVideoView() {
        val target = if (closed) null else {
            videoViews.entries.lastOrNull { it.value }?.key ?: videoViews.keys.lastOrNull()
        }
        if (target !== attachedView) {
            PlayerView.switchTargetView(video, attachedView, target)
            attachedView = target
        }
    }

    internal fun sample() {
        if (closed) return
        if (video.duration > 0L) durationMs = video.duration
        isPlaying = video.isPlaying
        position.advancing = video.isPlaying
        position.playbackSpeed = video.playbackParameters.speed
        // Preserve the last playhead across stop/prepare during a quality handover.
        if (!preparingSource && video.playbackState != Player.STATE_IDLE) {
            position.report(video.currentPosition.coerceAtLeast(0L))
        }
    }
    fun togglePlayPause() {
        if (closed) return
        desiredPlay = !video.playWhenReady
        video.playWhenReady = desiredPlay
        sample()
    }
    fun seek(target: Long) {
        if (closed || durationMs <= 0) return
        val clamped = InlineVideoPolicy.seekTarget(target, durationMs) ?: return
        pendingSeekMs = clamped
        position.advancing = false
        position.seeks++
        if (!preparingSource && video.playbackState != Player.STATE_IDLE) video.seekTo(clamped)
        sample()
    }
    fun seekFraction(fraction: Float) {
        if (!fraction.isFinite()) return
        if (durationMs <= 0L) { pendingFraction = fraction.coerceIn(0f, 1f); return }
        InlineVideoPolicy.fractionTarget(fraction, durationMs)?.let(::seek)
    }
    fun retry() {
        if (closed) return
        error = null
        loading = true
        desiredPlay = true
        attempt++
    }
    internal fun startPrepared() {
        if (closed) return
        video.prepare()
        video.playWhenReady = desiredPlay
    }
    internal fun onPlaybackInterrupted() {
        desiredPlay = false
    }
    internal fun onTransportPlayIntent(play: Boolean) {
        // Notification/lock-screen pause must survive a later quality handover.
        desiredPlay = play
    }
    internal fun syncRepeatMode() {
        video.repeatMode = if (audioController?.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ONE
            else Player.REPEAT_MODE_OFF
    }
    /** Idempotent: explicit Next must not resume the old item during disposal. */
    fun close(resumeAudio: Boolean = true) {
        if (closed) return
        closed = true
        val sameRecording = InlineVideoPolicy.canTransferPosition(playingId, originalId)
        val ready = video.playbackState == Player.STATE_READY || video.playbackState == Player.STATE_ENDED
        val targetPosition = video.currentPosition
        val shouldPlay = if (video.playbackState == Player.STATE_ENDED) false
            else if (error != null) desiredPlay else desiredPlay && video.playWhenReady
        updateVideoView()
        videoViews.keys.forEach { it.player = null; it.keepScreenOn = false }
        videoViews.clear()
        video.release()
        if (originalId != null && audioController?.currentMediaItem?.mediaId == originalId) {
            // A different music-video edit may have a different intro; don't jump the album audio.
            if (sameRecording && ready) {
                InlineVideoPolicy.seekTarget(targetPosition, audioController?.duration ?: 0L)
                    ?.let { audioController?.seekTo(it) }
            }
            if (resumeAudio && shouldPlay) {
                audioController?.play()
            }
        }
        onClosed?.invoke()
    }
}

@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
internal suspend fun InlineMusicVideoSession.load(song: Song, quality: VideoQuality, networkHandle: Long?) {
    val session = this
    if (session.closed) return
    val previousId = session.playingId
    session.error = null
    session.loading = true
    // Abort old downloads on a network/quality change, not just the visual playback.
    if (previousId != null && session.activeNetworkHandle != networkHandle) {
        session.preparingSource = true
        session.video.stop()
    }
    try {
        val target = session.resolvedVideo ?: withTimeout(45_000) {
            if (song.isVideo) song else YtMusicRepository.resolveVideo(song)
                ?: throw IllegalStateException("No matching music video was found")
        }.also { session.resolvedVideo = it }
        val stream = withTimeout(45_000) {
            StreamResolver.resolveVideoPlayback(target.videoId, quality,
                forceRefresh = session.attempt > 0 && session.attempt != session.loadedAttempt,
                networkScope = networkHandle.toString())
        }
        if (!session.closed) {
            val streamKey = "${stream.videoUrl}|${stream.audioUrl}"
            if (streamKey == session.activeStreamKey && session.loadedAttempt == session.attempt &&
                session.activeNetworkHandle == networkHandle && session.video.playbackState != Player.STATE_IDLE) {
                session.loading = session.video.playbackState == Player.STATE_BUFFERING
                return // Identical provider fallback: do not restart the same media.
            }
            session.activeStreamKey = streamKey
            session.activeNetworkHandle = networkHandle
            session.preparingSource = true
            session.loadedAttempt = session.attempt
            session.playingId = target.videoId
            fun source(url: String, mime: String? = null): androidx.media3.exoplayer.source.MediaSource {
                val http = OkHttpDataSource.Factory(Http.client)
                    .setDefaultRequestProperties(StreamResolver.mediaHeadersFor(url))
                    .setTransferListener(session.bandwidthMeter)
                val item = MediaItem.Builder().setUri(url).setMediaId(song.videoId)
                    .setMediaMetadata(androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title).setArtist(song.artist)
                        .setArtworkUri(song.thumbnailUrl?.let(android.net.Uri::parse)).build()).apply {
                    if (!mime.isNullOrBlank()) setMimeType(mime)
                }.build()
                // The audio player already uses bounded ranges to avoid CDN throttling.
                // Apply the same tested transport to both video and its merged audio.
                val ranged = ChunkedDataSource.Factory(http, 1024L * 1024L)
                return DefaultMediaSourceFactory(ranged).createMediaSource(item)
            }
            val visual = source(stream.videoUrl, stream.mimeType)
            val media = stream.audioUrl?.let { MergingMediaSource(visual, source(it)) } ?: visual
            val handoverPosition = session.requestedPosition(session.position.positionMs)
            session.video.setMediaSource(media)
            session.video.seekTo(when (target.videoId) {
                previousId -> handoverPosition
                session.originalId -> session.requestedPosition(session.originalPosition)
                else -> session.requestedPosition(0L)
            })
            session.startPrepared()
        }
    } catch (cancelled: CancellationException) {
        if (cancelled is kotlinx.coroutines.TimeoutCancellationException) {
            session.loading = false
            session.error = "Video lookup timed out. Check your connection and retry."
        } else throw cancelled
    } catch (failure: Exception) {
        session.loading = false
        session.error = "Video unavailable: ${failure.message ?: "no playable stream was supplied"}"
    } catch (failure: LinkageError) {
        session.loading = false
        session.error = "Video is not supported on this device. Audio is still available."
    }
}

/** Only the artwork slot. No dialog, navigation, duplicated metadata or second transport. */
@Composable
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
fun InlineMusicVideoArtwork(
    session: InlineMusicVideoSession,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val frame = InlineVideoPolicy.fitFrame(maxWidth.value, maxHeight.value, session.aspectRatio, 0f)
        // Centered aspect-fit on both axes, identical insets. No hard black frame or rounded-card clip.
        AndroidView(
            modifier = Modifier.size(frame.width.dp, frame.height.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    val horizontal = Brush.horizontalGradient(0f to Color.Transparent, 0.03f to Color.White,
                        0.97f to Color.White, 1f to Color.Transparent, endX = size.width)
                    val vertical = Brush.verticalGradient(0f to Color.Transparent, 0.05f to Color.White,
                        0.95f to Color.White, 1f to Color.Transparent, endY = size.height)
                    onDrawWithContent {
                        drawContent()
                        drawRect(horizontal, blendMode = BlendMode.DstIn)
                        drawRect(vertical, blendMode = BlendMode.DstIn)
                    }
                },
            factory = { viewContext ->
                (LayoutInflater.from(viewContext).inflate(R.layout.inline_music_video, null) as PlayerView).apply {
                    session.bindVideoView(this, expanded = true)
                    useController = false // All playback/seeking stays in the existing Now Playing controls.
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    keepScreenOn = session.isPlaying
                }
            },
            update = { session.bindVideoView(it, expanded = true); it.keepScreenOn = session.isPlaying },
            onRelease = { session.unbindVideoView(it) },
        )
        if (session.loading && session.error == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = Color.White)
                Text("Loading music video…", color = Color.White)
            }
        }
        session.error?.let { message ->
            Column(Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.85f)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message, color = Color.White)
                TextButton(onClick = session::retry) { Text("Retry video", color = Color.White) }
            }
        }
    }
}

/** A live picture confined to the existing mini-player cover, never a second player. */
@Composable
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
fun InlineMusicVideoThumbnail(session: InlineMusicVideoSession, modifier: Modifier = Modifier) {
    if (session.error == null && !session.closed) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                (LayoutInflater.from(context).inflate(R.layout.inline_music_video, null) as PlayerView).apply {
                    useController = false
                    isClickable = false
                    isFocusable = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    session.bindVideoView(this, expanded = false)
                }
            },
            update = { session.bindVideoView(it, expanded = false) },
            onRelease = { session.unbindVideoView(it) },
        )
    }
}
