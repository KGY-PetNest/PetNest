package ru.hukm.petnest.plugins.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ru.hukm.petnest.plugins.routing.endpoints.usePetsRoute
import ru.hukm.petnest.plugins.routing.endpoints.useRequestsRoute
import ru.hukm.petnest.plugins.routing.endpoints.useUsersRoute

fun Application.configureRouting() {
    routing {
        useUsersRoute()
        usePetsRoute()
        useRequestsRoute()
    }
}
