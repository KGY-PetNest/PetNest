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
import ru.hukm.petnest.modules.pets.PetCreateRequest
import ru.hukm.petnest.modules.pets.PetService
import ru.hukm.petnest.plugins.sessions.currentUserId
import ru.hukm.petnest.plugins.validation.ValidationException

fun Route.usePetsRoute() {
    route("/pets") {
        get {
            call.respond(PetService.list(call.currentUserId()))
        }

        post {
            val request = call.receive<PetCreateRequest>()
            call.respond(HttpStatusCode.Created, PetService.create(call.currentUserId(), request))
        }

        route("/{id}") {
            get {
                call.respond(PetService.get(call.currentUserId(), petId(call)))
            }

            put {
                val request = call.receive<PetCreateRequest>()
                call.respond(PetService.update(call.currentUserId(), petId(call), request))
            }

            delete {
                PetService.delete(call.currentUserId(), petId(call))
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun petId(call: RoutingCall): Long =
    call.parameters["id"]?.toLongOrNull() ?: throw ValidationException(listOf("Некорректный id питомца"))
