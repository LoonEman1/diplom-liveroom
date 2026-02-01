package com.example.liveroom.ui.view.main.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.model.ServerEvent
import com.example.liveroom.data.model.getErrorMessage
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.di.AppConfig
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
    val showConfirmationDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf(DialogMode.CREATE) }

    val serverList by serverViewModel.servers.collectAsState()
    val selectedServerId by serverViewModel.selectedServerId.collectAsState()
    var selectedServer by remember { mutableStateOf<Server?>(null) }
    var isErrorInDialog by remember { mutableStateOf(false) }


    val serverCreated = stringResource(R.string.server_created)
    val serverUpdated = stringResource(R.string.server_updated)
    val serverDeleted = stringResource(R.string.server_deleted)
    val tokenGenerated = stringResource(R.string.token_created)
    val userJoined = stringResource(R.string.user_joined)
    val userInvited = stringResource(R.string.user_invited)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        serverViewModel.serverEvents.collect { event ->
            when (event) {
                is ServerEvent.ServerCreated -> {
                    Toast.makeText(
                        context,
                        "$serverCreated: ${event.server.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showServerDialog = false
                    isErrorInDialog = false
                }
                is ServerEvent.ServerEdited -> {
                    Toast.makeText(
                        context,
                        "$serverUpdated ${event.serverName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showServerDialog = false
                }
                is ServerEvent.ServerDeleted -> {
                    Toast.makeText(context, "$serverDeleted ${event.serverName}", Toast.LENGTH_SHORT).show()
                    showServerDialog = false
                }
                is ServerEvent.TokenGenerated -> {
                    Toast.makeText(context, tokenGenerated, Toast.LENGTH_SHORT).show()
                }
                is ServerEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    showServerDialog = false
                }
                is ServerEvent.ValidationError -> {
                    Toast.makeText(context, getErrorMessage(context, event.error), Toast.LENGTH_SHORT).show()
                    isErrorInDialog = true
                }
                is ServerEvent.UserInvited -> {
                    Toast.makeText(context, "$userInvited ${event.username}", Toast.LENGTH_SHORT).show()
                }
                is ServerEvent.UserJoined -> {
                    Toast.makeText(context, userJoined, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


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
                        dialogMode = DialogMode.CREATE
                        showServerDialog = true
                        onTabSelected("create_server")
                        selectedServer = null
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }

        item {
            Image(
                imageVector = Icons.Filled.Search,
                contentDescription = "Settings",
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable {
                        dialogMode = DialogMode.SEARCH_SERVER
                        showServerDialog = true
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
                onEditServer = {
                    showServerDialog = true
                    dialogMode = DialogMode.EDIT
                    selectedServer = server
                },
                onDeleteServer = {
                    showServerDialog = true
                    dialogMode = DialogMode.DELETE
                    selectedServer = server
                },
                onInviteToServer = {
                    showServerDialog = true
                    dialogMode = DialogMode.CREATE_INVITE
                    selectedServer = server
                },
                onLeave = {
                    serverViewModel.leaveFromServer(server)
                }
            )
        }
    }


    if (showServerDialog) {
        val toastText = stringResource(R.string.cannot_be_empty)
        val toastError = stringResource(R.string.cannot_get_user_data)
        val context = LocalContext.current

        if (dialogMode == DialogMode.CREATE_INVITE) {
            InviteDialog(
                server = selectedServer,
                onDismiss = {
                    showServerDialog = false
                },
                title = stringResource(R.string.invite_to_server),
                label = stringResource(R.string.label_enter_nickname),
                primaryButtonLabel = stringResource(R.string.invite),
                dialogMode = dialogMode,
                onAction = { action ->
                    when (action) {
                        is InviteAction.InviteByUsername -> {
                            serverViewModel.inviteToServer(serverId = action.serverId, action.username)
                        }
                        is InviteAction.GenerateToken -> {
                            serverViewModel.createServerToken(serverId = action.serverId,
                            )
                        }
                        is InviteAction.JoinServer -> {
                            Unit
                        }
                    }
                },
                serverViewModel
            )

        } else if(dialogMode == DialogMode.SEARCH_SERVER) {
            InviteDialog(
                server = selectedServer,
                onDismiss = {
                    showServerDialog = false
                },
                title = stringResource(R.string.find_server),
                label = stringResource(R.string.enter_token),
                primaryButtonLabel = stringResource(R.string.join),
                dialogMode = dialogMode,
                onAction = {action ->
                    if(action is InviteAction.JoinServer) {
                        serverViewModel.joinByToken(action.serverToken)
                    }
                }
            )
        }
        else {
            ServerDialog(
                onDismiss = { showServerDialog = false },
                selectedServer = selectedServer,
                dialogMode = dialogMode,
                onConfirmClick = { formData ->
                    when (dialogMode) {

                        DialogMode.CREATE -> {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value

                            if (userId != null && token != null) {
                                if (!formData.name.isNullOrBlank()) {
                                    serverViewModel.createServer(
                                        name = formData.name,
                                        imageUri = formData.avatarUrl?.toUri(),
                                        onSuccess = {
                                            showServerDialog = false
                                        }
                                    )
                                } else {
                                    isErrorInDialog = true
                                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        DialogMode.EDIT -> {
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
                                        },
                                        onError = {
                                            Log.e("ServerDialog", "Edit error:")
                                        }
                                    )
                                }
                            }
                        }

                        DialogMode.DELETE -> {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value
                            val currentServer = selectedServer
                            if (userId != null && token != null && currentServer != null) {
                                if (!formData.name.isNullOrBlank() && formData.name == currentServer.name) {
                                    serverViewModel.deleteServer(
                                        currentServer,
                                        onSuccess = {
                                            isErrorInDialog = false
                                            showServerDialog = false
                                        },
                                        onError = {
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                                    isErrorInDialog = true
                                }
                            }
                        }
                        DialogMode.SEARCH_SERVER -> Unit
                        DialogMode.CREATE_INVITE  -> Unit
                    }
                },
                isError = isErrorInDialog
            )
        }

        Log.d("selectedServer", "avatarUrl : ${selectedServer?.avatarUrl.toString()}")
        Log.d("selectedServer", "server id : ${selectedServer?.id.toString()}")
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


