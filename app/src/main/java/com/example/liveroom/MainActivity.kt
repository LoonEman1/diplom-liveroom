package com.example.liveroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.local.WebSocketManager
import com.example.liveroom.ui.navigation.NavigationGraph
import com.example.liveroom.ui.theme.LiveRoomTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var tokenManager: TokenManager
    @Inject lateinit var wsManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navigationController = rememberNavController()


            LiveRoomTheme {
                NavigationGraph(navController = navigationController, tokenManager, wsManager)
            }
        }
    }
}