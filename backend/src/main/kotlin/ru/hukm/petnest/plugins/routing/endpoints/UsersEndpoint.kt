package ru.hukm.petnest.plugins.routing.endpoints

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.useUsersRoute() {
    route("/users") {
        post("/register") {

        }

        post("/login") {

        }
    }
}