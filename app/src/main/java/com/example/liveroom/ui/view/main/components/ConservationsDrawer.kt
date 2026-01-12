package com.example.liveroom.ui.view.main.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.liveroom.R
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

    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

    if (showCreateServerDialog) {
        CreateServerDialog(
            onDismiss = { showCreateServerDialog = false },
            userViewModel = userViewModel,
            serverViewModel = serverViewModel
        )
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
                        contentScale = ContentScale.Fit,
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
                                    userId = userId,
                                    token = token,
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
                                    "$toastError",
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


