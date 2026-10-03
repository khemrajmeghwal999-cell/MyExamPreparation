package com.example.myexampreparation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamProgressBar
import com.example.myexampreparation.ui.components.ExamButton
import com.example.myexampreparation.ui.components.QuizOptionCard
import com.example.myexampreparation.ui.components.OptionState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.QuestionStorage
import com.example.myexampreparation.data.QuizAttempt
import com.example.myexampreparation.data.QuizAttemptStorage
import com.example.myexampreparation.data.QuizSet
import com.example.myexampreparation.data.WrongQuestionStorage
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MockTestScreen(
    quizSet: QuizSet,
    onBack: () -> Unit
) {
    val context = LocalContext.current
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isHindi) "इस मॉक टेस्ट में कोई प्रश्न नहीं है।" else "No questions in this Mock Test.",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(if (isHindi) "वापस जाएं" else "Back")
            }
        }
        return
    }

    // State
    var currentQuestion by remember { mutableIntStateOf(0) }
    var isTestSubmitted by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }
    var showReviewView by remember { mutableStateOf(false) }

    // User's selected option ("A", "B", "C", "D" or null)
    val selectedAnswers = remember(questions.size) {
        mutableStateListOf<String?>().apply {
            repeat(questions.size) { add(null) }
        }
    }

    // Marked for review flags
    val markedForReview = remember(questions.size) {
        mutableStateListOf<Boolean>().apply {
            repeat(questions.size) { add(false) }
        }
    }

    // Calculate total duration in seconds based on question count
    val totalDurationSeconds = remember(questions.size) {
        when (questions.size) {
            10 -> 10 * 60
            20 -> 20 * 60
            30 -> 30 * 60
            50 -> 50 * 60
            else -> questions.size * 60
        }
    }

    var timeRemainingSeconds by remember { mutableIntStateOf(totalDurationSeconds) }

    // Timer countdown effect
    LaunchedEffect(isTestSubmitted) {
        while (!isTestSubmitted && timeRemainingSeconds > 0) {
            delay(1000L)
            if (!isTestSubmitted && timeRemainingSeconds > 0) {
                timeRemainingSeconds--
            }
        }
        if (timeRemainingSeconds == 0 && !isTestSubmitted) {
            // Automatic submission when timer reaches 0
            isTestSubmitted = true
        }
    }

    // Result calculation upon submission
    val correctCount = remember(isTestSubmitted) {
        if (!isTestSubmitted) 0
        else questions.indices.count { selectedAnswers[it] == questions[it].correctAnswer }
    }

    val wrongCount = remember(isTestSubmitted) {
        if (!isTestSubmitted) 0
        else questions.indices.count { index ->
            val ans = selectedAnswers[index]
            ans != null && ans != "SKIPPED" && ans != questions[index].correctAnswer
        }
    }

    val skippedCount = remember(isTestSubmitted) {
        if (!isTestSubmitted) 0
        else questions.indices.count { selectedAnswers[it] == null || selectedAnswers[it] == "SKIPPED" }
    }

    val attemptedCount = questions.size - skippedCount
    val accuracy = if (attemptedCount > 0) (correctCount * 100) / attemptedCount else 0
    val timeTakenSeconds = (totalDurationSeconds - timeRemainingSeconds).coerceAtLeast(0)

    // Save attempt and wrong questions when submitted
    LaunchedEffect(isTestSubmitted) {
        if (isTestSubmitted) {
            val firstQ = questions.firstOrNull()
            QuizAttemptStorage.saveAttempt(
                context = context,
                attempt = QuizAttempt(
                    quizSetId = quizSet.id,
                    quizSetTitle = quizSet.title,
                    score = correctCount,
                    totalQuestions = questions.size,
                    correct = correctCount,
                    wrong = wrongCount,
                    skipped = skippedCount,
                    accuracy = accuracy,
                    timestamp = System.currentTimeMillis(),
                    timeTakenSeconds = timeTakenSeconds.toLong(),
                    subject = firstQ?.subject ?: "",
                    topic = firstQ?.topic ?: ""
                )
            )

            val wrongQuestionsList = questions.indices.mapNotNull { index ->
                val ans = selectedAnswers[index]
                if (ans != null && ans != "SKIPPED" && ans != questions[index].correctAnswer) {
                    questions[index]
                } else null
            }

            val correctIdsList = questions.indices.mapNotNull { index ->
                val ans = selectedAnswers[index]
                if (ans == questions[index].correctAnswer) {
                    questions[index].id
                } else null
            }

            WrongQuestionStorage.saveWrongQuestions(
                context = context,
                wrongQuestions = wrongQuestionsList
            )

            WrongQuestionStorage.removeCorrectQuestions(
                context = context,
                correctIds = correctIdsList
            )
        }
    }

    // Format helper for timer
    fun formatTime(seconds: Int): String {
        val mins = (seconds.coerceAtLeast(0)) / 60
        val secs = (seconds.coerceAtLeast(0)) % 60
        return String.format("%02d:%02d", mins, secs)
    }

    // -------------------------------------------------------------
    // VIEW 1: QUESTION-WISE REVIEW (POST-TEST)
    // -------------------------------------------------------------
    if (isTestSubmitted && showReviewView) {
        val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
        val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isHindi) "मॉक टेस्ट समीक्षा" else "Mock Test Review") },
                    navigationIcon = {
                        IconButton(onClick = { showReviewView = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isHindi) "वापस जाएं" else "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(questions) { index, question ->
                    val userAns = selectedAnswers[index]
                    val isCorrect = userAns == question.correctAnswer
                    val isSkipped = userAns == null || userAns == "SKIPPED"

                    val statusText = when {
                        isCorrect -> if (isHindi) "✅ सही" else "✅ Correct"
                        isSkipped -> if (isHindi) "⏸️ छोड़ा गया" else "⏸️ Skipped"
                        else -> if (isHindi) "❌ गलत" else "❌ Wrong"
                    }

                    val statusColor = when {
                        isCorrect -> MaterialTheme.colorScheme.primary
                        isSkipped -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.error
                    }

                    var isBookmarked by remember(question.id) {
                        mutableStateOf(
                            QuestionStorage.loadQuestions(context)
                                .find { it.id == question.id }
                                ?.isBookmarked
                                ?: question.isBookmarked
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isHindi) "प्र." else "Q"}${index + 1}. $statusText",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = statusColor
                                )

                                IconButton(
                                    onClick = {
                                        isBookmarked = !isBookmarked
                                        QuestionStorage.updateBookmark(
                                            context = context,
                                            questionId = question.id,
                                            isBookmarked = isBookmarked
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                                        contentDescription = if (isHindi) "बुकमार्क करें" else "Bookmark",
                                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = question.question,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "A. ${question.optionA}")
                            Text(text = "B. ${question.optionB}")
                            Text(text = "C. ${question.optionC}")
                            Text(text = "D. ${question.optionD}")

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${if (isHindi) "आपका उत्तर" else "Your Answer"}: ${userAns ?: (if (isHindi) "कोई नहीं" else "None")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = statusColor
                            )

                            Text(
                                text = "${if (isHindi) "सही उत्तर: विकल्प" else "Correct Answer: Option"} ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (question.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 ${if (isHindi) "व्याख्या" else "Explanation"}: ${question.explanation}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // -------------------------------------------------------------
    // VIEW 2: MOCK TEST RESULT SCREEN (POST-TEST)
    // -------------------------------------------------------------
    if (isTestSubmitted) {
        val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
        val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isHindi) "🎯 मॉक टेस्ट परिणाम" else "🎯 Mock Test Result",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = if (isHindi) "आपका स्कोर" else "Your Score", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$correctCount / ${questions.size}",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${if (isHindi) "शुद्धता" else "Accuracy"}: $accuracy%", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = if (isHindi) "प्रदर्शन का विवरण" else "Performance Overview", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "${if (isHindi) "सही उत्तर" else "Correct Answers"}: $correctCount")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${if (isHindi) "गलत उत्तर" else "Wrong Answers"}: $wrongCount")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${if (isHindi) "छोड़े गए प्रश्न" else "Skipped / Unanswered"}: $skippedCount")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${if (isHindi) "हल किए गए प्रश्न" else "Attempted Questions"}: $attemptedCount / ${questions.size}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${if (isHindi) "लिया गया समय" else "Time Taken"}: ${formatTime(timeTakenSeconds)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showReviewView = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isHindi) "📖 उत्तर देखें" else "📖 Review Answers")
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
                val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
                Text(if (isHindi) "← वापस जाएं" else "← Back", style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    // -------------------------------------------------------------
    // VIEW 3: ACTIVE MOCK TEST (DURING TEST)
    // -------------------------------------------------------------
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val question = questions[currentQuestion]
    val selectedOption = selectedAnswers[currentQuestion]

    var isBookmarked by remember(question.id) {
        mutableStateOf(
            QuestionStorage.loadQuestions(context)
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Text(
                        text = quizSet.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showSubmitDialog = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Test",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // COMPACT MARK FOR REVIEW ICON
                    IconButton(
                        onClick = {
                            markedForReview[currentQuestion] = !markedForReview[currentQuestion]
                        }
                    ) {
                        Icon(
                            imageVector = if (markedForReview[currentQuestion]) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Mark for Review",
                            tint = if (markedForReview[currentQuestion]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // SAVE / BOOKMARK QUESTION ICON
                    IconButton(
                        onClick = {
                            isBookmarked = !isBookmarked
                            QuestionStorage.updateBookmark(
                                context = context,
                                questionId = question.id,
                                isBookmarked = isBookmarked
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Save Question",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // TIMER BADGE
                    Text(
                        text = "⏱️ ${formatTime(timeRemainingSeconds)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = if (timeRemainingSeconds < 120) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header Row: Question Count & Marked Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "प्रश्न ${currentQuestion + 1} / ${questions.size}" else "Question ${currentQuestion + 1} of ${questions.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (markedForReview[currentQuestion]) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = if (isHindi) "🔖 समीक्षा हेतु चिह्नित" else "🔖 Marked for Review",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // QUESTION NAVIGATION PANEL (Question Numbers 1..N)
            Text(
                text = if (isHindi) "प्रश्न नेविगेशन:" else "Question Navigation:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                questions.indices.forEach { idx ->
                    val isCurrent = idx == currentQuestion
                    val isAnswered = selectedAnswers[idx] != null && selectedAnswers[idx] != "SKIPPED"
                    val isMarked = markedForReview[idx]

                    val containerColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                        isMarked -> MaterialTheme.colorScheme.secondaryContainer
                        isAnswered -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    val labelText = "${idx + 1}" + if (isMarked) "🔖" else ""

                    Card(
                        modifier = Modifier.clickable { currentQuestion = idx },
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        border = if (isCurrent) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Text(
                            text = labelText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrent) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Text
            Text(
                text = question.question,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                modifier = Modifier.weight(0.35f, fill = false)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(options) { _, option ->
                    val isSelected = selectedOption == option.first
                    val optionState = if (isSelected) OptionState.SELECTED else OptionState.DEFAULT

                    QuizOptionCard(
                        optionKey = option.first,
                        optionText = option.second,
                        state = optionState,
                        onClick = {
                            selectedAnswers[currentQuestion] = if (isSelected) null else option.first
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTTOM CONTROL ROW (Matching QuizScreen.kt: Left = Previous, Right = Skip or Next / Submit Test)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (currentQuestion > 0) currentQuestion--
                    },
                    enabled = currentQuestion > 0,
                    modifier = Modifier.weight(0.38f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isHindi) "← पिछला" else "← Previous")
                }

                if (selectedOption == null) {
                    // Show Skip when NO option is selected
                    OutlinedButton(
                        onClick = {
                            selectedAnswers[currentQuestion] = "SKIPPED"
                            if (currentQuestion < questions.size - 1) {
                                currentQuestion++
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        modifier = Modifier.weight(0.62f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isHindi) "छोड़ें" else "Skip")
                    }
                } else if (currentQuestion < questions.size - 1) {
                    // Show Next when an option is selected and not on the last question
                    Button(
                        onClick = {
                            currentQuestion++
                        },
                        modifier = Modifier.weight(0.62f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isHindi) "अगला →" else "Next →")
                    }
                } else {
                    // Last question answered -> Submit Test
                    Button(
                        onClick = {
                            showSubmitDialog = true
                        },
                        modifier = Modifier.weight(0.62f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isHindi) "सबमिट करें" else "Submit Test")
                    }
                }
            }
        }
    }

    // SUBMIT CONFIRMATION DIALOG
    if (showSubmitDialog) {
        val answeredCount = questions.indices.count { selectedAnswers[it] != null && selectedAnswers[it] != "SKIPPED" }
        val unansweredCount = questions.size - answeredCount
        val markedCount = markedForReview.count { it }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text(if (isHindi) "क्या आप मॉक टेस्ट सबमिट करना चाहते हैं?" else "Submit Mock Test?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (isHindi) "कुल प्रश्न: ${questions.size}" else "Total Questions: ${questions.size}")
                    Text(if (isHindi) "उत्तर दिए: $answeredCount" else "Answered: $answeredCount")
                    Text(if (isHindi) "बिना उत्तर दिए: $unansweredCount" else "Unanswered: $unansweredCount")
                    Text(if (isHindi) "समीक्षा हेतु चिह्नित: $markedCount" else "Marked for Review: $markedCount")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitDialog = false
                        isTestSubmitted = true
                    }
                ) {
                    Text(if (isHindi) "सबमिट करें" else "Submit Test")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitDialog = false }) {
                    Text(if (isHindi) "जारी रखें" else "Continue Test")
                }
            }
        )
    }
}
