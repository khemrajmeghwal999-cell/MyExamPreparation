package com.example.myexampreparation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamProgressBar
import com.example.myexampreparation.ui.components.ExamCompactButton
import com.example.myexampreparation.ui.components.ExamTopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.Question
import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.QuizAttemptStorage
import com.example.myexampreparation.data.RecentSubjectStorage
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

data class PYQConfig(
    val exam: String,
    val topic: String,
    val selectedYear: Int?, // null = All Years
    val questions: List<Question>,
    val isTimedMode: Boolean = false,
    val totalTimeMinutes: Int = 0 // 0 = No Timer
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PYQScreen(
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onStartPYQ: (PYQConfig) -> Unit
) {
    val context = LocalContext.current
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val allQuestions = remember { QuestionBank.getAllQuestions() }

    // Filter PYQ questions: Any question from Master Question Bank with PYQ subtitle or specific exam tag
    val pyqQuestions = remember(allQuestions) {
        allQuestions.filter { q ->
            q.exam.isNotBlank() &&
            !q.exam.equals("General", ignoreCase = true) &&
            (q.quizSetSubtitle.contains("PYQ", ignoreCase = true) ||
             q.exam.contains("CET", ignoreCase = true) ||
             q.exam.contains("Rajasthan", ignoreCase = true) ||
             q.exam.length >= 3)
        }
    }

    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTopic by rememberSaveable { mutableStateOf<String?>(null) }

    // Step-by-Step Back Navigation Handler
    BackHandler {
        when {
            selectedTopic != null -> selectedTopic = null
            selectedSubject != null -> selectedSubject = null
            else -> onBack()
        }
    }

    // Dynamic Subject Layer (Group all PYQ questions by Subject)
    val availableSubjects = remember(pyqQuestions) {
        val groupedSubjects = pyqQuestions
            .map { q ->
                val s = q.subject.trim()
                if (s.isBlank()) "General" else s
            }
            .distinct()

        groupedSubjects.filter { subjectName ->
            pyqQuestions.count { q ->
                val qSub = if (q.subject.trim().isBlank()) "General" else q.subject.trim()
                qSub.equals(subjectName, ignoreCase = true) || SubjectMatcher.matchesSubjectName(qSub, subjectName)
            } > 0
        }
    }

    // Sort Subjects by Recently Used Order
    val sortedSubjects = remember(availableSubjects, context) {
        availableSubjects.sortedWith(
            compareByDescending<String> { subjectName ->
                RecentSubjectStorage.getSubjectUsageTimestamp(
                    context,
                    Subject(subjectName, subjectName, emptyList())
                )
            }.thenBy { it }
        )
    }

    if (pyqQuestions.isEmpty() || availableSubjects.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isHindi) "राजस्थान CET PYQ" else "RAJ CET PYQ") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "अभी PYQ उपलब्ध नहीं हैं।" else "No PYQs available at the moment.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    // -------------------------------------------------------------
    // LAYER 1: SUBJECT LIST SCREEN (RAJ CET PYQ -> SUBJECT)
    // -------------------------------------------------------------
    if (selectedSubject == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExamTopAppBar(
                    title = if (isHindi) "राजस्थान CET PYQ — विषय चुनें" else "RAJ CET PYQ — Select Subject",
                    onBack = onBack,
                    backContentDescription = if (isHindi) "वापस जाएं" else "Back"
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (isHindi) "विषय सूची (Subjects)" else "Subjects",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(sortedSubjects) { subjectName ->
                    val subjectQs = pyqQuestions.filter { q ->
                        val qSub = if (q.subject.trim().isBlank()) "General" else q.subject.trim()
                        qSub.equals(subjectName, ignoreCase = true) || SubjectMatcher.matchesSubjectName(qSub, subjectName)
                    }

                    val topicCount = subjectQs.map { it.topic.trim() }.filter { it.isNotBlank() }.distinct().size

                    ExamCard(
                        onClick = {
                            RecentSubjectStorage.recordSubjectUsage(context, subjectName)
                            selectedSubject = subjectName
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = subjectName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = subjectName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${subjectQs.size} ${if (isHindi) "प्रश्न" else "Questions"} • $topicCount ${if (isHindi) "टॉपिक्स" else "Topics"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "›",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // -------------------------------------------------------------
    // LAYER 2: TOPIC LIST SCREEN (SUBJECT -> TOPIC)
    // -------------------------------------------------------------
    val subjectQs = remember(selectedSubject, pyqQuestions) {
        pyqQuestions.filter { q ->
            val qSub = if (q.subject.trim().isBlank()) "General" else q.subject.trim()
            qSub.equals(selectedSubject!!, ignoreCase = true) || SubjectMatcher.matchesSubjectName(qSub, selectedSubject!!)
        }
    }

    val availableTopics = remember(subjectQs) {
        val topics = subjectQs.map { it.topic.trim() }.filter { it.isNotBlank() }.distinct()
        topics.filter { topicName ->
            subjectQs.count { it.topic.trim().equals(topicName, ignoreCase = true) } > 0
        }
    }

    // Sort Topics by Recently Used Order
    val sortedTopics = remember(availableTopics, selectedSubject, context) {
        RecentSubjectStorage.sortTopicsByRecentUsage(context, selectedSubject!!, availableTopics)
    }

    // Calculate overall Subject Summary coverage for Wireframe 2
    val attemptedTopicsCount = remember(availableTopics, selectedSubject) {
        availableTopics.count { topic ->
            QuizAttemptStorage.getQuizSetStatus(context, selectedSubject!!, topic, 0, "").isCompleted
        }
    }
    val subjectCoverageRatio = if (availableTopics.isNotEmpty()) (attemptedTopicsCount.toFloat() / availableTopics.size.toFloat()).coerceIn(0f, 1f) else 0f
    val subjectCoveragePercent = (subjectCoverageRatio * 100).toInt()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = if (isHindi) "$selectedSubject — विषय सूची" else "$selectedSubject Topics",
                onBack = { selectedSubject = null },
                backContentDescription = if (isHindi) "वापस जाएं" else "Back"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // WIREFRAME 2: SUBJECT SUMMARY CARD AT TOP OF TOPIC LIST
            item {
                ExamCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = selectedSubject,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSubject!!,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${subjectQs.size} ${if (isHindi) "प्रश्न" else "Questions"} • ${availableTopics.size} ${if (isHindi) "टॉपिक्स" else "Topics"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${attemptedTopicsCount} / ${availableTopics.size} ${if (isHindi) "टॉपिक्स पूर्ण" else "Topics Completed"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subjectCoveragePercent}% ${if (isHindi) "प्रगति" else "Coverage"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExamProgressBar(progress = subjectCoverageRatio)
                }
            }

            item {
                Text(
                    text = if (isHindi) "टॉपिक सूची (Topics)" else "Topic List",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            items(sortedTopics) { topicName ->
                val topicQs = subjectQs.filter { it.topic.trim().equals(topicName, ignoreCase = true) }
                val years = topicQs.mapNotNull { it.year }.distinct().sortedDescending()

                val topicAttempts = remember(topicName, topicQs) {
                    QuizAttemptStorage.loadAttempts(context).filter { att ->
                        att.quizSetTitle.contains(topicName, ignoreCase = true) ||
                        (att.subject.equals(selectedSubject, ignoreCase = true) && att.topic.equals(topicName, ignoreCase = true))
                    }
                }

                val bestScore = remember(topicAttempts) {
                    topicAttempts.maxOfOrNull { it.score } ?: 0
                }
                val isAttempted = topicAttempts.isNotEmpty()
                val topicProgressRatio = if (topicQs.isNotEmpty()) (bestScore.toFloat() / topicQs.size.toFloat()).coerceIn(0f, 1f) else 0f
                val percentComplete = (topicProgressRatio * 100).toInt()

                ExamCard(
                    onClick = {
                        RecentSubjectStorage.recordTopicUsage(context, selectedSubject, topicName)
                        onStartPYQ(
                            PYQConfig(
                                exam = "Rajasthan CET",
                                topic = topicName,
                                selectedYear = null,
                                questions = topicQs,
                                isTimedMode = false,
                                totalTimeMinutes = 0
                            )
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Folder,
                                contentDescription = topicName,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topicName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            if (isAttempted) {
                                Text(
                                    text = "$bestScore / ${topicQs.size} ${if (isHindi) "प्रश्न" else "Questions"} • $percentComplete% ${if (isHindi) "पूर्ण" else "Complete"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "${if (isHindi) "प्रयास" else "Attempts"}: ${topicAttempts.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (years.isNotEmpty()) {
                                        val yearsLabel = if (isHindi) "वर्ष" else "Years"
                                        Text(
                                            text = "$yearsLabel: ${years.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                val yearsLabel = if (isHindi) "वर्ष" else "Years"
                                val pyqsLabel = if (isHindi) "प्रश्न" else "PYQs"
                                val yearsText = if (years.isNotEmpty()) "$yearsLabel: ${years.joinToString(", ")}" else ""

                                Text(
                                    text = "${topicQs.size} $pyqsLabel${if (yearsText.isNotBlank()) " • $yearsText" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ExamCompactButton(
                            text = if (isAttempted) (if (isHindi) "पुनः प्रयास" else "Reattempt") else (if (isHindi) "शुरू करें" else "Start"),
                            onClick = {
                                RecentSubjectStorage.recordTopicUsage(context, selectedSubject, topicName)
                                onStartPYQ(
                                    PYQConfig(
                                        exam = "Rajasthan CET",
                                        topic = topicName,
                                        selectedYear = null,
                                        questions = topicQs,
                                        isTimedMode = false,
                                        totalTimeMinutes = 0
                                    )
                                )
                            },
                            isOutlined = isAttempted
                        )
                    }

                    if (isAttempted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ExamProgressBar(progress = topicProgressRatio)
                    }
                }
            }
        }
    }
}
