package com.music.bitchord

import com.music.bitchord.data.ReleasePolicy
import org.junit.Assert.*
import org.junit.Test

class ReleasePolicyTest {
    @Test fun acceptsRepositoryOrCanonicalGithubLink() {
        assertEquals("amitsami/music", ReleasePolicy.repository("amitsami/music"))
        assertEquals("amitsami/music", ReleasePolicy.repository("https://github.com/amitsami/music/"))
        assertEquals("amitsami/music", ReleasePolicy.repository("https://github.com/amitsami/music.git"))
    }
    @Test fun rejectsForeignHostsPathsTokensAndTraversal() {
        for (input in listOf("http://github.com/a/b", "https://evil.test/a/b", "https://github.com.evil/a/b", "a/b/releases", "a/b?token=secret", "a/..", "a/b#x", "", "a", "a/b/c", "a/b\\evil", "a/b@evil"))
            assertNull(input, ReleasePolicy.repository(input))
    }
    @Test fun comparesSemanticVersionsNotLexically() {
        assertTrue(ReleasePolicy.isNewer("v1.1.2", "1.1.1"))
        assertTrue(ReleasePolicy.isNewer("1.10.0", "1.9.9"))
        assertTrue(ReleasePolicy.isNewer("2.0.0", "1.99.99"))
        assertFalse(ReleasePolicy.isNewer("1.1.1", "1.1.1"))
        assertFalse(ReleasePolicy.isNewer("1.1.0", "1.1.1"))
        assertFalse(ReleasePolicy.isNewer("not a version", "1.1.1"))
        assertFalse(ReleasePolicy.isNewer("9999999999999999999.0.0", "1.1.1"))
    }
    @Test fun stableReleaseReplacesSameVersionBeta() {
        assertTrue(ReleasePolicy.isNewer("v1.1.1", "1.1.1-beta1"))
        assertFalse(ReleasePolicy.isNewer("1.1.1-beta1", "1.1.1"))
    }
    @Test fun artifactMustBelongToConfiguredRepository() {
        assertTrue(ReleasePolicy.assetUrl("https://github.com/a/b/releases/download/v1.1.2/Zunevio-universal.apk", "a/b"))
        assertFalse(ReleasePolicy.assetUrl("https://github.com/evil/b/releases/download/v1.1.2/x.apk", "a/b"))
        assertFalse(ReleasePolicy.assetUrl("http://github.com/a/b/releases/download/v1/x.apk", "a/b"))
        assertFalse(ReleasePolicy.assetUrl("https://github.com/a/b/releases/download/v1/x.apk?redirect=evil", "a/b"))
        assertFalse(ReleasePolicy.releaseUrl("https://github.com/a/b/releases.evil/tag/v1", "a/b"))
        assertTrue(ReleasePolicy.releaseUrl("https://github.com/a/b/releases/tag/v1.2.0", "a/b"))
    }
    @Test fun requiresUnambiguousUniversalApkAndSha256() {
        assertTrue(ReleasePolicy.universalApk("Zunevio-1.1.2-universal.apk"))
        assertFalse(ReleasePolicy.universalApk("Zunevio-arm64.apk"))
        assertFalse(ReleasePolicy.universalApk("universal.apk.exe"))
        assertEquals("a".repeat(64), ReleasePolicy.sha256("sha256:" + "A".repeat(64)))
        assertNull(ReleasePolicy.sha256(null))
        assertNull(ReleasePolicy.sha256("sha256:bad"))
        assertNull(ReleasePolicy.sha256("g".repeat(64)))
    }
    @Test fun githubRedirectAllowlistIsExact() {
        assertTrue(ReleasePolicy.allowedDownloadHost("release-assets.githubusercontent.com"))
        assertFalse(ReleasePolicy.allowedDownloadHost("github.com.evil.test"))
        assertFalse(ReleasePolicy.allowedDownloadHost("evil.githubusercontent.com"))
        assertFalse(ReleasePolicy.allowedDownloadHost("127.0.0.1"))
    }
}
