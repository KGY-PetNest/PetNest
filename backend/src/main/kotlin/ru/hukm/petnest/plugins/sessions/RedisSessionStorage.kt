package ru.hukm.petnest.plugins.sessions

import io.ktor.server.sessions.SessionStorage
import io.lettuce.core.GetExArgs
import io.lettuce.core.SetArgs
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.future.await
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class RedisSessionStorage(
    private val redis: RedisAsyncCommands<String, String>,
    private val ttl: Duration,
) : SessionStorage {
    override suspend fun write(id: String, value: String) {
        redis.set(key(id), value, SetArgs().ex(ttl.toJavaDuration())).await()
    }

    override suspend fun read(id: String): String =
        redis.getex(key(id), GetExArgs().ex(ttl.toJavaDuration())).await()
            ?: throw NoSuchElementException("Session $id not found")

    override suspend fun invalidate(id: String) {
        redis.del(key(id)).await()
    }

    private fun key(id: String) = "session:$id"
}
