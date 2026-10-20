package com.music.bitchord.ui.components

/** Pure result policy: never claim auto-language support when the provider did not report it. */
internal object AutoVoicePolicy {
    const val MIN_AUTO_LANGUAGE_API = 34
    fun supportsApi(api: Int) = api >= MIN_AUTO_LANGUAGE_API
    data class Candidate(val text: String, val confidence: Float?)
    fun best(words: List<String>, confidence: FloatArray?): Candidate? = words.mapIndexedNotNull { index, word ->
        word.trim().takeIf { it.isNotEmpty() }?.let {
            Candidate(it, confidence?.getOrNull(index)?.takeIf { score -> score.isFinite() && score in 0f..1f })
        }
    }.let { candidates -> candidates.maxByOrNull { it.confidence ?: -1f } }
    fun needsReview(candidate: Candidate, languageVerified: Boolean) =
        !languageVerified || (candidate.confidence?.let { it < 0.55f } == true)
}
