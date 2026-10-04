package ru.hukm.petnest

import io.ktor.server.application.*
import ru.hukm.petnest.plugins.database.configureDatabase
import ru.hukm.petnest.plugins.logging.configureLogging
import ru.hukm.petnest.plugins.metrics.configureMetrics
import ru.hukm.petnest.plugins.ratelimit.configureRateLimit
import ru.hukm.petnest.plugins.routing.configureRouting
import ru.hukm.petnest.plugins.serialization.configureSerialization
import ru.hukm.petnest.plugins.sessions.configureSessions
import ru.hukm.petnest.plugins.statuspages.configureStatusPages
import ru.hukm.petnest.plugins.validation.configureValidation

fun Application.module() {
    configureDatabase()
    configureSessions()
    configureMetrics()
    configureSerialization()
    configureValidation()
    configureStatusPages()
    configureRouting()
    configureRateLimit()
    configureLogging()
}
