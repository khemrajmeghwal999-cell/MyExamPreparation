package com.example.myexampreparation

import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.myexampreparation.data.Question
import com.example.myexampreparation.data.QuestionStorage
import com.example.myexampreparation.data.GoogleDriveManager
import com.example.myexampreparation.data.Note
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myexampreparation.data.QuizAttemptStorage
import com.example.myexampreparation.data.RecentSubjectStorage
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamProgressBar
import com.example.myexampreparation.ui.components.ExamButton
import com.example.myexampreparation.ui.components.CompactActionButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Menu as OutlinedNavMenu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.QuizSet
import com.example.myexampreparation.data.scienceQuizSets
import com.example.myexampreparation.ui.theme.MyExamPreparationTheme
import com.example.myexampreparation.data.QuestionBank

import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher

fun getMergedSubjects(
    baseSubjects: List<Subject>,
    importedQuestions: List<Question>
): List<Subject> {
    val mergedList = baseSubjects.map { subject ->
        Subject(
            name = subject.name,
            englishName = subject.englishName,
            topics = subject.topics.toMutableList()
        )
    }.toMutableList()

    val groupedBySubject = importedQuestions
        .filter { it.subject.isNotBlank() }
        .groupBy { it.subject.trim() }

    groupedBySubject.forEach { (rawSubjectName, questionsForSubject) ->
        val newTopics = questionsForSubject
            .map { it.topic.trim() }
            .filter { it.isNotBlank() }

        val existingSubject = mergedList.find { subject ->
            SubjectMatcher.matchesSubject(rawSubjectName, subject)
        }

        if (existingSubject != null) {
            val combinedTopics = (existingSubject.topics + newTopics)
                .distinct()
                .sorted()

            val updatedSubject = Subject(
                name = existingSubject.name,
                englishName = existingSubject.englishName,
                topics = combinedTopics
            )

            val index = mergedList.indexOf(existingSubject)
            if (index != -1) {
                mergedList[index] = updatedSubject
            }
        } else {
            val newSubject = Subject(
                name = rawSubjectName,
                englishName = rawSubjectName,
                topics = newTopics.distinct().sorted()
            )
            mergedList.add(newSubject)
        }
    }

    return mergedList.sortedBy { it.name }
}


enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}


val subjects = listOf(

    Subject(
        "इतिहास",
        "History",
        listOf(
            "प्राचीन भारत",
            "मध्यकालीन भारत",
            "आधुनिक भारत"
        )
    ),

    Subject(
        "भूगोल",
        "Geography",
        listOf(
            "भारत का भूगोल",
            "विश्व का भूगोल",
            "भौतिक भूगोल"
        )
    ),

    Subject(
        "भारतीय राजव्यवस्था",
        "Indian Polity",
        listOf(
            "संविधान",
            "मौलिक अधिकार",
            "संसद",
            "राष्ट्रपति"
        )
    ),

    Subject(
        "अर्थव्यवस्था",
        "Economy",
        listOf(
            "भारतीय अर्थव्यवस्था",
            "बैंकिंग",
            "बजट"
        )
    ),

    Subject(
        "विज्ञान",
        "Science",
        listOf(
            "पदार्थ की अवस्था",
            "ऊष्मा",
            "ध्वनि",
            "प्रकाश",
            "विद्युत"
        )
    ),

    Subject(
        "कला एवं संस्कृति",
        "Art & Culture",
        listOf(
            "भारतीय कला",
            "संगीत",
            "नृत्य",
            "त्योहार"
        )
    ),

    Subject(
        "कंप्यूटर",
        "Computer",
        listOf(
            "Computer Basics",
            "Internet",
            "MS Office"
        )
    ),

    Subject(
        "अंग्रेज़ी",
        "English",
        listOf(
            "Grammar",
            "Vocabulary",
            "Reading"
        )
    ),

    Subject(
        "हिन्दी",
        "Hindi",
        listOf(
            "व्याकरण",
            "संधि",
            "समास",
            "पर्यायवाची"
        )
    ),

    Subject(
        "राजस्थान सामान्य ज्ञान",
        "Rajasthan GK",
        listOf(
            "इतिहास",
            "भूगोल",
            "कला एवं संस्कृति",
            "राजस्थान GK"
        )
    ),

    Subject(
        "गणित",
        "Mathematics",
        listOf(
            "संख्या पद्धति",
            "प्रतिशत",
            "लाभ और हानि",
            "अनुपात और समानुपात",
            "औसत",
            "समय और कार्य"
        )
    ),

    Subject(
        "रिजनिंग",
        "Reasoning",
        listOf(
            "Number Series",
            "Analogy",
            "Coding-Decoding",
            "Blood Relation",
            "Direction Test",
            "Ranking"
        )
    )
)


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {
            ExamPrepApp()
        }
    }
}


@Composable
fun ExamPrepApp() {
    val context = LocalContext.current
    var themeMode by remember {
        mutableStateOf(com.example.myexampreparation.data.AppThemeStorage.getSavedThemeMode(context))
    }

    val systemDark = isSystemInDarkTheme()

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MyExamPreparationTheme(
        darkTheme = darkTheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                themeMode = themeMode,
                onThemeChange = { newTheme ->
                    themeMode = newTheme
                    com.example.myexampreparation.data.AppThemeStorage.saveThemeMode(context, newTheme)
                }
            )
        }
    }
}


