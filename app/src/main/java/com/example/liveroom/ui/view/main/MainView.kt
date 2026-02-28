package com.example.liveroom.ui.view.main

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.local.WebSocketManager
import com.example.liveroom.ui.view.main.layouts.MainLayout
import com.example.liveroom.ui.viewmodel.ServerViewModel
import com.example.liveroom.ui.viewmodel.UserViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MainView(navController: NavController,
             userViewModel: UserViewModel,
             serverViewModel: ServerViewModel,
             wsManager : WebSocketManager
) {

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
            launch {
                wsManager.connect()
            }
        }
    }


    MainLayout(userViewModel, serverViewModel, navController)
}

@Preview
@Composable
fun previewMain() {
    val navController = rememberNavController()
    val userViewModel = hiltViewModel<UserViewModel>()
    val serverViewModel = hiltViewModel<ServerViewModel>()
    val tokenManager : TokenManager = TokenManager(LocalContext.current)
    val webSocketManager : WebSocketManager = WebSocketManager(tokenManager)
    MainView(navController, userViewModel, serverViewModel, webSocketManager)
}





