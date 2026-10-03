package com.example.myexampreparation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamProgressBar
import com.example.myexampreparation.ui.components.ExamButton
import com.example.myexampreparation.ui.components.QuizOptionCard
import com.example.myexampreparation.ui.components.OptionState
import com.example.myexampreparation.ui.components.ExamTopAppBar
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.Question
import com.example.myexampreparation.data.QuestionStorage
import com.example.myexampreparation.data.QuizAttempt
import com.example.myexampreparation.data.QuizAttemptStorage
import com.example.myexampreparation.data.QuizSet

@Composable
fun QuizScreen(
    quizSet: QuizSet,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
    val haptic = LocalHapticFeedback.current
    val questions = remember(quizSet.id) {
        quizSet.questions.map { question ->
            com.example.myexampreparation.data.QuizQuestionSelector.shuffleOptionsSafely(question)
        }
    }

    if (questions.isEmpty()) {
        val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
        val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isHindi) "इस क्विज़ सेट में कोई प्रश्न नहीं है।" else "No questions in this quiz set.",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isHindi) "वापस जाएं" else "Back")
            }
        }
        return
    }

    val qSubject = questions.firstOrNull()?.subject ?: ""
    val qTopic = questions.firstOrNull()?.topic ?: ""

    val savedProgress = remember(quizSet) {
        com.example.myexampreparation.data.InAppQuizProgressStorage.getProgress(context, qSubject, qTopic, quizSet.id)
    }

    var currentQuestion by remember {
        mutableIntStateOf(
            savedProgress?.currentQuestionIndex?.coerceIn(0, questions.size - 1) ?: 0
        )
    }

    var quizFinished by remember {
        mutableStateOf(false)
    }

    val savedAttempts = remember(quizSet.id) {
        QuizAttemptStorage.getAttemptsForQuizSet(
            context = context,
            quizSetId = quizSet.id
        )
    }

    var attemptCount by remember {
        mutableIntStateOf(savedAttempts.size + 1)
    }

    var bestScore by remember {
        mutableIntStateOf(
            QuizAttemptStorage.getBestScore(
                context = context,
                quizSetId = quizSet.id
            )
        )
    }

    val answers = remember {
        mutableStateListOf<String?>().apply {
            if (savedProgress != null && savedProgress.selectedAnswers.size == questions.size) {
                addAll(savedProgress.selectedAnswers)
            } else {
                repeat(questions.size) {
                    add(null)
                }
            }
        }
    }

    LaunchedEffect(currentQuestion, answers.toList()) {
        if (!quizFinished) {
            com.example.myexampreparation.data.InAppQuizProgressStorage.saveProgress(
                context = context,
                progress = com.example.myexampreparation.data.InAppQuizProgress(
                    subject = qSubject,
                    topic = qTopic,
                    quizSetId = quizSet.id,
                    quizSetTitle = quizSet.title,
                    totalQuestions = questions.size,
                    currentQuestionIndex = currentQuestion,
                    selectedAnswers = answers.toList()
                )
            )
        }
    }

    var showResultScreen by remember { mutableStateOf(false) }

    LaunchedEffect(quizFinished) {
        if (quizFinished) {
            val activity = context as? android.app.Activity
            if (activity != null) {
                com.example.myexampreparation.InterstitialAdManager.showAdIfReady(
                    activity = activity,
                    onAdDismissedOrSkipped = {
                        showResultScreen = true
                    }
                )
            } else {
                showResultScreen = true
            }
        }
    }

    if (quizFinished && showResultScreen) {
        val correctCount = questions.indices.count { index ->
            answers[index] == questions[index].correctAnswer
        }

        val wrongCount = questions.indices.count { index ->
            val answer = answers[index]
            answer != null && answer != "SKIPPED" && answer != questions[index].correctAnswer
        }

        val skippedCount = answers.count { it == null || it == "SKIPPED" }
        val attemptedCount = questions.size - skippedCount

        ResultScreen(
            score = correctCount,
            totalQuestions = questions.size,
            wrongAnswers = wrongCount,
            skippedAnswers = skippedCount,
            attemptedQuestions = attemptedCount,
            attemptCount = attemptCount,
            bestScore = bestScore,
            onRetry = {
                for (index in answers.indices) {
                    answers[index] = null
                }
                currentQuestion = 0
                quizFinished = false
                showResultScreen = false

                attemptCount = QuizAttemptStorage.getTotalAttempts(
                    context = context,
                    quizSetId = quizSet.id
                ) + 1
            },
            onHome = {
                showResultScreen = false
                onBack()
            }
        )
        return
    }

    val question = questions[currentQuestion]
    val selectedAnswer = answers[currentQuestion]

    var isBookmarked by remember(question.id) {
        mutableStateOf(
            QuestionStorage
                .loadQuestions(context)
                .find { it.id == question.id }
                ?.isBookmarked
                ?: question.isBookmarked
        )
    }

    val options = listOf(
        "A" to question.optionA,
        "B" to question.optionB,
        "C" to question.optionC,
        "D" to question.optionD
    )

    val progress = (currentQuestion + 1).toFloat() / questions.size.toFloat()
    val isAnswered = selectedAnswer != null && selectedAnswer != "SKIPPED"
    val hasPyqBadge = question.exam.isNotBlank() && question.year != null

    // Shake animation offset for wrong answers
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(selectedAnswer, currentQuestion) {
        if (isAnswered) {
            val isCorrect = question.correctAnswer.equals(selectedAnswer, ignoreCase = true)
            if (isCorrect) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    // Ignore if haptics unavailable
                }
            } else {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                } catch (e: Exception) {
                    // Ignore
                }
                // Short horizontal shake animation for wrong answer
                shakeOffset.animateTo(-12f, animationSpec = tween(50))
                shakeOffset.animateTo(12f, animationSpec = tween(50))
                shakeOffset.animateTo(-6f, animationSpec = tween(50))
                shakeOffset.animateTo(6f, animationSpec = tween(50))
                shakeOffset.animateTo(0f, animationSpec = tween(50))
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = quizSet.title,
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back",
                actions = {
                    if (hasPyqBadge) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${question.exam} ${question.year}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            QuestionStorage.updateBookmark(
                                context = context,
                                questionId = question.id,
                                isBookmarked = isBookmarked
                            )
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = if (isHindi) "प्रश्न सहेजें" else "Save Question",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // MAIN SCROLLABLE QUESTION CONTENT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress Header
                Column {
                    val headerSubjectTopic = if (question.subject.isNotBlank() || question.topic.isNotBlank()) {
                        "${question.subject} • ${question.topic}"
                    } else {
                        quizSet.title
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerSubjectTopic,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (isHindi) "प्रश्न ${currentQuestion + 1} / ${questions.size}" else "Question ${currentQuestion + 1} of ${questions.size}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExamProgressBar(progress = progress)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // QUESTION TEXT (HeadlineSmall / 20.sp with Devanagari line-height)
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ANSWER OPTIONS USING QUIZ OPTION CARD
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    options.forEach { option ->
                        val optionKey = option.first
                        val optionText = option.second

                        val isCorrect = question.correctAnswer.equals(optionKey, ignoreCase = true)
                        val isUserSelection = selectedAnswer.equals(optionKey, ignoreCase = true)

                        val optionState = when {
                            isAnswered && isCorrect -> OptionState.CORRECT
                            isAnswered && isUserSelection && !isCorrect -> OptionState.WRONG
                            !isAnswered && isUserSelection -> OptionState.SELECTED
                            else -> OptionState.DEFAULT
                        }

                        QuizOptionCard(
                            optionKey = optionKey,
                            optionText = optionText,
                            state = optionState,
                            onClick = {
                                if (!isAnswered) {
                                    answers[currentQuestion] = optionKey
                                }
                            }
                        )
                    }
                }

                // EXPLANATION CARD
                AnimatedVisibility(
                    visible = isAnswered,
                    enter = fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220), initialOffsetY = { it / 2 })
                ) {
                    ExamCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = if (isHindi) "व्याख्या" else "Explanation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi) "व्याख्या" else "Explanation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (question.explanation.isNotBlank()) question.explanation else (if (isHindi) "इस प्रश्न के लिए व्याख्या उपलब्ध नहीं है।" else "No explanation available for this question."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FIXED BOTTOM ACTION BAR (48.dp Height)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExamButton(
                    text = if (isHindi) "← पिछला" else "← Previous",
                    onClick = {
                        if (currentQuestion > 0) {
                            currentQuestion--
                        }
                    },
                    enabled = currentQuestion > 0,
                    isOutlined = true,
                    modifier = Modifier.weight(0.38f)
                )

                if (!isAnswered) {
                    // Show Skip when NO answer has been selected
                    ExamButton(
                        text = if (isHindi) "छोड़ें" else "Skip",
                        onClick = {
                            answers[currentQuestion] = "SKIPPED"
                            if (currentQuestion < questions.size - 1) {
                                currentQuestion++
                            } else {
                                saveQuizAttempt(
                                    context = context,
                                    quizSet = quizSet,
                                    questions = questions,
                                    answers = answers
                                )
                                bestScore = QuizAttemptStorage.getBestScore(context, quizSet.id)
                                quizFinished = true
                            }
                        },
                        isOutlined = true,
                        modifier = Modifier.weight(0.62f)
                    )
                } else {
                    // Show Next / Finish prominently AFTER answer selection
                    ExamButton(
                        text = if (currentQuestion < questions.size - 1) (if (isHindi) "अगला →" else "Next →") else (if (isHindi) "पूर्ण करें" else "Finish"),
                        onClick = {
                            if (currentQuestion < questions.size - 1) {
                                currentQuestion++
                            } else {
                                saveQuizAttempt(
                                    context = context,
                                    quizSet = quizSet,
                                    questions = questions,
                                    answers = answers
                                )
                                bestScore = QuizAttemptStorage.getBestScore(context, quizSet.id)
                                quizFinished = true
                            }
                        },
                        isOutlined = false,
                        modifier = Modifier.weight(0.62f)
                    )
                }
            }
        }
    }
}


