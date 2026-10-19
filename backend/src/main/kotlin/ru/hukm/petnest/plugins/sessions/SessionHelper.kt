package ru.hukm.petnest.plugins.sessions

import io.ktor.server.application.ApplicationCall
import io.ktor.server.sessions.get
import io.ktor.server.sessions.sessions
import ru.hukm.petnest.plugins.statuspages.UnauthorizedException

fun ApplicationCall.currentUserId(): Long =
    sessions.get<UserSession>()?.userId ?: throw UnauthorizedException("Требуется авторизация")
