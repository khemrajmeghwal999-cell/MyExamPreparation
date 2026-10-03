package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object WrongQuestionStorage {

    private const val PREF_NAME = "wrong_questions_store"
    private const val WRONG_KEY = "wrong_questions"

    fun saveWrongQuestions(
        context: Context,
        wrongQuestions: List<Question>
    ) {
        if (wrongQuestions.isEmpty()) return

        val existing = loadWrongQuestions(context)
        val existingMap = existing.associateBy { it.id }.toMutableMap()

        wrongQuestions.forEach { question ->
            existingMap[question.id] = question
        }

        saveAll(context, existingMap.values.toList())
    }

    fun removeCorrectQuestions(
        context: Context,
        correctIds: List<Int>
    ) {
        if (correctIds.isEmpty()) return

        val existing = loadWrongQuestions(context)
        val filtered = existing.filterNot { it.id in correctIds }

        saveAll(context, filtered)
    }

    fun removeWrongQuestion(
        context: Context,
        questionId: Int
    ) {
        val existing = loadWrongQuestions(context)
        val filtered = existing.filterNot { it.id == questionId }

        saveAll(context, filtered)
    }

    private fun saveAll(context: Context, questions: List<Question>) {
        val jsonArray = JSONArray()

        questions.forEach { question ->
            val json = JSONObject()

            json.put("id", question.id)
            json.put("question", question.question)

            json.put("optionA", question.optionA)
            json.put("optionB", question.optionB)
            json.put("optionC", question.optionC)
            json.put("optionD", question.optionD)

            json.put("correctAnswer", question.correctAnswer)
            json.put("explanation", question.explanation)
            json.put("subject", question.subject)
            json.put("topic", question.topic)

            json.put("quizSetId", question.quizSetId)
            json.put("quizSetTitle", question.quizSetTitle)
            json.put("quizSetSubtitle", question.quizSetSubtitle)

            json.put("exam", question.exam)

            if (question.year != null) {
                json.put("year", question.year)
            } else {
                json.put("year", JSONObject.NULL)
            }

            json.put("difficulty", question.difficulty)
            json.put("isBookmarked", question.isBookmarked)

            jsonArray.put(json)
        }

        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(WRONG_KEY, jsonArray.toString())
            .apply()
    }

    fun loadWrongQuestions(context: Context): List<Question> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedData = prefs.getString(WRONG_KEY, null) ?: return emptyList()

        return try {
            val jsonArray = JSONArray(savedData)
            val list = mutableListOf<Question>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)

                val year = if (json.isNull("year")) null else json.getInt("year")

                list.add(
                    Question(
                        id = json.getInt("id"),
                        question = json.getString("question"),
                        optionA = json.getString("optionA"),
                        optionB = json.getString("optionB"),
                        optionC = json.getString("optionC"),
                        optionD = json.getString("optionD"),
                        correctAnswer = json.getString("correctAnswer"),
                        explanation = json.optString("explanation"),
                        subject = json.optString("subject"),
                        topic = json.optString("topic"),
                        quizSetId = json.optInt("quizSetId", 0),
                        quizSetTitle = json.optString("quizSetTitle"),
                        quizSetSubtitle = json.optString("quizSetSubtitle"),
                        exam = json.optString("exam"),
                        year = year,
                        difficulty = json.optString("difficulty", "Medium"),
                        isBookmarked = json.optBoolean("isBookmarked", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearWrongQuestions(context: Context) {
        context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(WRONG_KEY)
            .apply()
    }
}
