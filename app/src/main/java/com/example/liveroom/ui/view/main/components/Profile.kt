package com.example.liveroom.ui.view.main.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.UpdateProfileRequest
import com.example.liveroom.data.remote.dto.UserInfo
import com.example.liveroom.ui.components.EditableField
import com.example.liveroom.ui.components.PrimaryButton
import com.example.liveroom.ui.theme.ButtonColor
import com.example.liveroom.ui.view.main.components.common.ConfirmationDialog
import com.example.liveroom.ui.viewmodel.UserViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun ProfileComponent(
    userViewModel: UserViewModel,
) {
    val userInfo by userViewModel.userInfo.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        if (userInfo == null) {
            userViewModel.getUserInfo()
        }
    }

    if (isLoading && userInfo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    ProfileContent(
        userInfo = userInfo,
        userViewModel,
    )

}

@Composable
fun ProfileContent(
    userInfo: UserInfo?,
    userViewModel: UserViewModel,
) {
    if (userInfo == null) return


    val serverImageUrl by remember {
        derivedStateOf {
            userInfo?.hasAvatar?.let {
                if (it) "https://nighthunting23.ru${userInfo.avatarUrl}" else null
            }
        }
    }

    var imageModel: Any? by remember { mutableStateOf(serverImageUrl) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageModel = uri
    }


    val nickname by remember { mutableStateOf(userInfo.nickname) }
    var firstName by remember { mutableStateOf(userInfo.firstName.orEmpty()) }
    var lastName by remember { mutableStateOf(userInfo.lastName.orEmpty()) }
    var about by remember { mutableStateOf(userInfo.about.orEmpty()) }
    var showNicknamesOnServers by remember { mutableStateOf(userInfo.showNicknamesOnServers) }
    var allowProfileView by remember { mutableStateOf(userInfo.allowProfileView) }
    val coroutineScope = rememberCoroutineScope()

    val hasChanges by remember(nickname, firstName, lastName, about, showNicknamesOnServers, allowProfileView, imageModel) {
        derivedStateOf {
            nickname != userInfo.nickname ||
                    firstName != (userInfo.firstName ?: "") ||
                    lastName != (userInfo.lastName ?: "") ||
                    about != (userInfo.about ?: "") ||
                    showNicknamesOnServers != userInfo.showNicknamesOnServers ||
                    allowProfileView != userInfo.allowProfileView ||
                    ((imageModel is Uri) && imageModel != null)
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.secondary)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel ?: serverImageUrl,
                        contentDescription = "Profile avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile avatar",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = nickname.first().uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = nickname,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.user_id) + ": ${userInfo.userId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userInfo.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDateIso(isoString = userInfo.createdAt),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        EditableField(
            label = stringResource(R.string.first_name),
            value = firstName,
            onValueChange = { firstName = it }
        )

        EditableField(
            label = stringResource(R.string.last_name),
            value = lastName,
            onValueChange = { lastName = it }
        )

        EditableField(
            label = stringResource(R.string.about_me),
            value = about,
            singleLine = false,
            onValueChange = { about = it }
        )


        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.show_real_name),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = showNicknamesOnServers,
                onCheckedChange = { showNicknamesOnServers = it }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.allow_show_profile),
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = allowProfileView,
                onCheckedChange = { allowProfileView = it }
            )
        }

        PrimaryButton(
            text = stringResource(R.string.save_changes),
            modifier = Modifier.fillMaxWidth(),
            enabled = hasChanges,
            onClick = {
                coroutineScope.launch {
                    if (firstName != (userInfo.firstName ?: "") ||
                        lastName != (userInfo.lastName ?: "") ||
                        about != (userInfo.about ?: "") ||
                        showNicknamesOnServers != userInfo.showNicknamesOnServers ||
                        allowProfileView != userInfo.allowProfileView) {

                        val request = UpdateProfileRequest(
                            firstName = if (firstName != (userInfo.firstName ?: "")) firstName else null,
                            lastName = if (lastName != (userInfo.lastName ?: "")) lastName else null,
                            about = if (about != (userInfo.about ?: "")) about else null,
                            showNicknamesOnServers = if (showNicknamesOnServers != userInfo.showNicknamesOnServers) showNicknamesOnServers else null,
                            allowProfileView = if (allowProfileView != userInfo.allowProfileView) allowProfileView else null,
                        )
                        userViewModel.editProfile(request)
                    }
                }
                coroutineScope.launch {
                    val localImageModel = imageModel
                    (localImageModel as? Uri)?.let { uri ->
                        userViewModel.updateAvatar(uri)
                    }
                }
            }
        )
        PrimaryButton(
            text = stringResource(R.string.logout),
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.error,
            onClick = { showDialog = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (showDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.confirm_logout),
                showDialog = showDialog,
                onConfirm = {
                    userViewModel.logout()
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}


@Composable
fun formatDateIso(isoString: String): String {
    val systemLocale = Locale.getDefault()
    return Instant.parse(isoString)
        .atZone(ZoneId.of("Europe/Moscow"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(systemLocale))
}

