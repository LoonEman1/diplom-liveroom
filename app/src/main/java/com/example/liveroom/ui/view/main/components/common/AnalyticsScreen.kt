package com.example.liveroom.ui.view.main.components.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.liveroom.data.remote.dto.Conversation
import com.example.liveroom.data.remote.dto.SessionSummaryDto
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.R

@Composable
fun AnalyticsScreen(
    conversations: List<Conversation>,
    serverViewModel: ServerViewModel,
    serverId: Int,
    onBack: () -> Unit
) {
    val publicConversations = remember(conversations) {
        conversations.filter { !it.isPrivate }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.analytics_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        BackHandler {
            onBack()
        }

        HorizontalDivider()

        if (publicConversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                stringResource(R.string.no_available_chats)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(publicConversations) { convo ->
                    AnalyticsConversationItem(
                        convo = convo,
                        serverId = serverId,
                        serverViewModel = serverViewModel
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
@Composable
fun AnalyticsConversationItem(
    convo: Conversation,
    serverId: Int,
    serverViewModel: ServerViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val sessions by serverViewModel.analyticsSessions.collectAsState()
    val isAnalyticsLoading by serverViewModel.isAnalyticsLoading.collectAsState()


    fun triggerAnalyticsLoad(start: String, end: String) {
        if (start.isNotEmpty() && end.isNotEmpty()) {
            val formattedFrom = "${start}T00:00:00Z"
            val formattedTo = "${end}T23:59:59Z"

            serverViewModel.loadPeriodAnalytics(
                serverId = serverId,
                conversationId = convo.id.toLong(),
                from = formattedFrom,
                to = formattedTo
            )
        }
    }

    fun showDatePicker(isStartDate: Boolean) {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val date = "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

                if (isStartDate) {
                    startDate = date
                    triggerAnalyticsLoad(date, endDate)
                } else {
                    endDate = date
                    triggerAnalyticsLoad(startDate, date)
                }
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = convo.title,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showDatePicker(isStartDate = true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            startDate.ifEmpty { stringResource(R.string.from) },
                            color = MaterialTheme.colorScheme.onSurface,
                            )
                    }
                    OutlinedButton(
                        onClick = { showDatePicker(isStartDate = false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(endDate.ifEmpty { stringResource(R.string.to) },
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAnalyticsLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (startDate.isEmpty() || endDate.isEmpty()) {
                    Text(stringResource(R.string.choose_data_range),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,)
                } else {
                    sessions.forEach { session ->
                        SessionAnalyticsCard(session)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SessionAnalyticsCard(session: SessionSummaryDto) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Сессия: ${session.sessionId}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Участников: ${session.uniqueAttendees}", style = MaterialTheme.typography.bodySmall)
                Text("Длительность: ${session.durationSeconds} сек", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "Статус: ${session.status}",
                style = MaterialTheme.typography.bodySmall,
                color = if (session.status == "COMPLETED") Color(0xFF4CAF50) else Color.Gray
            )
        }
    }
}
