package ru.hukm.petnest.plugins.routing.endpoints

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.lettuce.core.KillArgs.Builder.user
import ru.hukm.petnest.modules.users.UserConfirmRequest
import ru.hukm.petnest.modules.users.UserLoginRequest
import ru.hukm.petnest.modules.users.UserRegisterRequest
import ru.hukm.petnest.modules.users.UserService
import ru.hukm.petnest.plugins.sessions.UserSession

fun Route.useUsersRoute() {
    route("/users") {
        post("/register") {
            val request = call.receive<UserRegisterRequest>()
            UserService.register(request)

            call.respond(HttpStatusCode.Accepted)
        }

        post("/register/confirm") {
            val request = call.receive<UserConfirmRequest>()
            val user = UserService.confirm(request)
            call.sessions.set(UserSession(user.id.value))
            call.respond(HttpStatusCode.Created)
        }

        post("/login") {
            val request = call.receive<UserLoginRequest>()
            val user = UserService.login(request)
            call.sessions.set(UserSession(user.id.value))
            call.respond(HttpStatusCode.OK)
        }
    }
}