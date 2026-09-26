package ru.hukm.petnest

import io.ktor.server.application.*
import ru.hukm.petnest.plugins.configureMonitoring
import ru.hukm.petnest.plugins.configureRouting
import ru.hukm.petnest.plugins.configureSerialization
import ru.hukm.petnest.plugins.configureStatusPages

fun Application.module() {
    configureMonitoring()
    configureSerialization()
    configureStatusPages()
    configureRouting()
}
