package com.example.pet

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class PetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}