private fun saveQuizAttempt(
    context: android.content.Context,
    quizSet: QuizSet,
    questions: List<Question>,
    answers: List<String?>
) {

    val correct = questions.indices.count { index ->
        answers[index] == questions[index].correctAnswer
    }

    val wrong = questions.indices.count { index ->
        val answer = answers[index]
        answer != null && answer != "SKIPPED" && answer != questions[index].correctAnswer
    }

    val skipped = answers.count {
        it == null || it == "SKIPPED"
    }

    val attempted = questions.size - skipped

    val accuracy = if (attempted > 0) {
        (correct * 100) / attempted
    } else {
        0
    }

    val firstQ = questions.firstOrNull()
    val qSubject = firstQ?.subject ?: ""
    val qTopic = firstQ?.topic ?: ""

    QuizAttemptStorage.saveAttempt(
        context = context,
        attempt = QuizAttempt(
            quizSetId = quizSet.id,
            quizSetTitle = quizSet.title,
            score = correct,
            totalQuestions = questions.size,
            correct = correct,
            wrong = wrong,
            skipped = skipped,
            accuracy = accuracy,
            timestamp = System.currentTimeMillis(),
            subject = qSubject,
            topic = qTopic
        )
    )

    com.example.myexampreparation.data.InAppQuizProgressStorage.clearProgress(
        context = context,
        subject = qSubject,
        topic = qTopic,
        quizSetId = quizSet.id
    )

    val wrongQuestionsList = questions.indices.mapNotNull { index ->
        val answer = answers[index]
        if (answer != null && answer != "SKIPPED" && answer != questions[index].correctAnswer) {
            questions[index]
        } else null
    }

    val correctIdsList = questions.indices.mapNotNull { index ->
        val answer = answers[index]
        if (answer == questions[index].correctAnswer) {
            questions[index].id
        } else null
    }

    com.example.myexampreparation.data.WrongQuestionStorage.saveWrongQuestions(
        context = context,
        wrongQuestions = wrongQuestionsList
    )

    com.example.myexampreparation.data.WrongQuestionStorage.removeCorrectQuestions(
        context = context,
        correctIds = correctIdsList
    )
}


