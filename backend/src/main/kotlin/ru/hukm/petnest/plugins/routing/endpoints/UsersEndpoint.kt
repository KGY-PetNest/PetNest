package ru.hukm.petnest.plugins.routing.endpoints

import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.hukm.petnest.modules.users.UserLoginRequest
import ru.hukm.petnest.modules.users.UserRegisterRequest
import ru.hukm.petnest.modules.users.UserService

fun Route.useUsersRoute() {
    route("/users") {
        post("/register") {
            val request = call.receive<UserRegisterRequest>()
            UserService.register(request)
        }

        post("/login") {
            val request = call.receive<UserLoginRequest>()
            UserService.login(request)
        }
    }
}