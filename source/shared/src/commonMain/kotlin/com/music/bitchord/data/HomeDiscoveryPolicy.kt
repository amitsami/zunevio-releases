package com.music.bitchord.data

import com.music.bitchord.data.model.HomeShelf
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.Song

/** Bounded discovery plan. These are catalogue searches, not fabricated Spotify charts. */
object HomeDiscoveryPolicy {
    const val PREFIX = "zunevio:home:"
    const val INITIAL_COUNT = 4
    const val PAGE_SIZE = 3
    const val MAX_ITEMS = 18
    data class Section(val key: String, val title: String, val query: String, val playlists: Boolean = false)
    // Song-first shelves avoid low-quality, keyword-stuffed community playlists.
    // These describe discovery moods, never fabricated charts or personal claims.
    val sections = listOf(
        Section("hits", "On repeat", "English indie pop songs"),
        Section("playlists", "Soft mornings", "English mellow acoustic songs"),
        Section("chill", "After hours", "English late night alternative rnb songs"),
        Section("pop", "Fresh finds", "English new alternative pop songs"),
        Section("rock", "Indie daydreams", "English dream pop indie rock songs"),
        Section("focus", "Quiet focus", "ambient instrumental piano songs"),
        Section("workout", "Find your rhythm", "English upbeat electronic pop songs"),
        Section("electronic", "Neon evenings", "melodic electronic synth pop songs"),
        Section("jazz", "Slow Sundays", "soft jazz soul songs"),
        Section("acoustic", "Stripped back", "English acoustic singer songwriter songs"),
        Section("night", "Blue hour", "English mellow indie folk songs"),
        Section("mixes", "A little more soul", "English neo soul alternative rnb songs"),
    )
    fun section(browseId: String): Section? = sections.firstOrNull { PREFIX + it.key == browseId }
    fun continuation(offset: Int): String? = if (offset in INITIAL_COUNT until sections.size) PREFIX + "page:" + offset else null
    fun pageOffset(token: String): Int? = token.removePrefix(PREFIX + "page:").toIntOrNull()
        ?.takeIf { token.startsWith(PREFIX + "page:") && it in INITIAL_COUNT until sections.size }
    fun page(offset: Int): List<Section> = if (offset in INITIAL_COUNT until sections.size) sections.drop(offset).take(PAGE_SIZE) else emptyList()

    private val compilationNoise = Regex(
        "(?i)\\b(\\d+\\s*(hours?|hrs?)|non[ -]?stop|full playlist|best of \\d{4}|top \\d{2,}|tiktok compilation|sped up|slowed.*reverb)\\b"
    )
    fun qualityLabel(title: String): Boolean = !compilationNoise.containsMatchIn(title)

    /** Global item de-duplication without inventing content when a provider is sparse. */
    fun uniqueShelves(shelves: List<HomeShelf>): List<HomeShelf> {
        val seen = HashSet<String>()
        val titles = HashSet<String>()
        return shelves.mapNotNull { shelf ->
            if (!titles.add(shelf.title.lowercase())) return@mapNotNull null
            // History is not a discovery claim; preserve it independently of recommendation overlap.
            val history = shelf.title == "Recents"
            val items = shelf.items.asSequence().filter { item ->
                val key = item.videoId?.takeIf { it.isNotBlank() }?.let { "v:$it" }
                    ?: item.browseId?.takeIf { it.isNotBlank() }?.let { "b:$it" }
                key != null && item.title.isNotBlank() &&
                    (history || (qualityLabel(item.title) && seen.add(key)))
            }.distinctBy { it.videoId ?: it.browseId }.take(if (history) shelf.items.size else MAX_ITEMS).toList()
            if (items.isEmpty()) null else shelf.copy(items = items)
        }
    }
}

/** Four distinct recent artists at most; seed storage/qualification remains unchanged. */
object PersonalDiscoveryPolicy {
    const val MORE_LIKE = "More like · "
    const val MAX_RADIOS = 4
    fun isPersonalTitle(title: String) = title == "Made for you" || title.startsWith(MORE_LIKE)
    fun requestSeeds(seeds: List<TasteSeed>): List<TasteSeed> = seeds.distinctBy {
        it.artist.trim().lowercase().ifEmpty { it.id }
    }.take(MAX_RADIOS)

    fun shelves(seeds: List<TasteSeed>, queues: List<List<Song>>): List<HomeShelf> {
        val heard = seeds.mapTo(HashSet()) { it.id }
        val familiarArtists = seeds.map { it.artist.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
        fun clean(queue: List<Song>) = queue.filter {
            it.videoId.isNotBlank() && it.title.isNotBlank() && it.videoId !in heard &&
                ZunevioDiscovery.allowedDiscoveryLabel(it.title + " " + it.artist) &&
                HomeDiscoveryPolicy.qualityLabel(it.title)
        }.distinctBy { it.videoId }
            .distinctBy { it.title.trim().lowercase() + "|" + it.artist.trim().lowercase() }
            .sortedByDescending { if (it.artist.trim().lowercase() in familiarArtists) 1 else 0 }
        val chosen = requestSeeds(seeds)
        val batches = chosen.indices.map { clean(queues.getOrNull(it).orEmpty()) }
        val mixed = diverseMix(LocalDiscoveryPolicy.candidates(seeds, batches)).take(12)
        val used = mixed.mapTo(HashSet()) { it.videoId }
        fun Song.item() = ShelfItem(title, artist, thumbnailUrl, videoId, null)
        return buildList {
            if (mixed.isNotEmpty()) add(HomeShelf("Made for you", mixed.map { it.item() }, "From your on-device listening"))
            chosen.forEachIndexed { index, seed ->
                val tracks = batches[index].filter { it.videoId !in used }.take(10)
                if (tracks.size >= 3) {
                    tracks.forEach { used.add(it.videoId) }
                    add(HomeShelf(MORE_LIKE + seed.artist.ifBlank { seed.title }, tracks.map { it.item() }, "Inspired by your listening"))
                }
            }
        }
    }
    /** Interleave radios, then balance artists before filling sparse results.
     * Never shuffle every recomposition or repeat one track under multiple shelves.
     */
    internal fun diverseMix(tracks: List<Song>): List<Song> {
        val counts = mutableMapOf<String, Int>()
        val preferred = mutableListOf<Song>()
        val overflow = mutableListOf<Song>()
        val titles = mutableSetOf<String>()
        tracks.forEach { song ->
            val artist = song.artist.trim().lowercase()
            val title = song.title.trim().lowercase() + "|" + artist
            if (titles.add(title)) {
                val count = counts[artist] ?: 0
                if (artist.isBlank() || count < 3) {
                    preferred.add(song)
                    counts[artist] = count + 1
                } else overflow.add(song)
            }
        }
        return preferred + overflow
    }

}
