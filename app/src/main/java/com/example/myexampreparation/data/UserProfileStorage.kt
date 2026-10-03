package com.example.myexampreparation.data

import android.content.Context
import android.util.Log

data class UserProfile(
    val name: String = "",
    val age: Int? = null,
    val mobileNumber: String? = null,
    val profileCompleted: Boolean = false
)

object UserProfileStorage {

    private const val TAG = "UserProfileStorage"
    private const val PREF_NAME = "user_profile"

    private const val KEY_NAME = "name"
    private const val KEY_AGE = "age"
    private const val KEY_MOBILE = "mobile_number"
    private const val KEY_COMPLETED = "profile_completed"

    fun getUserProfile(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, "") ?: ""
        val age = if (prefs.contains(KEY_AGE)) prefs.getInt(KEY_AGE, -1).takeIf { it != -1 } else null
        val mobile = prefs.getString(KEY_MOBILE, null)
        val completed = prefs.getBoolean(KEY_COMPLETED, false)

        return UserProfile(
            name = name,
            age = age,
            mobileNumber = mobile,
            profileCompleted = completed
        )
    }

    fun saveUserProfile(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString(KEY_NAME, profile.name.trim())
            .putString(KEY_MOBILE, profile.mobileNumber?.trim()?.takeIf { it.isNotBlank() })
            .putBoolean(KEY_COMPLETED, profile.profileCompleted)

        if (profile.age != null) {
            editor.putInt(KEY_AGE, profile.age)
        } else {
            editor.remove(KEY_AGE)
        }

        editor.apply()
        Log.i(TAG, "User profile saved: name='${profile.name}', age=${profile.age}, completed=${profile.profileCompleted}")
    }

    fun isProfileCompleted(context: Context): Boolean {
        return getUserProfile(context).profileCompleted
    }

    fun clearUserProfile(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        Log.i(TAG, "User profile cleared.")
    }
}
