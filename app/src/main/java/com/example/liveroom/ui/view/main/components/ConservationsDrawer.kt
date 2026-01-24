package com.example.liveroom.ui.view.main.components

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.model.ServerFormData
import com.example.liveroom.data.remote.dto.Role
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.di.AppConfig
import com.example.liveroom.ui.components.CustomTextField
import com.example.liveroom.ui.components.PrimaryButton
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun LeftNavigation(
    selectedTab : String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    serverViewModel: ServerViewModel,
    userViewModel: UserViewModel
) {

    var showServerDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf(ServerDialogMode.CREATE) }

    val serverList by serverViewModel.servers.collectAsState()
    val selectedServerId by serverViewModel.selectedServerId.collectAsState()
    var selectedServer by remember { mutableStateOf<Server?>(null) }
    var isErrorInDialog by remember { mutableStateOf(false) }

    LaunchedEffect(serverList) {

    }
    LazyColumn(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Image(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create Server",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable {
                        dialogMode = ServerDialogMode.CREATE
                        showServerDialog = true
                        onTabSelected("create_server")
                        selectedServer = null
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }

        items(
            serverList.sortedByDescending { it.id },
            key = { it.id }
        ) { server ->
            ServerItem(
                server = server,
                isSelected = server.id == selectedServerId,
                onClickServer = {
                    serverViewModel.setSelectedServerId(server.id)
                    Log.d("SelectedServer", serverViewModel.selectedServerId.value.toString())
                },
                serverViewModel = serverViewModel,
                onEditServer = {
                    showServerDialog = true
                    dialogMode = ServerDialogMode.EDIT
                    selectedServer = server
                },
                onDeleteServer = {
                    showServerDialog = true
                    dialogMode = ServerDialogMode.DELETE
                    selectedServer = server
                },
                onInviteToServer = {
                    showServerDialog = true
                    dialogMode = ServerDialogMode.INVITE
                    selectedServer = server
                }
            )
        }
    }


    if (showServerDialog) {
        val toastText = stringResource(R.string.cannot_be_empty)
        val toastEditText = stringResource(R.string.editToastText)
        val toastCreatedServerText = stringResource(R.string.server_created)
        val toastError = stringResource(R.string.cannot_get_user_data)
        val toastServerDeleted = stringResource(R.string.server_deleted)
        val context = LocalContext.current

        if (dialogMode == ServerDialogMode.INVITE) {
            InviteDialog(
                server = selectedServer,
                onDismiss = {
                    showServerDialog = false
                },
                title = stringResource(R.string.invite_to_server),
                onInvite = {

                }
            )
        } else {
            ServerDialog(
                onDismiss = { showServerDialog = false },
                selectedServer = selectedServer,
                dialogMode = dialogMode,
                onConfirmClick = { formData ->
                    when (dialogMode) {

                        ServerDialogMode.CREATE -> {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value

                            if (userId != null && token != null) {
                                if (!formData.name.isNullOrBlank()) {
                                    serverViewModel.createServer(
                                        name = formData.name,
                                        imageUri = formData.avatarUrl?.toUri(),
                                        onSuccess = {
                                            isErrorInDialog = false
                                            Toast.makeText(
                                                context,
                                                "$toastCreatedServerText ${formData.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            showServerDialog = false
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, toastError, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    )
                                } else {
                                    isErrorInDialog = true
                                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        ServerDialogMode.EDIT -> {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value
                            val serverId = selectedServer?.id
                            when {
                                userId == null || token == null -> {
                                    Log.w("ServerDialog", "User ID or token is null")
                                    Toast.makeText(context, toastError, Toast.LENGTH_SHORT).show()
                                }

                                serverId == null -> {
                                    Log.w("ServerDialog", "Server ID is null")
                                }

                                else -> {
                                    Log.d(
                                        "ServerDialog",
                                        "Starting editServer: name=${formData.name}, avatarUrl=${formData.avatarUrl}"
                                    )

                                    serverViewModel.editServer(
                                        serverId = serverId,
                                        name = formData.name,
                                        imageUri = formData.avatarUrl,
                                        onSuccess = {
                                            Log.i("ServerDialog", "Edit success, closing dialog")
                                            showServerDialog = false
                                            Toast.makeText(
                                                context,
                                                "Server updated: ${formData.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            Log.e("ServerDialog", "Edit error: $error")
                                            Toast.makeText(
                                                context,
                                                "Error: $error",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }

                        ServerDialogMode.DELETE -> {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value
                            val currentServer = selectedServer
                            if (userId != null && token != null && currentServer != null) {
                                if (!formData.name.isNullOrBlank() && formData.name == currentServer.name) {
                                    serverViewModel.deleteServer(
                                        currentServer,
                                        onSuccess = {
                                            Toast.makeText(
                                                context,
                                                toastServerDeleted,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            isErrorInDialog = false
                                            showServerDialog = false
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, toastError, Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                    isErrorInDialog = true
                                }
                            }
                        }
                        ServerDialogMode.INVITE -> Unit
                    }
                },
                isError = isErrorInDialog
            )
        }

        Log.d("selectedServer", "avatarUrl : ${selectedServer?.avatarUrl.toString()}")
        Log.d("selectedServer", "server id : ${selectedServer?.id.toString()}")
    }
}

@Composable
fun ServerItem(
    server : Server,
    isSelected: Boolean = false,
    onClickServer: () -> Unit,
    serverViewModel: ServerViewModel,
    onEditServer: () -> Unit,
    onDeleteServer: () -> Unit,
    onInviteToServer: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var popupOffset by remember { mutableStateOf(Offset.Zero) }

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
                serverViewModel = serverViewModel,
                onEdit = {
                    onEditServer()
                },
                onDelete =  {
                    onDeleteServer()
                },
                onInvite = {
                    onInviteToServer()
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
    serverViewModel: ServerViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onInvite: () -> Unit
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
            if(server.myRole.name == "ADMIN") {
                Text(
                    text = stringResource(R.string.invite_to_server),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        onInvite()
                    }
                )
            }
        }
    }


}


/*
@Preview
@Composable
fun PreviewServerDialog() {
    LeftNavigation(
        "",
        {

        },
        serverViewModel = serverViewModel,
        userViewModel = userViewModel

    )
}

 */


