package ru.hukm.petnest

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `root responds`() = testApplication {
        application { module() }

        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("PetNest API", response.bodyAsText())
    }

    @Test
    fun `health responds ok`() = testApplication {
        application { module() }

        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"status":"ok"}""", response.bodyAsText())
    }
}
