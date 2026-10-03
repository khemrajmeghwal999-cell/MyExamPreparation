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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.AppLanguage
import com.example.myexampreparation.data.AppLanguageStorage
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    signedInAccount: GoogleSignInAccount?,
    isUploading: Boolean,
    isRestoring: Boolean,
    lastBackupTime: Long,
    statusMessage: String?,
    isSuccessStatus: Boolean,
    onBack: () -> Unit,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBackupNowClick: () -> Unit,
    onRestoreNowClick: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    val lastBackupText = remember(lastBackupTime, isHindi) {
        if (lastBackupTime > 0L) {
            DateFormat.format("dd MMM yyyy, hh:mm a", Date(lastBackupTime)).toString()
        } else {
            if (isHindi) "कभी नहीं" else "Never"
        }
    }

    val isBusy = isUploading || isRestoring

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text(if (isHindi) "ड्राइव बैकअप और सिंक" else "Drive Backup & Restore", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
            // Account Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = if (isHindi) "गूगल खाता स्थिति" else "Google Account Status",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (signedInAccount != null) {
                        Text(
                            text = if (isHindi) "✅ साइन इन हैं:" else "✅ Signed in as:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = signedInAccount.email ?: signedInAccount.displayName ?: "Connected Account",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onSignOutClick,
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isHindi) "साइन आउट करें" else "Sign Out")
                        }
                    } else {
                        Text(
                            text = if (isHindi) "❌ साइन इन नहीं हैं" else "❌ Not signed in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
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

            // Backup & Restore Section Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = if (isHindi) "बैकअप और रिस्टोर (गूगल ड्राइव)" else "Backup & Restore (Google Drive)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "अंतिम बैकअप: $lastBackupText" else "Last Backup: $lastBackupText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "आपके प्रश्न, बुकमार्क, टेस्ट हिस्ट्री तथा गलत प्रश्नों को आपके गूगल ड्राइव पर सुरक्षित रूप से बैकअप एवं रिस्टोर किया जाता है।" else "Your questions, bookmarks, test history, and wrong questions are securely backed up and restored to your Google Drive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onBackupNowClick,
                        enabled = signedInAccount != null && !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isUploading) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(if (isHindi) "बैकअप अपलोड हो रहा है..." else "Uploading Backup...")
                            }
                        } else {
                            Text(if (isHindi) "☁️ बैकअप लें" else "☁️ Back Up Now")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRestoreNowClick,
                        enabled = signedInAccount != null && !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isRestoring) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(if (isHindi) "डेटा रिस्टोर हो रहा है..." else "Restoring Data...")
                            }
                        } else {
                            Text(if (isHindi) "📥 गूगल ड्राइव से रिस्टोर करें" else "📥 Restore from Google Drive")
                        }
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSuccessStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
