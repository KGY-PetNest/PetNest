package com.example.pet

import android.app.Application
import com.example.pet.data.AppContainer
import com.example.pet.notifications.ChatNotifier
import com.example.pet.notifications.PushRegistrar
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PetApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        ChatNotifier.createChannel(this)
        appScope.launch {
            AppContainer.chats.incoming.collect { incoming ->
                ChatNotifier.onIncoming(this@PetApplication, incoming)
            }
        }
        if (AppContainer.settings.sessionRole != null) {
            PushRegistrar.register(this)
        }
    }
}
