package com.example.myexampreparation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.QuizQuestionSelector
import com.example.myexampreparation.data.QuizSet
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestSetupScreen(
    availableSubjects: List<Subject>,
    onBack: () -> Unit,
    onStartMockTest: (QuizSet) -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedTopic by remember { mutableStateOf<String?>(if (isHindi) "सभी टॉपिक्स" else "All Topics") }
    var selectedCount by remember { mutableIntStateOf(10) }
    var selectedMode by remember { mutableStateOf(if (isHindi) "मॉक टेस्ट मोड" else "Mock Test Mode") }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var topicMenuExpanded by remember { mutableStateOf(false) }

    val questionCounts = listOf(10, 20, 30, 50)
    val testModes = if (isHindi) listOf("अभ्यास मोड", "मॉक टेस्ट मोड") else listOf("Practice Mode", "Mock Test Mode")

    val defaultTopicLabel = if (isHindi) "सभी टॉपिक्स" else "All Topics"
    val allSubjectsLabel = if (isHindi) "सभी विषय" else "All Subjects"

    val availableTopics = remember(selectedSubject, isHindi) {
        if (selectedSubject != null) {
            listOf(defaultTopicLabel) + selectedSubject!!.topics
        } else {
            listOf(defaultTopicLabel)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text(if (isHindi) "मॉक टेस्ट सेटअप" else "Mock Test Setup", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isHindi) "वापस जाएं" else "Back",
                            tint = MaterialTheme.colorScheme.onSurface
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isHindi) "मॉक टेस्ट कॉन्फ़िगर करें" else "Configure Mock Test",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            // 1. SUBJECT SELECTION
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
                        text = if (isHindi) "📚 विषय (Subject)" else "📚 Subject",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { subjectMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val subLabel = selectedSubject?.let { if (isHindi) it.name else "${it.englishName} (${it.name})" }
                                ?: allSubjectsLabel
                            Text(
                                text = subLabel,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text("▼", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(allSubjectsLabel) },
                            onClick = {
                                selectedSubject = null
                                selectedTopic = defaultTopicLabel
                                subjectMenuExpanded = false
                            }
                        )
                        availableSubjects.forEach { subject ->
                            val label = if (isHindi) subject.name else "${subject.englishName} (${subject.name})"
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedSubject = subject
                                    selectedTopic = defaultTopicLabel
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. TOPIC SELECTION
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
                        text = if (isHindi) "📂 टॉपिक (Topic)" else "📂 Topic",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedSubject != null) {
                                    topicMenuExpanded = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTopic ?: defaultTopicLabel,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (selectedSubject != null) {
                                Text("▼", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (selectedSubject != null) {
                        DropdownMenu(
                            expanded = topicMenuExpanded,
                            onDismissRequest = { topicMenuExpanded = false }
                        ) {
                            availableTopics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic) },
                                    onClick = {
                                        selectedTopic = topic
                                        topicMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. NUMBER OF QUESTIONS SELECTION
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
                        text = if (isHindi) "🔢 प्रश्नों की संख्या" else "🔢 Number of Questions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        questionCounts.forEach { count ->
                            FilterChip(
                                selected = selectedCount == count,
                                onClick = { selectedCount = count },
                                label = { Text("$count") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. TEST MODE SELECTION
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
                        text = if (isHindi) "⚙️ टेस्ट मोड" else "⚙️ Test Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        testModes.forEach { mode ->
                            FilterChip(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode },
                                label = { Text(mode) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. START TEST BUTTON
            Button(
                onClick = {
                    val allBankQuestions = QuestionBank.getAllQuestions()

                    // Filter by subject
                    val subjectFiltered = if (selectedSubject != null) {
                        allBankQuestions.filter { question ->
                            SubjectMatcher.matchesSubject(question.subject, selectedSubject)
                        }
                    } else {
                        allBankQuestions
                    }

                    // Filter by topic
                    val topicFiltered = if (selectedTopic != null && selectedTopic != "सभी Topics" && selectedTopic != "All Topics" && selectedTopic != defaultTopicLabel) {
                        subjectFiltered.filter { question ->
                            question.topic.equals(selectedTopic, ignoreCase = true)
                        }
                    } else {
                        subjectFiltered
                    }

                    if (topicFiltered.isEmpty()) {
                        Toast.makeText(
                            context,
                            if (isHindi) "इस विषय या टॉपिक के प्रश्न उपलब्ध नहीं हैं।" else "No questions available for this subject or topic.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val finalQuestions = QuizQuestionSelector.selectQuestionsWithVariety(
                            candidatePool = topicFiltered,
                            targetCount = selectedCount,
                            shuffleOptions = true,
                            isFixedPaper = false
                        )

                        val subjectLabel = selectedSubject?.name ?: allSubjectsLabel
                        val topicLabel = selectedTopic ?: defaultTopicLabel

                        val mockQuizSet = QuizSet(
                            id = -6,
                            title = if (isHindi) "🎯 मॉक टेस्ट ($subjectLabel)" else "🎯 Mock Test ($subjectLabel)",
                            subtitle = "$selectedMode • ${finalQuestions.size} ${if (isHindi) "प्रश्न" else "Questions"} ($topicLabel)",
                            questions = finalQuestions
                        )

                        onStartMockTest(mockQuizSet)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isHindi) "🚀 टेस्ट शुरू करें" else "🚀 Start Test",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
