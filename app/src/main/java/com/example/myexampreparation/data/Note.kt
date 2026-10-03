package com.example.myexampreparation.data

data class Note(
    val id: Int,
    val subject: String,
    val topic: String,
    val title: String,
    val content: String,
    val summaryPoints: List<String> = emptyList(),
    val isBookmarked: Boolean = false
)
