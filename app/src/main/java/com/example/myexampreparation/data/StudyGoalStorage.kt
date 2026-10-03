package com.example.myexampreparation.data

import android.content.Context
import android.text.format.DateFormat
import org.json.JSONObject
import java.util.Calendar
import java.util.Date
import kotlin.math.max

data class StudyGoalData(
    val dailyGoal: Int = 20,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String = "",
    val todayQuestionsAttempted: Int = 0
)

object StudyGoalStorage {

    private const val PREF_NAME = "study_goals_pref"
    private const val GOAL_KEY = "goal_data"

    fun getGoalData(context: Context): StudyGoalData {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(GOAL_KEY, null) ?: return updateTodayProgress(context, StudyGoalData())

        return try {
            val json = JSONObject(jsonString)
            val data = StudyGoalData(
                dailyGoal = json.optInt("dailyGoal", 20),
                currentStreak = json.optInt("currentStreak", 0),
                longestStreak = json.optInt("longestStreak", 0),
                lastCompletedDate = json.optString("lastCompletedDate", ""),
                todayQuestionsAttempted = json.optInt("todayQuestionsAttempted", 0)
            )
            updateTodayProgress(context, data)
        } catch (e: Exception) {
            updateTodayProgress(context, StudyGoalData())
        }
    }

    fun saveGoalData(context: Context, data: StudyGoalData) {
        val json = JSONObject()
        json.put("dailyGoal", data.dailyGoal)
        json.put("currentStreak", data.currentStreak)
        json.put("longestStreak", data.longestStreak)
        json.put("lastCompletedDate", data.lastCompletedDate)
        json.put("todayQuestionsAttempted", data.todayQuestionsAttempted)

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(GOAL_KEY, json.toString())
            .apply()
    }

    fun setDailyGoal(context: Context, newGoal: Int) {
        val current = getGoalData(context)
        val updated = current.copy(dailyGoal = newGoal)
        saveGoalData(context, updated)
    }

    private fun getTodayString(): String {
        return DateFormat.format("yyyy-MM-dd", Date()).toString()
    }

    private fun getYesterdayString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return DateFormat.format("yyyy-MM-dd", cal.time).toString()
    }

    private fun updateTodayProgress(context: Context, baseData: StudyGoalData): StudyGoalData {
        val todayStr = getTodayString()
        val attempts = QuizAttemptStorage.loadAttempts(context)

        // Calculate total questions attempted today from attempts timestamp
        val todayAttempted = attempts.filter { attempt ->
            val attemptDateStr = DateFormat.format("yyyy-MM-dd", Date(attempt.timestamp)).toString()
            attemptDateStr == todayStr
        }.sumOf { it.totalQuestions }

        var currentStreak = baseData.currentStreak
        var longestStreak = baseData.longestStreak
        var lastCompletedDate = baseData.lastCompletedDate

        val yesterdayStr = getYesterdayString()

        // Check if streak broke (if user missed yesterday and today isn't completed yet)
        if (lastCompletedDate.isNotBlank() && lastCompletedDate != todayStr && lastCompletedDate != yesterdayStr) {
            currentStreak = 0
        }

        // Check if today's goal is completed
        if (todayAttempted >= baseData.dailyGoal && lastCompletedDate != todayStr) {
            if (lastCompletedDate == yesterdayStr) {
                currentStreak += 1
            } else {
                currentStreak = 1
            }
            longestStreak = max(longestStreak, currentStreak)
            lastCompletedDate = todayStr
        }

        val updated = baseData.copy(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastCompletedDate = lastCompletedDate,
            todayQuestionsAttempted = todayAttempted
        )

        saveGoalData(context, updated)
        return updated
    }
}
