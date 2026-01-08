package com.example.liveroom.ui.view.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.liveroom.R


@Composable
fun BottomNavigationBar(selectedTab: String, onTabSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            NavigationBarItem(
                icon = {
                    Icon(Icons.Default.Home, contentDescription = stringResource(R.string.home))
                },
                label = {
                    Text(stringResource(R.string.home), color = MaterialTheme.colorScheme.onSurface)
                },
                selected = selectedTab == "home",
                onClick = {
                    onTabSelected("home")
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )

//            NavigationBarItem(
//                icon = {
//                    Icon(Icons.Default.Search, contentDescription = "Search")
//                },
//                label = {
//                    Text("Поиск", color = MaterialTheme.colorScheme.onSurface)
//                },
//                selected = selectedTab == "search",
//                onClick = {
//                    onTabSelected("search")
//                }
//            )

//            NavigationBarItem(
//                icon = {
//                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
//                },
//                label = {
//                    Text("Уведомления", color = MaterialTheme.colorScheme.onSurface)
//                },
//                selected = selectedTab == "notifications",
//                onClick = {
//                    onTabSelected("notifications")
//                }
//            )

            NavigationBarItem(
                icon = {
                    Icon(Icons.Default.Person, contentDescription = stringResource(R.string.profile))
                },
                label = {
                    Text(stringResource(R.string.profile), color = MaterialTheme.colorScheme.onSurface)
                },
                selected = selectedTab == "profile",
                onClick = {
                    onTabSelected("profile")
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }
}

@Preview
@Composable
fun PreviewBottomNavBar() {
    BottomNavigationBar("home") { }
}
