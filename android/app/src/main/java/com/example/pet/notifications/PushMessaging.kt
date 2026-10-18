package com.example.pet.notifications

import android.content.Context
import com.example.pet.data.AppContainer
import com.example.pet.data.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val pushScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

object PushRegistrar {
    fun register(context: Context) {
        if (!isFirebaseReady(context)) return
        runCatching {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                pushScope.launch { AppContainer.push.registerToken(token) }
            }
        }
    }

    fun signOut(context: Context) {
        pushScope.launch {
            AppContainer.auth.logout()
            AppContainer.push.unregister()
        }
        if (isFirebaseReady(context)) {
            runCatching { FirebaseMessaging.getInstance().deleteToken() }
        }
    }

    private fun isFirebaseReady(context: Context): Boolean =
        runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
}

class PetMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        if (AppContainer.settings.sessionRole == null) return
        pushScope.launch { AppContainer.push.registerToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data[KEY_TYPE] != TYPE_CHAT_MESSAGE) return
        val chatId = data[KEY_CHAT_ID] ?: return
        val role = data[KEY_ROLE]
            ?.let { name -> UserRole.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
            ?: return
        if (!ChatNotifier.acceptsRole(role)) return
        if (AppForeground.visible && AppForeground.openChatId == chatId) return
        ChatNotifier.show(
            context = this,
            link = ChatLink(chatId, role),
            title = data[KEY_TITLE].orEmpty(),
            text = data[KEY_BODY].orEmpty()
        )
    }

    private companion object {
        const val KEY_TYPE = "type"
        const val KEY_CHAT_ID = "chatId"
        const val KEY_ROLE = "role"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val TYPE_CHAT_MESSAGE = "chat_message"
    }
}
