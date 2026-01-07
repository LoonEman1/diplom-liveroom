package com.example.liveroom.ui.view.main

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.liveroom.ui.view.main.components.BottomNavigationBar
import com.example.liveroom.ui.view.main.components.HomeComponent
import com.example.liveroom.ui.view.main.components.TopDynamicHeader

@Composable
fun MainView(navController: NavController) {
    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        topBar = {
            TopDynamicHeader(
                selectedTab = selectedTab
            )
        }
    ) { innerPadding ->
        Row(
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