@Composable
fun AppNavigation(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current

    var importedQuestions by remember {
        mutableStateOf<List<Question>>(emptyList())
    }

    var otaUpdateData by remember {
        mutableStateOf<com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable?>(null)
    }

    LaunchedEffect(Unit) {
        // Initialize Google Mobile Ads SDK on app startup
        try {
            com.google.android.gms.ads.MobileAds.initialize(context) { status ->
                android.util.Log.d("AdMob", "MobileAds initialized: ${status.adapterStatusMap}")
                com.example.myexampreparation.InterstitialAdManager.preloadAd(context)
            }
        } catch (e: Exception) {
            android.util.Log.e("AdMob", "MobileAds initialization error: ${e.localizedMessage}", e)
        }

        importedQuestions =
            QuestionStorage.loadQuestions(context)

        QuestionBank.loadQuestions(context)

        // Asynchronously check for OTA update on app startup
        val updateResult = com.example.myexampreparation.update.AppUpdater.checkForUpdate(
            context = context,
            versionJsonUrl = com.example.myexampreparation.update.AppUpdater.DEFAULT_VERSION_JSON_URL
        )

        if (updateResult is com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable) {
            otaUpdateData = updateResult
        }
    }

    val mergedSubjects = remember(subjects, importedQuestions) {
        val merged = getMergedSubjects(subjects, importedQuestions)
        val allQs = QuestionBank.getAllQuestions()
        merged.map { subject ->
            val activeTopics = subject.topics.filter { topic ->
                allQs.any { q ->
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

    var selectedSubject by remember {
        mutableStateOf<Subject?>(null)
    }

    var selectedTopic by remember {
        mutableStateOf<String?>(null)
    }

    var selectedQuizSet by remember {
        mutableStateOf<QuizSet?>(null)
    }

    var showQuiz by remember {
        mutableStateOf(false)
    }

    var showProgress by remember {
        mutableStateOf(false)
    }

    var showBookmarked by remember {
        mutableStateOf(false)
    }

    var showWrongQuestions by remember {
        mutableStateOf(false)
    }

    var showMockTestSetup by remember {
        mutableStateOf(false)
    }

    var showBackupScreen by remember {
        mutableStateOf(false)
    }

    var showSettingsScreen by remember {
        mutableStateOf(false)
    }

    var showQuestionBankSyncScreen by remember {
        mutableStateOf(false)
    }

    var showNotesScreen by remember {
        mutableStateOf(false)
    }

    var showSearchScreen by remember {
        mutableStateOf(false)
    }

    var showStudyGoalsScreen by remember {
        mutableStateOf(false)
    }

    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var currentBottomTab by remember { mutableStateOf("home") }

    var appLanguage by remember {
        mutableStateOf(com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context))
    }

    var showPYQScreen by remember {
        mutableStateOf(false)
    }

    var pyqConfigToLaunch by remember {
        mutableStateOf<PYQConfig?>(null)
    }

    var googleAccount by remember {
        mutableStateOf<GoogleSignInAccount?>(GoogleDriveManager.getSignedInAccount(context))
    }

    var userProfileState by remember { mutableStateOf(com.example.myexampreparation.data.UserProfileStorage.getUserProfile(context)) }
    var isProfileCompleted by remember { mutableStateOf(userProfileState.profileCompleted) }

    if (!isProfileCompleted) {
        ProfileSetupScreen(
            appLanguage = appLanguage,
            onProfileCompleted = { completedProfile ->
                userProfileState = completedProfile
                isProfileCompleted = true
            }
        )
        return
    }

    // CENTRALIZED ANDROID SYSTEM BACK BUTTON HANDLER
    BackHandler(enabled = true) {
        when {
            // 1. Open Navigation Drawer
            drawerState.isOpen -> {
                coroutineScope.launch { drawerState.close() }
            }

            // 2. Active PYQ Quiz Overlay Screen -> Return to PYQ Topic List Screen
            pyqConfigToLaunch != null -> {
                pyqConfigToLaunch = null
            }

            // 3. Active Quiz or Mock Test Screen
            showQuiz && selectedQuizSet != null -> {
                if (selectedQuizSet!!.id == -6 || selectedQuizSet!!.title.contains("Mock Test", ignoreCase = true) || selectedQuizSet!!.subtitle.contains("Mock Test", ignoreCase = true)) {
                    showQuiz = false
                    showMockTestSetup = true
                } else {
                    showQuiz = false
                }
            }

            // 3. Quiz Set Screen -> Return to Topic
            selectedTopic != null -> {
                selectedTopic = null
            }

            // 4. Topic Screen -> Return to Subject / Home
            selectedSubject != null -> {
                selectedSubject = null
            }

            // 5. Full-Screen Overlays & Sub-Screens
            showSearchScreen -> showSearchScreen = false
            showPYQScreen -> showPYQScreen = false
            showMockTestSetup -> showMockTestSetup = false
            showBackupScreen -> showBackupScreen = false
            showQuestionBankSyncScreen -> showQuestionBankSyncScreen = false
            showSettingsScreen -> showSettingsScreen = false
            showStudyGoalsScreen -> showStudyGoalsScreen = false
            showNotesScreen -> showNotesScreen = false
            showBookmarked -> showBookmarked = false
            showWrongQuestions -> showWrongQuestions = false
            showProgress -> showProgress = false

            // 6. Bottom Navigation Tabs -> Return to Home Tab
            currentBottomTab != "home" -> {
                currentBottomTab = "home"
            }

            // 7. Root Home Screen -> Exit App Naturally
            else -> {
                (context as? android.app.Activity)?.finish()
            }
        }
    }

    var otaDownloadState by remember {
        mutableStateOf<com.example.myexampreparation.update.DownloadState>(
            com.example.myexampreparation.update.DownloadState.Idle
        )
    }
    var activeDownloadId by remember { mutableStateOf(-1L) }

    if (activeDownloadId != -1L && otaUpdateData != null) {
        LaunchedEffect(activeDownloadId) {
            com.example.myexampreparation.update.OtaDownloader.trackDownloadProgress(
                context = context,
                downloadId = activeDownloadId,
                versionCode = otaUpdateData!!.latestVersionCode
            ).collect { state ->
                otaDownloadState = state
            }
        }
    }

    if (otaUpdateData != null) {
        val updateData = otaUpdateData!!
        val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
        val currentVersionCode = com.example.myexampreparation.update.AppUpdater.getCurrentVersionCode(context)

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!updateData.forceUpdate && otaDownloadState !is com.example.myexampreparation.update.DownloadState.Downloading) {
                    android.util.Log.i("OTA_UPDATE", "User dismissed update dialog (Later)")
                    otaUpdateData = null
                    activeDownloadId = -1L
                    otaDownloadState = com.example.myexampreparation.update.DownloadState.Idle
                }
            },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = !updateData.forceUpdate && otaDownloadState !is com.example.myexampreparation.update.DownloadState.Downloading,
                dismissOnClickOutside = !updateData.forceUpdate && otaDownloadState !is com.example.myexampreparation.update.DownloadState.Downloading
            ),
            title = {
                Text(
                    text = when (otaDownloadState) {
                        is com.example.myexampreparation.update.DownloadState.Idle -> if (isHindi) "🚀 नया अपडेट उपलब्ध है" else "🚀 Update Available"
                        is com.example.myexampreparation.update.DownloadState.Queued -> if (isHindi) "⏳ डाउनलोड शुरू हो रहा है..." else "⏳ Download Queued..."
                        is com.example.myexampreparation.update.DownloadState.Downloading -> if (isHindi) "📥 अपडेट डाउनलोड हो रहा है..." else "📥 Downloading Update..."
                        is com.example.myexampreparation.update.DownloadState.Success -> if (isHindi) "✅ डाउनलोड पूरा हुआ" else "✅ Download Complete"
                        is com.example.myexampreparation.update.DownloadState.Failed -> if (isHindi) "❌ डाउनलोड विफल हुआ" else "❌ Download Failed"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (val state = otaDownloadState) {
                        is com.example.myexampreparation.update.DownloadState.Idle -> {
                            Text(
                                text = if (isHindi)
                                    "ऐप का नया वर्ज़न उपलब्ध है। बेहतर प्रदर्शन और नए प्रश्नों के लिए अपडेट करें।"
                                else
                                    "A new version of the app is available. Please update for new improvements and fixes.",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            Text(
                                text = if (isHindi)
                                    "• वर्तमान वर्ज़न: v$currentVersionCode\n• नया वर्ज़न: v${updateData.latestVersionCode}"
                                else
                                    "• Current Version: v$currentVersionCode\n• Latest Version: v${updateData.latestVersionCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (updateData.releaseNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isHindi) "नवीनतम बदलाव:" else "Release Notes:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = updateData.releaseNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is com.example.myexampreparation.update.DownloadState.Queued -> {
                            Text(
                                text = if (isHindi) "अपडेट डाउनलोड कतार में है..." else "Connecting to server and queuing download...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        is com.example.myexampreparation.update.DownloadState.Downloading -> {
                            val percent = state.progressPercent
                            val downloadedMb = String.format(java.util.Locale.US, "%.1f", state.bytesDownloaded / (1024f * 1024f))
                            val totalMb = String.format(java.util.Locale.US, "%.1f", state.totalBytes / (1024f * 1024f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (state.totalBytes > 0) "$downloadedMb MB / $totalMb MB" else "$downloadedMb MB",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { if (state.totalBytes > 0) percent / 100f else 0f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        is com.example.myexampreparation.update.DownloadState.Success -> {
                            Text(
                                text = if (isHindi)
                                    "अपडेट APK फ़ाइल सफलतापूर्वक डाउनलोड हो गई है:\n• ${state.apkFile.name}\n\nऐप को अपडेट करने के लिए 'अपडेट इंस्टॉल करें' पर टैप करें।"
                                else
                                    "Update APK file has been downloaded successfully:\n• ${state.apkFile.name}\n\nTap 'Install Update' to install the new version.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        is com.example.myexampreparation.update.DownloadState.Failed -> {
                            Text(
                                text = if (isHindi)
                                    "डाउनलोड विफल रहा: ${state.reason}\n\nकृपया इंटरनेट कनेक्शन जांचें और पुनः प्रयास करें।"
                                else
                                    "Download failed: ${state.reason}\n\nPlease check your internet connection and try again.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                when (val state = otaDownloadState) {
                    is com.example.myexampreparation.update.DownloadState.Idle -> {
                        Button(
                            onClick = {
                                android.util.Log.i(
                                    "OTA_UPDATE",
                                    "User tapped Update Now. Starting DownloadManager task for URL: ${updateData.apkUrl}"
                                )
                                val downloadId = com.example.myexampreparation.update.OtaDownloader.startOrResumeDownload(
                                    context = context,
                                    apkUrl = updateData.apkUrl,
                                    versionCode = updateData.latestVersionCode
                                )
                                if (downloadId != -1L) {
                                    activeDownloadId = downloadId
                                    otaDownloadState = com.example.myexampreparation.update.DownloadState.Queued
                                } else {
                                    otaDownloadState = com.example.myexampreparation.update.DownloadState.Failed("Unable to enqueue download request")
                                }
                            }
                        ) {
                            Text(if (isHindi) "अभी अपडेट करें" else "Update Now")
                        }
                    }

                    is com.example.myexampreparation.update.DownloadState.Downloading,
                    is com.example.myexampreparation.update.DownloadState.Queued -> {
                        // Background downloading indicator
                    }

                    is com.example.myexampreparation.update.DownloadState.Success -> {
                        Button(
                            onClick = {
                                val installResult = com.example.myexampreparation.update.OtaInstaller.installApk(
                                    context = context,
                                    versionCode = updateData.latestVersionCode,
                                    downloadId = state.downloadId
                                )
                                when (installResult) {
                                    is com.example.myexampreparation.update.InstallResult.Success -> {
                                        android.util.Log.i("OTA_UPDATE", "Package Installer launched successfully.")
                                    }
                                    is com.example.myexampreparation.update.InstallResult.PermissionRequired -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isHindi) "कृपया 'अनजान ऐप इंस्टॉल' अनुमति दें" else "Please allow 'Install unknown apps' permission in Settings",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                        try {
                                            context.startActivity(installResult.intent)
                                        } catch (e: Exception) {
                                            android.util.Log.e("OTA_UPDATE", "Failed to launch Manage Unknown Apps Settings", e)
                                        }
                                    }
                                    is com.example.myexampreparation.update.InstallResult.Error -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Installation error: ${installResult.message}",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text(if (isHindi) "अपडेट इंस्टॉल करें" else "Install Update")
                        }
                    }

                    is com.example.myexampreparation.update.DownloadState.Failed -> {
                        Button(
                            onClick = {
                                val downloadId = com.example.myexampreparation.update.OtaDownloader.startOrResumeDownload(
                                    context = context,
                                    apkUrl = updateData.apkUrl,
                                    versionCode = updateData.latestVersionCode
                                )
                                if (downloadId != -1L) {
                                    activeDownloadId = downloadId
                                    otaDownloadState = com.example.myexampreparation.update.DownloadState.Queued
                                }
                            }
                        ) {
                            Text(if (isHindi) "पुनः प्रयास करें" else "Retry")
                        }
                    }
                }
            },
            dismissButton = {
                when (otaDownloadState) {
                    is com.example.myexampreparation.update.DownloadState.Idle -> {
                        if (!updateData.forceUpdate) {
                            OutlinedButton(
                                onClick = {
                                    android.util.Log.i("OTA_UPDATE", "User selected Later")
                                    otaUpdateData = null
                                    activeDownloadId = -1L
                                }
                            ) {
                                Text(if (isHindi) "बाद में" else "Later")
                            }
                        }
                    }

                    is com.example.myexampreparation.update.DownloadState.Success -> {
                        OutlinedButton(
                            onClick = {
                                android.util.Log.i("OTA_UPDATE", "User closed update dialog after successful download.")
                                otaUpdateData = null
                                activeDownloadId = -1L
                                otaDownloadState = com.example.myexampreparation.update.DownloadState.Idle
                            }
                        ) {
                            Text(if (isHindi) "बंद करें" else "Close")
                        }
                    }

                    is com.example.myexampreparation.update.DownloadState.Failed -> {
                        if (!updateData.forceUpdate) {
                            OutlinedButton(
                                onClick = {
                                    otaUpdateData = null
                                    activeDownloadId = -1L
                                    otaDownloadState = com.example.myexampreparation.update.DownloadState.Idle
                                }
                            ) {
                                Text(if (isHindi) "बंद करें" else "Close")
                            }
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    var isBackupUploading by remember { mutableStateOf(false) }
    var isBackupRestoring by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var isBackupSuccess by remember { mutableStateOf(true) }
    var lastBackupTime by remember { mutableStateOf(GoogleDriveManager.getLastBackupTimestamp(context)) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            googleAccount = account
            android.widget.Toast.makeText(
                context,
                "Signed in as ${account.email ?: account.displayName}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } catch (e: ApiException) {
            android.widget.Toast.makeText(
                context,
                "Google Sign-In failed: ${e.statusCode}",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // PYQ QUIZ OVERLAY SCREEN

    if (pyqConfigToLaunch != null) {
        PYQQuizScreen(
            config = pyqConfigToLaunch!!,
            onBack = {
                pyqConfigToLaunch = null
            }
        )
        return
    }

    // PYQ SCREEN

    if (showPYQScreen) {
        PYQScreen(
            onBack = {
                showPYQScreen = false
            },
            onMenuClick = {
                coroutineScope.launch { drawerState.open() }
            },
            onStartPYQ = { config ->
                pyqConfigToLaunch = config
            }
        )
        return
    }

    // BACKUP SCREEN

    if (showBackupScreen) {

        BackupScreen(
            signedInAccount = googleAccount,
            isUploading = isBackupUploading,
            isRestoring = isBackupRestoring,
            lastBackupTime = lastBackupTime,
            statusMessage = backupStatusMessage,
            isSuccessStatus = isBackupSuccess,
            onBack = {
                showBackupScreen = false
            },
            onSignInClick = {
                val signInClient = GoogleDriveManager.getGoogleSignInClient(context)
                googleSignInLauncher.launch(signInClient.signInIntent)
            },
            onSignOutClick = {
                GoogleDriveManager.signOut(context) {
                    googleAccount = null
                    backupStatusMessage = null
                    android.widget.Toast.makeText(
                        context,
                        "Signed out successfully",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onBackupNowClick = {
                val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
                if (!GoogleDriveManager.isSignedIn(context)) {
                    backupStatusMessage = if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."
                    isBackupSuccess = false
                } else {
                    isBackupUploading = true
                    backupStatusMessage = if (isHindi) "गूगल ड्राइव पर बैकअप अपलोड हो रहा है..." else "Uploading backup to Google Drive..."
                    isBackupSuccess = true

                    coroutineScope.launch {
                        val backupJson = com.example.myexampreparation.data.BackupManager.createBackupJson(context)
                        val result = GoogleDriveManager.uploadBackupToDrive(context, backupJson)

                        isBackupUploading = false
                        result.fold(
                            onSuccess = { timestamp ->
                                lastBackupTime = timestamp
                                isBackupSuccess = true
                                backupStatusMessage = if (isHindi) "✅ बैकअप गूगल ड्राइव पर सफलतापूर्वक अपलोड हो गया!" else "✅ Backup successfully uploaded to Google Drive!"
                            },
                            onFailure = { error ->
                                isBackupSuccess = false
                                backupStatusMessage = "❌ ${error.message}"
                            }
                        )
                    }
                }
            },
            onRestoreNowClick = {
                val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
                if (!GoogleDriveManager.isSignedIn(context)) {
                    backupStatusMessage = if (isHindi) "कृपया पहले गूगल खाते से साइन इन करें।" else "Please sign in with Google first."
                    isBackupSuccess = false
                } else {
                    isBackupRestoring = true
                    backupStatusMessage = if (isHindi) "गूगल ड्राइव से बैकअप डाउनलोड हो रहा है..." else "Downloading backup from Google Drive..."
                    isBackupSuccess = true

                    coroutineScope.launch {
                        val downloadResult = GoogleDriveManager.downloadBackupFromDrive(context)

                        downloadResult.fold(
                            onSuccess = { backupJsonString ->
                                val restoreSuccess = com.example.myexampreparation.data.BackupManager.restoreFromBackupJson(context, backupJsonString)
                                isBackupRestoring = false

                                if (restoreSuccess) {
                                    importedQuestions = QuestionStorage.loadQuestions(context)
                                    QuestionBank.loadQuestions(context)
                                    isBackupSuccess = true
                                    backupStatusMessage = "✅ Backup restored successfully"
                                    android.widget.Toast.makeText(
                                        context,
                                        "Backup restored successfully",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isBackupSuccess = false
                                    backupStatusMessage = "❌ Invalid or corrupted backup payload."
                                }
                            },
                            onFailure = { error ->
                                isBackupRestoring = false
                                isBackupSuccess = false
                                backupStatusMessage = "❌ ${error.message}"
                            }
                        )
                    }
                }
            }
        )

        return
    }

    // QUESTION BANK SYNC SCREEN

    if (showQuestionBankSyncScreen) {

        QuestionBankSyncScreen(
            signedInAccount = googleAccount,
            availableSubjects = mergedSubjects,
            onBack = {
                showQuestionBankSyncScreen = false
            },
            onSignInClick = {
                val signInClient = GoogleDriveManager.getGoogleSignInClient(context)
                googleSignInLauncher.launch(signInClient.signInIntent)
            }
        )

        return
    }

    // SETTINGS SCREEN

    if (showSettingsScreen) {

        SettingsScreen(
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            appLanguage = appLanguage,
            onLanguageChange = { newLang ->
                appLanguage = newLang
                com.example.myexampreparation.data.AppLanguageStorage.saveSelectedLanguage(context, newLang)
            },
            onUpdateAvailable = { updateResult ->
                otaUpdateData = updateResult
            },
            signedInAccount = googleAccount,
            onSignInClick = {
                val signInClient = GoogleDriveManager.getGoogleSignInClient(context)
                googleSignInLauncher.launch(signInClient.signInIntent)
            },
            onSignOutClick = {
                GoogleDriveManager.signOut(context) {
                    googleAccount = null
                    backupStatusMessage = null
                    android.widget.Toast.makeText(
                        context,
                        "Signed out successfully",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onOpenBackupClick = {
                showBackupScreen = true
            },
            onOpenQuestionBankSyncClick = {
                showQuestionBankSyncScreen = true
            },
            onBack = {
                showSettingsScreen = false
            }
        )

        return
    }

    // MOCK TEST SETUP SCREEN

    if (showMockTestSetup) {

        MockTestSetupScreen(
            availableSubjects = mergedSubjects,
            onBack = {
                showMockTestSetup = false
            },
            onStartMockTest = { quizSet ->
                selectedQuizSet = quizSet
                showQuiz = true
                showMockTestSetup = false
            }
        )

        return
    }

    // SEARCH SCREEN

    if (showSearchScreen) {

        SearchScreen(
            availableSubjects = mergedSubjects,
            onBack = {
                showSearchScreen = false
            },
            onSelectQuestion = { question ->
                val quizSet = QuizSet(
                    id = -7,
                    title = "Practice - ${question.topic}",
                    subtitle = "1 Question Practice",
                    questions = listOf(question)
                )
                selectedQuizSet = quizSet
                showQuiz = true
                showSearchScreen = false
            },
            onSelectNote = { note ->
                showNotesScreen = true
                showSearchScreen = false
            },
            onSelectSubject = { subject ->
                selectedSubject = subject
                showSearchScreen = false
            },
            onSelectTopic = { subject, topic ->
                selectedSubject = subject
                selectedTopic = topic
                showSearchScreen = false
            }
        )

        return
    }

    // STUDY GOALS SCREEN

    if (showStudyGoalsScreen) {

        StudyGoalsScreen(
            onBack = {
                showStudyGoalsScreen = false
            }
        )

        return
    }

    // NOTES SCREEN

    if (showNotesScreen) {

        NotesScreen(
            availableSubjects = mergedSubjects,
            onBack = {
                showNotesScreen = false
            }
        )

        return
    }

    // BOOKMARKED SCREEN

    if (showBookmarked) {

        QuestionBank.loadQuestions(context)

        BookmarkedScreen(
            onBack = {
                showBookmarked = false
            },
            onStartPractice = { quizSet ->
                selectedQuizSet = quizSet
                showQuiz = true
                showBookmarked = false
            }
        )

        return
    }

    // WRONG QUESTIONS SCREEN

    if (showWrongQuestions) {

        WrongQuestionsScreen(
            onBack = {
                showWrongQuestions = false
            },
            onStartPractice = { quizSet ->
                selectedQuizSet = quizSet
                showQuiz = true
                showWrongQuestions = false
            }
        )

        return
    }

    // PROGRESS SCREEN

    if (showProgress) {

        ProgressScreen(
            onBack = {
                showProgress = false
            }
        )

        return
    }

    // QUIZ / MOCK TEST SCREEN

    if (showQuiz && selectedQuizSet != null) {

        if (selectedQuizSet!!.id == -6 || selectedQuizSet!!.title.contains("Mock Test", ignoreCase = true) || selectedQuizSet!!.subtitle.contains("Mock Test", ignoreCase = true)) {
            MockTestScreen(
                quizSet = selectedQuizSet!!,
                onBack = {
                    showQuiz = false
                    showMockTestSetup = true
                }
            )
        } else {
            QuizScreen(
                quizSet = selectedQuizSet!!,
                onBack = {
                    showQuiz = false
                }
            )
        }

        return
    }

    // QUIZ SET SCREEN

    if (selectedTopic != null) {

        val importedQuizSets =
            importedQuestions
                .filter { question ->
                    SubjectMatcher.matchesSubject(question.subject, selectedSubject) &&
                            question.topic.trim().equals(selectedTopic?.trim(), ignoreCase = true)
                }
                .groupBy {
                    it.quizSetId
                }
                .filterKeys {
                    it > 0
                }
                .map { (setId, questions) ->

                    val firstQuestion =
                        questions.firstOrNull()

                    QuizSet(
                        id = setId,

                        title =
                            firstQuestion
                                ?.quizSetTitle
                                ?.takeIf { it.isNotBlank() }
                                ?: "Set $setId",

                        subtitle =
                            firstQuestion
                                ?.quizSetSubtitle
                                ?.takeIf { it.isNotBlank() }
                                ?: "${questions.size} Questions",

                        questions = questions
                    )
                }

        val isScienceSubject =
            selectedSubject?.name?.trim().equals("Science", ignoreCase = true) ||
                    selectedSubject?.name?.trim().equals("विज्ञान", ignoreCase = true) ||
                    selectedSubject?.englishName?.trim().equals("Science", ignoreCase = true) ||
                    selectedSubject?.englishName?.trim().equals("विज्ञान", ignoreCase = true)

        val isStateOfMatterTopic =
            selectedTopic?.trim().equals("पदार्थ की अवस्था", ignoreCase = true)

        val builtInQuizSets =
            if (isScienceSubject && isStateOfMatterTopic) {
                scienceQuizSets
            } else {
                emptyList()
            }

        val unsortedQuizSets = builtInQuizSets + importedQuizSets
        val allQuizSets = NaturalOrderComparator.sortQuizSets(unsortedQuizSets)

        QuizSetScreen(
            topic = selectedTopic!!,
            quizSets = allQuizSets,
            appLanguage = appLanguage,
            onBack = {
                selectedTopic = null
            },
            onQuizSetClick = { quizSet ->
                selectedQuizSet = quizSet
                showQuiz = true
            }
        )

        return
    }

    // TOPIC SCREEN

    if (selectedSubject != null) {

        TopicScreen(
            subject = selectedSubject!!,
            appLanguage = appLanguage,
            onBack = {
                selectedSubject = null
            },
            onTopicClick = { topic ->
                selectedTopic = topic
            }
        )

        return
    }

    // HOME & NAVIGATION DRAWER & BOTTOM BAR

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val result = com.example.myexampreparation.data.CsvQuestionImporter.importFromCsv(context = context, uri = uri)
            android.widget.Toast.makeText(context, result.message, android.widget.Toast.LENGTH_LONG).show()
            if (result.success) {
                importedQuestions = QuestionStorage.loadQuestions(context)
                QuestionBank.loadQuestions(context)
            }
        }
    }



    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        drawerShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val userProfile = com.example.myexampreparation.data.UserProfileStorage.getUserProfile(LocalContext.current)
                            val googleAccount = GoogleDriveManager.getSignedInAccount(LocalContext.current)
                            val displayName = if (userProfile.name.isNotBlank()) userProfile.name else (googleAccount?.displayName ?: googleAccount?.givenName ?: "Khemraj")
                            val avatarInitial = displayName.firstOrNull()?.uppercase() ?: "K"
                            val goalData = com.example.myexampreparation.data.StudyGoalStorage.getGoalData(LocalContext.current)
                            val drawerStrings = com.example.myexampreparation.data.AppStrings.get(appLanguage)
                            val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

                            // 1. PREMIUM PROFILE HEADER (COMPACT HORIZONTAL)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = avatarInitial,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "🔥 ${goalData.currentStreak} ${if (isHindi) "दिन स्ट्रीक" else "Days Streak"}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. ACCOUNT / SYNC ITEM
                            Text(
                                text = drawerStrings.drawerAccount,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 4.dp)
                            )

                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.CloudSync, contentDescription = drawerStrings.drawerQuestionBankSync, modifier = Modifier.size(22.dp)) },
                                label = {
                                    Column {
                                        Text(text = if (isHindi) "गूगल ड्राइव सिंक" else "Google Drive Sync", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium))
                                        Text(
                                            text = googleAccount?.email ?: "khemrajmeghwal999@gmail.com",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                selected = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showSettingsScreen = true
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. STUDY & PRACTICE
                            Text(
                                text = drawerStrings.drawerStudyAndPractice,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = drawerStrings.drawerNotes, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerNotes, style = MaterialTheme.typography.bodyMedium) },
                                selected = showNotesScreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showNotesScreen = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Description, contentDescription = drawerStrings.drawerPyq, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerPyq, style = MaterialTheme.typography.bodyMedium) },
                                selected = showPYQScreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showPYQScreen = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Timer, contentDescription = drawerStrings.drawerMockTest, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerMockTest, style = MaterialTheme.typography.bodyMedium) },
                                selected = showMockTestSetup,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showMockTestSetup = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Shuffle, contentDescription = drawerStrings.drawerRandomQuiz, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerRandomQuiz, style = MaterialTheme.typography.bodyMedium) },
                                selected = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    val allQs = QuestionBank.getAllQuestions()
                                    if (allQs.isNotEmpty()) {
                                        val selected = com.example.myexampreparation.data.QuizQuestionSelector.selectQuestionsWithVariety(
                                            candidatePool = allQs,
                                            targetCount = minOf(20, allQs.size),
                                            shuffleOptions = true,
                                            isFixedPaper = false
                                        )
                                        selectedQuizSet = QuizSet(
                                            id = -5,
                                            title = "All Subjects Random Quiz",
                                            subtitle = "Randomized ${selected.size} Questions",
                                            questions = selected
                                        )
                                        showQuiz = true
                                    }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.StarOutline, contentDescription = drawerStrings.drawerSaved, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerSaved, style = MaterialTheme.typography.bodyMedium) },
                                selected = showBookmarked,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showBookmarked = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Warning, contentDescription = drawerStrings.drawerWrong, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerWrong, style = MaterialTheme.typography.bodyMedium) },
                                selected = showWrongQuestions,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showWrongQuestions = true
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. QUESTION BANK
                            Text(
                                text = drawerStrings.drawerQuestionBankCategory,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.FileUpload, contentDescription = drawerStrings.drawerImport, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerImport, style = MaterialTheme.typography.bodyMedium) },
                                selected = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    csvLauncher.launch(
                                        arrayOf(
                                            "text/csv",
                                            "text/comma-separated-values",
                                            "application/csv",
                                            "text/*"
                                        )
                                    )
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Sync, contentDescription = drawerStrings.drawerQuestionBankSync, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerQuestionBankSync, style = MaterialTheme.typography.bodyMedium) },
                                selected = showQuestionBankSyncScreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showQuestionBankSyncScreen = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.CloudUpload, contentDescription = drawerStrings.drawerBackup, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerBackup, style = MaterialTheme.typography.bodyMedium) },
                                selected = showBackupScreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showBackupScreen = true
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5. APP
                            Text(
                                text = drawerStrings.drawerAppCategory,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Settings, contentDescription = drawerStrings.settings, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.settings, style = MaterialTheme.typography.bodyMedium) },
                                selected = showSettingsScreen,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showSettingsScreen = true
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.RateReview, contentDescription = drawerStrings.drawerFeedback, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerFeedback, style = MaterialTheme.typography.bodyMedium) },
                                selected = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
                                    val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = android.net.Uri.parse("mailto:myexampreparationhelp@gmail.com")
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "My Exam Preparation - User Feedback")
                                    }
                                    try {
                                        context.startActivity(
                                            android.content.Intent.createChooser(
                                                emailIntent,
                                                if (isHindi) "ईमेल ऐप चुनें" else "Send Email Feedback..."
                                            )
                                        )
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            if (isHindi) "कोई ईमेल ऐप नहीं मिला। myexampreparationhelp@gmail.com पर संपर्क करें।" else "No email app found. Please contact myexampreparationhelp@gmail.com",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Outlined.Info, contentDescription = drawerStrings.drawerAbout, modifier = Modifier.size(22.dp)) },
                                label = { Text(drawerStrings.drawerAbout, style = MaterialTheme.typography.bodyMedium) },
                                selected = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(vertical = 2.dp),
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showSettingsScreen = true
                                }
                            )
                        }
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Home,
                                        contentDescription = "Home",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI) "होम" else "Home",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (currentBottomTab == "home" && !drawerState.isOpen) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                        )
                                    )
                                },
                                selected = currentBottomTab == "home" && !drawerState.isOpen,
                                onClick = { currentBottomTab = "home" }
                            )
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Route,
                                        contentDescription = "Journey",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI) "रोडमैप" else "Journey",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (currentBottomTab == "journey" && !drawerState.isOpen) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                        )
                                    )
                                },
                                selected = currentBottomTab == "journey" && !drawerState.isOpen,
                                onClick = { currentBottomTab = "journey" }
                            )
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Insights,
                                        contentDescription = "Progress",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI) "प्रगति" else "Progress",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (currentBottomTab == "progress" && !drawerState.isOpen) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                        )
                                    )
                                },
                                selected = currentBottomTab == "progress" && !drawerState.isOpen,
                                onClick = { currentBottomTab = "progress" }
                            )
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI) "मेनू" else "Menu",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (drawerState.isOpen) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                        )
                                    )
                                },
                                selected = drawerState.isOpen,
                                onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentBottomTab) {
                            "home" -> HomeScreen(
                                themeMode = themeMode,
                                onThemeChange = onThemeChange,
                                appLanguage = appLanguage,
                                onOpenPYQClick = { showPYQScreen = true },
                                onSubjectClick = { selectedSubject = it },
                                onQuestionsImported = {
                                    importedQuestions = QuestionStorage.loadQuestions(context)
                                    QuestionBank.loadQuestions(context)
                                },
                                onProgressClick = { showProgress = true },
                                onMockTestClick = { showMockTestSetup = true },
                                onBookmarkedClick = { showBookmarked = true },
                                onWrongQuestionsClick = { showWrongQuestions = true },
                                onRandomQuizClick = {
                                    val allQuestions = QuestionBank.getAllQuestions()
                                    if (allQuestions.isNotEmpty()) {
                                        val selected = com.example.myexampreparation.data.QuizQuestionSelector.selectQuestionsWithVariety(
                                            candidatePool = allQuestions,
                                            targetCount = minOf(20, allQuestions.size),
                                            shuffleOptions = true,
                                            isFixedPaper = false
                                        )
                                        selectedQuizSet = QuizSet(
                                            id = -5,
                                            title = "All Subjects Random Quiz",
                                            subtitle = "Randomized ${selected.size} Questions",
                                            questions = selected
                                        )
                                        showQuiz = true
                                    }
                                },
                                onBackupClick = { showBackupScreen = true },
                                onSettingsClick = { showSettingsScreen = true },
                                onNotesClick = { showNotesScreen = true },
                                onSearchClick = { showSearchScreen = true },
                                onStudyGoalsClick = { showStudyGoalsScreen = true },
                                onContinueQuizClick = { activeProgress ->
                                    val allQuestions = QuestionBank.getAllQuestions()

                                    // 1. Try matching by subject + topic + quizSetId
                                    var matchedQs = allQuestions.filter { q ->
                                        com.example.myexampreparation.data.SubjectMatcher.matchesSubjectName(q.subject, activeProgress.subject) &&
                                                q.topic.trim().equals(activeProgress.topic.trim(), ignoreCase = true) &&
                                                (activeProgress.quizSetId <= 0 || q.quizSetId == activeProgress.quizSetId)
                                    }

                                    // 2. If empty, try matching by subject + topic
                                    if (matchedQs.isEmpty() && activeProgress.topic.isNotBlank()) {
                                        matchedQs = allQuestions.filter { q ->
                                            com.example.myexampreparation.data.SubjectMatcher.matchesSubjectName(q.subject, activeProgress.subject) &&
                                                    q.topic.trim().equals(activeProgress.topic.trim(), ignoreCase = true)
                                        }
                                    }

                                    // 3. If empty, try matching by subject
                                    if (matchedQs.isEmpty() && activeProgress.subject.isNotBlank()) {
                                        matchedQs = allQuestions.filter { q ->
                                            com.example.myexampreparation.data.SubjectMatcher.matchesSubjectName(q.subject, activeProgress.subject)
                                        }
                                    }

                                    // 4. Fallback to all questions
                                    if (matchedQs.isEmpty()) {
                                        matchedQs = allQuestions
                                    }

                                    if (matchedQs.isNotEmpty()) {
                                        com.example.myexampreparation.data.RecentSubjectStorage.recordSubjectUsage(context, activeProgress.subject)
                                        selectedQuizSet = QuizSet(
                                            id = activeProgress.quizSetId,
                                            title = if (activeProgress.quizSetTitle.isNotBlank()) activeProgress.quizSetTitle else "${activeProgress.subject} — ${activeProgress.topic}",
                                            subtitle = "${matchedQs.size} Questions",
                                            questions = matchedQs
                                        )
                                        showQuiz = true
                                    }
                                },
                                onViewJourneyClick = { currentBottomTab = "journey" },
                                onMenuClick = { coroutineScope.launch { drawerState.open() } },
                                allSubjects = mergedSubjects
                            )
                            "journey" -> JourneyScreen(
                                subjects = mergedSubjects,
                                onSubjectClick = { selectedSubject = it },
                                onMenuClick = { coroutineScope.launch { drawerState.open() } }
                            )
                            "progress" -> ProgressScreen(
                                onBack = { currentBottomTab = "home" }
                            )
                            "settings" -> SettingsScreen(
                                themeMode = themeMode,
                                onThemeChange = onThemeChange,
                                appLanguage = appLanguage,
                                onLanguageChange = { newLang ->
                                    appLanguage = newLang
                                    com.example.myexampreparation.data.AppLanguageStorage.saveSelectedLanguage(context, newLang)
                                },
                                onUpdateAvailable = { updateResult ->
                                    otaUpdateData = updateResult
                                },
                                signedInAccount = googleAccount,
                                onSignInClick = {
                                    val signInClient = GoogleDriveManager.getGoogleSignInClient(context)
                                    googleSignInLauncher.launch(signInClient.signInIntent)
                                },
                                onSignOutClick = {
                                    GoogleDriveManager.signOut(context) {
                                        googleAccount = null
                                        backupStatusMessage = null
                                    }
                                },
                                onOpenBackupClick = { showBackupScreen = true },
                                onOpenQuestionBankSyncClick = { showQuestionBankSyncScreen = true },
                                onBack = { currentBottomTab = "home" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun performQuickSync(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    isHindi: Boolean,
    onQuestionsUpdated: () -> Unit
) {
    android.widget.Toast.makeText(
        context,
        if (isHindi) "प्रश्न बैंक सिंक हो रहा है..." else "Syncing Question Bank...",
        android.widget.Toast.LENGTH_SHORT
    ).show()

    coroutineScope.launch {
        if (com.example.myexampreparation.data.GoogleDriveManager.isSignedIn(context)) {
            val downloadResult = com.example.myexampreparation.data.GoogleDriveManager.downloadMasterQuestionBankFromDrive(context)
            downloadResult.fold(
                onSuccess = { csvContent ->
                    val syncResult = com.example.myexampreparation.data.QuestionBankSyncManager.parseAndMergeMasterCsv(context, csvContent)
                    com.example.myexampreparation.data.QuestionBank.loadQuestions(context)
                    onQuestionsUpdated()

                    android.widget.Toast.makeText(
                        context,
                        if (syncResult.success) {
                            if (isHindi) "✅ सिंक सफल: ${syncResult.message}" else "✅ Sync Complete: ${syncResult.message}"
                        } else {
                            "❌ ${syncResult.message}"
                        },
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                },
                onFailure = { error ->
                    com.example.myexampreparation.data.QuestionBank.loadQuestions(context)
                    onQuestionsUpdated()

                    val allQs = com.example.myexampreparation.data.QuestionBank.getAllQuestions()
                    android.widget.Toast.makeText(
                        context,
                        if (isHindi) "✅ प्रश्न बैंक री-सिंक हुआ (कुल ${allQs.size} प्रश्न)" else "✅ Question Bank Synced (${allQs.size} Total Questions)",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            com.example.myexampreparation.data.QuestionBank.loadQuestions(context)
            onQuestionsUpdated()

            val allQs = com.example.myexampreparation.data.QuestionBank.getAllQuestions()
            android.widget.Toast.makeText(
                context,
                if (isHindi) "✅ स्थानीय प्रश्न बैंक सिंक हुआ (कुल ${allQs.size} प्रश्न)" else "✅ Local Question Bank Synced (${allQs.size} Total Questions)",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    appLanguage: com.example.myexampreparation.data.AppLanguage,
    onOpenPYQClick: () -> Unit = {},
    onSubjectClick: (Subject) -> Unit,
    onQuestionsImported: () -> Unit,
    onProgressClick: () -> Unit,
    onMockTestClick: () -> Unit,
    onBookmarkedClick: () -> Unit,
    onWrongQuestionsClick: () -> Unit,
    onRandomQuizClick: () -> Unit,
    onBackupClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onStudyGoalsClick: () -> Unit,
    onContinueQuizClick: (com.example.myexampreparation.data.InAppQuizProgress) -> Unit,
    onViewJourneyClick: () -> Unit,
    onMenuClick: () -> Unit,
    allSubjects: List<Subject>
) {
    val context = LocalContext.current
    val strings = com.example.myexampreparation.data.AppStrings.get(appLanguage)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val userProfile = remember { com.example.myexampreparation.data.UserProfileStorage.getUserProfile(context) }
    val googleAccount = remember { GoogleDriveManager.getSignedInAccount(context) }
    val displayName = remember(userProfile, googleAccount) {
        if (userProfile.name.isNotBlank()) {
            userProfile.name.trim().split(" ").firstOrNull() ?: userProfile.name.trim()
        } else {
            googleAccount?.displayName?.split(" ")?.firstOrNull()
                ?: googleAccount?.givenName
                ?: "Khemraj"
        }
    }
    val greetingName = "${strings.greeting}, $displayName 👋"

    val goalData = remember { com.example.myexampreparation.data.StudyGoalStorage.getGoalData(context) }
    val activeProgress = remember { com.example.myexampreparation.data.InAppQuizProgressStorage.getLatestActiveProgress(context) }

    val sortedSubjects: List<Subject> = remember(allSubjects) {
        com.example.myexampreparation.data.RecentSubjectStorage.sortSubjectsByRecentUsage(context, allSubjects)
    }
    val recentSubjects = remember(sortedSubjects) { sortedSubjects.take(3) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable?>(null) }
    var updateDialogDismissedForSession by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!updateDialogDismissedForSession) {
            val result = com.example.myexampreparation.update.AppUpdater.checkForUpdate(context)
            if (result is com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable) {
                updateInfo = result
                showUpdateDialog = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "My Exam ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Preparation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    var syncMenuExpanded by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    Box {
                        IconButton(onClick = { syncMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = if (isHindi) "सिंक करें" else "Sync",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = syncMenuExpanded,
                            onDismissRequest = { syncMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Sync,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isHindi) "प्रश्न बैंक सिंक करें" else "Sync Question Bank",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                        )
                                    }
                                },
                                onClick = {
                                    syncMenuExpanded = false
                                    performQuickSync(
                                        context = context,
                                        coroutineScope = coroutineScope,
                                        isHindi = isHindi,
                                        onQuestionsUpdated = onQuestionsImported
                                    )
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val nextTheme = if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            onThemeChange(nextTheme)
                        }
                    ) {
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val homeScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(homeScrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 1. EXAM BADGE & GREETING CARD
                ExamCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (isHindi) "राजस्थान CET (Senior Secondary)" else "Rajasthan CET (Senior Secondary)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = greetingName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.greetingSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 2. TODAY'S GOAL CARD
                val progressRatio = if (goalData.dailyGoal > 0) {
                    (goalData.todayQuestionsAttempted.toFloat() / goalData.dailyGoal.toFloat()).coerceIn(0f, 1f)
                } else 0f
                val animatedProgress by animateFloatAsState(targetValue = progressRatio, label = "progress")
                val percent = (progressRatio * 100).toInt()
                val remainingQs = (goalData.dailyGoal - goalData.todayQuestionsAttempted).coerceAtLeast(0)

                ExamCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.todayGoal,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${goalData.todayQuestionsAttempted} / ${goalData.dailyGoal} ${strings.questions} (${if (isHindi) "शेष: $remainingQs" else "$remainingQs Left"})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // HIGHLIGHTED BADGE FOR STREAK & PERCENTAGE
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "🔥 ${goalData.currentStreak} ${if (isHindi) "दिन" else "Days"}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$percent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ExamProgressBar(progress = animatedProgress)
                }

                // 3. CONTINUE QUIZ BANNER CARD
                val fallbackSubject = allSubjects.firstOrNull()
                val targetProgress = activeProgress ?: if (fallbackSubject != null) {
                    com.example.myexampreparation.data.InAppQuizProgress(
                        subject = fallbackSubject.name,
                        topic = fallbackSubject.topics.firstOrNull() ?: "",
                        quizSetId = 1,
                        quizSetTitle = "${fallbackSubject.name} — Set 1",
                        totalQuestions = 10,
                        currentQuestionIndex = 0,
                        selectedAnswers = emptyList()
                    )
                } else null

                if (targetProgress != null) {
                    ExamCard(
                        onClick = {
                            com.example.myexampreparation.data.RecentSubjectStorage.recordSubjectUsage(context, targetProgress.subject)
                            onContinueQuizClick(targetProgress)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (activeProgress != null) strings.continueLearning else (if (isHindi) "पढ़ाई शुरू करें" else "Start Learning"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${targetProgress.subject}${if (targetProgress.topic.isNotBlank()) " — ${targetProgress.topic}" else ""}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (activeProgress != null) {
                                        "${targetProgress.quizSetTitle} • ${targetProgress.answeredCount}/${targetProgress.totalQuestions} ${strings.questions}"
                                    } else {
                                        if (isHindi) "तैयारी शुरू करने के लिए टैप करें" else "Tap to start your preparation"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            CompactActionButton(
                                text = if (activeProgress != null) strings.continueButton else (if (isHindi) "शुरू करें →" else "Start →"),
                                onClick = {
                                    com.example.myexampreparation.data.RecentSubjectStorage.recordSubjectUsage(context, targetProgress.subject)
                                    onContinueQuizClick(targetProgress)
                                },
                                isOutlined = false
                            )
                        }
                    }
                }

                // 3.5. RAJ CET PYQ FEATURED CARD
                ExamCard(
                    onClick = onOpenPYQClick
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
                                contentDescription = "RAJ CET PYQ",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "राजस्थान CET PYQ" else "RAJ CET PYQ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isHindi) "विषय व टॉपिक अनुसार विगत वर्षों के प्रश्न" else "Previous Year Papers by Subject & Topic",
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

                // 4. RECENT SUBJECTS SECTION
                val displaySubjects = if (recentSubjects.isNotEmpty()) recentSubjects.take(2) else sortedSubjects.take(2)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "हाल में प्रयुक्त विषय" else "Recent Subjects",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    )
                    TextButton(onClick = onViewJourneyClick) {
                        Text(strings.seeAll)
                    }
                }

                displaySubjects.forEach { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = {
                            com.example.myexampreparation.data.RecentSubjectStorage.recordSubjectUsage(context, subject.name)
                            com.example.myexampreparation.data.RecentSubjectStorage.recordSubjectUsage(context, subject.englishName)
                            onSubjectClick(subject)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Anchored Banner Test Ad at bottom of Home Screen
            BannerAdView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        val info = updateInfo!!
        val displayVersion = if (info.latestVersionName.isNotBlank()) info.latestVersionName else "v${info.latestVersionCode}"

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showUpdateDialog = false
                updateDialogDismissedForSession = true
            },
            title = {
                Text(
                    text = if (isHindi) "🚀 नया अपडेट उपलब्ध है" else "New Update Available",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isHindi)
                            "My Exam Preparation का नया वर्ज़न ($displayVersion) उपलब्ध है।"
                        else
                            "A new version of My Exam Preparation is available ($displayVersion).",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (info.releaseNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHindi) "नवीनतम बदलाव:" else "Release Notes:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                        )
                        Text(
                            text = info.releaseNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUpdateDialog = false
                        updateDialogDismissedForSession = true

                        if (info.apkUrl.isNotBlank()) {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(info.apkUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, if (isHindi) "डाउनलोड लिंक खोलने में असमर्थ" else "Unable to open download URL", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text(if (isHindi) "अभी अपडेट करें" else "Update Now")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showUpdateDialog = false
                        updateDialogDismissedForSession = true
                    }
                ) {
                    Text(if (isHindi) "बाद में" else "Later")
                }
            }
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val allBankQuestions = remember { QuestionBank.getAllQuestions() }
    val count = remember(subject, allBankQuestions) {
        allBankQuestions.count { q ->
            SubjectMatcher.matchesSubject(q.subject, subject)
        }
    }

    ExamCard(onClick = onClick) {
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
                    imageVector = getSubjectIcon(subject.name, subject.englishName),
                    contentDescription = subject.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHindi) subject.name else subject.englishName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count ${if (isHindi) "प्रश्न" else "Questions"} • ${subject.topics.size} ${if (isHindi) "टॉपिक्स" else "Topics"}",
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


@Composable
fun TopBarBackIconButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appLanguage = com.example.myexampreparation.data.AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
    IconButton(
        onClick = onBack,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = if (isHindi) "वापस जाएं" else "Back",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(
    subject: Subject,
    appLanguage: com.example.myexampreparation.data.AppLanguage,
    onBack: () -> Unit,
    onTopicClick: (String) -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.myexampreparation.data.AppStrings.get(appLanguage)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val allQuestions = remember { QuestionBank.getAllQuestions() }
    val activeTopics = remember(subject, allQuestions) {
        subject.topics.filter { topic ->
            allQuestions.any { question ->
                SubjectMatcher.matchesSubject(question.subject, subject) &&
                        question.topic.trim().equals(topic.trim(), ignoreCase = true)
            }
        }
    }

    val subjectQuestions = remember(subject, allQuestions) {
        allQuestions.filter { question ->
            SubjectMatcher.matchesSubject(question.subject, subject)
        }
    }

    val attemptedTopicsCount = remember(activeTopics, subject, context) {
        activeTopics.count { topic ->
            QuizAttemptStorage.getQuizSetStatus(context, subject.name, topic, 0, "").isCompleted
        }
    }
    val subjectCoverageRatio = if (activeTopics.isNotEmpty()) (attemptedTopicsCount.toFloat() / activeTopics.size.toFloat()).coerceIn(0f, 1f) else 0f
    val subjectCoveragePercent = (subjectCoverageRatio * 100).toInt()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            com.example.myexampreparation.ui.components.ExamTopAppBar(
                title = if (isHindi) subject.name else subject.englishName,
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back"
            )
        }
    ) { paddingValues ->
        if (activeTopics.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "इस विषय के प्रश्न अभी उपलब्ध नहीं हैं।" else "No questions available for this subject yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SUBJECT SUMMARY CARD AT TOP OF TOPIC LIST
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
                                    imageVector = getSubjectIcon(subject.name, subject.englishName),
                                    contentDescription = subject.name,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) subject.name else subject.englishName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${subjectQuestions.size} ${if (isHindi) "प्रश्न" else "Questions"} • ${activeTopics.size} ${if (isHindi) "टॉपिक्स" else "Topics"}",
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
                                text = "${attemptedTopicsCount} / ${activeTopics.size} ${if (isHindi) "टॉपिक्स पूर्ण" else "Topics Completed"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${subjectCoveragePercent}% ${if (isHindi) "प्रगति" else "Coverage"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.myexampreparation.ui.components.ExamProgressBar(progress = subjectCoverageRatio)
                    }
                }

                item {
                    Text(
                        text = if (isHindi) "टॉपिक सूची (Topics)" else "Topic List",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    )
                }

                items(activeTopics) { topic ->
                    val topicQuestions = subjectQuestions.filter { question ->
                        question.topic.trim().equals(topic.trim(), ignoreCase = true)
                    }

                    val quizSetCount = topicQuestions
                        .map { it.quizSetId }
                        .filter { it > 0 }
                        .distinct()
                        .size

                    val questionCount = topicQuestions.size

                    val topicAttempts = remember(topic, topicQuestions) {
                        QuizAttemptStorage.loadAttempts(context).filter { att ->
                            att.quizSetTitle.contains(topic, ignoreCase = true) ||
                            (att.subject.equals(subject.name, ignoreCase = true) && att.topic.equals(topic, ignoreCase = true))
                        }
                    }

                    val bestScore = remember(topicAttempts) {
                        topicAttempts.maxOfOrNull { it.score } ?: 0
                    }
                    val isAttempted = topicAttempts.isNotEmpty()
                    val topicProgressRatio = if (questionCount > 0) (bestScore.toFloat() / questionCount.toFloat()).coerceIn(0f, 1f) else 0f

                    TopicCard(
                        topic = topic,
                        quizSetCount = quizSetCount,
                        questionCount = questionCount,
                        bestScore = bestScore,
                        isAttempted = isAttempted,
                        topicAttemptsCount = topicAttempts.size,
                        topicProgressRatio = topicProgressRatio,
                        appLanguage = appLanguage,
                        onClick = {
                            onTopicClick(topic)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TopicCard(
    topic: String,
    quizSetCount: Int,
    questionCount: Int,
    bestScore: Int,
    isAttempted: Boolean,
    topicAttemptsCount: Int,
    topicProgressRatio: Float,
    appLanguage: com.example.myexampreparation.data.AppLanguage,
    onClick: () -> Unit
) {
    val strings = com.example.myexampreparation.data.AppStrings.get(appLanguage)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI
    val percentComplete = (topicProgressRatio * 100).toInt()

    ExamCard(onClick = onClick) {
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
                    contentDescription = topic,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (isAttempted) {
                    Text(
                        text = "$bestScore / $questionCount ${if (isHindi) "प्रश्न" else "Questions"} • $percentComplete% ${if (isHindi) "पूर्ण" else "Complete"}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "${if (isHindi) "प्रयास" else "Attempts"}: $topicAttemptsCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (quizSetCount > 0) {
                            "$quizSetCount ${strings.quizSets} · $questionCount ${strings.questions}"
                        } else {
                            "$questionCount ${strings.questions}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            CompactActionButton(
                text = if (isAttempted) (if (isHindi) "पुनः प्रयास" else "Reattempt") else (if (isHindi) "शुरू करें" else "Start"),
                onClick = onClick,
                isOutlined = isAttempted
            )
        }

        if (isAttempted) {
            Spacer(modifier = Modifier.height(10.dp))
            com.example.myexampreparation.ui.components.ExamProgressBar(progress = topicProgressRatio)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSetScreen(
    topic: String,
    quizSets: List<QuizSet>,
    appLanguage: com.example.myexampreparation.data.AppLanguage,
    onBack: () -> Unit,
    onQuizSetClick: (QuizSet) -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.myexampreparation.data.AppStrings.get(appLanguage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(strings.quizSets)
                },
                navigationIcon = {
                    TopBarBackIconButton(onBack = onBack)
                }
            )
        }
    ) { paddingValues ->
        if (quizSets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📚 $topic",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = strings.noQuizSetsAvailable,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = topic,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "${quizSets.size} ${strings.quizSetsAvailable}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    val completedCount = remember(quizSets) {
                        quizSets.count { set ->
                            val firstQ = set.questions.firstOrNull()
                            val sub = firstQ?.subject ?: ""
                            val top = firstQ?.topic ?: ""
                            val status = com.example.myexampreparation.data.QuizAttemptStorage.getQuizSetStatus(context, sub, top, set.id, set.title)
                            status.isCompleted
                        }
                    }
                    val totalSets = quizSets.size
                    val progressRatio = if (totalSets > 0) completedCount.toFloat() / totalSets.toFloat() else 0f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.topicProgress,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "$completedCount / $totalSets ${strings.setsCompleted}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    val allTopicQuestions = quizSets.flatMap { it.questions }
                    if (allTopicQuestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val selected = com.example.myexampreparation.data.QuizQuestionSelector.selectQuestionsWithVariety(
                                        candidatePool = allTopicQuestions,
                                        targetCount = minOf(20, allTopicQuestions.size),
                                        shuffleOptions = true,
                                        isFixedPaper = false
                                    )
                                    val randomQuizSet = QuizSet(
                                        id = -4,
                                        title = "${strings.randomQuiz} - $topic",
                                        subtitle = "Randomized ${selected.size} Questions",
                                        questions = selected
                                    )
                                    onQuizSetClick(randomQuizSet)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔀",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Spacer(modifier = Modifier.padding(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.randomQuiz,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "${strings.shuffledQuestionsFrom} $topic",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Text(
                                    text = "›",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                items(quizSets) { quizSet ->
                    QuizSetCard(
                        quizSet = quizSet,
                        appLanguage = appLanguage,
                        onClick = {
                            onQuizSetClick(quizSet)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSetCard(
    quizSet: QuizSet,
    appLanguage: com.example.myexampreparation.data.AppLanguage,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val strings = com.example.myexampreparation.data.AppStrings.get(appLanguage)
    val isHindi = appLanguage == com.example.myexampreparation.data.AppLanguage.HINDI

    val firstQ = quizSet.questions.firstOrNull()
    val qSubject = firstQ?.subject ?: ""
    val qTopic = firstQ?.topic ?: ""

    var showStartAgainDialog by remember { mutableStateOf(false) }

    val completedStatus = remember(quizSet, qSubject, qTopic) {
        com.example.myexampreparation.data.QuizAttemptStorage.getQuizSetStatus(
            context = context,
            subject = qSubject,
            topic = qTopic,
            quizSetId = quizSet.id,
            quizSetTitle = quizSet.title
        )
    }

    val inProgressData = remember(quizSet, qSubject, qTopic) {
        com.example.myexampreparation.data.InAppQuizProgressStorage.getProgress(
            context = context,
            subject = qSubject,
            topic = qTopic,
            quizSetId = quizSet.id
        )
    }

    val isInProgress = inProgressData != null && inProgressData.answeredCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = quizSet.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = quizSet.subtitle,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${quizSet.questions.size} ${strings.questions}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    when {
                        completedStatus.isCompleted -> {
                            val completedBadge = if (isHindi) "✅ पूर्ण" else "✅ Completed"
                            val scoreLabel = if (isHindi) "अंक" else "Score"
                            val scoreText = if (completedStatus.totalQuestions > 0) {
                                "$completedBadge • $scoreLabel: ${completedStatus.latestScore}/${completedStatus.totalQuestions}"
                            } else {
                                "$completedBadge • $scoreLabel: ${completedStatus.latestScore}"
                            }

                            Text(
                                text = scoreText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        isInProgress -> {
                            val continueBadge = if (isHindi) "🟡 जारी रखें" else "🟡 Continue"
                            Text(
                                text = "$continueBadge • ${inProgressData!!.answeredCount}/${inProgressData.totalQuestions}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        else -> {
                            val notStartedText = if (isHindi) "⚪ शुरू नहीं किया" else "⚪ Not Started"
                            Text(
                                text = notStartedText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (isInProgress) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "▶ जारी रखें" else "▶ Continue")
                    }

                    OutlinedButton(
                        onClick = { showStartAgainDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "🔄 पुनः शुरू करें" else "🔄 Start Again")
                    }
                }
            }
        }
    }

    if (showStartAgainDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showStartAgainDialog = false },
            title = { Text(if (isHindi) "क्या आप यह क्विज़ पुनः शुरू करना चाहते हैं?" else "Do you want to restart this Quiz?") },
            text = { Text(if (isHindi) "पुनः शुरू करने पर आपकी पिछली जारी प्रगति रीसेट हो जाएगी।" else "Restarting will reset your previous in-progress data.") },
            confirmButton = {
                Button(
                    onClick = {
                        showStartAgainDialog = false
                        com.example.myexampreparation.data.InAppQuizProgressStorage.clearProgress(
                            context = context,
                            subject = qSubject,
                            topic = qTopic,
                            quizSetId = quizSet.id
                        )
                        onClick()
                    }
                ) {
                    Text(if (isHindi) "हाँ, पुनः शुरू करें" else "Yes, Restart")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showStartAgainDialog = false }) {
                    Text(if (isHindi) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

object NaturalOrderComparator {
    fun extractNumber(title: String, id: Int): Int {
        val regex = Regex("\\d+")
        val match = regex.find(title)
        return match?.value?.toIntOrNull() ?: id
    }

    fun sortQuizSets(quizSets: List<QuizSet>): List<QuizSet> {
        return quizSets.sortedWith(compareBy<QuizSet> { extractNumber(it.title, it.id) }.thenBy { it.id })
    }
}