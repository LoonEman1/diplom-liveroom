package com.example.liveroom.ui.view.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.ui.view.main.layouts.MainLayout
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MainView(navController: NavController, userViewModel: UserViewModel, serverViewModel: ServerViewModel) {

    val userId = userViewModel.userId.collectAsState()
    val accessToken = userViewModel.accessToken.collectAsState()
    val userInfo = userViewModel.userInfo.collectAsState()
    val serverInvites = serverViewModel.serverInvites.collectAsState()

    LaunchedEffect(accessToken) {
        if (userInfo.value == null) {
            userViewModel.getUserInfo()
        }
        userViewModel.userInfo.first { it != null }

        coroutineScope {
            launch {
                if (serverViewModel.servers.value.isEmpty())
                    serverViewModel.getServers(userViewModel.userId.value)
            }
            launch {
                if (serverInvites.value.isEmpty())
                    serverViewModel.getInvites()
            }
        }
    }


    MainLayout(userViewModel, serverViewModel)
}

@Preview
@Composable
fun previewMain() {
    val navController = rememberNavController()
    val userViewModel = hiltViewModel<UserViewModel>()
    val serverViewModel = hiltViewModel<ServerViewModel>()
    MainView(navController, userViewModel, serverViewModel)
}





