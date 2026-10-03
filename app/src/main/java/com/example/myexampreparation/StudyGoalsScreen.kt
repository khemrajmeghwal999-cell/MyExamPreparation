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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.myexampreparation.data.StudyGoalStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyGoalsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val appLanguage = AppLanguageStorage.getSelectedLanguage(context)
    val isHindi = appLanguage == AppLanguage.HINDI

    var goalData by remember {
        mutableStateOf(StudyGoalStorage.getGoalData(context))
    }

    val goalOptions = listOf(10, 20, 30, 50)
    val progressRatio = (goalData.todayQuestionsAttempted.toFloat() / goalData.dailyGoal.toFloat()).coerceIn(0f, 1f)
    val remaining = (goalData.dailyGoal - goalData.todayQuestionsAttempted).coerceAtLeast(0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text(if (isHindi) "लक्ष्य एवं स्ट्रीक" else "Goals & Streak", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
            // 1. STREAK CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "🔥 वर्तमान स्ट्रीक" else "🔥 Current Streak",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "${goalData.currentStreak} दिन" else "${goalData.currentStreak} Days",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isHindi) "🏆 उच्चतम स्ट्रीक: ${goalData.longestStreak} दिन" else "🏆 Longest Streak: ${goalData.longestStreak} Days",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 2. DAILY GOAL PROGRESS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "आज की प्रगति" else "Today's Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isHindi) "${goalData.todayQuestionsAttempted} / ${goalData.dailyGoal} प्रश्न" else "${goalData.todayQuestionsAttempted} / ${goalData.dailyGoal} Questions",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (remaining > 0) {
                        Text(
                            text = if (isHindi) "🎯 आज का लक्ष्य पूरा करने के लिए $remaining प्रश्न और हल करें!" else "🎯 $remaining more questions to complete today's goal!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = if (isHindi) "🎉 शानदार! आज का अध्ययन लक्ष्य पूरा हो गया है!" else "🎉 Awesome! Today's study goal is completed!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 3. SET DAILY GOAL SECTION
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
                        text = if (isHindi) "दैनिक प्रश्न लक्ष्य" else "Daily Question Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "प्रतिदिन हल करने के लिए प्रश्नों की संख्या चुनें:" else "Select the number of questions to solve daily:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        goalOptions.forEach { count ->
                            FilterChip(
                                selected = goalData.dailyGoal == count,
                                onClick = {
                                    StudyGoalStorage.setDailyGoal(context, count)
                                    goalData = StudyGoalStorage.getGoalData(context)
                                },
                                label = { Text(if (isHindi) "$count प्रश्न" else "$count Qs") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. MOTIVATION CARD
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
                        text = if (isHindi) "💡 स्ट्रीक के नियम" else "💡 Streak Rules",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "• प्रतिदिन अपना चुना हुआ डेली गोल पूरा करें।\n• लगातार हर दिन गोल पूरा करने पर आपकी 🔥 Streak बढ़ेगी।\n• किसी दिन गोल पूरा न होने पर Streak रीसेट हो जाएगी।" else "• Complete your selected daily goal every day.\n• Completing your goal daily builds your 🔥 Streak.\n• Missing a daily goal resets your Streak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
