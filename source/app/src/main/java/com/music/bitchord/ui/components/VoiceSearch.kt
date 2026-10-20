package com.music.bitchord.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** User-initiated, in-app speech-to-text. No background recording or humming recognition. */
@Composable
fun rememberVoiceSearch(onListening: () -> Unit = {}, onResult: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val resultCallback = rememberUpdatedState(onResult)
    val listeningCallback = rememberUpdatedState(onListening)
    val autoSupported = AutoVoicePolicy.supportsApi(Build.VERSION.SDK_INT)
    var open by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var listening by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var detectedLanguage by remember { mutableStateOf<String?>(null) }
    var languageVerified by remember { mutableStateOf(false) }
    var switchFailed by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf<String?>(null) }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var watchdog by remember { mutableStateOf<Job?>(null) }
    fun cancel() {
        watchdog?.cancel(); watchdog = null
        val old = recognizer; recognizer = null
        old?.cancel(); old?.destroy(); listening = false; busy = false
    }
    fun fail(text: String) { cancel(); message = text }
    fun submit(text: String) { cancel(); open = false; resultCallback.value(text) }
    fun start() {
        cancel(); transcript = ""; detectedLanguage = null
        languageVerified = false; switchFailed = false; reviewText = null
        if (!autoSupported) {
            message = "Automatic voice-language recognition needs Android 14 or later. You can still type in any language."
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            message = "Microphone access is required. Allow it in app permissions."
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            message = "No speech recognition service is available. Enable or update your phone's speech service, or type your search."
            return
        }
        try {
            val engine = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer = it }
            engine.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    if (recognizer !== engine || !open) return
                    message = "Listening… say a song, artist or lyric phrase. Language is automatic."
                    listening = true
                }
                override fun onBeginningOfSpeech() {
                    if (recognizer !== engine || !open) return
                    message = "Hearing you…"
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    if (recognizer !== engine || !open) return
                    listening = false; message = "Recognizing…"
                }
                override fun onLanguageDetection(results: Bundle) {
                    if (recognizer !== engine || !open || Build.VERSION.SDK_INT < 34) return
                    val tag = results.getString(SpeechRecognizer.DETECTED_LANGUAGE)?.takeIf { it.isNotBlank() }
                    val confidence = results.getInt(SpeechRecognizer.LANGUAGE_DETECTION_CONFIDENCE_LEVEL,
                        SpeechRecognizer.LANGUAGE_DETECTION_CONFIDENCE_LEVEL_UNKNOWN)
                    val switched = results.getInt(SpeechRecognizer.LANGUAGE_SWITCH_RESULT,
                        SpeechRecognizer.LANGUAGE_SWITCH_RESULT_NOT_ATTEMPTED)
                    switchFailed = when (switched) {
                        SpeechRecognizer.LANGUAGE_SWITCH_RESULT_SUCCEEDED -> false
                        SpeechRecognizer.LANGUAGE_SWITCH_RESULT_FAILED, SpeechRecognizer.LANGUAGE_SWITCH_RESULT_SKIPPED_NO_MODEL -> true
                        else -> switchFailed
                    }
                    if (tag != null) detectedLanguage = tag
                    languageVerified = tag != null && !switchFailed && confidence >=
                        SpeechRecognizer.LANGUAGE_DETECTION_CONFIDENCE_LEVEL_CONFIDENT
                }
                override fun onError(error: Int) {
                    if (recognizer !== engine || !open) return
                    fail(when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "No words recognized. Speak clearly near the microphone and retry."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap Retry and speak after Listening appears."
                        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech service connection failed. Check the internet and retry."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone access is required. Allow it in app permissions."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Wait a moment and tap Retry."
                        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
                            "Automatic language switching or its speech models are unavailable in this speech service. Update its language models, or type your search."
                        else -> "Speech service failed (code $error). Retry or type your search."
                    })
                }
                override fun onResults(results: Bundle?) {
                    if (recognizer !== engine || !open) return
                    val candidate = AutoVoicePolicy.best(
                        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty(),
                        results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES))
                    if (candidate == null) { fail("No words returned. Tap Retry or type your search."); return }
                    transcript = candidate.text
                    if (AutoVoicePolicy.needsReview(candidate, languageVerified)) {
                        cancel(); reviewText = candidate.text
                        message = when {
                            switchFailed -> "The speech service could not switch languages; a required model may be missing. Check the words before searching."
                            !languageVerified -> "This speech service did not confirm automatic language recognition. Check the words before searching."
                            else -> "The speech service is unsure about these words. Check them before searching."
                        }
                    } else submit(candidate.text)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    if (recognizer !== engine || !open) return
                    AutoVoicePolicy.best(partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty(),
                        partialResults?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES))?.let { transcript = it.text }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            // Deliberately no language picker, fixed language, or restricted language list.
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
                .putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH, RecognizerIntent.LANGUAGE_SWITCH_HIGH_PRECISION)
                .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            busy = true; message = "Checking speech service…"
            var begun = false
            fun beginListening() {
                if (begun || recognizer !== engine || !open) return
                begun = true; watchdog?.cancel()
                try {
                    listeningCallback.value() // Pause our music before the microphone starts.
                    message = "Starting microphone…"
                    engine.startListening(intent)
                    watchdog = scope.launch {
                        delay(30_000L)
                        if (recognizer === engine && open) fail("The speech service took too long. Tap Retry or type your search.")
                    }
                } catch (_: SecurityException) { fail("Microphone access was denied.") }
                catch (_: RuntimeException) { fail("The speech service could not start. Tap Retry.") }
            }
            // SDK 34+ is checked above. This capability query alone is not proof that optional flags work;
            // language callbacks and confidence still determine whether results may auto-submit.
            if (Build.VERSION.SDK_INT >= 34) {
                engine.checkRecognitionSupport(intent, context.mainExecutor, object : RecognitionSupportCallback {
                    override fun onSupportResult(support: RecognitionSupport) { beginListening() }
                    override fun onError(error: Int) {
                        if (begun || recognizer !== engine || !open) return
                        if (error == SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT) beginListening()
                        else fail("Your speech service cannot use this automatic recognition request (code $error). Update the service/models or type your search.")
                    }
                })
                watchdog = scope.launch { delay(4_000L); beginListening() }
            }
        } catch (_: SecurityException) { fail("Microphone access was denied.") }
        catch (_: RuntimeException) { fail("The speech service could not start. Tap Retry.") }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        if (allowed && open) start()
        else if (open) message = "Microphone permission denied. Allow it in app settings or type your search."
    }
    fun requestStart() {
        if (!autoSupported || ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) start()
        else permission.launch(Manifest.permission.RECORD_AUDIO)
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                cancel()
                message = "Listening stopped while the app was in the background. Tap Retry."
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); watchdog?.cancel(); recognizer?.destroy(); recognizer = null }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { cancel(); open = false },
            title = { Text("Search with your voice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(message)
                    detectedLanguage?.let { tag ->
                        val label = Locale.forLanguageTag(tag).getDisplayName(Locale.getDefault()).ifBlank { tag }
                        Text(if (languageVerified) "Detected: $label" else "Language suggestion: $label (unconfirmed)")
                    }
                    if (transcript.isNotBlank()) Text(transcript)
                    if (autoSupported) Text("No language selection needed. Supported languages depend on your speech service and its installed models. Say words, not a melody; internet may be needed.")
                }
            },
            confirmButton = {
                if (autoSupported) TextButton(enabled = !busy || listening, onClick = {
                    if (listening) { recognizer?.stopListening(); message = "Recognizing…"; listening = false }
                    else requestStart()
                }) { Text(if (listening) "Done" else if (busy) "Working…" else "Retry") }
                else TextButton(onClick = { cancel(); open = false }) { Text("OK") }
            },
            dismissButton = {
                Column {
                    reviewText?.let { text -> TextButton(onClick = { submit(text) }) { Text("Search these words") } }
                    TextButton(onClick = { cancel(); open = false }) { Text("Cancel") }
                }
            },
        )
    }
    return { open = true; requestStart() }
}
