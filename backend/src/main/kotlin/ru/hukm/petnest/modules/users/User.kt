package ru.hukm.petnest.modules.users

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

object UsersTable : LongIdTable("users") {
    val fullName = varchar("full_name", 255)
    val phone = varchar("phone", 20)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val role = enumerationByName("role", 20, UserRole::class)
}

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(UsersTable)

    var fullName by UsersTable.fullName
    var phone by UsersTable.phone
    var email by UsersTable.email
    var passwordHash by UsersTable.passwordHash
    var role by UsersTable.role
}

@Serializable
data class UserResponse(
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
)

fun User.toResponse() = UserResponse(fullName, email, phone, role)
