package com.example.pet.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pet.MainActivity
import com.example.pet.R
import com.example.pet.data.AppContainer
import com.example.pet.data.AttachmentKind
import com.example.pet.data.ChatMessage
import com.example.pet.data.IncomingMessage
import com.example.pet.data.UserRole
import com.example.pet.data.shortPersonName
import com.example.pet.ui.components.labelFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppForeground {
    @Volatile
    var visible: Boolean = false

    @Volatile
    var openChatId: String? = null
}

data class ChatLink(val chatId: String, val role: UserRole)

object PendingChatLink {
    private const val EXTRA_CHAT_ID = "com.example.pet.extra.CHAT_ID"
    private const val EXTRA_ROLE = "com.example.pet.extra.ROLE"

    private val state = MutableStateFlow<ChatLink?>(null)
    val link: StateFlow<ChatLink?> = state.asStateFlow()

    fun intentFor(context: Context, link: ChatLink): Intent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CHAT_ID, link.chatId)
            putExtra(EXTRA_ROLE, link.role.name)
        }

    fun offer(intent: Intent?) {
        val chatId = intent?.getStringExtra(EXTRA_CHAT_ID) ?: return
        val role = intent.getStringExtra(EXTRA_ROLE)
            ?.let { name -> UserRole.entries.firstOrNull { it.name == name } }
            ?: return
        intent.removeExtra(EXTRA_CHAT_ID)
        intent.removeExtra(EXTRA_ROLE)
        state.value = ChatLink(chatId, role)
    }

    fun consume() {
        state.value = null
    }
}

object ChatNotifier {
    const val CHANNEL_ID = "chat_messages"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.text_25_1),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.text_25_2)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun acceptsRole(role: UserRole): Boolean =
        AppContainer.settings.sessionRole != null &&
                AppContainer.auth.account.value?.roles?.contains(role) == true

    fun onIncoming(context: Context, incoming: IncomingMessage) {
        if (!acceptsRole(incoming.role)) return
        if (incoming.chat.blocked) return
        if (AppForeground.visible && AppForeground.openChatId == incoming.chat.id) return
        show(
            context = context,
            link = ChatLink(incoming.chat.id, incoming.role),
            title = shortPersonName(incoming.chat.companionName),
            text = preview(context, incoming.message, incoming.role)
        )
    }

    fun show(context: Context, link: ChatLink, title: String, text: String) {
        if (!canNotify(context)) return
        val pendingIntent = PendingIntent.getActivity(
            context,
            link.chatId.hashCode(),
            PendingChatLink.intentFor(context, link),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(link.chatId.hashCode(), notification)
    }

    fun cancel(context: Context, chatId: String) {
        NotificationManagerCompat.from(context).cancel(chatId.hashCode())
    }

    fun cancelAll(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    fun canNotify(context: Context): Boolean {
        val granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun preview(context: Context, message: ChatMessage, role: UserRole): String {
        message.event?.let { event ->
            return context.getString(event.labelFor(role))
        }
        if (message.text.isNotBlank()) return message.text
        return when (message.attachment?.kind) {
            AttachmentKind.Image -> context.getString(R.string.text_10_16)
            AttachmentKind.Pdf -> message.attachment?.name.orEmpty()
            null -> ""
        }
    }
}
