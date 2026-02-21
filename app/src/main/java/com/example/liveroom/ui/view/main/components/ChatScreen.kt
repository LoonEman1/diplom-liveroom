package com.example.liveroom.ui.view.main.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.liveroom.ui.theme.DarkPrimary
import com.example.liveroom.ui.theme.LightSurface
import com.example.liveroom.ui.theme.SurfaceColor
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
    onBackToServer: () -> Unit = {}
) {
    val isLoading by serverViewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
                items(messages.reversed()) { message ->
                    MessageBubble(
                        message = message,
                        isOwn = message.author?.userId == currentUserId
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = {
                    Text(
                        stringResource(R.string.message),
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
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    cursorColor = MaterialTheme.colorScheme.primary
                ),
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (messageText.trim().isNotBlank()) {
                        serverViewModel.sendMessage(conversationId, messageText.trim())
                        messageText = ""
                        keyboardController?.show()
                    }
                },
                enabled = messageText.trim().isNotBlank(),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = stringResource(R.string.send),
                    tint = if (messageText.trim().isNotBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
@Composable
fun MessageBubble(
    message: Message,
    isOwn: Boolean
) {
    val isDeleted = message.deletedAt != null || message.content.isNullOrBlank()
    val time = formatTime(message.createdAt)

    val author = message.author
    val showFullName = !author?.firstName.isNullOrBlank() ||
            !author?.lastName.isNullOrBlank()
    val fullName = "${author?.firstName ?: ""} ${author?.lastName ?: ""}".trim()
    val username = "@${author?.username ?: "unknown"}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isOwn && isSystemInDarkTheme() -> DarkPrimary
                    isOwn && !isSystemInDarkTheme() -> Color(0xFFE3F2FD)
                    !isOwn && isSystemInDarkTheme() -> SurfaceColor
                    else -> LightSurface
                }
            ),
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = if (isOwn) 20.dp else 4.dp,
                bottomEnd = if (isOwn) 4.dp else 20.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = if (isDeleted) 8.dp else 12.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isOwn) {
                        Column {
                            if (showFullName) {
                                Text(
                                    fullName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    username,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                Text(
                                    username,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.me),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isDeleted && !message.content.isNullOrBlank()) {
                    Text(
                        text = message.content!!,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(stringResource(R.string.message_deleted), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
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