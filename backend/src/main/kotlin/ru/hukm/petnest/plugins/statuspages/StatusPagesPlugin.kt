package ru.hukm.petnest.plugins.statuspages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import ru.hukm.petnest.plugins.validation.ValidationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val validation = generateSequence<Throwable>(cause) { it.cause }
                .filterIsInstance<ValidationException>()
                .firstOrNull()
            val errors = validation?.errors ?: listOf("Некорректные данные запроса")
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to errors))
        }
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to cause.errors))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, mapOf("errors" to listOf(cause.message)))
        }
    }
}