@Composable
fun ResultScreen(
    score: Int,
    totalQuestions: Int,
    wrongAnswers: Int,
    skippedAnswers: Int,
    attemptedQuestions: Int,
    attemptCount: Int,
    bestScore: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val accuracy = if (attemptedQuestions > 0) {
        (score * 100) / attemptedQuestions
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = if (isHindi) "🎉 क्विज़ पूर्ण हुआ!" else "🎉 Quiz Completed!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "आपने quiz पूरा कर लिया।",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isHindi) "आपका स्कोर" else "Your Score",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$score / $totalQuestions",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isHindi) "शुद्धता: $accuracy%" else "Accuracy: $accuracy%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (isHindi) "प्रदर्शन का विवरण" else "Performance Overview",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = if (isHindi) "सही उत्तर: $score" else "Correct Answers: $score")
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (isHindi) "गलत उत्तर: $wrongAnswers" else "Wrong Answers: $wrongAnswers")
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (isHindi) "छोड़े गए: $skippedAnswers" else "Skipped: $skippedAnswers")
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (isHindi) "हल किए गए प्रश्न: $attemptedQuestions" else "Attempted Questions: $attemptedQuestions")
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (isHindi) "प्रयास संख्या: $attemptCount" else "Attempts: $attemptCount")
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = if (isHindi) "सर्वश्रेष्ठ स्कोर: $bestScore / $totalQuestions" else "Best Score: $bestScore / $totalQuestions")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isHindi) "🔄 पुनः प्रयास करें" else "🔄 Retry Quiz", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isHindi) "← वापस जाएं" else "← Back", style = MaterialTheme.typography.titleMedium)
        }
    }
}
