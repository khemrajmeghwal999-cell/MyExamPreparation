package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class InAppQuizProgress(
    val subject: String,
    val topic: String,
    val quizSetId: Int,
    val quizSetTitle: String,
    val totalQuestions: Int,
    val currentQuestionIndex: Int,
    val selectedAnswers: List<String?>,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val answeredCount: Int
        get() = selectedAnswers.count { it != null && it != "SKIPPED" }
}

object InAppQuizProgressStorage {

    private const val PREF_NAME = "in_app_quiz_progress_prefs"

    private fun generateKey(subject: String, topic: String, quizSetId: Int): String {
        val s = subject.lowercase().trim().replace("\\s+".toRegex(), "_")
        val t = topic.lowercase().trim().replace("\\s+".toRegex(), "_")
        return "progress_${s}_${t}_$quizSetId"
    }

    fun saveProgress(
        context: Context,
        progress: InAppQuizProgress
    ) {
        val key = generateKey(progress.subject, progress.topic, progress.quizSetId)
        val json = JSONObject()
        json.put("subject", progress.subject)
        json.put("topic", progress.topic)
        json.put("quizSetId", progress.quizSetId)
        json.put("quizSetTitle", progress.quizSetTitle)
        json.put("totalQuestions", progress.totalQuestions)
        json.put("currentQuestionIndex", progress.currentQuestionIndex)

        val answersArray = JSONArray()
        progress.selectedAnswers.forEach { ans ->
            if (ans != null) {
                answersArray.put(ans)
            } else {
                answersArray.put(JSONObject.NULL)
            }
        }
        json.put("selectedAnswers", answersArray)
        json.put("lastUpdated", progress.lastUpdated)

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, json.toString())
            .apply()
    }

    fun getProgress(
        context: Context,
        subject: String,
        topic: String,
        quizSetId: Int
    ): InAppQuizProgress? {
        val key = generateKey(subject, topic, quizSetId)
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString(key, null) ?: return null

        return try {
            val json = JSONObject(savedData)
            val answersArray = json.optJSONArray("selectedAnswers")
            val answersList = mutableListOf<String?>()

            if (answersArray != null) {
                for (i in 0 until answersArray.length()) {
                    if (answersArray.isNull(i)) {
                        answersList.add(null)
                    } else {
                        answersList.add(answersArray.getString(i))
                    }
                }
            }

            InAppQuizProgress(
                subject = json.optString("subject", subject),
                topic = json.optString("topic", topic),
                quizSetId = json.optInt("quizSetId", quizSetId),
                quizSetTitle = json.optString("quizSetTitle", ""),
                totalQuestions = json.optInt("totalQuestions", 0),
                currentQuestionIndex = json.optInt("currentQuestionIndex", 0),
                selectedAnswers = answersList,
                lastUpdated = json.optLong("lastUpdated", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getLatestActiveProgress(context: Context): InAppQuizProgress? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allEntries = prefs.all
        var latest: InAppQuizProgress? = null

        allEntries.values.forEach { value ->
            if (value is String) {
                try {
                    val json = JSONObject(value)
                    val answersArray = json.optJSONArray("selectedAnswers")
                    val answersList = mutableListOf<String?>()

                    if (answersArray != null) {
                        for (i in 0 until answersArray.length()) {
                            if (answersArray.isNull(i)) {
                                answersList.add(null)
                            } else {
                                answersList.add(answersArray.getString(i))
                            }
                        }
                    }

                    val progress = InAppQuizProgress(
                        subject = json.optString("subject", ""),
                        topic = json.optString("topic", ""),
                        quizSetId = json.optInt("quizSetId", 0),
                        quizSetTitle = json.optString("quizSetTitle", ""),
                        totalQuestions = json.optInt("totalQuestions", 0),
                        currentQuestionIndex = json.optInt("currentQuestionIndex", 0),
                        selectedAnswers = answersList,
                        lastUpdated = json.optLong("lastUpdated", 0L)
                    )

                    if (progress.answeredCount > 0) {
                        if (latest == null || progress.lastUpdated > latest!!.lastUpdated) {
                            latest = progress
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse errors
                }
            }
        }

        return latest
    }

    fun clearProgress(
        context: Context,
        subject: String,
        topic: String,
        quizSetId: Int
    ) {
        val key = generateKey(subject, topic, quizSetId)
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(key)
            .apply()
    }
}
