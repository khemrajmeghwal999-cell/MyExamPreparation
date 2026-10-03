package com.example.myexampreparation

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamButton
import com.example.myexampreparation.ui.components.ExamTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.AppLanguage
import com.example.myexampreparation.data.AppLanguageStorage
import com.example.myexampreparation.data.Question
import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.QuestionStorage
import com.example.myexampreparation.data.QuizSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkedScreen(
    onBack: () -> Unit,
    onStartPractice: (QuizSet) -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    var bookmarkedQuestions by remember {
        QuestionBank.loadQuestions(context)
        mutableStateOf(
            QuestionBank.getAllQuestions().filter { it.isBookmarked }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = if (isHindi) "बुकमार्क प्रश्न" else "Saved Questions",
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back"
            )
        }
    ) { paddingValues ->
        if (bookmarkedQuestions.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isHindi) "कोई बुकमार्क प्रश्न नहीं है" else "No Saved Questions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isHindi) "क्विज़ के दौरान ⭐ बटन पर क्लिक करके प्रश्नों को यहाँ सेव करें।" else "Tap the ⭐ button during a quiz to save questions here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ExamButton(
                    text = if (isHindi) "अभ्यास शुरू करें (${bookmarkedQuestions.size} प्रश्न)" else "Start Practice (${bookmarkedQuestions.size} Questions)",
                    onClick = {
                        val practiceSet = QuizSet(
                            id = -2,
                            title = if (isHindi) "बुकमार्क प्रश्न अभ्यास" else "Saved Questions Practice",
                            subtitle = if (isHindi) "${bookmarkedQuestions.size} बुकमार्क प्रश्न" else "${bookmarkedQuestions.size} Saved Questions",
                            questions = bookmarkedQuestions
                        )
                        onStartPractice(practiceSet)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookmarkedQuestions, key = { it.id }) { question ->
                        BookmarkedQuestionCard(
                            question = question,
                            isHindi = isHindi,
                            onRemoveBookmark = {
                                QuestionStorage.updateBookmark(
                                    context = context,
                                    questionId = question.id,
                                    isBookmarked = false
                                )
                                QuestionBank.loadQuestions(context)
                                bookmarkedQuestions = QuestionBank.getAllQuestions().filter { it.isBookmarked }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarkedQuestionCard(
    question: Question,
    isHindi: Boolean,
    onRemoveBookmark: () -> Unit
) {
    ExamCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (question.subject.isNotBlank() || question.topic.isNotBlank()) {
                    Text(
                        text = "${question.subject} • ${question.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onRemoveBookmark) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (isHindi) "बुकमार्क हटाएँ" else "Remove Bookmark",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "A. ${question.optionA}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "B. ${question.optionB}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "C. ${question.optionC}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "D. ${question.optionD}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHindi) "सही उत्तर: विकल्प ${question.correctAnswer}" else "Correct Answer: Option ${question.correctAnswer}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isHindi) "💡 व्याख्या: ${question.explanation}" else "💡 Explanation: ${question.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
