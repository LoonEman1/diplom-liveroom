package com.example.liveroom.ui.view.main.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.liveroom.data.model.ActiveCall
import com.example.liveroom.data.webrtc.CallStateManager
import com.example.liveroom.ui.theme.linkTextColor
import com.example.liveroom.ui.viewmodel.ServerViewModel
@Composable
fun CallHeader(
    activeCall: ActiveCall?,
    conversationId: Long,
    serverViewModel: ServerViewModel,
    modifier: Modifier = Modifier,
    userId : Int
) {
    val isParticipant = activeCall?.participants?.any { it.toInt() == userId } == true

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = if (activeCall != null) Color.Green else linkTextColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        activeCall == null -> "🎤 Создать звонок"
                        isParticipant -> "📞 Ты в звонке"
                        else -> "📞 Активный звонок"
                    },
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (activeCall != null) {
                    Text(
                        text = "${activeCall.participants.size} участников • ${activeCall.kind}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                enabled = activeCall == null || !isParticipant,
                onClick = {
                    if (activeCall != null) {
                        if (!isParticipant) {
                            serverViewModel.joinCall(activeCall.callId)
                        }
                    } else {
                        val serverId = serverViewModel.selectedServer.value?.id?.toLong()
                            ?: return@Button
                        serverViewModel.startCall(serverId, conversationId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = when {
                        activeCall == null -> "Создать"
                        isParticipant -> "В звонке"
                        else -> "Присоединиться"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
