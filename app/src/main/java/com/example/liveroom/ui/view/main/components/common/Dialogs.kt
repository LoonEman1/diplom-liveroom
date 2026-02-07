package com.example.liveroom.ui.view.main.components.common

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
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
import com.example.liveroom.ui.theme.linkTextColor
import com.example.liveroom.ui.viewmodel.ServerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDialog(
    onDismiss: () -> Unit,
    onConfirmClick:(server : ServerFormData) -> Unit,
    dialogMode: DialogMode,
    selectedServer : Server?,
    isError : Boolean = false,
) {
    var serverName by remember { mutableStateOf("") }

    val previousServerName = if (selectedServer != null && dialogMode == DialogMode.EDIT) {
        selectedServer.name
    } else null
    if(previousServerName != null) serverName = previousServerName

    val initialImageUrl = when(dialogMode) {
        DialogMode.EDIT -> {
            selectedServer?.avatarUrl?.let {
                AppConfig.IMAGE_BASE_URL + it
            }
        }
        else -> null
    }

    var imageModel : Any? by remember { mutableStateOf(initialImageUrl) }

    val server = when (dialogMode) {
        DialogMode.CREATE -> null
        DialogMode.EDIT -> "${stringResource(R.string.server)} ${selectedServer?.name}"
        DialogMode.DELETE -> "${stringResource(R.string.server)} ${selectedServer?.name}"
        else -> "unknown"
    }

    val title = when (dialogMode){
        DialogMode.CREATE -> stringResource(R.string.create_new_server)
        DialogMode.EDIT -> stringResource(R.string.edit_server)
        DialogMode.DELETE -> stringResource(R.string.delete_server)
        else -> "unknown"
    }

    val showAsyncImage = when(dialogMode) {
        DialogMode.CREATE, DialogMode.EDIT -> true
        DialogMode.DELETE -> false
        else -> true
    }

    val subTitle = when(dialogMode) {
        DialogMode.DELETE ->{
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
                    modifier = Modifier.weight(1f).padding(20.dp),
                    text = stringResource(R.string.cancel),
                    containerColor = MaterialTheme.colorScheme.error
                )

                val string = when (dialogMode) {
                    DialogMode.CREATE -> stringResource(R.string.create)
                    DialogMode.EDIT -> stringResource(R.string.edit)
                    DialogMode.DELETE -> stringResource(R.string.delete)
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
                    modifier = Modifier.weight(1f).padding(20.dp)
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
    label :  String,
    primaryButtonLabel: String,
    dialogMode: DialogMode,
    onAction: (InviteAction) -> Unit,
    viewModel: ServerViewModel? = null,
) {
    var selectedTab by remember { mutableStateOf(InviteTab.USERNAME) }
    var inputValue by remember { mutableStateOf("") }

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

            if (dialogMode == DialogMode.CREATE_INVITE) {
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

                when (selectedTab) {
                    InviteTab.USERNAME -> {
                        EnterTheValueTab(
                            onSubmit = {onAction(InviteAction.InviteByUsername(server?.id!!, inputValue))}
                            , onNicknameChange = { it ->
                                inputValue = it
                            },
                            inputValue,
                            label = label,
                            primaryButtonLabel = primaryButtonLabel
                        )
                    }

                    InviteTab.GENERATE -> {
                        val serverViewModel = if(viewModel == null) {
                            hiltViewModel<ServerViewModel>()
                        } else {
                            viewModel
                        }
                        GenerateTokenTab(onGenerate = {onAction(InviteAction.GenerateToken(server?.id!!))},
                            server = server,
                            serverViewModel)
                    }
                }
            }
            else if (dialogMode == DialogMode.SEARCH_SERVER) {
                EnterTheValueTab(
                    onSubmit = { onAction(InviteAction.JoinServer(inputValue)) },
                    onNicknameChange = { it ->
                        inputValue = it
                    },
                    value = inputValue,
                    label = label,
                    primaryButtonLabel = primaryButtonLabel
                )
            }
        }
    }
}

@Composable
fun EnterTheValueTab(
    onSubmit: () -> Unit,
    onNicknameChange : (String) -> Unit,
    value : String,
    label : String,
    primaryButtonLabel : String
    ) {

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
            value = value,
            onValueChange = { onNicknameChange(it) },
            label = label,
            modifier = Modifier.fillMaxWidth(),
            isError = value.isBlank()
        )
        PrimaryButton(
            onClick = onSubmit,
            text = primaryButtonLabel,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun GenerateTokenTab(
    onGenerate: () -> Unit,
    server : Server?,
    viewModel: ServerViewModel,
) {
    val token by viewModel.getServerToken(server?.id!!).collectAsState()

    val clipboardManager = LocalClipboardManager.current

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
                text= stringResource(R.string.current_token),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            val context = LocalContext.current
            val toastText = stringResource(R.string.token_copied)
            Text(
                token.toString(),
                color = linkTextColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(token.toString()))
                        Toast.makeText(
                            context, toastText, Toast.LENGTH_SHORT
                        ).show()
                    }
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

sealed class InviteAction {
    data class InviteByUsername(val serverId : Int, val username: String) : InviteAction()
    data class GenerateToken(val serverId: Int) : InviteAction()
    data class JoinServer(val serverToken: String) : InviteAction()
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
            label = "test",
            primaryButtonLabel = "test",
            dialogMode = DialogMode.CREATE_INVITE,
            {

            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message : String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if(message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
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
                        modifier = Modifier.weight(1f),
                        icon = null
                    )

                    PrimaryButton(
                        text = stringResource(R.string.confirm),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        icon = null
                    )
                }
            }
        }
    }
}

