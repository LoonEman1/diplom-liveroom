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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Server
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

    var showCreateServerDialog by remember { mutableStateOf(false) }
    val serverList by serverViewModel.servers.collectAsState()
    val selectedServerId by serverViewModel.selectedServerId.collectAsState()

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
                        showCreateServerDialog = true
                        onTabSelected("create_server")
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
                }
            )
        }
    }

    if (showCreateServerDialog) {
        CreateServerDialog(
            onDismiss = { showCreateServerDialog = false },
            userViewModel = userViewModel,
            serverViewModel = serverViewModel
        )
    }
}

@Composable
fun ServerItem(
    server : Server,
    isSelected: Boolean = false,
    onClickServer: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondary)
            .clickable { onClickServer() }
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
                model = "https://nighthunting23.ru${server.avatarUrl}",
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
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServerDialog(
    onDismiss: () -> Unit,
    serverViewModel: ServerViewModel,
    userViewModel: UserViewModel
) {
    var serverName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val toastText = stringResource(R.string.cannot_be_empty)
    val toastCreatedServerText = stringResource(R.string.server_created)
    val toastError = stringResource(R.string.cannot_get_user_data)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
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
                text = stringResource(R.string.create_new_server),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

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
                if(selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            CustomTextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = stringResource(R.string.server_name),
                modifier = Modifier.fillMaxWidth()
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

                PrimaryButton(
                    text = stringResource(R.string.create),
                    onClick = {
                        if (serverName.isNotBlank()) {
                            val userId = userViewModel.userId.value
                            val token = userViewModel.accessToken.value

                            if (userId != null && token != null) {
                                serverViewModel.createServer(
                                    name = serverName,
                                    imageUri = selectedImageUri,
                                    onSuccess = { server ->
                                        Toast.makeText(
                                            context,
                                             "$toastCreatedServerText ${serverName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    },
                                    onError = { error ->
                                        Toast.makeText(
                                            context,
                                            error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    toastError,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



@Preview
@Composable
fun PreviewServerDialog() {
    val serverViewModel = hiltViewModel<ServerViewModel>()
    val userViewModel = hiltViewModel<UserViewModel>()
    LeftNavigation(
        "",
        {

        },
        serverViewModel = serverViewModel,
        userViewModel = userViewModel

    )
    CreateServerDialog(
        onDismiss =  {

        },
        serverViewModel = serverViewModel,
        userViewModel = userViewModel
    )
}


