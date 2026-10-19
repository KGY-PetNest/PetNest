package ru.hukm.petnest.plugins.routing.endpoints

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import ru.hukm.petnest.modules.requests.RequestCreateRequest
import ru.hukm.petnest.modules.requests.RequestService
import ru.hukm.petnest.plugins.sessions.currentUserId
import ru.hukm.petnest.plugins.validation.ValidationException

fun Route.useRequestsRoute() {
    route("/requests") {
        get {
            call.respond(RequestService.list(call.currentUserId()))
        }

        post {
            val request = call.receive<RequestCreateRequest>()
            call.respond(HttpStatusCode.Created, RequestService.create(call.currentUserId(), request))
        }

        route("/{id}") {
            get {
                call.respond(RequestService.get(call.currentUserId(), requestId(call)))
            }

            put {
                val request = call.receive<RequestCreateRequest>()
                call.respond(RequestService.update(call.currentUserId(), requestId(call), request))
            }

            delete {
                RequestService.delete(call.currentUserId(), requestId(call))
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun requestId(call: RoutingCall): Long =
    call.parameters["id"]?.toLongOrNull() ?: throw ValidationException(listOf("Некорректный id заявки"))
