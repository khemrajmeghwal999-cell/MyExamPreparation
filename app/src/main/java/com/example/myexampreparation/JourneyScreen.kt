package com.example.myexampreparation

import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.QuizAttempt
import com.example.myexampreparation.data.QuizAttemptStorage

fun getSubjectIcon(subjectName: String, englishName: String): ImageVector {
    val name = "$subjectName $englishName".lowercase()
    return when {
        name.contains("history") || name.contains("इतिहास") -> Icons.AutoMirrored.Outlined.MenuBook
        name.contains("geography") || name.contains("भूगोल") -> Icons.Outlined.Place
        name.contains("polity") || name.contains("राजनीति") -> Icons.Outlined.Description
        name.contains("economy") || name.contains("अर्थशास्त्र") -> Icons.Outlined.Folder
        name.contains("science") || name.contains("विज्ञान") -> Icons.Outlined.Folder
        else -> Icons.Outlined.Folder
    }
}

fun getCompletedTopicsCount(attempts: List<QuizAttempt>, subject: Subject): Int {
    return subject.topics.count { topic ->
        attempts.any { att ->
            (att.subject.isBlank() || SubjectMatcher.matchesSubject(att.subject, subject)) &&
            att.topic.trim().equals(topic.trim(), ignoreCase = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(
    subjects: List<Subject>,
    onSubjectClick: (Subject) -> Unit,
    onMenuClick: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val attempts = remember { QuizAttemptStorage.loadAttempts(context) }
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val allQuestions = remember { QuestionBank.getAllQuestions() }
    val activeSubjects = remember(subjects, allQuestions) {
        subjects.map { subject ->
            val activeTopics = subject.topics.filter { topic ->
                allQuestions.any { q ->
                    SubjectMatcher.matchesSubject(q.subject, subject) &&
                            q.topic.trim().equals(topic.trim(), ignoreCase = true)
                }
            }
            Subject(
                name = subject.name,
                englishName = subject.englishName,
                topics = activeTopics
            )
        }.filter { it.topics.isNotEmpty() }
    }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "तैयारी यात्रा" else "Preparation Journey",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isHindi) "आपकी परीक्षा तैयारी की रूपरेखा" else "Your Exam Preparation Roadmap",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isHindi) "तैयारी रोडमैप" else "Preparation Roadmap",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isHindi) "प्रत्येक विषय एक पड़ाव है। अपनी तैयारी के अनुसार विषय पूर्ण करें।" else "Each subject is a milestone. Complete topics according to your preparation.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(activeSubjects) { subject: Subject ->
                    val completedTopics = remember(subject, attempts) { getCompletedTopicsCount(attempts, subject) }
                    val totalTopics = subject.topics.size
                    val progressRatio = if (totalTopics > 0) completedTopics.toFloat() / totalTopics.toFloat() else 0f
                    val percent = (progressRatio * 100).toInt()

                    val statusText = when {
                        percent == 100 -> if (isHindi) "✅ पूर्ण" else "✅ Completed"
                        completedTopics > 0 -> if (isHindi) "🟡 जारी" else "🟡 In Progress"
                        else -> if (isHindi) "⭕ शुरू करें" else "⭕ Start"
                    }

                    val subjectDisplayName = if (isHindi) subject.name else subject.englishName

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubjectClick(subject) },
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
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
                                                imageVector = getSubjectIcon(subject.name, subject.englishName),
                                                contentDescription = subjectDisplayName,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = subjectDisplayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                    }

                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (percent == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$completedTopics / $totalTopics ${if (isHindi) "विषय पूर्ण" else "Topics Completed"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$percent%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
