package com.example.liveroom.ui.view.main.components

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.model.ServerFormData
import com.example.liveroom.data.remote.dto.Role
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.di.AppConfig
import com.example.liveroom.ui.components.CustomTextField
import com.example.liveroom.ui.components.PrimaryButton
import com.example.liveroom.ui.theme.LiveRoomTheme
import com.example.liveroom.ui.viewmodel.ServerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDialog(
    onDismiss: () -> Unit,
    onConfirmClick:(server : ServerFormData) -> Unit,
    dialogMode: ServerDialogMode,
    selectedServer : Server?,
    isError : Boolean = false
) {
    var serverName by remember { mutableStateOf("") }

    val previousServerName = if (selectedServer != null && dialogMode == ServerDialogMode.EDIT) {
        selectedServer.name
    } else null
    if(previousServerName != null) serverName = previousServerName

    val initialImageUrl = when(dialogMode) {
        ServerDialogMode.EDIT -> {
            selectedServer?.avatarUrl?.let {
                AppConfig.IMAGE_BASE_URL + it
            }
        }
        else -> null
    }

    var imageModel : Any? by remember { mutableStateOf(initialImageUrl) }

    val server = when (dialogMode) {
        ServerDialogMode.CREATE -> null
        ServerDialogMode.EDIT -> "${stringResource(R.string.server)} ${selectedServer?.name}"
        ServerDialogMode.DELETE -> "${stringResource(R.string.server)} ${selectedServer?.name}"
        else -> "unknown"
    }

    val title = when (dialogMode){
        ServerDialogMode.CREATE -> stringResource(R.string.create_new_server)
        ServerDialogMode.EDIT -> stringResource(R.string.edit_server)
        ServerDialogMode.DELETE -> stringResource(R.string.delete_server)
        else -> "unknown"
    }

    val showAsyncImage = when(dialogMode) {
        ServerDialogMode.CREATE, ServerDialogMode.EDIT -> true
        ServerDialogMode.DELETE -> false
        else -> true
    }

    val subTitle = when(dialogMode) {
        ServerDialogMode.DELETE ->{
            stringResource(R.string.confirm_delete_server) + " ${selectedServer?.name}"
        }
        else -> null
    }




    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageModel = uri
    }


    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if(server != null) {
                Text(
                    text = server,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            if(subTitle != null)
            {
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            if(showAsyncImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageModel != null) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = "Server Icon",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .size(120.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Image",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = stringResource(R.string.add_image),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            CustomTextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = stringResource(R.string.server_name),
                modifier = Modifier.fillMaxWidth(),
                isError = isError || serverName.isEmpty()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.cancel),
                    containerColor = MaterialTheme.colorScheme.error
                )

                val string = when (dialogMode) {
                    ServerDialogMode.CREATE -> stringResource(R.string.create)
                    ServerDialogMode.EDIT -> stringResource(R.string.edit)
                    ServerDialogMode.DELETE -> stringResource(R.string.delete)
                    else -> "unknown"
                }
                PrimaryButton(
                    text = string,
                    onClick = {
                        onConfirmClick(
                            ServerFormData(
                                id = null,
                                name = if (serverName != (previousServerName ?: "")) serverName else null,
                                avatarUrl = if (imageModel is Uri) {
                                    Log.d("imageUri", imageModel.toString())
                                    imageModel.toString()
                                } else {
                                    Log.d("imageUri", "image model is not Uri -> return null: ${imageModel.toString()}")
                                    null
                                }
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteDialog(
    server : Server?,
    onDismiss: () -> Unit,
    title : String,
    onInvite: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(InviteTab.USERNAME) }
    var nickname by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                InviteTab.entries.forEachIndexed { index, tab ->
                    Tab(
                       selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                        },
                        text = {
                            Text(
                                text = when (tab) {
                                    InviteTab.USERNAME -> stringResource(R.string.invite_by_nickname)
                                    InviteTab.GENERATE -> stringResource(R.string.generate_token)
                                },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            when(selectedTab) {
                InviteTab.USERNAME -> {
                    InviteByUsernameTab(onInvite, onNicknameChange = {it ->
                        nickname = it
                    }, nickname)
                }
                InviteTab.GENERATE -> {
                    GenerateTokenTab(onGenerate = onInvite)
                }
            }
        }
    }
}

@Composable
fun InviteByUsernameTab(onSubmit: () -> Unit, onNicknameChange : (String) -> Unit, nickname : String) {

    Column(
        modifier = Modifier
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.enter_nickname),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        CustomTextField(
            value = nickname,
            onValueChange = { onNicknameChange(it) },
            label = stringResource(R.string.label_enter_nickname),
            modifier = Modifier.fillMaxWidth(),
            isError = nickname.isBlank()
        )
        PrimaryButton(
            onClick = onSubmit,
            text = stringResource(R.string.invite)
        )
    }
}

@Composable
fun GenerateTokenTab(
    onGenerate: () -> Unit,
    token : String? = null
) {
    Column(
        modifier = Modifier
            .padding(bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.generate_token_tab),
            color = MaterialTheme.colorScheme.onSurface
        )
        if(token != null) {
            Text(
                text= stringResource(R.string.current_token) + token,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        PrimaryButton(
            onClick = onGenerate,
            text = stringResource(R.string.generate_token)
        )
    }
}


enum class InviteTab {
    USERNAME,
    GENERATE
}

@Preview(showBackground = true,
    apiLevel = 36)
@Composable
fun PreviewInviteDialog() {
    LiveRoomTheme() {
        val mockRole = Role(
            id = 1,
            name = "OWNER",
            power = 100,
            canManageMembers = true,
            canManageConversations = true
        )

        val mockServer = Server(
            id = 1,
            name = "Development Team",
            avatarUrl = null,
            myRole = mockRole,
            createdAt = "2026-01-24"
        )

        InviteDialog(
            server = mockServer,
            onDismiss = {},
            title = "Create invite token, or invite user by nickname",
            onInvite = {

            }
        )
    }
}

