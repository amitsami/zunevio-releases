package com.music.bitchord.ui.components

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import com.music.bitchord.ui.components.BrandText as Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.BuildConfig
import com.music.bitchord.data.AppUpdateChecker
import com.music.bitchord.ui.icons.ZunevioBrand
import kotlinx.coroutines.launch

@Composable
fun UpdateSettingsCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showLegal by remember { mutableStateOf(false) }
    val status by AppUpdateChecker.status.collectAsStateWithLifecycle()
    val update by AppUpdateChecker.available.collectAsStateWithLifecycle()
    val download by AppUpdateChecker.download.collectAsStateWithLifecycle()
    var enabled by remember { mutableStateOf(AppUpdateChecker.notificationsEnabled() && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                enabled = AppUpdateChecker.notificationsEnabled() && androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        enabled = allowed
        AppUpdateChecker.setNotificationsEnabled(allowed)
    }
    val colors = MaterialTheme.colorScheme
    val checking = status == "Checking for updates…"
    // Explicit content colors: settings deliberately uses its own dark/light palette.
    CompositionLocalProvider(LocalContentColor provides colors.onBackground) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant, contentColor = colors.onSurface)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(shape = RoundedCornerShape(16.dp), color = colors.surface) {
                        Icon(Icons.Rounded.SystemUpdate, null, tint = ZunevioBrand.Red,
                            modifier = Modifier.padding(14.dp).size(28.dp))
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Zunévio ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleLarge)
                        Text("Installed · Build ${BuildConfig.VERSION_CODE}", color = colors.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider(color = colors.outline.copy(alpha = 0.5f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Verified, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text("Official stable channel", style = MaterialTheme.typography.labelLarge)
                }
                Text("Signed updates, release notes and verified downloads, all in one place.",
                    color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (update != null) "A new version is ready" else "Keep your music moving",
                style = MaterialTheme.typography.titleMedium)
            Text(status, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            FilledTonalButton(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(15.dp), enabled = !checking,
                onClick = { scope.launch { AppUpdateChecker.check() } }) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Text(if (checking) "Checking…" else "Check for updates")
            }
        }
        if (update != null) {
            val info = update!!
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(
                containerColor = colors.surfaceVariant, contentColor = colors.onSurface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("What's new in ${info.version}", color = colors.onSurface, style = MaterialTheme.typography.titleMedium)
                    if (!info.notes.isNullOrBlank()) Text(ZunevioBrand.styled(info.notes.orEmpty()), color = colors.onSurface, style = MaterialTheme.typography.bodyMedium)
                    when (val state = download) {
                        is AppUpdateChecker.DownloadState.Downloading -> {
                            LinearProgressIndicator(progress = { state.fraction }, modifier = Modifier.fillMaxWidth())
                            TextButton(onClick = AppUpdateChecker::cancelDownload) { Text("Cancel download") }
                        }
                        is AppUpdateChecker.DownloadState.Ready -> {
                            Text("Download verified. Ready to install.", color = colors.onSurfaceVariant)
                            Button(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp),
                                onClick = { AppUpdateChecker.installApk(context, state.file) }) { Text("Install update") }
                        }
                        else -> {
                            if (state is AppUpdateChecker.DownloadState.Failed) Text(state.message, color = colors.error)
                            Button(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(15.dp), enabled = info.apkUrl != null,
                                onClick = { scope.launch { AppUpdateChecker.downloadApk(context) } }) { Text("Download update") }
                        }
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant, contentColor = colors.onSurface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Update notifications", style = MaterialTheme.typography.titleSmall)
                        Text("Checks on launch and about every 12 hours. Android may delay background checks.",
                            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = enabled, colors = settingsSwitchColors(), onCheckedChange = { value ->
                        enabled = value
                        AppUpdateChecker.setNotificationsEnabled(value)
                        if (value && Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    })
                }
                TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) {
                    Text("Android notification settings")
                }
            }
        }
        Text("Downloads start only when you choose. The app checks the package and signing certificate; Android asks before installation. Keep Play Protect enabled.",
            color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { showLegal = true }) { Text("Licenses & source availability") }
    }
    }
    if (showLegal) {
        LicensesSourceDialog(onDismiss = { showLegal = false })
    }
}

/** Creator identity first; required third-party notices remain available one tap away. */
@Composable
private fun LicensesSourceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var showOpenSourceNotices by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showOpenSourceNotices) "Open-source notices" else "Licenses & source") },
        text = {
            if (showOpenSourceNotices) {
                val notices by produceState("Loading notices…") {
                    value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        listOf("NOTICE.md", "SOURCE.txt", "THIRD_PARTY_NOTICES.md", "LICENSE", "AGPL-3.0.txt", "Apache-2.0.txt", "Beat-This-MIT.txt", "Open-Unmix-LICENSE.txt", "LGPL-2.1.txt").joinToString("\n\n") { name ->
                            context.assets.open("legal/$name").bufferedReader().use { it.readText() }
                        }
                    }
                }
                Text(notices, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.bodySmall)
            } else {
                Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Zunéviu", color = colors.onSurface, style = MaterialTheme.typography.headlineSmall)
                    Text("Developed and maintained by Amit Hasan Sami.",
                        color = colors.onSurface, style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/amitsami")))
                    }) { Text("GitHub · @amitsami") }
                    Text("Matching source code and build instructions are included with this APK. Published releases and corresponding source archives are available on the official release page.",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/amitsami/zunevio-releases/releases")))
                    }) { Text("Releases & source downloads") }
                    HorizontalDivider(color = colors.outline.copy(alpha = 0.5f))
                    Text("Includes open-source software. Original authors' credits, applicable licenses and warranty notices are available below.",
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { showOpenSourceNotices = true }) { Text("Open-source notices") }
                }
            }
        },
        dismissButton = {
            if (showOpenSourceNotices) TextButton(onClick = { showOpenSourceNotices = false }) { Text("Back") }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
