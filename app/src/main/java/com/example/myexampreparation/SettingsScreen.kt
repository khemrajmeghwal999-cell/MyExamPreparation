package com.example.myexampreparation

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.myexampreparation.ui.components.ExamCard
import com.example.myexampreparation.ui.components.ExamTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.myexampreparation.data.AppStrings
import com.example.myexampreparation.data.UserProfile
import com.example.myexampreparation.data.UserProfileStorage
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    appLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onUpdateAvailable: (com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable) -> Unit,
    signedInAccount: GoogleSignInAccount?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onOpenBackupClick: () -> Unit,
    onOpenQuestionBankSyncClick: () -> Unit,
    onBack: () -> Unit
) {
    val strings = AppStrings.get(appLanguage)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isCheckingUpdates by remember { mutableStateOf(false) }
    val currentVersionCode = remember { com.example.myexampreparation.update.AppUpdater.getCurrentVersionCode(context) }
    val currentVersionName = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    android.content.pm.PackageManager.PackageInfoFlags.of(0)
                ).versionName ?: "1.1"
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1"
            }
        } catch (e: Exception) {
            "1.1"
        }
    }
    val isHindi = appLanguage == AppLanguage.HINDI

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ExamTopAppBar(
                title = "⚙️ ${strings.settings}",
                onBack = onBack,
                backContentDescription = if (isHindi) "वापस जाएं" else "Back"
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

            // 1. LANGUAGE SECTION
            ExamCard {
                Column {
                    Text(
                        text = "🌐 ${strings.language.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = strings.language,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = appLanguage == AppLanguage.HINDI,
                            onClick = { onLanguageChange(AppLanguage.HINDI) },
                            label = { Text(strings.hindi) },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = appLanguage == AppLanguage.ENGLISH,
                            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                            label = { Text(strings.english) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. MY PROFILE SECTION
            var userProfileState by remember { mutableStateOf(UserProfileStorage.getUserProfile(context)) }
            var showEditProfileDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👤 ${strings.myProfile.uppercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = userProfileState.name.ifBlank { if (isHindi) "उपयोगकर्ता" else "User" },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val ageText = if (userProfileState.age != null) "${strings.age}: ${userProfileState.age}" else null
                    val mobileText = if (!userProfileState.mobileNumber.isNullOrBlank()) "${strings.mobileNumber}: ${userProfileState.mobileNumber}" else null
                    val detailsList = listOfNotNull(ageText, mobileText)
                    val detailsText = if (detailsList.isNotEmpty()) detailsList.joinToString(" • ") else strings.privacyNote

                    Text(
                        text = detailsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.editProfile)
                    }
                }
            }

            if (showEditProfileDialog) {
                var editName by remember { mutableStateOf(userProfileState.name) }
                var editAge by remember { mutableStateOf(userProfileState.age?.toString() ?: "") }
                var editMobile by remember { mutableStateOf(userProfileState.mobileNumber ?: "") }

                val isEditNameValid = editName.trim().isNotBlank()
                val isEditMobileValid = editMobile.isBlank() || (editMobile.trim().length == 10 && editMobile.trim().all { it.isDigit() })
                val isEditValid = isEditNameValid && isEditMobileValid

                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showEditProfileDialog = false },
                    title = { Text(strings.editProfile) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text(strings.fullName) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { input ->
                                    if (input.isBlank() || (input.all { it.isDigit() } && input.length <= 3)) {
                                        editAge = input
                                    }
                                },
                                label = { Text(strings.age) },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = editMobile,
                                onValueChange = { input ->
                                    if (input.isBlank() || (input.all { it.isDigit() } && input.length <= 10)) {
                                        editMobile = input
                                    }
                                },
                                label = { Text(strings.mobileNumber) },
                                singleLine = true,
                                isError = editMobile.isNotBlank() && !isEditMobileValid,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isEditValid) {
                                    val updatedProfile = UserProfile(
                                        name = editName.trim(),
                                        age = editAge.toIntOrNull(),
                                        mobileNumber = editMobile.trim().takeIf { it.isNotBlank() },
                                        profileCompleted = true
                                    )
                                    UserProfileStorage.saveUserProfile(context, updatedProfile)
                                    userProfileState = updatedProfile
                                    showEditProfileDialog = false
                                }
                            },
                            enabled = isEditValid
                        ) {
                            Text(strings.saveProfile)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showEditProfileDialog = false }) {
                            Text(if (appLanguage == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                        }
                    }
                )
            }

            // 3. ACCOUNT SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👤 ${if (isHindi) "खाता" else "ACCOUNT"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Google Account",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (signedInAccount != null) {
                        Text(
                            text = signedInAccount.email ?: signedInAccount.displayName ?: "Connected Account",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onSignOutClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isHindi) "साइन आउट करें" else "Sign Out")
                        }
                    } else {
                        Text(
                            text = if (isHindi) "साइन इन नहीं हैं" else "Not signed in",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onSignInClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isHindi) "गूगल से साइन इन करें" else "Sign in with Google")
                        }
                    }
                }
            }

            // 4. QUESTION BANK SYNC SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .clickable { onOpenQuestionBankSyncClick() }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📚 ${if (isHindi) "प्रश्न बैंक सिंक" else "QUESTION BANK SYNC"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "केंद्रीय प्रश्न बैंक सिंक" else "Central Question Bank Sync",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isHindi) "गूगल ड्राइव मास्टर प्रश्न बैंक से नए प्रश्न सिंक करें" else "Sync new questions from Google Drive Master Question Bank",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "›",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // 5. BACKUP & RESTORE SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .clickable { onOpenBackupClick() }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "☁️ ${if (isHindi) "व्यक्तिगत बैकअप और सिंक" else "PERSONAL BACKUP & RESTORE"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isHindi) "ऐप डेटा बैकअप और रिस्टोर" else "Personal App Data Backup & Restore",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isHindi) "टेस्ट हिस्ट्री, बुकमार्क और गलत प्रश्नों को गूगल ड्राइव पर बैकअप करें" else "Backup test history, bookmarks and wrong questions to Google Drive",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "›",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // 6. APPEARANCE SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎨 ${if (isHindi) "थीम और रूप-रंग" else "APPEARANCE"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isHindi) "थीम चुनें" else "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeChange(ThemeMode.SYSTEM) },
                            label = { Text(if (isHindi) "⚙️ सिस्टम" else "⚙️ System") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeChange(ThemeMode.LIGHT) },
                            label = { Text(if (isHindi) "☀️ लाइट" else "☀️ Light") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = themeMode == ThemeMode.DARK,
                            onClick = { onThemeChange(ThemeMode.DARK) },
                            label = { Text(if (isHindi) "🌙 डार्क" else "🌙 Dark") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 7. APP UPDATES SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "🚀 ऐप अपडेट" else "🚀 APP UPDATES",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isHindi) "नए वर्ज़न के लिए जाँच करें" else "Check for a newer version",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isHindi) "वर्तमान वर्ज़न: v$currentVersionCode" else "Current Version: v$currentVersionCode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (!isCheckingUpdates) {
                                isCheckingUpdates = true
                                coroutineScope.launch {
                                    val result = com.example.myexampreparation.update.AppUpdater.checkForUpdate(
                                        context = context,
                                        versionJsonUrl = com.example.myexampreparation.update.AppUpdater.DEFAULT_VERSION_JSON_URL
                                    )
                                    isCheckingUpdates = false

                                    when (result) {
                                        is com.example.myexampreparation.update.UpdateCheckResult.UpdateAvailable -> {
                                            onUpdateAvailable(result)
                                        }
                                        is com.example.myexampreparation.update.UpdateCheckResult.UpToDate -> {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isHindi) "आप नवीनतम वर्ज़न का उपयोग कर रहे हैं।" else "You're using the latest version.",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        is com.example.myexampreparation.update.UpdateCheckResult.Error -> {
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isHindi) "अपडेट की जाँच नहीं हो सकी।" else "Couldn't check for updates.",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isCheckingUpdates,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isCheckingUpdates) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(if (isHindi) "अपडेट की जाँच हो रही है..." else "Checking for updates...")
                            }
                        } else {
                            Text(if (isHindi) "अपडेट की जाँच करें" else "Check for Updates")
                        }
                    }
                }
            }

            // 8. ABOUT SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "ℹ️ ऐप के बारे में" else "ℹ️ ABOUT",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isHindi) "वर्ज़न $currentVersionName" else "Version $currentVersionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "व्यक्तिगत ऑफ़लाइन परीक्षा तैयारी ऐप" else "Personal offline exam preparation app",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
