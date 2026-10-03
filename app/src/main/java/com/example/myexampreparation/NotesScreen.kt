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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.CardDefaults
import com.example.myexampreparation.ui.components.ExamCard
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
import com.example.myexampreparation.data.Note
import com.example.myexampreparation.data.NoteStorage
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    availableSubjects: List<Subject>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var showSavedNotesOnly by remember { mutableStateOf(false) }

    // -------------------------------------------------------------
    // VIEW 1: NOTE DETAIL VIEW
    // -------------------------------------------------------------
    if (selectedNote != null) {
        val note = selectedNote!!
        var isBookmarked by remember(note.id) {
            mutableStateOf(
                NoteStorage.getAllNotes(context).find { it.id == note.id }?.isBookmarked ?: note.isBookmarked
            )
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExamTopAppBar(
                    title = note.title,
                    onBack = { selectedNote = null },
                    backContentDescription = if (isHindi) "वापस जाएं" else "Back",
                    actions = {
                        IconButton(
                            onClick = {
                                isBookmarked = !isBookmarked
                                NoteStorage.updateBookmark(context, note.id, isBookmarked)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (isHindi) "बुकमार्क करें" else "Bookmark Note",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
                // Subject & Topic Badge
                Text(
                    text = "${note.subject} • ${note.topic}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Title
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )

                // Key Points Card (Summary)
                if (note.summaryPoints.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isHindi) "💡 मुख्य बिंदु" else "💡 Key Points",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            note.summaryPoints.forEach { point ->
                                Text(
                                    text = "• $point",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Main Content Card
                ExamCard {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        return
    }

    // -------------------------------------------------------------
    // VIEW 2: BOOKMARKED NOTES VIEW
    // -------------------------------------------------------------
    if (showSavedNotesOnly) {
        val savedNotes = NoteStorage.getBookmarkedNotes(context)

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExamTopAppBar(
                    title = if (isHindi) "बुकमार्क नोट्स" else "Saved Notes",
                    onBack = { showSavedNotesOnly = false },
                    backContentDescription = if (isHindi) "वापस जाएं" else "Back"
                )
            }
        ) { paddingValues ->
            if (savedNotes.isEmpty()) {
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
                        text = if (isHindi) "कोई बुकमार्क नोट नहीं है" else "No Saved Notes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "नोट्स पढ़ते समय ⭐ बटन पर टैप करके उन्हें यहाँ सेव करें।" else "Tap the ⭐ button while reading notes to save them here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isHindi = isHindi,
                            onClick = { selectedNote = note }
                        )
                    }
                }
            }
        }
        return
    }

    // -------------------------------------------------------------
    // VIEW 3: TOPIC NOTES LIST VIEW
    // -------------------------------------------------------------
    if (selectedTopic != null && selectedSubject != null) {
        val topicNotes = NoteStorage.getAllNotes(context).filter { note ->
            val subjectMatches = SubjectMatcher.matchesSubject(note.subject, selectedSubject!!)
            val topicMatches = note.topic.equals(selectedTopic, ignoreCase = true)
            subjectMatches && topicMatches
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExamTopAppBar(
                    title = selectedTopic!!,
                    onBack = { selectedTopic = null },
                    backContentDescription = if (isHindi) "वापस जाएं" else "Back"
                )
            }
        ) { paddingValues ->
            if (topicNotes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = selectedTopic!!,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "इस टॉपिक के नोट्स शीघ्र ही उपलब्ध होंगे।" else "Notes for this topic will be available soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topicNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isHindi = isHindi,
                            onClick = { selectedNote = note }
                        )
                    }
                }
            }
        }
        return
    }

    // -------------------------------------------------------------
    // VIEW 4: SUBJECT'S TOPICS LIST VIEW
    // -------------------------------------------------------------
    if (selectedSubject != null) {
        val subject = selectedSubject!!
        val subjectDisplayName = if (isHindi) subject.name else subject.englishName

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                ExamTopAppBar(
                    title = subjectDisplayName,
                    onBack = { selectedSubject = null },
                    backContentDescription = if (isHindi) "वापस जाएं" else "Back"
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subject.topics) { topic ->
                    val noteCount = NoteStorage.getAllNotes(context).count { note ->
                        val subjectMatches = SubjectMatcher.matchesSubject(note.subject, subject)
                        subjectMatches && note.topic.equals(topic, ignoreCase = true)
                    }

                    ExamCard(
                        onClick = { selectedTopic = topic }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (noteCount > 0) {
                                        if (isHindi) "$noteCount नोट्स उपलब्ध" else "$noteCount Notes Available"
                                    } else {
                                        if (isHindi) "शीघ्र उपलब्ध होगा" else "Coming Soon"
                                    },
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
    // VIEW 5: MAIN SUBJECTS LIST VIEW
    // -------------------------------------------------------------
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = if (isHindi) "स्टडी नोट्स" else "Study Notes",
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back",
                actions = {
                    IconButton(
                        onClick = { showSavedNotesOnly = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (isHindi) "बुकमार्क नोट्स" else "Saved Notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (isHindi) "विषय चुनें (Subjects)" else "Select Subject",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(availableSubjects) { subject ->
                val subjectDisplayName = if (isHindi) subject.name else subject.englishName
                ExamCard(
                    onClick = { selectedSubject = subject }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subjectDisplayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isHindi) "${subject.topics.size} टॉपिक्स उपलब्ध" else "${subject.topics.size} Topics Available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
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
}

@Composable
private fun NoteCard(
    note: Note,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    ExamCard(
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${note.subject} • ${note.topic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (note.isBookmarked) {
                    Text(
                        text = if (isHindi) "⭐ सेव्ड" else "⭐ Saved",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            if (note.summaryPoints.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• ${note.summaryPoints.first()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
