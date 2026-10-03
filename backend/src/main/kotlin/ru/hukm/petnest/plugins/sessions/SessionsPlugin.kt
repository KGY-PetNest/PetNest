package ru.hukm.petnest.plugins.sessions

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.header
import io.lettuce.core.RedisClient
import kotlin.time.Duration.Companion.days

fun Application.configureSessions() {
    val config = environment.config
    val client = RedisClient.create(config.property("redis.url").getString())
    val connection = client.connect()

    monitor.subscribe(ApplicationStopped) {
        connection.close()
        client.shutdown()
    }

    val storage = RedisSessionStorage(
        redis = connection.async(),
        ttl = config.property("session.ttlDays").getString().toInt().days,
    )

    install(Sessions) {
        header<UserSession>(config.property("session.header").getString(), storage)
    }
}
