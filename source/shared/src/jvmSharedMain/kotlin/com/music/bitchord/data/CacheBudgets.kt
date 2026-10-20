package com.music.bitchord.data

/** Disposable caches only. Never applied to saved downloads or account/session data. */
object CacheBudgets {
    const val DEFAULT_AUDIO_BYTES = 128L * 1024 * 1024
    const val IMAGE_BYTES = 64L * 1024 * 1024
    const val CANVAS_BYTES = 64L * 1024 * 1024
    const val REMOTE_ART_BYTES = 32L * 1024 * 1024

    fun obsoleteInstaller(candidatePackage: String?, installedPackage: String,
        candidateVersion: Long?, installedVersion: Long): Boolean =
        candidatePackage == installedPackage && candidateVersion != null &&
            candidateVersion > 0 && candidateVersion <= installedVersion

    fun abandonedPartial(name: String, modifiedMs: Long, nowMs: Long): Boolean =
        name == "download.apk" && modifiedMs > 0 && nowMs >= modifiedMs &&
            nowMs - modifiedMs > 86_400_000L
}
