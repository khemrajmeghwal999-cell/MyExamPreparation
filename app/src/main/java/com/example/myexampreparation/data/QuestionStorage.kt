package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object QuestionStorage {

    private const val PREF_NAME = "question_bank"
    private const val QUESTIONS_KEY = "questions"

    fun saveQuestions(
        context: Context,
        questions: List<Question>
    ) {
        val existingQuestions =
            loadQuestions(context)

        val mergedQuestions =
            (existingQuestions + questions)
                .associateBy {
                    it.id
                }
                .values
                .toList()

        val jsonArray = JSONArray()

        mergedQuestions.forEach { question ->
            val json = JSONObject()

            json.put("id", question.id)
            json.put("question", question.question)

            json.put("optionA", question.optionA)
            json.put("optionB", question.optionB)
            json.put("optionC", question.optionC)
            json.put("optionD", question.optionD)

            json.put(
                "correctAnswer",
                question.correctAnswer
            )

            json.put(
                "explanation",
                question.explanation
            )

            json.put(
                "subject",
                question.subject
            )

            json.put(
                "topic",
                question.topic
            )

            json.put(
                "quizSetId",
                question.quizSetId
            )

            json.put(
                "quizSetTitle",
                question.quizSetTitle
            )

            json.put(
                "quizSetSubtitle",
                question.quizSetSubtitle
            )

            json.put(
                "exam",
                question.exam
            )

            if (question.year != null) {
                json.put(
                    "year",
                    question.year
                )
            } else {
                json.put(
                    "year",
                    JSONObject.NULL
                )
            }

            json.put(
                "difficulty",
                question.difficulty
            )

            json.put(
                "isBookmarked",
                question.isBookmarked
            )

            jsonArray.put(json)
        }

        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                QUESTIONS_KEY,
                jsonArray.toString()
            )
            .apply()
    }

    fun loadQuestions(
        context: Context
    ): List<Question> {

        val preferences =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val savedData =
            preferences.getString(
                QUESTIONS_KEY,
                null
            ) ?: return emptyList()

        val jsonArray =
            JSONArray(savedData)

        val questions =
            mutableListOf<Question>()

        for (i in 0 until jsonArray.length()) {

            val json =
                jsonArray.getJSONObject(i)

            val year =
                if (json.isNull("year")) {
                    null
                } else {
                    json.getInt("year")
                }

            questions.add(
                Question(
                    id = json.getInt("id"),

                    question =
                        json.getString(
                            "question"
                        ),

                    optionA =
                        json.getString(
                            "optionA"
                        ),

                    optionB =
                        json.getString(
                            "optionB"
                        ),

                    optionC =
                        json.getString(
                            "optionC"
                        ),

                    optionD =
                        json.getString(
                            "optionD"
                        ),

                    correctAnswer =
                        json.getString(
                            "correctAnswer"
                        ),

                    explanation =
                        json.optString(
                            "explanation"
                        ),

                    subject =
                        json.optString(
                            "subject"
                        ),

                    topic =
                        json.optString(
                            "topic"
                        ),

                    quizSetId =
                        json.optInt(
                            "quizSetId",
                            0
                        ),

                    quizSetTitle =
                        json.optString(
                            "quizSetTitle"
                        ),

                    quizSetSubtitle =
                        json.optString(
                            "quizSetSubtitle"
                        ),

                    exam =
                        json.optString(
                            "exam"
                        ),

                    year = year,

                    difficulty =
                        json.optString(
                            "difficulty",
                            "Medium"
                        ),

                    isBookmarked =
                        json.optBoolean(
                            "isBookmarked",
                            false
                        )
                )
            )
        }

        return questions
    }

    fun clearQuestions(
        context: Context
    ) {
        context
            .getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .remove(QUESTIONS_KEY)
            .apply()
    }
    fun updateBookmark(
        context: Context,
        questionId: Int,
        isBookmarked: Boolean
    ) {
        val existingQuestions = loadQuestions(context)
        val existingTarget = existingQuestions.find { it.id == questionId }

        val targetQuestion = if (existingTarget != null) {
            existingTarget.copy(isBookmarked = isBookmarked)
        } else {
            QuestionBank.getAllQuestions()
                .find { it.id == questionId }
                ?.copy(isBookmarked = isBookmarked)
        }

        if (targetQuestion != null) {
            saveQuestions(context, listOf(targetQuestion))
            QuestionBank.loadQuestions(context)
        }
    }
}