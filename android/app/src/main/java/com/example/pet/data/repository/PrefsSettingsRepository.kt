package com.example.pet.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.pet.data.GeoPoint
import com.example.pet.data.SavedLocation
import com.example.pet.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrefsSettingsRepository(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val theme = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.System
    )

    override val themeMode: StateFlow<ThemeMode> = theme.asStateFlow()

    override fun setThemeMode(mode: ThemeMode) {
        theme.value = mode
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
    }

    private val location = MutableStateFlow(readLocation())

    override val volunteerLocation: StateFlow<SavedLocation?> = location.asStateFlow()

    override fun setVolunteerLocation(location: SavedLocation) {
        this.location.value = location
        prefs.edit {
            putString(KEY_LOCATION_LAT, location.point.lat.toString())
            putString(KEY_LOCATION_LON, location.point.lon.toString())
            putString(KEY_LOCATION_LABEL, location.label)
        }
    }

    private fun readLocation(): SavedLocation? {
        val lat = prefs.getString(KEY_LOCATION_LAT, null)?.toDoubleOrNull() ?: return null
        val lon = prefs.getString(KEY_LOCATION_LON, null)?.toDoubleOrNull() ?: return null
        return SavedLocation(GeoPoint(lat, lon), prefs.getString(KEY_LOCATION_LABEL, null).orEmpty())
    }

    private companion object {
        const val PREFS_NAME = "pet_settings"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LOCATION_LAT = "volunteer_location_lat"
        const val KEY_LOCATION_LON = "volunteer_location_lon"
        const val KEY_LOCATION_LABEL = "volunteer_location_label"
    }
}