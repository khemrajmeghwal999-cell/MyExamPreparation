package com.example.myexampreparation

import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamProgressBar
import com.example.myexampreparation.ui.components.ExamTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.AppLanguage
import com.example.myexampreparation.data.AppLanguageStorage
import com.example.myexampreparation.data.QuizAttempt
import com.example.myexampreparation.data.QuizAttemptStorage
import java.util.Date

private data class SubjectProgressData(
    val subjectName: String,
    val totalAttemptedQsSum: Int,
    val totalCorrectQsSum: Int,
    val bankTotalQs: Int,
    val accuracyPercent: Int,
    val totalAttemptsCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    val attempts = remember { QuizAttemptStorage.loadAttempts(context) }

    val totalAttempts = remember(attempts) { attempts.size }
    val totalQuestions = remember(attempts) { attempts.sumOf { it.totalQuestions } }
    val totalCorrect = remember(attempts) { attempts.sumOf { it.correct } }
    val totalWrong = remember(attempts) { attempts.sumOf { it.wrong } }
    val totalSkipped = remember(attempts) { attempts.sumOf { it.skipped } }
    val overallAccuracy = remember(totalQuestions, totalCorrect) { if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0 }
    val bestScore = remember(attempts) { attempts.maxOfOrNull { it.score } ?: 0 }

    val completedSetsCount = remember(attempts) {
        attempts.filter { it.quizSetId > 0 || it.quizSetTitle.isNotBlank() }
            .distinctBy { if (it.quizSetId > 0) "ID_${it.quizSetId}" else "TITLE_${it.quizSetTitle}" }
            .size
    }

    // Practice vs Mock Test Breakdown
    val mockAttempts = remember(attempts) {
        attempts.filter { it.quizSetTitle.contains("Mock Test", ignoreCase = true) || it.timeTakenSeconds > 0 }
    }
    val practiceAttempts = remember(attempts) {
        attempts.filterNot { it.quizSetTitle.contains("Mock Test", ignoreCase = true) || it.timeTakenSeconds > 0 }
    }

    // Weak Topics Breakdown (attempts grouped by title/topic where accuracy < 60%)
    val weakSetAttempts = remember(attempts) {
        attempts
            .groupBy { it.quizSetTitle }
            .mapValues { entry ->
                val qCount = entry.value.sumOf { it.totalQuestions }
                val cCount = entry.value.sumOf { it.correct }
                val acc = if (qCount > 0) (cCount * 100) / qCount else 0
                Pair(qCount, acc)
            }
            .filter { it.value.second < 60 && it.value.first > 0 }
    }

    // Subject Performance Breakdown:
    // Honest attempt-based metrics without false claims of unique bank coverage
    val allBankQuestions = remember { com.example.myexampreparation.data.QuestionBank.getAllQuestions() }

    val subjectProgressList = remember(attempts, allBankQuestions) {
        attempts
            .groupBy { if (it.subject.isNotBlank()) it.subject else "General" }
            .map { entry ->
                val subName = entry.key
                val attemptsForSubject = entry.value
                val totalAttemptsCount = attemptsForSubject.size

                val totalCorrectQsSum = attemptsForSubject.sumOf { it.correct }
                val totalAttemptedQsSum = attemptsForSubject.sumOf { it.totalQuestions }
                val accuracyPercent = if (totalAttemptedQsSum > 0) (totalCorrectQsSum * 100) / totalAttemptedQsSum else 0

                val questionsForSubjectInBank = allBankQuestions.filter {
                    com.example.myexampreparation.data.SubjectMatcher.matchesSubjectName(it.subject, subName) ||
                    (subName == "General" && it.subject.isBlank())
                }

                val bankTotalQs = questionsForSubjectInBank.size

                SubjectProgressData(
                    subjectName = subName,
                    totalAttemptedQsSum = totalAttemptedQsSum,
                    totalCorrectQsSum = totalCorrectQsSum,
                    bankTotalQs = bankTotalQs,
                    accuracyPercent = accuracyPercent,
                    totalAttemptsCount = totalAttemptsCount
                )
            }
            .sortedByDescending { it.totalAttemptsCount }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = if (isHindi) "प्रगति" else "Progress",
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back"
            )
        }
    ) { paddingValues ->
        if (attempts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isHindi) "अभी कोई प्रगति नहीं है" else "No progress yet",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isHindi) "अपनी प्रगति देखने के लिए अभ्यास शुरू करें।" else "Start practicing to see your progress.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. OVERALL ACCURACY BANNER CARD (WIREFRAME 4)
                item {
                    ExamCard {
                        Text(
                            text = if (isHindi) "कुल प्रदर्शन (Overall Performance)" else "Overall Performance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$overallAccuracy%",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isHindi) "कुल शुद्धता (Overall Accuracy)" else "Overall Accuracy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "[ ${totalCorrect} / ${totalQuestions} ${if (isHindi) "सही" else "Correct"} ]",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ExamProgressBar(progress = overallAccuracy / 100f)
                    }
                }

                // 2. STATS GRID CARDS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = if (isHindi) "कुल प्रयास" else "Total Attempts",
                            value = "$totalAttempts",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isHindi) "प्रश्नों का प्रयास" else "Questions Attempted",
                            value = "$totalQuestions",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = if (isHindi) "सही" else "Correct",
                            value = "$totalCorrect",
                            valueColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isHindi) "गलत" else "Wrong",
                            value = "$totalWrong",
                            valueColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isHindi) "छोड़े गए" else "Skipped",
                            value = "$totalSkipped",
                            valueColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = if (isHindi) "पूर्ण क्विज़ सेट" else "Completed Sets",
                            value = "$completedSetsCount",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = if (isHindi) "सर्वश्रेष्ठ स्कोर" else "Best Score",
                            value = "$bestScore",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 3. SUBJECT PRACTICE VOLUME & ACCURACY LIST (WIREFRAME 4)
                if (subjectProgressList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi) "विषयवार प्रदर्शन (Subject Performance)" else "Subject Performance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    items(subjectProgressList) { item ->
                        ExamCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.subjectName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${item.accuracyPercent}% ${if (isHindi) "शुद्धता" else "Accuracy"}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.totalAttemptedQsSum} ${if (isHindi) "प्रश्न-अभ्यास" else "Question Attempts"} (${item.bankTotalQs} ${if (isHindi) "बैंक प्रश्न" else "Bank Qs"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (isHindi) "कुल प्रयास" else "Total Attempts"}: ${item.totalAttemptsCount}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            ExamProgressBar(progress = item.accuracyPercent / 100f)
                        }
                    }
                }

                // 4. PRACTICE vs MOCK TEST COMPARISON
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "प्रदर्शन विश्लेषण (Performance Breakdown)" else "Performance Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val pQuestions = practiceAttempts.sumOf { it.totalQuestions }
                        val pCorrect = practiceAttempts.sumOf { it.correct }
                        val pAcc = if (pQuestions > 0) (pCorrect * 100) / pQuestions else 0

                        ExamCard(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "📝 अभ्यास क्विज़" else "📝 Practice Quiz",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${if (isHindi) "प्रयास" else "Attempts"}: ${practiceAttempts.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${if (isHindi) "प्रश्न" else "Questions"}: $pQuestions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isHindi) "शुद्धता" else "Accuracy"}: $pAcc%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExamProgressBar(progress = pAcc / 100f)
                        }

                        val mQuestions = mockAttempts.sumOf { it.totalQuestions }
                        val mCorrect = mockAttempts.sumOf { it.correct }
                        val mAcc = if (mQuestions > 0) (mCorrect * 100) / mQuestions else 0

                        ExamCard(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "🎯 मॉक टेस्ट" else "🎯 Mock Test",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${if (isHindi) "प्रयास" else "Attempts"}: ${mockAttempts.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${if (isHindi) "प्रश्न" else "Questions"}: $mQuestions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (isHindi) "शुद्धता" else "Accuracy"}: $mAcc%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExamProgressBar(progress = mAcc / 100f)
                        }
                    }
                }

                // 5. WEAK TOPICS / AREAS NEEDING REVIEW
                if (weakSetAttempts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi) "पुनरीक्षण योग्य क्षेत्र (Areas Needing Review)" else "Areas Needing Review",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    items(weakSetAttempts.entries.toList()) { entry ->
                        ExamCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.key,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${if (isHindi) "शुद्धता" else "Accuracy"}: ${entry.value.second}% • ${entry.value.first} ${if (isHindi) "प्रश्न" else "Questions"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isHindi) "पुनरीक्षण" else "Review",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            ExamProgressBar(progress = entry.value.second / 100f)
                        }
                    }
                }

                // 6. RECENT ATTEMPTS LOG
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "हाल के प्रयास (Recent Attempts)" else "Recent Attempt Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                items(attempts.sortedByDescending { it.timestamp }) { attempt ->
                    AttemptCard(attempt = attempt, isHindi = isHindi)
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )
        }
    }
}

@Composable
private fun AttemptCard(attempt: QuizAttempt, isHindi: Boolean) {
    val dateText = remember(attempt.timestamp) {
        DateFormat.format("dd MMM yyyy, hh:mm a", Date(attempt.timestamp)).toString()
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
            Text(
                text = attempt.quizSetTitle,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (isHindi) "स्कोर" else "Score"}: ${attempt.score} / ${attempt.totalQuestions}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${if (isHindi) "सही" else "Correct"}: ${attempt.correct}  •  ${if (isHindi) "गलत" else "Wrong"}: ${attempt.wrong}  •  ${if (isHindi) "छोड़े गए" else "Skipped"}: ${attempt.skipped}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${if (isHindi) "शुद्धता" else "Accuracy"}: ${attempt.accuracy}%" +
                        if (attempt.timeTakenSeconds > 0) "  •  ${if (isHindi) "समय" else "Time"}: ${attempt.timeTakenSeconds / 60}m ${attempt.timeTakenSeconds % 60}s" else "",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
