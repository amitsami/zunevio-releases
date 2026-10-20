package com.music.bitchord.data

import com.music.bitchord.data.model.MoodGenre
import com.music.bitchord.data.model.MoodGenreSection

/** Owner-curated discovery topics, independent of upstream app infrastructure. */
object ZunevioDiscovery {
    const val PREFIX = "zunevio:mood:"
    val topics = listOf(
        "chill" to "Chill", "focus" to "Focus", "sleep" to "Sleep",
        "energy" to "Energize", "workout" to "Workout", "romance" to "Romance",
        "party" to "Party", "feel_good" to "Feel good", "road_trip" to "Road trip",
        "rain" to "Rainy day", "pop" to "Pop", "rock" to "Rock",
        "hip_hop" to "Hip-hop", "electronic" to "Electronic",
        "jazz" to "Jazz", "classical" to "Classical",
    )
    fun sections(): List<MoodGenreSection> = listOf(MoodGenreSection(
        title = "Find your mood",
        items = topics.map { (key, title) -> MoodGenre(title, PREFIX + key, null) },
    ))
    fun topicTitle(browseId: String): String? = topics.firstOrNull { PREFIX + it.first == browseId }?.second
    fun artworkKey(browseId: String): String = topics.firstOrNull { PREFIX + it.first == browseId }?.first ?: "chill"

    // These are language/genre tags, not a filter on an artist's nationality.
    private val excludedTags = Regex(
        "(?i)\\b(indian|india|bollywood|hindi|punjabi|tamil|telugu|kannada|malayalam|marathi|gujarati|bhojpuri|desi|haryanvi|rajasthani|bhangra|carnatic|hindustani|ghazal)\\b"
    )
    fun allowedDiscoveryLabel(label: String): Boolean = !excludedTags.containsMatchIn(label)
}
