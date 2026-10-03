package com.example.myexampreparation.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun createBackupJson(context: Context): String {
        val questions = QuestionStorage.loadQuestions(context)
        val attempts = QuizAttemptStorage.loadAttempts(context)
        val wrongQuestions = WrongQuestionStorage.loadWrongQuestions(context)

        val rootJson = JSONObject()
        rootJson.put("appName", "MyExamPreparation")
        rootJson.put("version", 1)
        rootJson.put("backupTimestamp", System.currentTimeMillis())

        // Profile
        val profile = UserProfileStorage.getUserProfile(context)
        if (profile.name.isNotBlank()) {
            val profileJson = JSONObject()
            profileJson.put("name", profile.name)
            if (profile.age != null) profileJson.put("age", profile.age)
            if (profile.mobileNumber != null) profileJson.put("mobileNumber", profile.mobileNumber)
            profileJson.put("profileCompleted", profile.profileCompleted)
            rootJson.put("userProfile", profileJson)
        }

        // 1. Questions
        val questionsArray = JSONArray()
        questions.forEach { q ->
            val json = JSONObject()
            json.put("id", q.id)
            json.put("question", q.question)
            json.put("optionA", q.optionA)
            json.put("optionB", q.optionB)
            json.put("optionC", q.optionC)
            json.put("optionD", q.optionD)
            json.put("correctAnswer", q.correctAnswer)
            json.put("explanation", q.explanation)
            json.put("subject", q.subject)
            json.put("topic", q.topic)
            json.put("quizSetId", q.quizSetId)
            json.put("quizSetTitle", q.quizSetTitle)
            json.put("quizSetSubtitle", q.quizSetSubtitle)
            json.put("exam", q.exam)
            if (q.year != null) {
                json.put("year", q.year)
            } else {
                json.put("year", JSONObject.NULL)
            }
            json.put("difficulty", q.difficulty)
            json.put("isBookmarked", q.isBookmarked)
            questionsArray.put(json)
        }
        rootJson.put("questions", questionsArray)

        // 2. Attempts
        val attemptsArray = JSONArray()
        attempts.forEach { a ->
            val json = JSONObject()
            json.put("quizSetId", a.quizSetId)
            json.put("quizSetTitle", a.quizSetTitle)
            json.put("score", a.score)
            json.put("totalQuestions", a.totalQuestions)
            json.put("correct", a.correct)
            json.put("wrong", a.wrong)
            json.put("skipped", a.skipped)
            json.put("accuracy", a.accuracy)
            json.put("timestamp", a.timestamp)
            json.put("timeTakenSeconds", a.timeTakenSeconds)
            attemptsArray.put(json)
        }
        rootJson.put("attempts", attemptsArray)

        // 3. Wrong Questions
        val wrongArray = JSONArray()
        wrongQuestions.forEach { w ->
            val json = JSONObject()
            json.put("id", w.id)
            json.put("question", w.question)
            json.put("optionA", w.optionA)
            json.put("optionB", w.optionB)
            json.put("optionC", w.optionC)
            json.put("optionD", w.optionD)
            json.put("correctAnswer", w.correctAnswer)
            json.put("explanation", w.explanation)
            json.put("subject", w.subject)
            json.put("topic", w.topic)
            json.put("quizSetId", w.quizSetId)
            json.put("quizSetTitle", w.quizSetTitle)
            json.put("quizSetSubtitle", w.quizSetSubtitle)
            json.put("exam", w.exam)
            if (w.year != null) {
                json.put("year", w.year)
            } else {
                json.put("year", JSONObject.NULL)
            }
            json.put("difficulty", w.difficulty)
            json.put("isBookmarked", w.isBookmarked)
            wrongArray.put(json)
        }
        rootJson.put("wrongQuestions", wrongArray)

        return rootJson.toString()
    }

    fun verifyBackupJson(jsonString: String): BackupSummary {
        return try {
            val root = JSONObject(jsonString)
            val appName = root.optString("appName", "")
            val version = root.optInt("version", 0)
            val timestamp = root.optLong("backupTimestamp", 0L)

            if (appName != "MyExamPreparation") {
                return BackupSummary(
                    isValid = false,
                    appName = appName,
                    version = version,
                    backupTimestamp = timestamp,
                    questionCount = 0,
                    attemptCount = 0,
                    wrongQuestionCount = 0,
                    message = "Invalid app backup payload."
                )
            }

            val qArray = root.optJSONArray("questions") ?: JSONArray()
            val aArray = root.optJSONArray("attempts") ?: JSONArray()
            val wArray = root.optJSONArray("wrongQuestions") ?: JSONArray()

            BackupSummary(
                isValid = true,
                appName = appName,
                version = version,
                backupTimestamp = timestamp,
                questionCount = qArray.length(),
                attemptCount = aArray.length(),
                wrongQuestionCount = wArray.length(),
                message = "Backup verification successful."
            )
        } catch (e: Exception) {
            BackupSummary(
                isValid = false,
                appName = "",
                version = 0,
                backupTimestamp = 0L,
                questionCount = 0,
                attemptCount = 0,
                wrongQuestionCount = 0,
                message = "JSON parse error: ${e.message}"
            )
        }
    }

    fun restoreFromBackupJson(context: Context, jsonString: String): Boolean {
        val summary = verifyBackupJson(jsonString)
        if (!summary.isValid) return false

        return try {
            val root = JSONObject(jsonString)

            // 1. Restore Questions
            val qArray = root.optJSONArray("questions") ?: JSONArray()
            val restoredQuestions = mutableListOf<Question>()
            for (i in 0 until qArray.length()) {
                val json = qArray.getJSONObject(i)
                val year = if (json.isNull("year")) null else json.getInt("year")
                restoredQuestions.add(
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
            if (restoredQuestions.isNotEmpty()) {
                QuestionStorage.saveQuestions(context, restoredQuestions)
            }

            // 2. Restore Attempts
            val aArray = root.optJSONArray("attempts") ?: JSONArray()
            val restoredAttempts = mutableListOf<QuizAttempt>()
            for (i in 0 until aArray.length()) {
                val json = aArray.getJSONObject(i)
                restoredAttempts.add(
                    QuizAttempt(
                        quizSetId = json.getInt("quizSetId"),
                        quizSetTitle = json.getString("quizSetTitle"),
                        score = json.getInt("score"),
                        totalQuestions = json.getInt("totalQuestions"),
                        correct = json.getInt("correct"),
                        wrong = json.getInt("wrong"),
                        skipped = json.getInt("skipped"),
                        accuracy = json.getInt("accuracy"),
                        timestamp = json.getLong("timestamp"),
                        timeTakenSeconds = json.optLong("timeTakenSeconds", 0L)
                    )
                )
            }
            if (restoredAttempts.isNotEmpty()) {
                QuizAttemptStorage.restoreAttempts(context, restoredAttempts)
            }

            // 3. Restore Wrong Questions
            val wArray = root.optJSONArray("wrongQuestions") ?: JSONArray()
            val restoredWrong = mutableListOf<Question>()
            for (i in 0 until wArray.length()) {
                val json = wArray.getJSONObject(i)
                val year = if (json.isNull("year")) null else json.getInt("year")
                restoredWrong.add(
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
            if (restoredWrong.isNotEmpty()) {
                WrongQuestionStorage.saveWrongQuestions(context, restoredWrong)
            }

            // 4. Restore Profile
            val profileJson = root.optJSONObject("userProfile")
            if (profileJson != null && !profileJson.isNull("name")) {
                val restoredProfile = UserProfile(
                    name = profileJson.optString("name", ""),
                    age = if (profileJson.isNull("age")) null else profileJson.optInt("age"),
                    mobileNumber = if (profileJson.isNull("mobileNumber")) null else profileJson.optString("mobileNumber"),
                    profileCompleted = profileJson.optBoolean("profileCompleted", true)
                )
                if (restoredProfile.name.isNotBlank()) {
                    UserProfileStorage.saveUserProfile(context, restoredProfile)
                }
            }

            QuestionBank.loadQuestions(context)
            true
        } catch (e: Exception) {
            false
        }
    }
}
