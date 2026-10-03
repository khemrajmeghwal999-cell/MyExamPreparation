package com.example.myexampreparation.data

data class Question(
    val id: Int,
    val question: String,

    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,

    val correctAnswer: String,

    val explanation: String = "",

    val subject: String = "",
    val topic: String = "",
    val quizSetId: Int = 0,

    val exam: String = "",
    val year: Int? = null,

    val difficulty: String = "Medium",

    val quizSetTitle: String = "",
    val quizSetSubtitle: String = "",

    val isBookmarked: Boolean = false
)