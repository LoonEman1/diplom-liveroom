package com.example.liveroom.ui.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.liveroom.ui.view.main.components.BottomNavigationBar
import com.example.liveroom.ui.view.main.components.HomeComponent
import com.example.liveroom.ui.view.main.components.LeftNavigation
import com.example.liveroom.ui.view.main.components.TopDynamicHeader
import com.example.liveroom.ui.viewmodel.UserViewModel

@Composable
fun MainView(navController: NavController, userViewModel: UserViewModel) {
    var selectedTab by remember { mutableStateOf("home") }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxSize()
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding(),
                    bottom = WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
        ) {
            LeftNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        Scaffold(
            modifier = Modifier
                .weight(0.85f)
                .padding(top = 18.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 32.dp,
                        topEnd = 32.dp
                    )
                )
                .background(MaterialTheme.colorScheme.secondary),
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            },
            topBar = {
                TopDynamicHeader(
                    selectedTab = selectedTab,
                    userViewModel
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    "home" -> HomeComponent()
                }
            }
        }
    }
}





