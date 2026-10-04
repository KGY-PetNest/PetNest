package ru.hukm.petnest.modules.users

import org.jetbrains.exposed.v1.core.eq
import ru.hukm.petnest.plugins.database.dbQuery

object UserRepository {
    suspend fun create(fullName: String, phone: String, email: String, passwordHash: String, role: UserRole): User = dbQuery {
        User.new {
            this.fullName = fullName
            this.phone = phone
            this.email = email
            this.passwordHash = passwordHash
            this.role = role
        }
    }

    suspend fun findById(id: Long): User? = dbQuery { User.findById(id) }

    suspend fun findByEmail(email: String): User? = dbQuery {
        User.find { UsersTable.email eq email }.singleOrNull()
    }

    suspend fun existsByEmail(email: String): Boolean = dbQuery {
        !User.find { UsersTable.email eq email }.empty()
    }
}