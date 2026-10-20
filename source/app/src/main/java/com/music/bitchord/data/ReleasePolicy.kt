package com.music.bitchord.data

/** Pure policy: no Android or network dependency, so security rules can be regression-tested. */
object ReleasePolicy {
    fun repository(input: String): String? {
        val value = input.trim().removePrefix("https://github.com/").trimEnd('/').removeSuffix(".git")
        return value.takeIf { Regex("[A-Za-z0-9][A-Za-z0-9-]{0,38}/[A-Za-z0-9_.-]{1,100}").matches(it) &&
            it.substringAfter('/') !in listOf(".", "..") }
    }
    fun isNewer(remote: String, local: String): Boolean {
        fun parts(s: String): Pair<List<Int>, Boolean>? {
            val m = Regex("^[vV]?(\\d+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9.-]+))?(?:\\+[A-Za-z0-9.-]+)?$").matchEntire(s.trim()) ?: return null
            return (1..3).map { m.groupValues[it].toIntOrNull() ?: return null } to m.groupValues[4].isNotEmpty()
        }
        val r = parts(remote) ?: return false
        val l = parts(local) ?: return false
        for (i in 0..2) if (r.first[i] != l.first[i]) return r.first[i] > l.first[i]
        return !r.second && l.second
    }
    fun releaseUrl(url: String, repo: String): Boolean =
        url.startsWith("https://github.com/$repo/releases/tag/") && !url.contains('?') && !url.contains('#') && !url.contains('\\')
    fun assetUrl(url: String, repo: String): Boolean =
        url.startsWith("https://github.com/$repo/releases/download/") && !url.contains('?') && !url.contains('#') && !url.contains('\\')
    fun sha256(digest: String?): String? = digest?.removePrefix("sha256:")?.lowercase()
        ?.takeIf { Regex("[0-9a-f]{64}").matches(it) }
    fun universalApk(name: String): Boolean = name.endsWith(".apk", true) && name.contains("universal", true)
    fun allowedDownloadHost(host: String): Boolean = host in setOf(
        "github.com", "api.github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com")
}
