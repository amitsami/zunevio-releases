package com.music.bitchord.data.spotify

import com.music.bitchord.data.model.Song

internal object DeviceFavorites {
    fun toggle(playlists: List<LocalPlaylist>, song: Song): List<LocalPlaylist> {
        val previous = playlists.firstOrNull { it.id == LocalPlaylistStore.FAVORITES_ID }
        val songs = previous?.songs.orEmpty()
        val next = if (songs.any { it.videoId == song.videoId }) {
            songs.filterNot { it.videoId == song.videoId }
        } else listOf(song) + songs
        val favorites = LocalPlaylist(LocalPlaylistStore.FAVORITES_ID,
            previous?.title ?: "Liked songs · On this device", next)
        return listOf(favorites) + playlists.filterNot { it.id == favorites.id }
    }
}
