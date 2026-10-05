package ru.hukm.petnest.modules.users

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import ru.hukm.petnest.plugins.statuspages.ConflictException
import ru.hukm.petnest.plugins.statuspages.UnauthorizedException
import ru.hukm.petnest.plugins.validation.Validatable
import ru.hukm.petnest.plugins.validation.requireValid

@Serializable
enum class UserRole {
    OWNER, VOLUNTEER
}

@Serializable
data class UserRegisterRequest(
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: UserRole,
) : Validatable {
    init { requireValid() }

    override fun validate() = buildList {
        if (fullName.isBlank() || fullName.split(" ").size != 3) add("Укажите правильное ФИО")
        if (!phone.matches(PHONE)) add("Некорректный номер телефона")
        if (!email.matches(EMAIL)) add("Некорректный e-mail")
        if (password.length !in 4..8) add("Пароль должен быть от 4 до 8 символов")
    }

    companion object {
        private val PHONE = Regex("^\\+?\\d{10,15}$")
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String,
) : Validatable {
    override fun validate() = buildList {
        if (!email.matches(EMAIL)) add("Некорректный e-mail")
        if (password.isBlank()) add("Укажите пароль")
    }

    companion object {
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

object UserService {
    suspend fun register(dto: UserRegisterRequest): User {
        if (UserRepository.existsByEmail(dto.email)) {
            throw ConflictException("Пользователь с таким e-mail уже существует")
        }

        val passwordHash = BCrypt.hashpw(dto.password, BCrypt.gensalt())
        return UserRepository.create(dto.fullName, dto.phone, dto.email, passwordHash, dto.role)
    }

    suspend fun login(dto: UserLoginRequest): User {
        val user = UserRepository.findByEmail(dto.email)
            ?: throw UnauthorizedException("Неверный e-mail или пароль")
        if (!BCrypt.checkpw(dto.password, user.passwordHash)) {
            throw UnauthorizedException("Неверный e-mail или пароль")
        }
        return user
    }
}