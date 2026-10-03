package com.example.myexampreparation.data

import android.content.Context
import android.util.Log

data class SyncMetadata(
    val lastSyncTimestamp: Long = 0L,
    val lastSyncQuestionCount: Int = 0
)

data class SyncResult(
    val success: Boolean,
    val addedCount: Int = 0,
    val updatedCount: Int = 0,
    val skippedCount: Int = 0,
    val totalCount: Int = 0,
    val message: String
)

object QuestionBankSyncManager {

    private const val TAG = "QuestionBankSyncManager"
    private const val PREF_SYNC_NAME = "question_bank_sync_prefs"
    private const val KEY_LAST_SYNC_TIME = "last_sync_timestamp"
    private const val KEY_LAST_SYNC_COUNT = "last_sync_count"

    fun getSyncMetadata(context: Context): SyncMetadata {
        val prefs = context.getSharedPreferences(PREF_SYNC_NAME, Context.MODE_PRIVATE)
        return SyncMetadata(
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            lastSyncQuestionCount = prefs.getInt(KEY_LAST_SYNC_COUNT, 0)
        )
    }

    fun saveSyncMetadata(context: Context, timestamp: Long, count: Int) {
        context.getSharedPreferences(PREF_SYNC_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .putInt(KEY_LAST_SYNC_COUNT, count)
            .apply()
    }

    /**
     * Converts current QuestionBank questions to Master Question Bank 16-column CSV string.
     */
    fun exportLocalQuestionsToCsv(): String {
        val questions = QuestionBank.getAllQuestions()
        val sb = StringBuilder()

        // Header row
        sb.append("id,subject,topic,quizSetId,quizSetTitle,quizSetSubtitle,question,optionA,optionB,optionC,optionD,correctAnswer,explanation,exam,year,difficulty\n")

        questions.forEach { q ->
            sb.append("${q.id},")
            sb.append("\"${escapeCsv(q.subject)}\",")
            sb.append("\"${escapeCsv(q.topic)}\",")
            sb.append("Set ${q.quizSetId},")
            sb.append("\"${escapeCsv(q.quizSetTitle)}\",")
            sb.append("\"${escapeCsv(q.quizSetSubtitle)}\",")
            sb.append("\"${escapeCsv(q.question)}\",")
            sb.append("\"${escapeCsv(q.optionA)}\",")
            sb.append("\"${escapeCsv(q.optionB)}\",")
            sb.append("\"${escapeCsv(q.optionC)}\",")
            sb.append("\"${escapeCsv(q.optionD)}\",")
            sb.append("\"${escapeCsv(q.correctAnswer)}\",")
            sb.append("\"${escapeCsv(q.explanation)}\",")
            sb.append("\"${escapeCsv(q.exam)}\",")
            sb.append("${q.year ?: ""},")
            sb.append("\"${escapeCsv(q.difficulty)}\"\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }

    /**
     * Robust multiline-aware CSV reader that handles quoted newlines and escaped quotes.
     */
    private fun parseCsvToRows(csvContent: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentColumn = StringBuilder()
        var currentRow = mutableListOf<String>()
        var insideQuotes = false

        var i = 0
        while (i < csvContent.length) {
            val ch = csvContent[i]

            when {
                ch == '"' -> {
                    if (insideQuotes && i + 1 < csvContent.length && csvContent[i + 1] == '"') {
                        currentColumn.append('"')
                        i++ // Skip next quote
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                ch == ',' && !insideQuotes -> {
                    currentRow.add(currentColumn.toString().trim())
                    currentColumn = StringBuilder()
                }
                (ch == '\n' || ch == '\r') && !insideQuotes -> {
                    if (ch == '\r' && i + 1 < csvContent.length && csvContent[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentColumn.toString().trim())
                    currentColumn = StringBuilder()

                    if (currentRow.any { it.isNotEmpty() }) {
                        rows.add(currentRow.toList())
                    }
                    currentRow = mutableListOf()
                }
                else -> {
                    currentColumn.append(ch)
                }
            }
            i++
        }

        if (currentColumn.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentColumn.toString().trim())
            if (currentRow.any { it.isNotEmpty() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }

    /**
     * Parses CSV content, validates questions, merges with local question bank while preserving bookmarks,
     * and saves back to QuestionStorage.
     */
    fun parseAndMergeMasterCsv(context: Context, csvContent: String): SyncResult {
        val rows = parseCsvToRows(csvContent)
        if (rows.size <= 1) {
            return SyncResult(
                success = false,
                message = "Question Bank contains invalid rows or no questions found."
            )
        }

        Log.d(TAG, "Downloaded Master CSV total rows (including header): ${rows.size}")

        val parsedQuestions = mutableListOf<Question>()
        var skippedCount = 0

        // Skip row 0 (header)
        for (i in 1 until rows.size) {
            val columns = rows[i]
            if (columns.size < 16) {
                skippedCount++
                continue
            }

            val id = columns[0].toIntOrNull()
            if (id == null) {
                skippedCount++
                continue
            }

            val year = columns[14].toIntOrNull()
            val quizSetId = columns[3].removePrefix("Set ").trim().toIntOrNull() ?: 0

            val question = Question(
                id = id,
                question = columns[6],
                optionA = columns[7],
                optionB = columns[8],
                optionC = columns[9],
                optionD = columns[10],
                correctAnswer = columns[11],
                explanation = columns[12],
                subject = columns[1],
                topic = columns[2],
                quizSetId = quizSetId,
                quizSetTitle = columns[4],
                quizSetSubtitle = columns[5],
                exam = columns[13],
                year = year,
                difficulty = columns[15]
            )

            parsedQuestions.add(question)
        }

        Log.i(TAG, "Parsed valid questions from Master CSV: ${parsedQuestions.size}")

        if (parsedQuestions.isEmpty()) {
            return SyncResult(
                success = false,
                message = "Question Bank contains invalid rows."
            )
        }

        // Merge with existing local storage
        val existingLocal = QuestionStorage.loadQuestions(context)
        val localMap = existingLocal.associateBy { it.id }.toMutableMap()

        var addedCount = 0
        var updatedCount = 0

        parsedQuestions.forEach { masterQ ->
            val existing = localMap[masterQ.id]
            if (existing != null) {
                // Preserve bookmark state!
                val preservedBookmark = existing.isBookmarked
                localMap[masterQ.id] = masterQ.copy(isBookmarked = preservedBookmark)
                updatedCount++
            } else {
                localMap[masterQ.id] = masterQ
                addedCount++
            }
        }

        val finalQuestions = localMap.values.toList()

        // Non-destructive save
        QuestionStorage.saveQuestions(context, finalQuestions)
        QuestionBank.loadQuestions(context)

        val timestamp = System.currentTimeMillis()
        saveSyncMetadata(context, timestamp, finalQuestions.size)

        return SyncResult(
            success = true,
            addedCount = addedCount,
            updatedCount = updatedCount,
            skippedCount = skippedCount,
            totalCount = finalQuestions.size,
            message = "Question Bank synced successfully.\n\nAdded: $addedCount\nUpdated: $updatedCount\nSkipped: $skippedCount\nTotal: ${finalQuestions.size}"
        )
    }
}
