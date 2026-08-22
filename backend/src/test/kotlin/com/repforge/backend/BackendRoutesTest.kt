package com.repforge.backend

import com.repforge.backend.auth.UnconfiguredVerifier
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Contract smoke tests. UnconfiguredVerifier = local fail-closed mode:
 * no Firebase credentials on CI, so protected routes must reject with 401.
 */
class BackendRoutesTest {

    @Test
    fun `health returns 200 ok`() = testApplication {
        application { module(UnconfiguredVerifier) }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"status\":\"ok\""))
    }

    @Test
    fun `unauthenticated sync push returns 401`() = testApplication {
        application { module(UnconfiguredVerifier) }
        val response = client.post("/v1/sync/push") {
            contentType(ContentType.Application.Json)
            setBody("""{"operations":[]}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `unauthenticated sync pull returns 401`() = testApplication {
        application { module(UnconfiguredVerifier) }
        val response = client.get("/v1/sync/pull?cursor=2026-01-01T00:00:00Z")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `malformed bearer token returns 401`() = testApplication {
        application { module(UnconfiguredVerifier) }
        val response = client.post("/v1/sync/push") {
            header(HttpHeaders.Authorization, "Bearer not-a-real-token")
            contentType(ContentType.Application.Json)
            setBody("""{"operations":[]}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
