package com.music.bitchord.data

import android.content.Context
import android.content.SharedPreferences
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.settings.AppSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Only eight qualified YouTube track seeds, stored locally. No account is required. */
object GuestTasteStore {
    private var prefs: SharedPreferences? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val codec = ListSerializer(TasteSeed.serializer())
    private val listened = linkedMapOf<String, Long>()
    private val _seeds = MutableStateFlow<List<TasteSeed>>(emptyList())
    val seeds = _seeds.asStateFlow()
    @Volatile private var generation = 0L
    fun init(context: Context) {
        // Open/restore off the first-frame path. A later listen/reset wins over a stale restore.
        val owner = context.applicationContext
        val before = generation
        scope.launch {
            val p = owner.getSharedPreferences("zunevio_local_taste", Context.MODE_PRIVATE)
            val restored = runCatching { json.decodeFromString(codec, p.getString("seeds", "[]")!!) }.getOrDefault(emptyList())
            synchronized(this@GuestTasteStore) {
                prefs = p
                if (generation == before) _seeds.value = restored.take(LocalDiscoveryPolicy.MAX_SEEDS)
                else p.edit().putString("seeds", json.encodeToString(codec, _seeds.value)).apply()
            }
        }
    }
    @Synchronized fun recordListening(song: Song, deltaMs: Long) {
        if (!AppSettings.localRecommendations.value || deltaMs <= 0) return
        if ((listened[song.videoId] ?: 0L) >= LocalDiscoveryPolicy.QUALIFY_MS) return
        val seed = LocalDiscoveryPolicy.seed(song) ?: return
        val old = listened[seed.id] ?: 0L
        if (old >= LocalDiscoveryPolicy.QUALIFY_MS) return
        val total = (old + deltaMs.coerceAtMost(5_000L)).coerceAtMost(LocalDiscoveryPolicy.QUALIFY_MS)
        listened[seed.id] = total
        while (listened.size > 32) listened.remove(listened.keys.first())
        if (total < LocalDiscoveryPolicy.QUALIFY_MS) return
        generation++
        _seeds.value = LocalDiscoveryPolicy.remember(_seeds.value, seed)
        persist(_seeds.value, generation)
    }
    @Synchronized fun clear() {
        generation++; listened.clear(); _seeds.value = emptyList()
        persist(emptyList(), generation)
    }
    private fun persist(value: List<TasteSeed>, at: Long) {
        scope.launch {
            val encoded = json.encodeToString(codec, value)
            synchronized(this@GuestTasteStore) {
                if (generation == at) prefs?.edit()?.putString("seeds", encoded)?.apply()
            }
        }
    }
}
