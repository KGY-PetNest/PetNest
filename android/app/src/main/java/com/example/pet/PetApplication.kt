package com.example.pet

import android.app.Application
import com.example.pet.data.AppContainer
import com.yandex.mapkit.MapKitFactory

class PetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}