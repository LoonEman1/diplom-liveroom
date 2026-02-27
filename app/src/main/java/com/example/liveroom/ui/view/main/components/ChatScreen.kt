package com.example.liveroom.ui.view.main.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.liveroom.data.remote.dto.Message
import com.example.liveroom.ui.components.CustomTextField
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Role
import com.example.liveroom.ui.theme.ButtonColor
import com.example.liveroom.ui.theme.DarkPrimary
import com.example.liveroom.ui.theme.ErrorRed
import com.example.liveroom.ui.theme.LightSurface
import com.example.liveroom.ui.theme.SurfaceColor
import com.example.liveroom.ui.view.main.components.common.ConfirmationDialog
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ChatScreen(
    conversationId: Long,
    messages: List<Message>,
    serverViewModel: ServerViewModel,
    currentUserId: Int,
    onBackToServer: () -> Unit = {},
) {
    val isLoading by serverViewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var messageToDelete by remember { mutableStateOf<Message?>(null) }

    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var selectedMessageForMenu by remember { mutableStateOf<Message?>(null) }

    val myRole = serverViewModel.selectedServer.collectAsState()
    val canManageMessages = (myRole.value?.myRole?.name == "ADMIN" || myRole.value?.myRole?.name == "OWNER")

    ConfirmationDialog(
        showDialog = messageToDelete != null,
        title = stringResource(R.string.delete_message_title),
        message = stringResource(R.string.delete_message_confirm),
        onConfirm = {
            messageToDelete?.let { serverViewModel.deleteMessage(conversationId, it.id) }
            messageToDelete = null
        },
        onDismiss = { messageToDelete = null }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.lastIndex)
        }
    }

    BackHandler {
        onBackToServer()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading && messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { keyboardController?.hide() })
                        },
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(messages) { index, message ->
                        val showAuthor = if (index == 0) {
                            true
                        } else {
                            val previousMessage = messages[index - 1]
                            previousMessage.author?.userId != message.author?.userId
                        }

                        MessageBubble(
                            message = message,
                            isOwn = message.author?.userId == currentUserId,
                            showAuthor = showAuthor,
                            onLongPress = { offset ->
                                if (message.deletedAt == null) {
                                    selectedMessageForMenu = message
                                    contextMenuOffset = offset
                                    showContextMenu = true
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = {
                        Text(
                            if (editingMessage != null) stringResource(R.string.edit) else stringResource(R.string.message),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    maxLines = 4,
                    singleLine = false,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (editingMessage != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = if (editingMessage != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (editingMessage != null) {
                        IconButton(
                            onClick = {
                                editingMessage = null
                                messageText = ""
                            },
                            modifier = Modifier.size(32.dp).padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (messageText.trim().isNotBlank()) {
                                if (editingMessage != null) {
                                    serverViewModel.editMessage(conversationId, editingMessage!!.id, messageText.trim())
                                    editingMessage = null
                                } else {
                                    serverViewModel.sendMessage(conversationId, messageText.trim())
                                }
                                messageText = ""
                                keyboardController?.show()
                            }
                        },
                        enabled = messageText.trim().isNotBlank(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (editingMessage != null) Icons.Default.Check else Icons.Default.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = if (messageText.trim().isNotBlank())
                                (if (editingMessage != null) ButtonColor else MaterialTheme.colorScheme.onSurfaceVariant)
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        if (showContextMenu && selectedMessageForMenu != null) {
            MessageContextMenu(
                message = selectedMessageForMenu!!,
                isOwn = selectedMessageForMenu!!.author?.userId == currentUserId,
                canDelete = (selectedMessageForMenu!!.author?.userId == currentUserId) || canManageMessages,
                onDismiss = { showContextMenu = false },
                offset = contextMenuOffset,
                onCopy = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedMessageForMenu?.content ?: ""))
                    showContextMenu = false
                },
                onEdit = {
                    editingMessage = selectedMessageForMenu
                    messageText = selectedMessageForMenu?.content ?: ""
                    showContextMenu = false
                    focusRequester.requestFocus()
                },
                onDelete = {
                    messageToDelete = selectedMessageForMenu
                    showContextMenu = false
                }
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean,
    showAuthor: Boolean,
    onLongPress: (androidx.compose.ui.geometry.Offset) -> Unit = {}
) {
    val isDeleted = message.deletedAt != null || message.content.isNullOrBlank()
    val time = formatTime(message.createdAt)

    val author = message.author
    Log.d("ChatDebug", "Message ID=${message.id}, author=$author, firstName=${author?.firstName}, userId=${author?.userId}, isOwn=$isOwn")
    val fullName = "${author?.firstName ?: ""} ${author?.lastName ?: ""}".trim()
    val username = "@${author?.username ?: "unknown"}"
    Log.d("ChatDebug", "fullName='$fullName', username='$username'")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .wrapContentWidth(if (isOwn) Alignment.End else Alignment.Start)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { offset -> onLongPress(offset) })
                },
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isOwn && isSystemInDarkTheme() -> DarkPrimary
                    isOwn && !isSystemInDarkTheme() -> Color(0xFFE3F2FD)
                    !isOwn && isSystemInDarkTheme() -> SurfaceColor
                    else -> LightSurface
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwn || !showAuthor) 16.dp else 4.dp,
                bottomEnd = if (!isOwn || !showAuthor) 16.dp else 4.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (showAuthor && !isDeleted) {
                    val authorLabel = if (isOwn) {
                        stringResource(R.string.me)
                    } else {
                        if (fullName.isNotBlank()) fullName else username
                    }
                    Text(
                        text = authorLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp),
                        maxLines = 1
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (!isDeleted && !message.content.isNullOrBlank()) {
                        Text(
                            text = message.content!!,
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                        Row(
                            modifier = Modifier.align(Alignment.BottomEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (message.editedAt != null && message.editedAt != message.createdAt) {
                                Text(
                                    text = stringResource(R.string.edited_message).lowercase(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = time,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        Column(modifier = Modifier.padding(bottom = 14.dp)) {
                            Text(
                                text = stringResource(R.string.message_deleted),
                                color = ErrorRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        message.deletedAt?.let { deletedIso ->
                            Text(
                                text = formatTime(deletedIso),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageContextMenu(
    message: Message,
    isOwn: Boolean,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    offset: androidx.compose.ui.geometry.Offset,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.ui.window.Popup(
        alignment = androidx.compose.ui.Alignment.TopStart,
        offset = androidx.compose.ui.unit.IntOffset(offset.x.toInt(), offset.y.toInt()),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 8.dp)
                .width(140.dp)
        ) {
            val itemModifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .padding(horizontal = 16.dp, vertical = 12.dp)

            Text(
                text = stringResource(R.string.copy),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = itemModifier.clickable { onCopy(); onDismiss() }
            )

            if (isOwn) {
                Text(
                    text = stringResource(R.string.edit),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = itemModifier.clickable { onEdit(); onDismiss() }
                )
            }

            if (canDelete) {
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error,
                    modifier = itemModifier.clickable { onDelete(); onDismiss() }
                )
            }
        }
    }
}

fun formatTime(isoString: String): String {
    return try {
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        utcFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = utcFormat.parse(isoString)

        val localFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        localFormat.timeZone = TimeZone.getDefault()
        localFormat.format(date!!)
    } catch (e: Exception) {
        isoString.takeLast(5)
    }
}