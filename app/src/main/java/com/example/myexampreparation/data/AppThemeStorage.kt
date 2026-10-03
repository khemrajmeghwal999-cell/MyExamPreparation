package com.example.myexampreparation.data

import android.content.Context
import com.example.myexampreparation.ThemeMode

object AppThemeStorage {

    private const val PREF_NAME = "app_theme_prefs"
    private const val KEY_THEME_MODE = "saved_theme_mode"

    fun getSavedThemeMode(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME_MODE, null)
        return if (savedName != null) {
            try {
                ThemeMode.valueOf(savedName)
            } catch (e: Exception) {
                ThemeMode.DARK
            }
        } else {
            // Default theme for first launch is DARK
            ThemeMode.DARK
        }
    }

    fun saveThemeMode(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
}
