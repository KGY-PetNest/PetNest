package com.example.pet.data.repository

import android.content.Context
import androidx.core.content.edit
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

    private companion object {
        const val PREFS_NAME = "pet_settings"
        const val KEY_THEME_MODE = "theme_mode"
    }
}