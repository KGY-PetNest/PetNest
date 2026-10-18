package com.example.pet.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.pet.data.Account
import com.example.pet.data.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class FakeAuthRepository(
    context: Context,
    restoredRole: UserRole?
) : AuthRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val current = MutableStateFlow(restoreAccount(restoredRole))

    override val account: StateFlow<Account?> = current.asStateFlow()

    override val demoLogin: String = DEMO_EMAIL

    override suspend fun login(email: String, password: String): Result<UserRole> {
        delay(FAKE_NETWORK_DELAY_MS)
        val key = normalize(email)
        val found = readAccount(key) ?: return Result.failure(AccountNotFoundException())
        prefs.edit { putString(KEY_CURRENT_EMAIL, key) }
        current.value = found
        return Result.success(found.lastRole)
    }

    override suspend fun register(
        name: String,
        phone: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        val key = normalize(email)
        if (readAccount(key) != null) return Result.failure(AccountExistsException())
        writeAccount(Account(key, setOf(role), role))
        return Result.success(Unit)
    }

    override suspend fun addRole(role: UserRole): Result<Unit> {
        val account = current.value ?: return Result.failure(IllegalStateException("Not signed in"))
        delay(FAKE_NETWORK_DELAY_MS)
        val updated = account.copy(roles = account.roles + role, lastRole = role)
        writeAccount(updated)
        current.value = updated
        return Result.success(Unit)
    }

    override suspend fun switchRole(role: UserRole): Result<Unit> {
        val account = current.value ?: return Result.failure(IllegalStateException("Not signed in"))
        if (role !in account.roles) return Result.failure(IllegalStateException("Role is not available"))
        delay(FAKE_NETWORK_DELAY_MS)
        val updated = account.copy(lastRole = role)
        writeAccount(updated)
        current.value = updated
        return Result.success(Unit)
    }

    override suspend fun changeEmail(email: String): Result<Unit> {
        val account = current.value ?: return Result.failure(IllegalStateException("Not signed in"))
        val key = normalize(email)
        if (key == account.email) return Result.success(Unit)
        delay(FAKE_NETWORK_DELAY_MS)
        if (readAccount(key) != null) return Result.failure(AccountExistsException())
        if (account.email.isNotEmpty()) {
            prefs.edit {
                remove(KEY_ROLES_PREFIX + account.email)
                remove(KEY_LAST_ROLE_PREFIX + account.email)
                putString(KEY_CURRENT_EMAIL, key)
            }
        } else {
            prefs.edit { putString(KEY_CURRENT_EMAIL, key) }
        }
        val updated = account.copy(email = key)
        writeAccount(updated)
        current.value = updated
        return Result.success(Unit)
    }

    override suspend fun confirmCode(code: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun requestPasswordReset(target: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun resetPassword(newPassword: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(Unit)
    }

    override suspend fun logout() {
        prefs.edit { remove(KEY_CURRENT_EMAIL) }
        current.value = null
    }

    private fun restoreAccount(sessionRole: UserRole?): Account? {
        if (sessionRole == null) return null
        val email = prefs.getString(KEY_CURRENT_EMAIL, null)
        val stored = email?.let { readAccount(it) }
        return when {
            stored == null -> Account("", setOf(sessionRole), sessionRole)
            sessionRole in stored.roles -> stored.copy(lastRole = sessionRole)
            else -> stored.copy(roles = stored.roles + sessionRole, lastRole = sessionRole)
        }
    }

    private fun readAccount(email: String): Account? {
        val storedRoles = prefs.getStringSet(KEY_ROLES_PREFIX + email, null)
            ?.mapNotNull { name -> UserRole.entries.firstOrNull { it.name == name } }
            ?.toSet()
        val roles = storedRoles?.takeIf { it.isNotEmpty() } ?: DemoAccounts[email] ?: return null
        val lastRole = prefs.getString(KEY_LAST_ROLE_PREFIX + email, null)
            ?.let { name -> UserRole.entries.firstOrNull { it.name == name } }
            ?.takeIf { it in roles }
            ?: UserRole.entries.first { it in roles }
        return Account(email, roles, lastRole)
    }

    private fun writeAccount(account: Account) {
        if (account.email.isEmpty()) return
        prefs.edit {
            putStringSet(KEY_ROLES_PREFIX + account.email, account.roles.map { it.name }.toSet())
            putString(KEY_LAST_ROLE_PREFIX + account.email, account.lastRole.name)
        }
    }

    private fun normalize(email: String): String = email.trim().lowercase(Locale.ROOT)

    companion object {
        const val DEMO_EMAIL = "demo@petnest.ru"

        private val DemoAccounts = mapOf(
            DEMO_EMAIL to setOf(UserRole.Owner, UserRole.Volunteer),
            "owner@petnest.ru" to setOf(UserRole.Owner),
            "volunteer@petnest.ru" to setOf(UserRole.Volunteer)
        )

        private const val PREFS_NAME = "pet_accounts"
        private const val KEY_CURRENT_EMAIL = "current_email"
        private const val KEY_ROLES_PREFIX = "roles_"
        private const val KEY_LAST_ROLE_PREFIX = "last_role_"
    }
}
