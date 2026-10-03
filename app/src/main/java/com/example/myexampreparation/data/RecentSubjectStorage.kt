package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONObject

object RecentSubjectStorage {

    private const val PREF_NAME = "recent_subject_prefs"
    private const val KEY_SUBJECT_TIMESTAMPS = "subject_timestamps"

    fun recordSubjectUsage(context: Context, subjectName: String?) {
        if (subjectName.isNullOrBlank()) return
        val cleanName = subjectName.trim().lowercase()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString(KEY_SUBJECT_TIMESTAMPS, null)

        val json = try {
            if (savedData != null) JSONObject(savedData) else JSONObject()
        } catch (e: Exception) {
            JSONObject()
        }

        json.put(cleanName, System.currentTimeMillis())

        prefs.edit().putString(KEY_SUBJECT_TIMESTAMPS, json.toString()).apply()
    }

    fun getSubjectUsageTimestamp(context: Context, subject: Subject): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString(KEY_SUBJECT_TIMESTAMPS, null) ?: return 0L

        return try {
            val json = JSONObject(savedData)
            val nameTime = json.optLong(subject.name.trim().lowercase(), 0L)
            val engTime = if (subject.englishName.isNotBlank()) json.optLong(subject.englishName.trim().lowercase(), 0L) else 0L
            maxOf(nameTime, engTime)
        } catch (e: Exception) {
            0L
        }
    }

    fun sortSubjectsByRecentUsage(context: Context, subjects: List<Subject>): List<Subject> {
        return subjects.sortedWith(
            compareByDescending<Subject> { getSubjectUsageTimestamp(context, it) }
                .thenBy { it.name }
        )
    }

    fun recordTopicUsage(context: Context, subjectName: String?, topicName: String?) {
        if (subjectName.isNullOrBlank() || topicName.isNullOrBlank()) return
        val key = "${subjectName.trim().lowercase()}_${topicName.trim().lowercase()}"
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString("topic_timestamps", null)

        val json = try {
            if (savedData != null) JSONObject(savedData) else JSONObject()
        } catch (e: Exception) {
            JSONObject()
        }

        json.put(key, System.currentTimeMillis())
        prefs.edit().putString("topic_timestamps", json.toString()).apply()
    }

    fun getTopicUsageTimestamp(context: Context, subjectName: String, topicName: String): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString("topic_timestamps", null) ?: return 0L

        return try {
            val json = JSONObject(savedData)
            val key = "${subjectName.trim().lowercase()}_${topicName.trim().lowercase()}"
            json.optLong(key, 0L)
        } catch (e: Exception) {
            0L
        }
    }

    fun sortTopicsByRecentUsage(context: Context, subjectName: String, topics: List<String>): List<String> {
        return topics.sortedWith(
            compareByDescending<String> { getTopicUsageTimestamp(context, subjectName, it) }
                .thenBy { it }
        )
    }
}
