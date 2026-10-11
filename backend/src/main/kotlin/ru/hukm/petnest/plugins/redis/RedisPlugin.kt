package ru.hukm.petnest.plugins.redis

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands

lateinit var redis: RedisAsyncCommands<String, String>
    private set

fun Application.configureRedis() {
    val client = RedisClient.create(environment.config.property("redis.url").getString())
    val connection = client.connect()
    redis = connection.async()

    monitor.subscribe(ApplicationStopped) {
        connection.close()
        client.shutdown()
    }
}
