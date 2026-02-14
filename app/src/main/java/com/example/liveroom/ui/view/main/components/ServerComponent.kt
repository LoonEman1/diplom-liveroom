package com.example.liveroom.ui.view.main.components

import android.graphics.drawable.Icon
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.remote.dto.ServerMember
import com.example.liveroom.ui.viewmodel.ServerViewModel
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.liveroom.data.remote.dto.Conversation
import com.example.liveroom.ui.components.CustomTextField
import com.example.liveroom.ui.components.PrimaryButton
import com.example.liveroom.ui.theme.ButtonColor
import com.example.liveroom.ui.theme.ErrorRed
import com.example.liveroom.ui.theme.linkTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerComponent(
    server: Server?,
    serverViewModel: ServerViewModel
) {
    var showCreateChatDialog by remember { mutableStateOf(false) }

    LaunchedEffect(server) {
        if (server != null) serverViewModel.loadServerData(server.id)
    }

    val conversations by serverViewModel.conversations.collectAsState()
    val members by serverViewModel.members.collectAsState()
    var showMembers by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { showMembers = !showMembers },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    "Members",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        stringResource(R.string.members),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("${members?.size ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (showMembers) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(visible = showMembers) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondary),
                colors = CardDefaults.cardColors(
                    contentColor = MaterialTheme.colorScheme.secondary.copy(),
                    containerColor = MaterialTheme.colorScheme.secondary.copy()
                ),
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(members) { member ->
                        MemberRow(member)
                    }
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.primary,
            thickness = 1.dp,
            modifier = Modifier
                .fillMaxWidth().padding(vertical = if (showMembers) 16.dp else 0.dp)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Text Chats",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        if (conversations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(conversations) { convo ->
                    ConversationItem(convo)
                }
            }

        }

        PrimaryButton(
            text = stringResource(R.string.create_text_channel),
            onClick = { showCreateChatDialog = true },
            icon = {
                Icon(Icons.Default.Add, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 48.dp),
            containerColor = MaterialTheme.colorScheme.primary
        )
    }

    if (showCreateChatDialog) {
        CreateChatDialog(
            serverId = server?.id ?: -1,
            serverViewModel = serverViewModel,
            onDismiss = { showCreateChatDialog = false }
        )
    }
}


@Composable
fun ConversationItem(convo: Conversation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "text channel",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        convo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (convo.isPrivate) {
                        Icon(
                            Icons.Default.Lock,
                            "private channel",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (convo.unreadCount > 0) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text("${convo.unreadCount}", color = Color.White)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    convo.createdAt.take(10),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatDialog(
    serverId: Int,
    serverViewModel: ServerViewModel,
    onDismiss: () -> Unit
) {
    var chatName by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.create_text_channel),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            CustomTextField(
                value = chatName,
                onValueChange = {
                    chatName = it
                    isError = it.isBlank()
                },
                label = stringResource(R.string.chat_name),
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                //supportingText = if (isError) stringResource(R.string.cannot_be_empty) else null
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPrivate = !isPrivate
                }
            ) {
                Checkbox(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    stringResource(R.string.private_chat),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    containerColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f).height(48.dp)
                )

                PrimaryButton(
                    text = stringResource(R.string.create),
                    onClick = {
                        if (chatName.isNotBlank()) {
                            serverViewModel.createConversation(
                                serverId = serverId,
                                title = chatName,
                                isPrivate = isPrivate
                            )
                            onDismiss()
                        } else {
                            isError = true
                        }
                    },
                    enabled = chatName.isNotBlank(),
                    modifier = Modifier.weight(1f).height(48.dp)
                )
            }
        }
    }
}

@Composable
fun MemberRow(member: ServerMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { Log.d("Server", "Клик по ${member.username}") }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            "member",
            tint = MaterialTheme.colorScheme.onSurface
        )

        /*Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )*/

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                member.username,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                member.role.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Badge(
            containerColor = when (member.role.name) {
                "OWNER" -> ErrorRed
                "ADMIN" -> linkTextColor
                else -> MaterialTheme.colorScheme.onSurface
            }
        ) {
            Text(
                member.role.name,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

