package com.example.liveroom.ui.view.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.liveroom.data.local.DataStoreManager
import com.example.liveroom.ui.navigation.Screen
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Composable
fun LoginView(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(
                "Login"
            )
            Button(
                onClick = {
//                    scope.launch {
//                        DataStoreManager.saveToken(context, "wadwadasdsabasd12384121512")
//                        DataStoreManager.logAllPreferences(context)
//                    }
                    navController.navigate(route = Screen.RegistrationScreen.route)
                }
            )
            {
            }
        }
    }
}