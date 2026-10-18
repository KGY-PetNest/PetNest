package ru.hukm.petnest.modules.users

import io.lettuce.core.SetArgs
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.hukm.petnest.plugins.redis.redis
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Serializable
data class PendingRegistration(
    val fullName: String,
    val phone: String,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val code: String,
    val attempts: Int = 0,
)

object PendingRegistrationRepository {
    private val TTL = 10.minutes

    private fun key(email: String) = "register:$email"

    suspend fun save(pending: PendingRegistration) {
        redis.set(key(pending.email), Json.encodeToString(pending), SetArgs().ex(TTL.toJavaDuration())).await()
    }

    suspend fun find(email: String): PendingRegistration? =
        redis.get(key(email)).await()?.let { Json.decodeFromString<PendingRegistration>(it) }

    suspend fun update(pending: PendingRegistration) {
        redis.set(key(pending.email), Json.encodeToString(pending), SetArgs().keepttl()).await()
    }

    suspend fun delete(email: String) {
        redis.del(key(email)).await()
    }
}
