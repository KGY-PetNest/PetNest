package com.example.pet.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.pet.data.Account
import com.example.pet.data.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

class RemoteAuthRepository(
    context: Context,
    restoredRole: UserRole?,
    private val baseUrl: String = BASE_URL
) : AuthRepository {

    private class Pending(
        val name: String, val phone: String, val email: String,
        val password: String, val role: UserRole
    )
    private class Response(val code: Int, val body: String, val sessionId: String?)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private var pending: Pending? = null

    private val current = MutableStateFlow(restore(restoredRole))
    override val account: StateFlow<Account?> = current.asStateFlow()

    val sessionId: String? get() = prefs.getString(KEY_SESSION, null)

    override suspend fun register(
        name: String, phone: String, email: String, password: String, role: UserRole
    ): Result<Unit> = runCatching {
        val key = normalize(email)
        val body = JSONObject()
            .put("fullName", name)
            .put("phone", phone)
            .put("email", key)
            .put("password", password)
            .put("role", role.name.uppercase())
        val response = post("/users/register", body)
        when (response.code) {
            202 -> pending = Pending(name, phone, key, password, role)
            409 -> throw AccountExistsException()
            else -> throw Exception(errorText(response))
        }
    }

    override suspend fun resendCode(): Result<Unit> {
        val p = pending ?: return Result.failure(UnsupportedOperationException("Нет ожидающей регистрации"))
        return register(p.name, p.phone, p.email, p.password, p.role)
    }

    override suspend fun confirmCode(code: String): Result<Unit> = runCatching {
        val p = pending ?: throw UnsupportedOperationException("Нет ожидающей регистрации")
        val response = post("/users/register/confirm", JSONObject().put("email", p.email).put("code", code))
        if (response.code != 201) throw Exception(errorText(response))
        val session = response.sessionId ?: throw Exception("Сервер не вернул сессию")

        val account = Account(p.email, setOf(p.role), p.role)
        prefs.edit { putString(KEY_SESSION, session) }
        save(account)
        pending = null
        current.value = account
    }

    override suspend fun login(email: String, password: String): Result<UserRole> = runCatching {
        val key = normalize(email)
        val response = post("/users/login", JSONObject().put("email", key).put("password", password))
        when (response.code) {
            200 -> Unit
            401 -> throw AccountNotFoundException()
            else -> throw Exception(errorText(response))
        }
        val session = response.sessionId ?: throw Exception("Сервер не вернул сессию")

        val me = get("/users/me", session)
        if (me.code != 200) throw Exception(errorText(me))
        val serverRole = JSONObject(me.body).getString("role")
        val role = UserRole.entries.first { it.name.equals(serverRole, ignoreCase = true) }

        val account = Account(key, setOf(role), role)
        prefs.edit { putString(KEY_SESSION, session) }
        save(account)
        current.value = account
        role
    }

    override suspend fun logout() {
        prefs.edit { remove(KEY_SESSION); remove(KEY_EMAIL) }
        current.value = null
    }

    override suspend fun addRole(role: UserRole): Result<Unit> {
        val account = current.value ?: return Result.failure(IllegalStateException("Not signed in"))
        val updated = account.copy(roles = account.roles + role, lastRole = role)
        save(updated)
        current.value = updated
        return Result.success(Unit)
    }

    override suspend fun switchRole(role: UserRole): Result<Unit> {
        val account = current.value ?: return Result.failure(IllegalStateException("Not signed in"))
        if (role !in account.roles) return Result.failure(IllegalStateException("Role is not available"))
        val updated = account.copy(lastRole = role)
        save(updated)
        current.value = updated
        return Result.success(Unit)
    }

    // на бэкенде этих эндпоинтов пока нет
    override suspend fun changeEmail(email: String) = unsupported()
    override suspend fun requestPasswordReset(target: String) = unsupported()
    override suspend fun resetPassword(newPassword: String) = unsupported()
    override suspend fun changePassword(currentPassword: String, newPassword: String) = unsupported()

    private fun unsupported(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Не поддерживается бэкендом"))

    private fun restore(role: UserRole?): Account? {
        if (role == null) return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return Account("", setOf(role), role)
        val roles = (readRoles(email) ?: emptySet()) + role
        return Account(email, roles, role)
    }

    private fun readRoles(email: String): Set<UserRole>? =
        prefs.getStringSet(KEY_ROLES + email, null)
            ?.mapNotNull { n -> UserRole.entries.firstOrNull { it.name == n } }
            ?.toSet()
            ?.takeIf { it.isNotEmpty() }

    private fun save(account: Account) {
        if (account.email.isEmpty()) return
        prefs.edit {
            putString(KEY_EMAIL, account.email)
            putStringSet(KEY_ROLES + account.email, account.roles.map { it.name }.toSet())
        }
    }

    private suspend fun post(path: String, json: JSONObject): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl + path)
            .post(json.toString().toRequestBody(jsonType))
            .apply { sessionId?.let { header("X-Session-Id", it) } }
            .build()
        client.newCall(request).execute().use {
            Response(it.code, it.body?.string().orEmpty(), it.header("X-Session-Id"))
        }
    }

    private suspend fun get(path: String, session: String): Response = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl + path)
            .header("X-Session-Id", session)
            .build()
        client.newCall(request).execute().use {
            Response(it.code, it.body?.string().orEmpty(), it.header("X-Session-Id"))
        }
    }

    private fun errorText(response: Response): String = runCatching {
        val errors = JSONObject(response.body).getJSONArray("errors")
        (0 until errors.length()).joinToString("\n") { errors.getString(it) }
    }.getOrDefault("Ошибка ${response.code}")

    private fun normalize(email: String) = email.trim().lowercase(Locale.ROOT)

    companion object {
        const val BASE_URL = "https://petnest.hukm.dev"
        private const val PREFS_NAME = "pet_auth"
        private const val KEY_SESSION = "session_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLES = "roles_"
    }
}