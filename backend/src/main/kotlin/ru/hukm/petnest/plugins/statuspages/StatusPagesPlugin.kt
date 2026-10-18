package ru.hukm.petnest.plugins.statuspages

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import ru.hukm.petnest.plugins.validation.ValidationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val chain = generateSequence<Throwable>(cause) { it.cause }.toList()
            val validation = chain.filterIsInstance<ValidationException>().firstOrNull()
            val missing = chain.filterIsInstance<MissingFieldException>().firstOrNull()
            val serialization = chain.filterIsInstance<SerializationException>().firstOrNull()
            val errors = when {
                validation != null -> validation.errors
                missing != null -> missing.missingFields.map { "Поле '$it' обязательно" }
                serialization != null -> listOf(serialization.toRussian())
                else -> listOf("Некорректные данные запроса")
            }
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to errors))
        }
        exception<ValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("errors" to cause.errors))
        }
        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, mapOf("errors" to listOf(cause.message)))
        }
        exception<UnauthorizedException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("errors" to listOf(cause.message)))
        }
    }
}

private val UNKNOWN_KEY = Regex("unknown key '([^']+)'")
private val ENUM_VALUE = Regex("element with name '([^']+)' at path \\$\\.(\\S+)")
private val FIELD_PATH = Regex("at path:? \\$\\.(\\S+)")

private fun SerializationException.toRussian(): String {
    val text = message.orEmpty()
    UNKNOWN_KEY.find(text)?.let { return "Неизвестное поле '${it.groupValues[1]}'" }
    ENUM_VALUE.find(text)?.let { return "Недопустимое значение '${it.groupValues[1]}' в поле '${it.groupValues[2]}'" }
    if ("EOF" in text) return "Некорректный JSON"
    FIELD_PATH.find(text)?.let { return "Некорректное значение поля '${it.groupValues[1]}'" }
    return "Некорректный JSON"
}
