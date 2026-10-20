package com.music.bitchord.data

import com.music.bitchord.data.model.Song
import kotlinx.serialization.Serializable

@Serializable
data class TasteSeed(val id: String, val title: String, val artist: String, val art: String? = null)

/** Bounded, account-independent discovery. No device identifiers or account cookies are stored. */
object LocalDiscoveryPolicy {
    const val QUALIFY_MS = 20_000L
    const val MAX_SEEDS = 8
    private val videoIdPattern = Regex("[A-Za-z0-9_-]{11}")
    fun seed(song: Song): TasteSeed? = song.takeIf {
        videoIdPattern.matches(it.videoId)
    }?.let { TasteSeed(it.videoId, it.title, it.artist, it.thumbnailUrl) }
    fun remember(previous: List<TasteSeed>, incoming: TasteSeed): List<TasteSeed> =
        (listOf(incoming) + previous.filterNot { it.id == incoming.id }).take(MAX_SEEDS)
    fun candidates(seeds: List<TasteSeed>, queues: List<List<Song>>): List<Song> {
        val heard = seeds.mapTo(HashSet()) { it.id }
        val seen = HashSet<String>()
        return buildList {
            for (index in 0 until (queues.maxOfOrNull { it.size } ?: 0)) {
                for (queue in queues) queue.getOrNull(index)?.let { song ->
                    if (song.videoId !in heard && song.videoId.isNotBlank() &&
                        song.title.isNotBlank() && seen.add(song.videoId)) add(song)
                }
            }
        }.take(20)
    }
}
