package com.music.bitchord.ui.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.music.bitchord.MainActivity
import com.music.bitchord.data.innertube.AutoVideoPolicy
import com.music.bitchord.data.innertube.VideoQuality
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.durationMillis
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.playback.PlaybackService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await

/** Process-level request/state only. No Activity, view or composition owns the decoder. */
object InlineVideoPlayback {
    private val requestState = MutableStateFlow<Song?>(null)
    val request = requestState.asStateFlow()
    private val sessionState = MutableStateFlow<InlineMusicVideoSession?>(null)
    val session = sessionState.asStateFlow()
    fun setRequest(context: Context, song: Song?) {
        if (song == null) {
            sessionState.value?.close()
            requestState.value = null
            context.applicationContext.stopService(Intent(context, InlineVideoService::class.java))
        } else if (requestState.value?.videoId != song.videoId) {
            sessionState.value?.close(false)
            requestState.value = song
            try {
                ContextCompat.startForegroundService(context.applicationContext,
                    Intent(context, InlineVideoService::class.java))
            } catch (failure: RuntimeException) {
                requestState.value = null
                android.widget.Toast.makeText(context.applicationContext,
                    "Video playback could not start. Please reopen the app and retry.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    internal fun attach(value: InlineMusicVideoSession) { sessionState.value = value }
    internal fun detach(value: InlineMusicVideoSession?) {
        if (sessionState.value === value) {
            sessionState.value = null
            requestState.value = null
        }
    }
}

/** A real media-playback foreground service: lock screen, task swipe and UI disposal do not stop it.
 * Android Force stop, OS process termination and explicit stop still end playback. */
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
class InlineVideoService : MediaSessionService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var mediaSession: MediaSession? = null
    private var owner: InlineMusicVideoSession? = null
    private var audio: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null
    private var loadJob: Job? = null
    private var runJob: Job? = null
    private var starting = false
    private var startingFor: String? = null
    private var startJob: Job? = null
    private var destroying = false

    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this)
            .setNotificationId(NOTIFICATION_ID).build())
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Zunéviu playback", NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notice = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_media_play).setContentTitle("Zunéviu")
            .setContentText("Loading music video…").setContentIntent(open).setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIFICATION_ID, notice, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(NOTIFICATION_ID, notice)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val song = InlineVideoPlayback.request.value
        if (song == null) { stopSelf(); return START_NOT_STICKY }
        if (starting && startingFor != song.videoId) {
            startJob?.cancel()
            controllerFuture?.let(MediaController::releaseFuture)
            controllerFuture = null
            starting = false
        }
        if (!starting && owner == null) {
            starting = true
            startingFor = song.videoId
            startJob = scope.launch {
                try {
                    val future = MediaController.Builder(applicationContext,
                        SessionToken(applicationContext, ComponentName(applicationContext, PlaybackService::class.java)))
                        .buildAsync()
                    controllerFuture = future
                    val queue = future.await()
                    audio = queue
                    if (InlineVideoPlayback.request.value?.videoId != song.videoId) { stopSelf(); return@launch }
                    queue.pause() // Keep its queue but never play duplicate audio.
                    val meter = DefaultBandwidthMeter.Builder(applicationContext).build()
                    val video = ExoPlayer.Builder(applicationContext)
                        .setLoadControl(DefaultLoadControl.Builder()
                            .setBufferDurationsMs(15_000, 45_000, 750, 1_500).setBackBuffer(5_000, false)
                            .setTargetBufferBytes(32 * 1024 * 1024).setPrioritizeTimeOverSizeThresholds(true).build())
                        .setBandwidthMeter(meter).build().apply {
                            setAudioAttributes(androidx.media3.common.AudioAttributes.DEFAULT, true)
                            setHandleAudioBecomingNoisy(true)
                            setWakeMode(C.WAKE_MODE_NETWORK)
                        }
                    val session = InlineMusicVideoSession(video, queue, meter, song.durationMillis())
                    owner = session
                    session.onClosed = {
                        loadJob?.cancel(); runJob?.cancel()
                        mediaSession?.let { removeSession(it); it.release() }
                        mediaSession = null
                        owner = null
                        starting = false
                        val oldController = controllerFuture
                        controllerFuture = null; audio = null
                        InlineVideoPlayback.detach(session)
                        // Queue commands issued immediately after close must cross IPC before unbinding.
                        Handler(Looper.getMainLooper()).post {
                            oldController?.let(MediaController::releaseFuture)
                            if (!destroying && owner == null && InlineVideoPlayback.request.value == null) stopSelf()
                        }
                    }
                    val transport = object : ForwardingPlayer(video) {
                        override fun getAvailableCommands(): Player.Commands = super.getAvailableCommands().buildUpon()
                            .add(Player.COMMAND_SEEK_TO_NEXT).add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                            .add(Player.COMMAND_SEEK_TO_PREVIOUS).add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM).build()
                        override fun hasNextMediaItem() = queue.hasNextMediaItem()
                        override fun hasPreviousMediaItem() = queue.hasPreviousMediaItem()
                        override fun seekToNext() = next()
                        override fun seekToNextMediaItem() = next()
                        override fun seekToPrevious() = previous()
                        override fun seekToPreviousMediaItem() = previous()
                        private fun next() { session.close(false); queue.seekToNextMediaItem(); queue.play() }
                        private fun previous() { session.close(false); queue.seekToPrevious(); queue.play() }
                    }
                    mediaSession = MediaSession.Builder(this@InlineVideoService, transport)
                        .setId("zuneviu-inline-video").setSessionActivity(openIntent()).build().also(::addSession)
                    val listener = object : Player.Listener {
                        override fun onVideoSizeChanged(size: VideoSize) {
                            if (size.width > 0 && size.height > 0) session.aspectRatio =
                                size.width * size.pixelWidthHeightRatio / size.height
                        }
                        override fun onPlaybackStateChanged(state: Int) {
                            session.loading = state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                            session.updatePlaybackState(state); session.sample()
                            if (state == Player.STATE_ENDED && !session.closed && queue.hasNextMediaItem()) {
                                session.close(false); queue.seekToNextMediaItem(); queue.play()
                            }
                        }
                        override fun onPlayWhenReadyChanged(play: Boolean, reason: Int) {
                            if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST) session.onTransportPlayIntent(play)
                            if (!play && (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS ||
                                reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY)) session.onPlaybackInterrupted()
                        }
                        override fun onIsPlayingChanged(playing: Boolean) { session.sample() }
                        override fun onPositionDiscontinuity(old: Player.PositionInfo, new: Player.PositionInfo, reason: Int) {
                            session.position.seeks++; session.sample()
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            session.loading = false
                            session.error = "Video unavailable. Check the connection or retry."
                            session.sample()
                        }
                    }
                    video.addListener(listener)
                    val audioListener = object : Player.Listener {
                        override fun onRepeatModeChanged(mode: Int) { if (!session.closed) session.syncRepeatMode() }
                        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                            if (item?.mediaId != session.originalId && !session.closed) session.close(false)
                        }
                        override fun onPlayWhenReadyChanged(play: Boolean, reason: Int) {
                            if (play && !session.closed) session.close(false)
                        }
                    }
                    queue.addListener(audioListener)
                    val bandwidth = BandwidthMeter.EventListener { _, _, bitrate -> session.measuredBitrate = bitrate }
                    meter.addEventListener(Handler(Looper.getMainLooper()), bandwidth)
                    session.syncRepeatMode()
                    InlineVideoPlayback.attach(session)
                    runJob = scope.launch {
                        var lastKey = ""
                        var network: Long? = null
                        var preference: VideoQuality? = null
                        var tier = VideoQuality.LOW
                        var changedAt = SystemClock.elapsedRealtime()
                        var autoSampleAt = changedAt
                        try {
                            while (isActive && !session.closed) {
                                session.sample()
                                val connection = AppSettings.videoConnection.value
                                val pref = VideoQuality.forNetwork(connection.wifi,
                                    AppSettings.videoQualityWifi.value, AppSettings.videoQualityCellular.value)
                                val now = SystemClock.elapsedRealtime()
                                if (network != connection.handle || preference != pref) {
                                    network = connection.handle; preference = pref
                                    tier = AutoVideoPolicy.initial(connection.wifi)
                                    changedAt = now; session.measuredBitrate = 0L
                                }
                                if (pref == VideoQuality.AUTO && now - autoSampleAt >= 3_000L) {
                                    autoSampleAt = now
                                    val waiting = if (session.bufferingSinceMs > 0L) now - session.bufferingSinceMs else 0L
                                    val next = AutoVideoPolicy.next(tier, session.measuredBitrate, video.totalBufferedDuration, waiting, now - changedAt)
                                    if (next != tier) { tier = next; changedAt = now }
                                }
                                val quality = if (pref == VideoQuality.AUTO) tier else pref
                                val key = "$network|$quality|${session.attempt}"
                                if (key != lastKey) {
                                    lastKey = key; loadJob?.cancel()
                                    loadJob = scope.launch {
                                        val openingVideo = session.playingId == null
                                        session.load(song, quality, network)
                                        if (!session.closed && session.error != null && openingVideo) {
                                            val message = if (session.error?.contains("timed out") == true)
                                                "Video lookup timed out. Check your connection and retry."
                                                else "Video unavailable"
                                            android.widget.Toast.makeText(applicationContext, message,
                                                android.widget.Toast.LENGTH_LONG).show()
                                            session.close() // Restore audio and clear the failed video request.
                                        }
                                    }
                                }
                                delay(200L)
                            }
                        } finally {
                            meter.removeEventListener(bandwidth)
                            queue.removeListener(audioListener)
                            video.removeListener(listener)
                        }
                    }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (failure: Exception) {
                    android.widget.Toast.makeText(applicationContext, "Video could not start. Please retry.", android.widget.Toast.LENGTH_LONG).show()
                    InlineVideoPlayback.detach(owner); stopSelf()
                }
            }
        }
        return START_NOT_STICKY // Do not unexpectedly restart after OS termination/Force stop.
    }
    private fun openIntent() = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession
    override fun onTaskRemoved(rootIntent: Intent?) {
        // A task swipe is not a transport stop. Respect the user's explicit stop-on-swipe preference.
        if (AppSettings.stopOnTaskRemoved.value) { owner?.close(false); stopSelf() }
    }
    override fun onDestroy() {
        destroying = true
        startJob?.cancel()
        loadJob?.cancel(); runJob?.cancel()
        mediaSession?.let { removeSession(it); it.release() }; mediaSession = null
        owner?.close(false); InlineVideoPlayback.detach(owner); owner = null
        controllerFuture?.let(MediaController::releaseFuture); controllerFuture = null; audio = null
        scope.cancel()
        super.onDestroy()
    }
    private companion object { const val CHANNEL = "zuneviu_video"; const val NOTIFICATION_ID = 1018 }
}
