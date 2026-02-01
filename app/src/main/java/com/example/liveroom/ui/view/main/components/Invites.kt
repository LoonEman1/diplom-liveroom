package com.example.liveroom.ui.view.main.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.ui.theme.ButtonColor
import com.example.liveroom.ui.theme.LiveRoomTheme
import com.example.liveroom.ui.viewmodel.ServerViewModel

@Composable
fun Invites(
    invites : List<Invite.UserInvite>,
    modifier: Modifier = Modifier,
    serverViewModel: ServerViewModel? = null
) {

    if (invites.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.MailOutline,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.invites_empty),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(invites)  { invite ->
                InviteCard(
                    invite = invite,
                    onAccept = { it ->
                        Log.d("onAcceptInvite", "clicked")
                        serverViewModel?.acceptInvite(it)
                    },
                    onDecline = { it ->
                        Log.d("onDeclineInvite", "clicked")
                        serverViewModel?.declineInvite(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun InviteCard(
    invite : Invite.UserInvite,
    onAccept: (Invite.UserInvite) -> Unit,
    onDecline: (Invite.UserInvite) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = invite.serverName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${stringResource(R.string.invited_by_user)} ${invite.invitedByUsername}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.invite_valid) + " ${invite.getFormattedExpiresAt(invite.expiresAt)}",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                IconButton(onClick = { onDecline(invite) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        "Decline",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(
                    onClick = { onAccept(invite) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        "Accept",
                        tint = ButtonColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun InvitesPreview_Empty() {
    LiveRoomTheme {
        Invites(
            invites = emptyList(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InvitesPreview_WithInvites() {
    LiveRoomTheme {
        Invites(
            invites = listOf(
                Invite.UserInvite(
                    inviteId = 1,
                    serverId = 42,
                    serverName = "My Awesome Server",
                    invitedByUsername = "friend123",
                    expiresAt = "2026-02-05"
                ),
                Invite.UserInvite(
                    inviteId = 2,
                    serverId = 69,
                    serverName = "Dev Team",
                    invitedByUsername = "devlead",
                    expiresAt = "02.03, 23:48"
                )
            )
        )
    }
}


