package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class QuizAttempt(
    val quizSetId: Int,
    val quizSetTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
    val accuracy: Int,
    val timestamp: Long,
    val timeTakenSeconds: Long = 0L,
    val subject: String = "",
    val topic: String = ""
)

data class QuizSetStatus(
    val isCompleted: Boolean,
    val latestScore: Int = 0,
    val bestScore: Int = 0,
    val totalQuestions: Int = 0
)

object QuizAttemptStorage {

    private const val PREF_NAME = "quiz_attempts"
    private const val ATTEMPTS_KEY = "attempts"

    fun saveAttempt(
        context: Context,
        attempt: QuizAttempt
    ) {
        val existingAttempts =
            loadAttempts(context).toMutableList()

        existingAttempts.add(attempt)

        val jsonArray = JSONArray()

        existingAttempts.forEach { item ->

            val json = JSONObject()

            json.put("quizSetId", item.quizSetId)
            json.put("quizSetTitle", item.quizSetTitle)
            json.put("score", item.score)
            json.put("totalQuestions", item.totalQuestions)
            json.put("correct", item.correct)
            json.put("wrong", item.wrong)
            json.put("skipped", item.skipped)
            json.put("accuracy", item.accuracy)
            json.put("timestamp", item.timestamp)
            json.put("timeTakenSeconds", item.timeTakenSeconds)
            json.put("subject", item.subject)
            json.put("topic", item.topic)

            jsonArray.put(json)
        }

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                ATTEMPTS_KEY,
                jsonArray.toString()
            )
            .apply()
    }

    fun loadAttempts(
        context: Context
    ): List<QuizAttempt> {

        val preferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val savedData =
            preferences.getString(
                ATTEMPTS_KEY,
                null
            ) ?: return emptyList()

        return try {

            val jsonArray =
                JSONArray(savedData)

            val attempts =
                mutableListOf<QuizAttempt>()

            for (i in 0 until jsonArray.length()) {

                val json =
                    jsonArray.getJSONObject(i)

                attempts.add(
                    QuizAttempt(

                        quizSetId =
                            json.getInt(
                                "quizSetId"
                            ),

                        quizSetTitle =
                            json.getString(
                                "quizSetTitle"
                            ),

                        score =
                            json.getInt(
                                "score"
                            ),

                        totalQuestions =
                            json.getInt(
                                "totalQuestions"
                            ),

                        correct =
                            json.getInt(
                                "correct"
                            ),

                        wrong =
                            json.getInt(
                                "wrong"
                            ),

                        skipped =
                            json.getInt(
                                "skipped"
                            ),

                        accuracy =
                            json.getInt(
                                "accuracy"
                            ),

                        timestamp =
                            json.getLong(
                                "timestamp"
                            ),

                        timeTakenSeconds =
                            json.optLong(
                                "timeTakenSeconds",
                                0L
                            ),

                        subject =
                            json.optString(
                                "subject",
                                ""
                            ),

                        topic =
                            json.optString(
                                "topic",
                                ""
                            )
                    )
                )
            }

            attempts

        } catch (e: Exception) {

            emptyList()
        }
    }

    fun getAttemptsForQuizSet(
        context: Context,
        quizSetId: Int
    ): List<QuizAttempt> {

        return loadAttempts(context)
            .filter {
                it.quizSetId == quizSetId
            }
            .sortedByDescending {
                it.timestamp
            }
    }

    fun getBestScore(
        context: Context,
        quizSetId: Int
    ): Int {

        return getAttemptsForQuizSet(
            context,
            quizSetId
        )
            .maxOfOrNull {
                it.score
            } ?: 0
    }

    fun getTotalAttempts(
        context: Context,
        quizSetId: Int
    ): Int {

        return getAttemptsForQuizSet(
            context,
            quizSetId
        ).size
    }

    fun restoreAttempts(
        context: Context,
        attempts: List<QuizAttempt>
    ) {
        val existingAttempts = loadAttempts(context)
        val merged = (existingAttempts + attempts)
            .associateBy { it.timestamp }
            .values
            .sortedBy { it.timestamp }

        val jsonArray = JSONArray()
        merged.forEach { item ->
            val json = JSONObject()
            json.put("quizSetId", item.quizSetId)
            json.put("quizSetTitle", item.quizSetTitle)
            json.put("score", item.score)
            json.put("totalQuestions", item.totalQuestions)
            json.put("correct", item.correct)
            json.put("wrong", item.wrong)
            json.put("skipped", item.skipped)
            json.put("accuracy", item.accuracy)
            json.put("timestamp", item.timestamp)
            json.put("timeTakenSeconds", item.timeTakenSeconds)
            json.put("subject", item.subject)
            json.put("topic", item.topic)
            jsonArray.put(json)
        }

        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ATTEMPTS_KEY, jsonArray.toString())
            .apply()
    }

    fun getQuizSetStatus(
        context: Context,
        subject: String,
        topic: String,
        quizSetId: Int,
        quizSetTitle: String
    ): QuizSetStatus {
        val attempts = loadAttempts(context).filter { attempt ->
            val subjectMatches = subject.isBlank() || attempt.subject.isBlank() ||
                    attempt.subject.equals(subject, ignoreCase = true)
            val topicMatches = topic.isBlank() || attempt.topic.isBlank() ||
                    attempt.topic.equals(topic, ignoreCase = true)

            val idMatches = (quizSetId > 0 && attempt.quizSetId == quizSetId) ||
                    (quizSetTitle.isNotBlank() && attempt.quizSetTitle.equals(quizSetTitle, ignoreCase = true))

            subjectMatches && topicMatches && idMatches
        }

        if (attempts.isEmpty()) {
            return QuizSetStatus(isCompleted = false)
        }

        val latestAttempt = attempts.maxByOrNull { it.timestamp }!!
        val bestScore = attempts.maxOfOrNull { it.score } ?: 0

        return QuizSetStatus(
            isCompleted = true,
            latestScore = latestAttempt.score,
            bestScore = bestScore,
            totalQuestions = latestAttempt.totalQuestions
        )
    }

    fun clearAttempts(
        context: Context
    ) {

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(ATTEMPTS_KEY)
            .apply()
    }
}
