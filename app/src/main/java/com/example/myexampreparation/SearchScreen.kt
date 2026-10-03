package com.example.myexampreparation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.myexampreparation.data.Note
import com.example.myexampreparation.data.NoteStorage
import com.example.myexampreparation.data.Question
import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

sealed class SearchResultItem {
    data class QuestionResult(val question: Question) : SearchResultItem()
    data class NoteResult(val note: Note) : SearchResultItem()
    data class SubjectResult(val subject: Subject) : SearchResultItem()
    data class TopicResult(val subjectName: String, val topicName: String) : SearchResultItem()
}

enum class SearchFilterCategory {
    ALL, QUESTIONS, NOTES, SUBJECTS, TOPICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    availableSubjects: List<Subject>,
    onBack: () -> Unit,
    onSelectQuestion: (Question) -> Unit,
    onSelectNote: (Note) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onSelectTopic: (Subject, String) -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchFilterCategory.ALL) }

    val allQuestions = remember { QuestionBank.getAllQuestions() }
    val allNotes = remember { NoteStorage.getAllNotes(context) }

    val searchResults = remember(searchQuery, selectedCategory) {
        val q = searchQuery.trim()
        if (q.isBlank()) return@remember emptyList<SearchResultItem>()

        val list = mutableListOf<SearchResultItem>()

        // 1. Subjects
        if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.SUBJECTS) {
            availableSubjects.forEach { sub ->
                if (sub.name.contains(q, ignoreCase = true) || sub.englishName.contains(q, ignoreCase = true)) {
                    list.add(SearchResultItem.SubjectResult(sub))
                }
            }
        }

        // 2. Topics
        if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.TOPICS) {
            availableSubjects.forEach { sub ->
                sub.topics.forEach { topic ->
                    if (topic.contains(q, ignoreCase = true)) {
                        list.add(SearchResultItem.TopicResult(sub.name, topic))
                    }
                }
            }
        }

        // 3. Questions
        if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.QUESTIONS) {
            allQuestions.forEach { question ->
                val matchesText = question.question.contains(q, ignoreCase = true) ||
                        question.optionA.contains(q, ignoreCase = true) ||
                        question.optionB.contains(q, ignoreCase = true) ||
                        question.optionC.contains(q, ignoreCase = true) ||
                        question.optionD.contains(q, ignoreCase = true) ||
                        question.explanation.contains(q, ignoreCase = true) ||
                        question.subject.contains(q, ignoreCase = true) ||
                        question.topic.contains(q, ignoreCase = true)

                if (matchesText) {
                    list.add(SearchResultItem.QuestionResult(question))
                }
            }
        }

        // 4. Notes
        if (selectedCategory == SearchFilterCategory.ALL || selectedCategory == SearchFilterCategory.NOTES) {
            allNotes.forEach { note ->
                val matchesText = note.title.contains(q, ignoreCase = true) ||
                        note.content.contains(q, ignoreCase = true) ||
                        note.subject.contains(q, ignoreCase = true) ||
                        note.topic.contains(q, ignoreCase = true) ||
                        note.summaryPoints.any { it.contains(q, ignoreCase = true) }

                if (matchesText) {
                    list.add(SearchResultItem.NoteResult(note))
                }
            }
        }

        list.distinctBy {
            when (it) {
                is SearchResultItem.QuestionResult -> "Q_${it.question.id}"
                is SearchResultItem.NoteResult -> "N_${it.note.id}"
                is SearchResultItem.SubjectResult -> "S_${it.subject.name}"
                is SearchResultItem.TopicResult -> "T_${it.subjectName}_${it.topicName}"
            }
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
                title = {
                    Text(
                        text = if (isHindi) "खोजें" else "Search",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
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
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (isHindi) "प्रश्न, नोट्स, विषय या टॉपिक खोजें..." else "Search questions, notes, subjects, topics...") },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = if (isHindi) "साफ़ करें" else "Clear search"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == SearchFilterCategory.ALL,
                    onClick = { selectedCategory = SearchFilterCategory.ALL },
                    label = { Text(if (isHindi) "सभी" else "All") }
                )
                FilterChip(
                    selected = selectedCategory == SearchFilterCategory.QUESTIONS,
                    onClick = { selectedCategory = SearchFilterCategory.QUESTIONS },
                    label = { Text(if (isHindi) "प्रश्न" else "Questions") }
                )
                FilterChip(
                    selected = selectedCategory == SearchFilterCategory.NOTES,
                    onClick = { selectedCategory = SearchFilterCategory.NOTES },
                    label = { Text(if (isHindi) "नोट्स" else "Notes") }
                )
                FilterChip(
                    selected = selectedCategory == SearchFilterCategory.SUBJECTS,
                    onClick = { selectedCategory = SearchFilterCategory.SUBJECTS },
                    label = { Text(if (isHindi) "विषय" else "Subjects") }
                )
                FilterChip(
                    selected = selectedCategory == SearchFilterCategory.TOPICS,
                    onClick = { selectedCategory = SearchFilterCategory.TOPICS },
                    label = { Text(if (isHindi) "टॉपिक" else "Topics") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (searchQuery.trim().isBlank()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "🔍 खोज शुरू करें" else "🔍 Type to Search",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) "उदा. 'गुरुत्वाकर्षण', 'गति', 'विज्ञान', 'प्रकाश'" else "e.g. 'gravity', 'motion', 'Science', 'light'",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (searchResults.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "कोई परिणाम नहीं मिला" else "No results found",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isHindi) "कृपया कोई अन्य शब्द या टॉपिक खोजें।" else "Please try searching for another keyword or topic.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = if (isHindi) "${searchResults.size} परिणाम मिले" else "${searchResults.size} Results Found",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { item ->
                        SearchResultCard(
                            item = item,
                            isHindi = isHindi,
                            availableSubjects = availableSubjects,
                            onSelectQuestion = onSelectQuestion,
                            onSelectNote = onSelectNote,
                            onSelectSubject = onSelectSubject,
                            onSelectTopic = onSelectTopic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    isHindi: Boolean,
    availableSubjects: List<Subject>,
    onSelectQuestion: (Question) -> Unit,
    onSelectNote: (Note) -> Unit,
    onSelectSubject: (Subject) -> Unit,
    onSelectTopic: (Subject, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (item) {
                    is SearchResultItem.QuestionResult -> onSelectQuestion(item.question)
                    is SearchResultItem.NoteResult -> onSelectNote(item.note)
                    is SearchResultItem.SubjectResult -> onSelectSubject(item.subject)
                    is SearchResultItem.TopicResult -> {
                        val sub = SubjectMatcher.resolveSubject(item.subjectName, availableSubjects)
                            ?: availableSubjects.firstOrNull()

                        if (sub != null) {
                            onSelectTopic(sub, item.topicName)
                        }
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeText = when (item) {
                    is SearchResultItem.QuestionResult -> if (isHindi) "❓ प्रश्न" else "❓ QUESTION"
                    is SearchResultItem.NoteResult -> if (isHindi) "📖 नोट" else "📖 NOTE"
                    is SearchResultItem.SubjectResult -> if (isHindi) "📚 विषय" else "📚 SUBJECT"
                    is SearchResultItem.TopicResult -> if (isHindi) "📂 टॉपिक" else "📂 TOPIC"
                }

                val badgeColor = when (item) {
                    is SearchResultItem.QuestionResult -> MaterialTheme.colorScheme.primary
                    is SearchResultItem.NoteResult -> MaterialTheme.colorScheme.secondary
                    is SearchResultItem.SubjectResult -> MaterialTheme.colorScheme.tertiary
                    is SearchResultItem.TopicResult -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium,
                    color = badgeColor
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            when (item) {
                is SearchResultItem.QuestionResult -> {
                    Text(
                        text = item.question.question,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.question.subject} • ${item.question.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is SearchResultItem.NoteResult -> {
                    Text(
                        text = item.note.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${item.note.subject} • ${item.note.topic}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is SearchResultItem.SubjectResult -> {
                    val displayName = if (isHindi) item.subject.name else item.subject.englishName
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "${item.subject.topics.size} टॉपिक्स उपलब्ध" else "${item.subject.topics.size} Topics Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is SearchResultItem.TopicResult -> {
                    Text(
                        text = item.topicName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isHindi) "विषय: ${item.subjectName}" else "Subject: ${item.subjectName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
