package com.example.liveroom.ui.view.main.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.liveroom.ui.view.main.components.*
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.liveroom.ui.view.main.components.common.BottomNavigationBar
import com.example.liveroom.ui.view.main.components.common.LeftNavigation
import com.example.liveroom.ui.view.main.components.common.TopDynamicHeader

@Composable
fun MainLayout(
    userViewModel: UserViewModel,
    serverViewModel: ServerViewModel
) {
    var selectedTab by remember { mutableStateOf("home") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Box(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            LeftNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                serverViewModel = serverViewModel,
                userViewModel = userViewModel
            )
        }
        Scaffold(
            modifier = Modifier
                .weight(0.85f)
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.secondary),
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            topBar = {
                TopDynamicHeader(selectedTab = selectedTab, userViewModel = userViewModel)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    "home" -> HomeComponent()
                    "invites" -> {
                        val invites by serverViewModel.serverInvites.collectAsState()
                        Invites(invites, serverViewModel = serverViewModel)
                    }
                }
            }
        }
    }
}
