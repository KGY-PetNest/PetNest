package ru.hukm.petnest.plugins.mail

import io.ktor.server.application.Application
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

object Mailer {
    private lateinit var session: Session
    private lateinit var sender: InternetAddress

    fun init(host: String, port: Int, user: String, password: String) {
        val props = Properties().apply {
            put("mail.smtp.host", host)
            put("mail.smtp.port", port.toString())
            put("mail.smtp.auth", "true")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.connectiontimeout", "10000")
            put("mail.smtp.timeout", "10000")
        }
        session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(user, password)
        })
        sender = InternetAddress(user, "PetNest", "UTF-8")
    }

    suspend fun send(to: String, subject: String, text: String) = withContext(Dispatchers.IO) {
        val message = MimeMessage(session).apply {
            setFrom(sender)
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject, "UTF-8")
            setText(text, "UTF-8")
        }
        Transport.send(message)
    }
}

fun Application.configureMail() {
    val config = environment.config
    Mailer.init(
        host = config.property("smtp.host").getString(),
        port = config.property("smtp.port").getString().toInt(),
        user = config.property("smtp.user").getString(),
        password = System.getenv("SMTP_PASSWORD") ?: error("SMTP_PASSWORD is not set"),
    )
}
