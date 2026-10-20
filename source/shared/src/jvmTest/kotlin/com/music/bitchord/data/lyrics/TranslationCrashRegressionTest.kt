package com.music.bitchord.data.lyrics

import com.music.bitchord.data.NetworkIdentity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class TranslationCrashRegressionTest {
    @Test fun suppliedAccentedUserAgentReproducesTheReportedCrash() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            Request.Builder().url("https://example.invalid/").header("User-Agent", "Zunévio/1.7.1").build()
        }
        assertTrue(error.message.orEmpty().contains("Unexpected char"))
    }
    @Test fun currentIdentityIsPrintableAsciiAndAcceptedByOkHttp() {
        assertTrue(NetworkIdentity.USER_AGENT.all { it.code in 32..126 })
        val request = Request.Builder().url("https://example.invalid/")
            .header("User-Agent", NetworkIdentity.USER_AGENT).build()
        assertEquals(NetworkIdentity.USER_AGENT, request.header("User-Agent"))
    }
    @Test fun acceptedIdentityReachesAnActualLocalHttpServer() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("translated"))
            val response = translationBodyOrNull {
                val request = Request.Builder().url(server.url("/translate"))
                    .header("User-Agent", NetworkIdentity.USER_AGENT).build()
                OkHttpClient().newCall(request).execute().use { it.body!!.string() }
            }
            assertEquals("translated", response)
            assertEquals(NetworkIdentity.USER_AGENT, server.takeRequest(2, TimeUnit.SECONDS)!!.getHeader("User-Agent"))
        }
    }
    @Test fun constructionFailureReturnsUnavailableInsteadOfEscapingCoroutine() = runBlocking {
        val result = translationBodyOrNull {
            Request.Builder().url("https://example.invalid/").header("User-Agent", "Zunévio/1.7.1")
            "unreachable"
        }
        assertNull(result)
    }
    @Test fun transportFailureReturnsUnavailable() = runBlocking {
        assertNull(translationBodyOrNull { throw IOException("offline") })
    }
    @Test fun cancellationIsNotSwallowed() {
        val cancellation = CancellationException("track changed")
        val caught = assertThrows(CancellationException::class.java) {
            runBlocking { translationBodyOrNull { throw cancellation } }
        }
        assertSame(cancellation, caught)
    }
    @Test fun successfulBodiesAreNotChanged() = runBlocking {
        assertEquals("[translation]", translationBodyOrNull { "[translation]" })
    }
    @Test fun fatalErrorsAreNotHidden() {
        assertThrows(AssertionError::class.java) {
            runBlocking { translationBodyOrNull { throw AssertionError("fatal") } }
        }
    }
}
