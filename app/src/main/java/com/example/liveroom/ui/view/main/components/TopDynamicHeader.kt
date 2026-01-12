package com.example.liveroom.ui.view.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.liveroom.R
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun TopDynamicHeader(selectedTab : String, userViewModel: UserViewModel) {

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val username by userViewModel.username.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarPadding.calculateTopPadding(), bottom = 14.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedTab) {
                    "home" -> stringResource(R.string.home)
                    "profile" -> stringResource(R.string.profile)
                    else -> "LiveRoom"
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.secondary,
            thickness = 1.dp
        )
    }
}

@Preview
@Composable
fun PreviewTopDynamicHeader() {
    TopDynamicHeader(
        "home", userViewModel = hiltViewModel<UserViewModel>()
    )
}