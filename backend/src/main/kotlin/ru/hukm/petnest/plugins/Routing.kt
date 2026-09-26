package ru.hukm.petnest.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String)

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("PetNest API")
        }
        get("/health") {
            call.respond(HealthResponse("ok"))
        }
    }
}
