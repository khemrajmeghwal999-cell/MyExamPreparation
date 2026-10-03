package com.example.myexampreparation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.myexampreparation.data.QuizSet

@Composable
fun PYQQuizScreen(
    config: PYQConfig,
    onBack: () -> Unit
) {
    val pyqQuizSet = remember(config) {
        QuizSet(
            id = -100,
            title = "${config.exam} — ${config.topic}",
            subtitle = "${config.questions.size} PYQ Questions (${config.exam})",
            questions = config.questions
        )
    }

    QuizScreen(
        quizSet = pyqQuizSet,
        onBack = onBack
    )
}
