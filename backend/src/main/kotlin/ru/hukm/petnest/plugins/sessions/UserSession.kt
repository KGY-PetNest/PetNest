package ru.hukm.petnest.plugins.sessions

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val userId: Long)
