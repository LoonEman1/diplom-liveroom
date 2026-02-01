package com.example.liveroom.ui.view.main.components.common

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.di.AppConfig

@Composable
fun ServerItem(
    server : Server,
    isSelected: Boolean = false,
    onClickServer: () -> Unit,
    onEditServer: () -> Unit,
    onDeleteServer: () -> Unit,
    onInviteToServer: () -> Unit,
    onLeave: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var popupOffset by remember { mutableStateOf(Offset.Zero) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        popupOffset = offset
                        showContextMenu = true
                    },
                    onTap = {
                        onClickServer()
                    }
                )
            }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = Color.Gray,
                        shape = RoundedCornerShape(24.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (server.avatarUrl != null) {
            AsyncImage(
                model = "${AppConfig.IMAGE_BASE_URL}${server.avatarUrl}",
                contentDescription = server.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                onError = { state ->
                    Log.e("AsyncImage", "Error loading image: ${state.result.throwable}")
                }
            )
        } else {
            Text(
                text = server.name.firstOrNull()?.uppercase() ?: "S",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if(showContextMenu) {
            ServerContextMenu(
                server = server,
                onDismiss = { showContextMenu = false },
                offset = popupOffset,
                onEdit = {
                    onEditServer()
                },
                onDelete =  {
                    onDeleteServer()
                },
                onInvite = {
                    onInviteToServer()
                },
                onLeave = {
                    showContextMenu = false
                    showLeaveDialog = true
                }
            )
        }

        if (showLeaveDialog) {
            ConfirmationDialog(
                showDialog = showLeaveDialog,
                title = stringResource(R.string.leave_from_server),
                message = "${stringResource(R.string.server_name)}: ${server.name}",
                onConfirm = {
                    showLeaveDialog = false
                    onLeave()
                },
                onDismiss = {
                    showLeaveDialog = false
                }
            )
        }
    }
}

@Composable
fun ServerContextMenu(
    server : Server,
    onDismiss: () -> Unit,
    offset: Offset,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onInvite: () -> Unit,
    onLeave: () -> Unit
) {

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(offset.x.toInt(),offset.y.toInt()),
        onDismissRequest = { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp)
        ) {
            if(server.myRole.name == "OWNER") {
                Text(
                    text = stringResource(R.string.edit),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onEdit()
                    }
                )

                Text(
                    text = stringResource(R.string.invite_to_server),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onInvite()
                    }
                )

                Text(
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onDelete()
                    }
                )
            }
            else if(server.myRole.name == "ADMIN") {
                Text(
                    text = stringResource(R.string.invite_to_server),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onInvite()
                    }
                )
                Text(
                    text = stringResource(R.string.leave),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onLeave()
                    }
                )
            }
            else {
                Text(
                    text = stringResource(R.string.leave),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onLeave()
                    }
                )
            }
        }
    }
}