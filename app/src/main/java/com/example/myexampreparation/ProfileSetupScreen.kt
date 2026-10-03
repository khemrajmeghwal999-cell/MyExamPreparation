package com.example.myexampreparation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myexampreparation.data.AppLanguage
import com.example.myexampreparation.data.AppStrings
import com.example.myexampreparation.data.UserProfile
import com.example.myexampreparation.data.UserProfileStorage

@Composable
fun ProfileSetupScreen(
    appLanguage: AppLanguage,
    onProfileCompleted: (UserProfile) -> Unit
) {
    val context = LocalContext.current
    val strings = AppStrings.get(appLanguage)

    var nameInput by remember { mutableStateOf("") }
    var ageInput by remember { mutableStateOf("") }
    var mobileInput by remember { mutableStateOf("") }

    val isNameValid = nameInput.trim().isNotBlank()
    val isMobileValid = mobileInput.isBlank() || (mobileInput.trim().length == 10 && mobileInput.trim().all { it.isDigit() })
    val isFormValid = isNameValid && isMobileValid

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // BRAND LOGO BADGE
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }

            // BRAND TITLE & TAGLINE
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "My Exam ",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Preparation",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.welcomeSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CARD CONTAINER FOR FORM
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "✨ ${strings.personalizeSubtitle}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 1. FULL NAME
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(strings.fullName) },
                        placeholder = { Text(strings.fullNameHint) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. AGE (OPTIONAL)
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { input ->
                            if (input.isBlank() || (input.all { it.isDigit() } && input.length <= 3)) {
                                ageInput = input
                            }
                        },
                        label = { Text(strings.age) },
                        placeholder = { Text(strings.ageHint) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. MOBILE NUMBER (OPTIONAL)
                    OutlinedTextField(
                        value = mobileInput,
                        onValueChange = { input ->
                            if (input.isBlank() || (input.all { it.isDigit() } && input.length <= 10)) {
                                mobileInput = input
                            }
                        },
                        label = { Text(strings.mobileNumber) },
                        placeholder = { Text(strings.mobileNumberHint) },
                        singleLine = true,
                        isError = mobileInput.isNotBlank() && !isMobileValid,
                        supportingText = {
                            if (mobileInput.isNotBlank() && !isMobileValid) {
                                Text(
                                    text = strings.invalidMobile,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // PRIVACY NOTE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.privacyNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // CONTINUE BUTTON
                    Button(
                        onClick = {
                            if (isFormValid) {
                                val parsedAge = ageInput.toIntOrNull()
                                val profile = UserProfile(
                                    name = nameInput.trim(),
                                    age = parsedAge,
                                    mobileNumber = mobileInput.trim().takeIf { it.isNotBlank() },
                                    profileCompleted = true
                                )
                                UserProfileStorage.saveUserProfile(context, profile)
                                onProfileCompleted(profile)
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = strings.continueBtn,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
