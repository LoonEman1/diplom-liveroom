package com.example.liveroom.ui.view.main.layouts

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.liveroom.R
import com.example.liveroom.data.model.UserEvent
import com.example.liveroom.ui.navigation.Screen
import com.example.liveroom.ui.view.main.components.common.BottomNavigationBar
import com.example.liveroom.ui.view.main.components.common.LeftNavigation
import com.example.liveroom.ui.view.main.components.common.TopDynamicHeader

@Composable
fun MainLayout(
    userViewModel: UserViewModel,
    serverViewModel: ServerViewModel,
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf("home") }

    val isLoadingUserVM by userViewModel.isLoading.collectAsState()
    val isLoadingServer by serverViewModel.isLoading.collectAsState()
    val userEvents by userViewModel.userEvents.collectAsState(initial = null)
    val context = LocalContext.current

    val textUserUpdate = stringResource(R.string.profile_updated)
    val successLogout = stringResource(R.string.success_logout)
    val avatarUpdated = stringResource(R.string.avatar_updated)

    LaunchedEffect(userEvents) {
        userEvents?.let { event ->
            Log.d("userEvent", "event")
            when (event) {
                is UserEvent.ProfileUpdated -> {
                    Toast.makeText(context, textUserUpdate, Toast.LENGTH_SHORT).show()
                }
                is UserEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                is UserEvent.UserLoaded -> {

                }
                is UserEvent.UserLogOuted -> {
                    Toast.makeText(context, successLogout, Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.LoginScreen.route) {
                        userViewModel.clearUserData()
                        serverViewModel.clear()
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
                is UserEvent.AvatarUpdated -> {
                    Toast.makeText(context, avatarUpdated, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


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
                if (isLoadingUserVM || isLoadingServer) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@Box
                }

                when (selectedTab) {
                    "home" -> {
                        HomeComponent()
                    }
                    "invites" -> {
                        val invites by serverViewModel.serverInvites.collectAsState()
                        Invites(invites, serverViewModel = serverViewModel)
                    }
                    "profile" -> {
                        ProfileComponent(userViewModel)
                    }
                }
            }
        }
    }
}
