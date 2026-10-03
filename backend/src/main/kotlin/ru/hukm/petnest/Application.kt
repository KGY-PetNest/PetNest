package ru.hukm.petnest

import io.ktor.server.application.*
import ru.hukm.petnest.plugins.logging.configureLogging
import ru.hukm.petnest.plugins.ratelimit.configureRateLimit
import ru.hukm.petnest.plugins.routing.configureRouting

fun Application.module() {
    configureRouting()
    configureRateLimit()
    configureLogging()
}
