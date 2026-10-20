package com.music.bitchord.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.saveable.mapSaver

/** Owned above Settings navigation: root, categories and search never share an offset. */
class SettingsScrollPositions private constructor(
    private val states: MutableMap<String, ScrollState>,
) {
    constructor() : this(mutableMapOf())

    fun stateFor(category: String?, searching: Boolean = false): ScrollState {
        // One search slot per page, not one allocation for every typed query.
        val key = (category ?: "root") + if (searching) ":search" else ":page"
        return states.getOrPut(key) { ScrollState(0) }
    }

    internal fun snapshot(): Map<String, Int> = states.mapValues { it.value.value }

    companion object {
        internal fun restore(offsets: Map<String, Int>) = SettingsScrollPositions(
            offsets.mapValues { ScrollState(it.value.coerceAtLeast(0)) }.toMutableMap(),
        )

        val Saver = mapSaver<SettingsScrollPositions>(
            save = { it.snapshot() },
            restore = { saved -> restore(saved.mapValues { it.value as Int }) },
        )
    }
}
