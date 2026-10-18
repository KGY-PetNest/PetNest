package ru.hukm.petnest.plugins.sessions

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import ru.hukm.petnest.plugins.redis.redis
import kotlin.time.Duration.Companion.days

fun Application.configureSessions() {
    val config = environment.config
    val storage = RedisSessionStorage(
        redis = redis,
        ttl = config.property("session.ttlDays").getString().toInt().days,
    )

    install(Sessions) {
        header<UserSession>(config.property("session.header").getString(), storage)
    }
}
