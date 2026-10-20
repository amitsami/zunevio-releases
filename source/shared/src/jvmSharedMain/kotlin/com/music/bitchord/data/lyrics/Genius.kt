package com.music.bitchord.data.lyrics

import com.music.bitchord.data.Http
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Web scraper for Genius.com lyrics.
 *
 * Used strictly as a last resort fallback when none of the time-synced providers
 * (LRCLIB, Musixmatch, BetterLyrics, KuGou, etc.) have lyrics for a track.
 *
 * Genius does not offer timestamps, but carries an immense catalogue. The scraper:
 *  1. Searches Genius's open multi-search endpoint (no API key required).
 *  2. Matches the best candidate song and grabs its webpage URL.
 *  3. Fetches the page HTML and extracts lyrics from `data-lyrics-container` tags via Jsoup.
 *  4. Cleans out Genius metadata (headers, translation links, "You might also like", "Embed").
 *  5. Retains section headers (e.g. "[Verse 1]", "[Chorus]") for beautiful formatted display.
 */
object Genius {

    /** Identify the app honestly. Some networks receive a Cloudflare challenge even
     * with this identity; no User-Agent can guarantee access. A block must be
     * reported as a provider failure, never silently as a missing song.
     */
    private const val USER_AGENT = com.music.bitchord.data.NetworkIdentity.USER_AGENT

    private val json by lazy { Json { ignoreUnknownKeys = true; isLenient = true } }

    private val httpClient by lazy {
        Http.client.newBuilder()
            .callTimeout(8, TimeUnit.SECONDS)
            .connectTimeout(4, TimeUnit.SECONDS)
            .build()
    }

    suspend fun lyrics(title: String, artist: String): List<LyricLine>? =
        withContext(Dispatchers.IO) { fetchLyrics(title, artist, ::httpGet) }

