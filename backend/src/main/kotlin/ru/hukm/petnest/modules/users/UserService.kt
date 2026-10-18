package ru.hukm.petnest.modules.users

import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import ru.hukm.petnest.plugins.mail.Mailer
import ru.hukm.petnest.plugins.statuspages.ConflictException
import ru.hukm.petnest.plugins.statuspages.UnauthorizedException
import ru.hukm.petnest.plugins.validation.Validatable
import ru.hukm.petnest.plugins.validation.ValidationException
import ru.hukm.petnest.plugins.validation.requireValid
import java.security.SecureRandom

const val MAX_ATTEMPTS = 3

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
        if (password.length <= 4) add("Пароль должен быть больше 4 символов")
    }

    companion object {
        private val PHONE = Regex("^\\+?\\d{10,15}$")
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

@Serializable
data class UserConfirmRequest(
    val email: String,
    val code: String,
) : Validatable {
    init { requireValid() }

    override fun validate() = buildList {
        if (email.isBlank()) add("Укажите e-mail")
        if (!code.matches(CODE)) add("Код должен состоять из 6 цифр")
    }

    companion object {
        private val CODE = Regex("^\\d{6}$")
    }
}

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String,
) : Validatable {
    init { requireValid() }

    override fun validate() = buildList {
        if (!email.matches(EMAIL)) add("Некорректный e-mail")
        if (password.isBlank()) add("Укажите пароль")
    }

    companion object {
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

object UserService {
    private val random = SecureRandom()

    suspend fun register(dto: UserRegisterRequest) {
        val email = dto.email.lowercase()

        if (UserRepository.existsByEmail(email)) {
            throw ConflictException("Пользователь с таким e-mail уже существует")
        }

        val passwordHash = BCrypt.hashpw(dto.password, BCrypt.gensalt())

        val code = (random.nextInt(900_000) + 100_000).toString()
        PendingRegistrationRepository.save(
            PendingRegistration(
                fullName = dto.fullName,
                phone = dto.phone,
                email = email,
                passwordHash = passwordHash,
                role = dto.role,
                code = code,
            )
        )
        Mailer.send(
            to = email,
            subject = "Код подтверждения PetNest",
            text = "Ваш код подтверждения: $code\n\nКод действует 10 минут. Если вы не регистрировались в PetNest, просто проигнорируйте это письмо.",
        )
    }

    suspend fun login(dto: UserLoginRequest): User {
        val user = UserRepository.findByEmail(dto.email)
            ?: throw UnauthorizedException("Неверный e-mail или пароль")
        if (!BCrypt.checkpw(dto.password, user.passwordHash)) {
            throw UnauthorizedException("Неверный e-mail или пароль")
        }
        return user
    }

    suspend fun confirm(dto: UserConfirmRequest): User {
        val email = dto.email.lowercase()
        val pending = PendingRegistrationRepository.find(email)
            ?: throw ValidationException(listOf("Код истёк, зарегистрируйтесь заново"))

        if (pending.code != dto.code) {
            val attempts = pending.attempts + 1
            if (attempts >= MAX_ATTEMPTS) {
                PendingRegistrationRepository.delete(email)
                throw ValidationException(listOf("Слишком много попыток, пройдите процесс регестрации заново"))
            }
            PendingRegistrationRepository.update(pending.copy(attempts = attempts))
            throw ValidationException(listOf("Неверный код"))
        }

        PendingRegistrationRepository.delete(email)
        if (UserRepository.existsByEmail(email)) {
            throw ConflictException("Пользователь с таким e-mail уже существует")
        }
        return UserRepository.create(pending.fullName, pending.phone, pending.email, pending.passwordHash, pending.role)
    }
}