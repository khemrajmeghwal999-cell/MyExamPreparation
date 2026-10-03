package com.example.myexampreparation.data

data class BackupData(
    val appName: String = "MyExamPreparation",
    val version: Int = 1,
    val backupTimestamp: Long = System.currentTimeMillis(),
    val questions: List<Question> = emptyList(),
    val attempts: List<QuizAttempt> = emptyList(),
    val wrongQuestions: List<Question> = emptyList()
)

data class BackupSummary(
    val isValid: Boolean,
    val appName: String,
    val version: Int,
    val backupTimestamp: Long,
    val questionCount: Int,
    val attemptCount: Int,
    val wrongQuestionCount: Int,
    val message: String
)