    /** Injected transport makes parser, missing-song and failure paths testable offline.
     * Production always uses the real Genius transport, never a test fixture/fake fallback.
     */
    internal suspend fun fetchLyrics(
        title: String, artist: String, get: suspend (String) -> String?,
    ): List<LyricLine>? {
        val cleanTitle = cleanQuery(title)
        val cleanArtist = cleanQuery(artist)

        // If the title is in "Artist - Title" format, extract both parts
        val titleParts = if (cleanTitle.contains(TITLE_SEPARATOR)) {
            cleanTitle.split(TITLE_SEPARATOR, limit = 2)
        } else null

        val extractedTitle = when {
            titleParts != null && titleParts[0].trim().equals(cleanArtist, ignoreCase = true) -> titleParts[1].trim()
            titleParts != null && titleParts[1].trim().equals(cleanArtist, ignoreCase = true) -> titleParts[0].trim()
            titleParts != null && titleParts[0].isNotBlank() && titleParts[1].isNotBlank() -> titleParts[1].trim()
            else -> cleanTitle
        }

        val extractedArtist = when {
            titleParts != null && titleParts[0].trim().equals(cleanArtist, ignoreCase = true) -> cleanArtist
            titleParts != null && titleParts[1].trim().equals(cleanArtist, ignoreCase = true) -> cleanArtist
            titleParts != null && cleanArtist.isBlank() -> titleParts[0].trim()
            else -> cleanArtist
        }

        val titleWithoutBrackets = extractedTitle
            .replace(BRACKETED_CONTENT, " ")
            .replace(NON_ALPHANUMERIC, " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val searchAttempts = mutableListOf<SearchAttempt>()

        // 1. Extracted artist + extracted title
        if (extractedArtist.isNotBlank() && extractedTitle.isNotBlank()) {
            searchAttempts.add(SearchAttempt("$extractedArtist $extractedTitle".trim(), extractedTitle, extractedArtist))
        }

        // 2. Extracted artist + title without brackets
        if (extractedArtist.isNotBlank() && titleWithoutBrackets.isNotBlank() && titleWithoutBrackets != extractedTitle) {
            searchAttempts.add(SearchAttempt("$extractedArtist $titleWithoutBrackets".trim(), titleWithoutBrackets, extractedArtist))
        }

        // 3. Clean artist + clean title (in case separator wasn't Artist - Title)
        if (cleanTitle != extractedTitle) {
            searchAttempts.add(SearchAttempt("$cleanArtist $cleanTitle".trim(), cleanTitle, cleanArtist))
            searchAttempts.add(SearchAttempt(cleanTitle, extractedTitle, extractedArtist))
        }

        // 4. Standalone extracted title without artist prefix
        if (titleWithoutBrackets.isNotBlank()) {
            searchAttempts.add(SearchAttempt(titleWithoutBrackets, titleWithoutBrackets, extractedArtist))
        } else if (extractedTitle.isNotBlank()) {
            searchAttempts.add(SearchAttempt(extractedTitle, extractedTitle, extractedArtist))
        }

        val distinctAttempts = searchAttempts.distinctBy { it.query }

        var songUrl: String? = null
        for (attempt in distinctAttempts) {
            // An HTTP block/outage throws and aborts the whole lookup. Do not issue
            // the same blocked request once for every spelling of the song title.
            songUrl = searchSongUrl(attempt.query, attempt.title, attempt.artist, get)
            if (songUrl != null) break
        }
        val html = get(songUrl ?: return null) ?: return null
        return parseHtml(html)?.takeIf { it.isNotEmpty() }
            ?: throw LyricsProviderUnavailableException("Genius returned an unrecognized lyrics page")
    }

    internal suspend fun searchSongUrl(cleanTitle: String, cleanArtist: String): String? =
        searchSongUrl("$cleanArtist $cleanTitle".trim(), cleanTitle, cleanArtist, ::httpGet)

    private suspend fun searchSongUrl(
        query: String, targetTitle: String, targetArtist: String, get: suspend (String) -> String?,
    ): String? {
        val url = "https://genius.com/api/search/multi?q=${URLEncoder.encode(query, "UTF-8")}"
        val body = get(url) ?: return null
        return parseSearchResponse(body, targetTitle, targetArtist)
    }

    internal fun parseSearchResponse(body: String, title: String, artist: String): String? {
        val response = try {
            json.parseToJsonElement(body).jsonObject["response"]?.jsonObject
                ?: throw IllegalArgumentException("Missing response")
        } catch (failure: Exception) {
            throw LyricsProviderUnavailableException("Genius returned an invalid search response", cause = failure)
        }
        val sections = response["sections"] as? JsonArray
        val hits = if (sections != null) {
            val section = sections.filterIsInstance<JsonObject>().firstOrNull {
                it["type"]?.jsonPrimitive?.contentOrNull == "song"
            }
            // Valid empty search: a response with no song section is a genuine miss.
            section?.get("hits") as? JsonArray ?: JsonArray(emptyList())
        } else response["hits"] as? JsonArray
            ?: throw LyricsProviderUnavailableException("Genius search response has no song results field")
        val candidates = hits.mapNotNull { (it as? JsonObject)?.get("result") as? JsonObject }
        val best = bestMatch(candidates, title, artist)
        return best?.get("url")?.jsonPrimitive?.contentOrNull?.takeIf { value ->
            val url = runCatching { value.toHttpUrl() }.getOrNull()
            url?.scheme == "https" && url.host in setOf("genius.com", "www.genius.com")
        }
    }

    private fun bestMatch(
        candidates: List<JsonObject>,
        targetTitle: String,
        targetArtist: String,
    ): JsonObject? {
        if (candidates.isEmpty()) return null
        val normTitle = targetTitle.lowercase(Locale.ROOT)
        val normArtist = targetArtist.lowercase(Locale.ROOT)

        val scored = candidates.mapNotNull { item ->
            val title = item["title"]?.jsonPrimitive?.contentOrNull?.lowercase(Locale.ROOT) ?: ""
            val artist = item["artist_names"]?.jsonPrimitive?.contentOrNull?.lowercase(Locale.ROOT) ?: ""
            var score = 0

            val titleMatches = normTitle.isNotBlank() && (title == normTitle || title.contains(normTitle) || normTitle.contains(title))
            val artistMatches = normArtist.isNotBlank() && (artist == normArtist || artist.contains(normArtist) || normArtist.contains(artist))

            if (!titleMatches && !artistMatches) return@mapNotNull null

            if (title == normTitle) score += 50
            else if (titleMatches) score += 25

            if (artistMatches) {
                if (artist == normArtist) score += 40
                else score += 20
            }

            // Penalize translations / instrumentals / reviews unless specifically requested
            val path = item["path"]?.jsonPrimitive?.contentOrNull ?: ""
            if (path.contains("translation", ignoreCase = true) && !normTitle.contains("translation")) score -= 30
            if (path.contains("türkçe", ignoreCase = true) || path.contains("polskie-tlumaczenie", ignoreCase = true)) score -= 40
            if (path.contains("tracklist", ignoreCase = true) || path.contains("album-art", ignoreCase = true)) score -= 50

            if (score <= 0) return@mapNotNull null

            Pair(item, score)
        }
        return scored.maxByOrNull { it.second }?.first
    }

    /**
     * Parses the Genius lyrics HTML page using Jsoup, extracting text while preserving
     * stanzas and section headers.
     */
    fun parseHtml(html: String): List<LyricLine>? {
        return runCatching {
            parseHtmlUnsafe(html)
        }.getOrNull()
    }

    private fun parseHtmlUnsafe(html: String): List<LyricLine>? {
        val doc = Jsoup.parse(html)

        // Modern Genius uses data-lyrics-container="true", older pages use div.lyrics
        var containers = doc.select("div[data-lyrics-container=true]")
        if (containers.isEmpty()) {
            containers = doc.select("div.lyrics")
        }
        if (containers.isEmpty()) return null

        val fullTextBuilder = StringBuilder()

        for (container in containers) {
            // Remove headers, contributors, translation links, buttons, ads
            container.select(
                "[data-exclude-from-selection=true], " +
                    ".LyricsHeader__Container, " +
                    ".SongBioPreview__Container, " +
                    ".InreadAd__Container, " +
                    "button, " +
                    "script, " +
                    "style",
            ).remove()

            // Turn <br> into explicit newlines before extracting text
            container.select("br").forEach { it.replaceWith(TextNode("\n")) }
            container.select("p").forEach { it.prepend("\n") }

            val text = container.wholeText()
            if (text.isNotBlank()) {
                fullTextBuilder.append(text).append("\n")
            }
        }

        val rawLyrics = fullTextBuilder.toString()
        if (rawLyrics.isBlank()) return null

        val cleaned = stripArtifacts(rawLyrics)
        return textToLyricLines(cleaned).takeIf { it.isNotEmpty() }
    }

    /**
     * Cleans common web scraper artifacts found on Genius:
     * - "You might also like" insertions
     * - Trailing "Embed" or "[0-9]+Embed"
     * - Non-breaking or zero-width unicode spaces
     */
    fun stripArtifacts(raw: String): String {
        return raw
            // Replace non-breaking / special unicode spaces with normal space
            .replace('\u00A0', ' ')
            .replace('\u200B', ' ')
            .replace('\uFEFF', ' ')
            // Remove "You might also like" mid-text recommendations
            .replace(YOU_MIGHT_ALSO_LIKE, "")
            .trim()
            // Remove trailing "123Embed" or "Embed" at the very end
            .replace(TRAILING_EMBED, "")
            .trim()
    }

    /**
     * Converts clean multi-line lyrics text into structured [LyricLine]s.
     */
    fun textToLyricLines(text: String): List<LyricLine> {
        val lines = text.lines()
        val result = mutableListOf<LyricLine>()
        var lastWasGap = false

        for (rawLine in lines) {
            val line = rawLine.trim().replace(TRAILING_EMBED, "").trim()
            if (line.isEmpty()) {
                if (!lastWasGap && result.isNotEmpty()) {
                    result.add(LyricLine(timeMs = 0L, text = ""))
                    lastWasGap = true
                }
            } else {
                result.add(LyricLine(timeMs = 0L, text = line))
                lastWasGap = false
            }
        }

        // Drop leading and trailing gaps
        while (result.isNotEmpty() && result.first().isGap) result.removeAt(0)
        while (result.isNotEmpty() && result.last().isGap) result.removeAt(result.lastIndex)

        return result
    }

    /**
     * Checks if a line is a section header like "[Verse 1]", "[Chorus]", "[Bridge]".
     */
    fun isSectionHeader(text: String): Boolean = isGeniusSectionHeader(text)

    private fun cleanQuery(text: String): String {
        var cleaned = text
            .replace(DECORATIVE_CHARS, " ")
            .replace(NOISE, " ")
            .replace(PRODUCER_TAGS, " ")
            .substringBefore(" | ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned.ifBlank { text.trim() }
    }

    private data class SearchAttempt(
        val query: String,
        val title: String,
        val artist: String,
    )

    internal fun responseBody(status: Int, body: String, challenged: Boolean = false): String? {
        if (status == 404) return null
        if (challenged || status == 401 || status == 403) {
            throw LyricsProviderUnavailableException("Genius access is temporarily blocked (HTTP $status)", status)
        }
        if (status == 429) {
            throw LyricsProviderUnavailableException("Genius is rate limiting requests (HTTP 429)", status)
        }
        if (status !in 200..299) {
            throw LyricsProviderUnavailableException("Genius is unavailable (HTTP $status)", status)
        }
        if (body.isBlank()) throw LyricsProviderUnavailableException("Genius returned an empty response", status)
        return body
    }

    private suspend fun httpGet(url: String): String? = suspendCancellableCoroutine { continuation ->
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json,text/html;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "en-US,en;q=0.9")
            .build()
        val call = httpClient.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, failure: IOException) {
                if (continuation.isActive) continuation.resumeWithException(
                    LyricsProviderUnavailableException("Genius could not be reached", cause = failure))
            }
            override fun onResponse(call: Call, response: Response) {
                try {
                    val text = response.use {
                        responseBody(it.code, it.body?.string().orEmpty(),
                            it.header("cf-mitigated").equals("challenge", ignoreCase = true))
                    }
                    if (continuation.isActive) continuation.resume(text)
                } catch (cancelled: CancellationException) {
                    continuation.cancel(cancelled)
                } catch (failure: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(
                        if (failure is LyricsProviderUnavailableException) failure
                        else LyricsProviderUnavailableException("Genius response could not be read", cause = failure))
                }
            }
        })
    }

    private val TITLE_SEPARATOR by lazy { Regex("""\s*[-–—:]\s*""") }
    private val DECORATIVE_CHARS by lazy { Regex("""[♪♫★☆【】《》「」~_]""") }
    private val PRODUCER_TAGS by lazy {
        Regex("""(?i)\b(?:prod(?:uced)?\.?(?:\s+by)?)\s+.*$""")
    }
    private val NOISE by lazy {
        Regex(
            """\s*[(\[]\s*(?:from|feat\.?|ft\.?|featuring|with|prod\.?|produced by|official|lyrical|video|audio|remix|music video|visualizer|mv|hd|4k|hq|full song)[^)\]]*[)\]]|""" +
                """\s*\b(?:official\s+(?:music\s+)?(?:video|audio)|lyrical(?:\s+video)?|full\s+song|4k\s+video|hd\s+video|music\s+video)\b""",
            RegexOption.IGNORE_CASE,
        )
    }
    private val BRACKETED_CONTENT by lazy { Regex("""\s*[\(\[].*?[\)\]]""") }
    private val NON_ALPHANUMERIC by lazy { Regex("[^\\p{L}\\p{N}\\s]") }
    private val YOU_MIGHT_ALSO_LIKE by lazy { Regex("""\d*You might also like""", RegexOption.IGNORE_CASE) }
    private val TRAILING_EMBED by lazy { Regex("""\d*Embed\s*$""", RegexOption.IGNORE_CASE) }
}
