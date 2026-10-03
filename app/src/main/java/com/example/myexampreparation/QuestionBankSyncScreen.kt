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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.AppLanguage
import com.example.myexampreparation.data.AppLanguageStorage
import com.example.myexampreparation.data.GoogleDriveManager
import com.example.myexampreparation.data.QuestionBank
import com.example.myexampreparation.data.QuestionBankSyncManager
import com.example.myexampreparation.data.Subject
import com.example.myexampreparation.data.SubjectMatcher
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankSyncScreen(
    signedInAccount: GoogleSignInAccount?,
    availableSubjects: List<Subject>,
    onBack: () -> Unit,
    onSignInClick: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI
    val coroutineScope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var isCheckingDrive by remember { mutableStateOf(false) }
    var driveFileFound by remember { mutableStateOf<Boolean?>(null) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(true) }

    var syncMetadata by remember {
        mutableStateOf(QuestionBankSyncManager.getSyncMetadata(context))
    }

    val lastSyncText = remember(syncMetadata.lastSyncTimestamp, isHindi) {
        if (syncMetadata.lastSyncTimestamp > 0L) {
            DateFormat.format("dd MMM yyyy, hh:mm a", Date(syncMetadata.lastSyncTimestamp)).toString()
        } else {
            if (isHindi) "कभी नहीं" else "Never"
        }
    }

    val allQuestions = remember(syncMetadata) { QuestionBank.getAllQuestions() }

    // Check if Master File exists on Google Drive upon screen open / sign-in status
    LaunchedEffect(signedInAccount) {
        if (signedInAccount != null && GoogleDriveManager.isSignedIn(context)) {
            isCheckingDrive = true
            driveFileFound = GoogleDriveManager.checkMasterFileExistsOnDrive(context)
            isCheckingDrive = false
        } else {
            driveFileFound = null
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
                title = { Text(if (isHindi) "प्रश्न बैंक सिंक" else "Question Bank Sync", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
            // 1. ACCOUNT & DRIVE STATUS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "गूगल ड्राइव खाता एवं फ़ाइल स्थिति" else "Google Drive Account & File Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (signedInAccount != null) {
                        Text(
                            text = "${if (isHindi) "गूगल खाता" else "Google Account"}: ${signedInAccount.email ?: signedInAccount.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Master File: My_Exam_Preparation_Master_Questions.csv",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val driveStatusText = when {
                            isCheckingDrive -> if (isHindi) "☁️ स्थिति: ड्राइव जाँची जा रही है..." else "☁️ Status: Checking Drive..."
                            driveFileFound == true -> if (isHindi) "☁️ स्थिति: गूगल ड्राइव में फ़ाइल मौजूद है" else "☁️ Status: Found on Google Drive (My Drive)"
                            driveFileFound == false -> if (isHindi) "☁️ स्थिति: गूगल ड्राइव में फ़ाइल नहीं मिली" else "☁️ Status: Not Found on Google Drive"
                            else -> if (isHindi) "☁️ स्थिति: अज्ञात" else "☁️ Status: Unknown"
                        }

                        Text(
                            text = driveStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (driveFileFound == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = if (isHindi) "कृपया पहले गूगल से साइन इन करें।" else "Please sign in with Google first.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onSignInClick, modifier = Modifier.fillMaxWidth()) {
                            Text(if (isHindi) "गूगल से साइन इन करें" else "Sign in with Google")
                        }
                    }
                }
            }

            // 2. SYNC METADATA CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "प्रश्न बैंक सिंक क्रियाएँ" else "Question Bank Sync Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(if (isHindi) "📊 कुल स्थानीय प्रश्न: ${allQuestions.size}" else "📊 Total Local Questions: ${allQuestions.size}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isHindi) "🕐 अंतिम सिंक: $lastSyncText" else "🕐 Last Sync: $lastSyncText", color = MaterialTheme.colorScheme.primary)

                    Spacer(modifier = Modifier.height(16.dp))

                    // SYNC BUTTON
                    Button(
                        onClick = {
                            if (!GoogleDriveManager.isSignedIn(context)) {
                                syncStatusMessage = if (isHindi) "कृपया पहले गूगल से साइन इन करें।" else "Please sign in with Google first."
                                isSuccessStatus = false
                            } else {
                                isSyncing = true
                                syncStatusMessage = if (isHindi) "गूगल ड्राइव से प्रश्न बैंक डाउनलोड हो रहा है..." else "Downloading Master Question Bank from Google Drive..."
                                isSuccessStatus = true

                                coroutineScope.launch {
                                    val downloadResult = GoogleDriveManager.downloadMasterQuestionBankFromDrive(context)

                                    downloadResult.fold(
                                        onSuccess = { csvContent ->
                                            val syncResult = QuestionBankSyncManager.parseAndMergeMasterCsv(context, csvContent)
                                            isSyncing = false

                                            if (syncResult.success) {
                                                isSuccessStatus = true
                                                syncStatusMessage = syncResult.message
                                                syncMetadata = QuestionBankSyncManager.getSyncMetadata(context)
                                                driveFileFound = true
                                            } else {
                                                isSuccessStatus = false
                                                syncStatusMessage = "❌ ${syncResult.message}"
                                            }
                                        },
                                        onFailure = { error ->
                                            isSyncing = false
                                            isSuccessStatus = false
                                            syncStatusMessage = "❌ ${error.message}"
                                        }
                                    )
                                }
                            }
                        },
                        enabled = signedInAccount != null && !isSyncing && !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSyncing) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(if (isHindi) "प्रश्न बैंक सिंक हो रहा है..." else "Syncing Question Bank...")
                            }
                        } else {
                            Text(if (isHindi) "☁️ प्रश्न बैंक सिंक करें" else "☁️ Sync Question Bank")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // CREATE / UPLOAD MASTER BUTTON
                    OutlinedButton(
                        onClick = {
                            if (!GoogleDriveManager.isSignedIn(context)) {
                                syncStatusMessage = if (isHindi) "कृपया पहले गूगल से साइन इन करें।" else "Please sign in with Google first."
                                isSuccessStatus = false
                            } else {
                                isUploading = true
                                syncStatusMessage = if (isHindi) "स्थानीय प्रश्नों को मास्टर CSV के रूप में अपलोड किया जा रहा है..." else "Uploading Local Questions as Master CSV to Google Drive..."
                                isSuccessStatus = true

                                coroutineScope.launch {
                                    val csvContent = QuestionBankSyncManager.exportLocalQuestionsToCsv()
                                    val uploadResult = GoogleDriveManager.uploadMasterQuestionBankToDrive(context, csvContent)

                                    isUploading = false
                                    uploadResult.fold(
                                        onSuccess = { timestamp ->
                                            isSuccessStatus = true
                                            syncStatusMessage = if (isHindi) "✅ मास्टर प्रश्न बैंक गूगल ड्राइव पर सफलतापूर्वक अपलोड हो गया है!" else "✅ Master Question Bank (My_Exam_Preparation_Master_Questions.csv) created/updated on Google Drive!"
                                            QuestionBankSyncManager.saveSyncMetadata(context, timestamp, allQuestions.size)
                                            syncMetadata = QuestionBankSyncManager.getSyncMetadata(context)
                                            driveFileFound = true
                                        },
                                        onFailure = { error ->
                                            isSuccessStatus = false
                                            syncStatusMessage = "❌ ${error.message}"
                                        }
                                    )
                                }
                            }
                        },
                        enabled = signedInAccount != null && !isSyncing && !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUploading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(if (isHindi) "मास्टर प्रश्न बैंक अपलोड हो रहा है..." else "Uploading Master CSV...")
                            }
                        } else {
                            Text(if (isHindi) "⬆️ मास्टर प्रश्न बैंक अपलोड करें" else "⬆️ Create/Upload Master Question Bank")
                        }
                    }

                    if (!syncStatusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = syncStatusMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSuccessStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // 3. QUESTION BANK SUMMARY SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "📊 स्थानीय प्रश्न बैंक विवरण" else "📊 Local Question Bank Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    availableSubjects.forEach { subject ->
                        val count = allQuestions.count { q ->
                            SubjectMatcher.matchesSubject(q.subject, subject)
                        }
                        val subjectName = if (isHindi) subject.name else "${subject.englishName} (${subject.name})"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = subjectName, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "$count ${if (isHindi) "प्रश्न" else "Qs"}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
