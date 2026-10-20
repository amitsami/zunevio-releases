package com.music.bitchord.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.work.*
import com.music.bitchord.BuildConfig
import com.music.bitchord.MainActivity
import com.music.bitchord.R
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request

/** Official public release channel. Never downloads or installs without a tap. */
object AppUpdateChecker {
    data class UpdateInfo(val version: String, val releaseUrl: String, val apkUrl: String?, val notes: String?,
        val digest: String? = null, val size: Long = 0, val repository: String = "")
    sealed interface DownloadState {
        data object Idle : DownloadState
        data class Downloading(val fraction: Float) : DownloadState
        data class Ready(val file: File) : DownloadState
        data class Failed(val message: String) : DownloadState
    }
    private lateinit var app: Context
    private val prefs get() = app.getSharedPreferences("zunevio_updates", Context.MODE_PRIVATE)
    private val _available = MutableStateFlow<UpdateInfo?>(null)
    val available = _available.asStateFlow()
    private val _download = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val download = _download.asStateFlow()
    private val _openRequested = MutableStateFlow(false)
    val openRequested = _openRequested.asStateFlow()
    fun consumeIntent(intent: Intent) {
        if (intent.action == OPEN_UPDATE) { _openRequested.value = true; intent.action = null }
    }
    fun acknowledgeOpenRequest() { _openRequested.value = false }
    private val _status = MutableStateFlow("Official stable updates are ready.")
    val status = _status.asStateFlow()
    private val checkMutex = Mutex()
    private val downloadMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val generation = java.util.concurrent.atomic.AtomicInteger(0)
    @Volatile private var activeCall: Call? = null
    private val client = OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS).callTimeout(15, TimeUnit.MINUTES)
        .addNetworkInterceptor { chain ->
            val url = chain.request().url
            require(url.isHttps && ReleasePolicy.allowedDownloadHost(url.host)) { "Untrusted update host" }
            chain.proceed(chain.request())
        }.build()
    fun init(context: Context) {
        app = context.applicationContext
        scope.launch {
            downloadMutex.withLock {
                if (_download.value is DownloadState.Idle) pruneObsoleteUpdateCache()
            }
        }
        schedule()
    }
    /** Never remove a newer ready installer or the listener's saved music. */
    private fun pruneObsoleteUpdateCache() {
        val root = File(app.cacheDir, "updates")
        val now = System.currentTimeMillis()
        root.listFiles()?.filter { it.isFile }?.forEach { file ->
            if (file.name == "update.apk") {
                val info = runCatching { app.packageManager.getPackageArchiveInfo(file.path, 0) }.getOrNull()
                val version = info?.let {
                    if (Build.VERSION.SDK_INT >= 28) it.longVersionCode else {
                        @Suppress("DEPRECATION")
                        it.versionCode.toLong()
                    }
                }
                if (CacheBudgets.obsoleteInstaller(info?.packageName, app.packageName, version, BuildConfig.VERSION_CODE.toLong())) file.delete()
            } else if (CacheBudgets.abandonedPartial(file.name, file.lastModified(), now)) {
                file.delete() // Abandoned temporary download from an earlier process.
            }
        }
    }

    const val OFFICIAL_REPOSITORY = "amitsami/zunevio-releases"
    fun repository(): String = OFFICIAL_REPOSITORY
    fun notificationPrompted(): Boolean = prefs.getBoolean("notification_prompted", false)
    fun markNotificationPrompted() { prefs.edit().putBoolean("notification_prompted", true).apply() }
    fun notificationsEnabled(): Boolean = prefs.getBoolean("notify", true)
    fun setNotificationsEnabled(enabled: Boolean) { prefs.edit().putBoolean("notify", enabled).apply() }
    fun setRepository(input: String): Boolean {
        val repo = ReleasePolicy.repository(input) ?: return false
        if (repo != OFFICIAL_REPOSITORY) return false
        cancelDownload()
        prefs.edit().putString("repository", repo).remove("notified").apply()
        _available.value = null
        _status.value = "Repository saved: $repo. Check for updates when ready."
        schedule()
        return true
    }
    private fun schedule() {
        if (repository().isBlank()) return
        val work = PeriodicWorkRequestBuilder<ReleaseCheckWorker>(12, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
        WorkManager.getInstance(app).enqueueUniquePeriodicWork("zunevio-release-check", ExistingPeriodicWorkPolicy.KEEP, work)
    }
    private fun request(url: String): Request = Request.Builder().url(url)
        .header("User-Agent", "Zunevio-Android/${BuildConfig.VERSION_NAME}")
        .header("Accept", "application/vnd.github+json").build()
    suspend fun check() = withContext(Dispatchers.IO) {
        checkMutex.withLock {
            val repo = repository()
            if (repo.isBlank()) return@withLock
            _status.value = "Checking for updates…"
            try {
                val text = client.newCall(request("https://api.github.com/repos/$repo/releases/latest")).execute().use { response ->
                    if (response.code == 404) {
                        _available.value = null
                        _status.value = "No stable update is available yet. Please check again later."
                        return@withLock
                    }
                    check(response.isSuccessful) { "GitHub returned ${response.code}. Try again later." }
                    val body = response.body ?: error("Empty release response")
                    check(body.contentLength() <= 2 * 1024 * 1024) { "Release metadata too large" }
                    body.byteStream().use { stream ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val n = stream.read(buffer)
                            if (n < 0) break
                            check(output.size() + n <= 2 * 1024 * 1024) { "Release metadata too large" }
                            output.write(buffer, 0, n)
                        }
                        val bytes = output.toByteArray()
                        check(bytes.size <= 2 * 1024 * 1024) { "Release metadata too large" }
                        bytes.toString(Charsets.UTF_8)
                    }
                }
                val release = Json.parseToJsonElement(text).jsonObject
                fun field(name: String) = release[name]?.jsonPrimitive?.contentOrNull.orEmpty()
                if (release["draft"]?.jsonPrimitive?.booleanOrNull == true || release["prerelease"]?.jsonPrimitive?.booleanOrNull == true) return@withLock
                val tag = field("tag_name")
                val releaseUrl = field("html_url")
                check(ReleasePolicy.releaseUrl(releaseUrl, repo)) { "Release URL does not match your repository" }
                if (repository() != repo) return@withLock
                if (!ReleasePolicy.isNewer(tag, BuildConfig.VERSION_NAME)) {
                    _available.value = null
                    _status.value = "You're up to date (${BuildConfig.VERSION_NAME})."
                    return@withLock
                }
                val asset = release["assets"]?.jsonArray?.map { it.jsonObject }?.firstOrNull {
                    ReleasePolicy.universalApk(it["name"]?.jsonPrimitive?.content.orEmpty()) &&
                    ReleasePolicy.assetUrl(it["browser_download_url"]?.jsonPrimitive?.content.orEmpty(), repo)
                }
                val info = UpdateInfo(tag.removePrefix("v").removePrefix("V"), releaseUrl,
                    asset?.get("browser_download_url")?.jsonPrimitive?.contentOrNull,
                    field("body").take(24_000).ifBlank { "No changelog was provided for this release." },
                    ReleasePolicy.sha256(asset?.get("digest")?.jsonPrimitive?.contentOrNull),
                    asset?.get("size")?.jsonPrimitive?.longOrNull ?: 0, repo)
                _available.value = info
                _status.value = "${info.version} is available. Open the update icon for the changelog."
                notifyAvailable(info)
            } catch (e: CancellationException) { throw e }
              catch (e: Exception) { _status.value = "Update check failed: ${e.message ?: "Network unavailable"}" }
        }
    }
    private fun notifyAvailable(info: UpdateInfo) {
        if (!notificationsEnabled() || prefs.getString("notified", null) == info.version) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (!NotificationManagerCompat.from(app).areNotificationsEnabled()) return
        val manager = app.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "App updates", NotificationManager.IMPORTANCE_DEFAULT))
        val tap = PendingIntent.getActivity(app, 811, Intent(app, MainActivity::class.java).setAction(OPEN_UPDATE)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(app, CHANNEL).setSmallIcon(R.drawable.ic_notification_logo)
            .setContentTitle("Zunévio ${info.version} is available")
            .setContentText("Tap to see the changelog and choose whether to download.")
            .setContentIntent(tap).setAutoCancel(true).build()
        runCatching { manager.notify(NOTIFICATION_ID, notification) }.onSuccess {
            prefs.edit().putString("notified", info.version).apply()
        }
    }
    suspend fun clearCache(context: Context) = withContext(Dispatchers.IO) {
        if (_download.value !is DownloadState.Downloading && _download.value !is DownloadState.Ready)
            File(context.cacheDir, "updates").listFiles()?.forEach { it.delete() }
    }
    suspend fun downloadApk(sourceContext: Context) {
        val context = sourceContext.applicationContext
        // App-owned scope survives closing the dialog; process death safely requires a retry.
        if (!downloadMutex.tryLock()) return
        val token = generation.get()
        scope.launch {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val temp = File(dir, "download.apk")
            val final = File(dir, "update.apk")
            try {
                val info = _available.value ?: error("Check for updates first")
                check(info.repository == repository()) { "Repository changed. Check again." }
                val url = info.apkUrl ?: error("The update download is not ready yet. Please try again later.")
                check(ReleasePolicy.assetUrl(url, info.repository)) { "Untrusted APK URL" }
                val expected = info.digest ?: error("Missing GitHub SHA-256 digest. Re-upload the release APK before downloading.")
                check(info.size in 1..MAX_APK) { "Invalid APK size" }
                check(token == generation.get()) { "Download cancelled" }
                _download.value = DownloadState.Downloading(0f)
                val call = client.newCall(request(url)); activeCall = call
                val hash = MessageDigest.getInstance("SHA-256")
                call.execute().use { response ->
                    check(response.isSuccessful) { "Download failed (${response.code})" }
                    val body = response.body ?: error("Empty APK response")
                    var received = 0L
                    var lastProgress = 0L
                    body.byteStream().use { input -> temp.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            val read = input.read(buffer); if (read < 0) break
                            check(!call.isCanceled() && token == generation.get()) { "Download cancelled" }
                            received += read
                            check(received <= info.size && received <= MAX_APK) { "Unexpected APK size" }
                            hash.update(buffer, 0, read); output.write(buffer, 0, read)
                            val now = android.os.SystemClock.elapsedRealtime()
                            if (now - lastProgress > 250) { _download.value = DownloadState.Downloading(received.toFloat() / info.size); lastProgress = now }
                        }
                    } }
                    check(received == info.size) { "Incomplete APK download" }
                }
                val actual = hash.digest().joinToString("") { "%02x".format(it) }
                check(actual == expected) { "SHA-256 mismatch. APK rejected." }
                verifyArchive(context, temp)
                check(info.repository == repository()) { "Repository changed. APK discarded." }
                check(activeCall?.isCanceled() == false && token == generation.get()) { "Download cancelled" }
                final.delete(); check(temp.renameTo(final)) { "Could not save verified APK" }
                _download.value = DownloadState.Ready(final)
            } catch (e: Exception) {
                temp.delete(); final.delete()
                if (token == generation.get()) _download.value = DownloadState.Failed(e.message ?: "Download failed")
            } finally { activeCall = null; downloadMutex.unlock() }
        }
    }
    @Suppress("DEPRECATION")
    private fun verifyArchive(context: Context, file: File) {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val apk = pm.getPackageArchiveInfo(file.path, flags) ?: error("Not a valid Android APK")
        val current = pm.getPackageInfo(context.packageName, flags)
        check(apk.packageName == context.packageName) { "APK belongs to a different package. Use the matching release flavor." }
        val nextCode = if (Build.VERSION.SDK_INT >= 28) apk.longVersionCode else apk.versionCode.toLong()
        val currentCode = if (Build.VERSION.SDK_INT >= 28) current.longVersionCode else current.versionCode.toLong()
        check(nextCode > currentCode) { "APK versionCode must be newer than the installed app" }
        check(apk.applicationInfo?.let { it.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0 } == true) { "Debuggable APK rejected" }
        check((apk.applicationInfo?.minSdkVersion ?: Int.MAX_VALUE) <= Build.VERSION.SDK_INT) { "APK requires a newer Android version" }
        fun certs(info: android.content.pm.PackageInfo): Set<String> {
            val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
            return signatures.orEmpty().map { signature -> MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString("") { "%02x".format(it) } }.toSet()
        }
        val installed = certs(current)
        check(installed.isNotEmpty() && installed == certs(apk)) { "Signing certificate mismatch. APK rejected." }
        // Android's package installer also verifies the APK's cryptographic signatures.
    }
    fun cancelDownload() { generation.incrementAndGet(); activeCall?.cancel(); _download.value = DownloadState.Idle }
    fun resetDownload() { cancelDownload() }
    fun installApk(sourceContext: Context, file: File) {
        val context = sourceContext.applicationContext
        scope.launch {
            try {
                val expected = File(context.cacheDir, "updates/update.apk")
                check(file.canonicalFile == expected.canonicalFile && file.isFile) { "Invalid update file" }
                verifyArchive(context, file)
                withContext(Dispatchers.Main) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        _status.value = "Allow installs from this app if you trust this release, then return and tap Install again."
                    } else {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            } catch (e: Exception) { _download.value = DownloadState.Failed(e.message ?: "Could not open Android installer") }
        }
    }
    private const val OPEN_UPDATE = "com.zunevio.OPEN_UPDATE"
    private const val MAX_APK = 400L * 1024 * 1024
    private const val CHANNEL = "app_updates"
    private const val NOTIFICATION_ID = 1111
}

class ReleaseCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        AppUpdateChecker.check()
        return Result.success()
    }
}
