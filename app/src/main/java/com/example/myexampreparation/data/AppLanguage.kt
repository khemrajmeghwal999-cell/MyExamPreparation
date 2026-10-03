package com.example.myexampreparation.data

import android.content.Context

enum class AppLanguage(val code: String, val displayName: String) {
    HINDI("hi", "हिंदी"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) }
                ?: HINDI
        }
    }
}

object AppLanguageStorage {
    private const val PREF_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSelectedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.HINDI.code)
        return AppLanguage.fromCode(code)
    }

    fun saveSelectedLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }
}
