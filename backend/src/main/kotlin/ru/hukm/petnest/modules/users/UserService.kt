package ru.hukm.petnest.modules.users

import kotlinx.serialization.Serializable
import ru.hukm.petnest.plugins.validation.Validatable

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

object UserService {
    fun register(dto: UserRegisterRequest) {

    }
}