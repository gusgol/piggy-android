package com.goldhardt.piggy.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.goldhardt.piggy.navigation.Screen

/**
 * PiggyBottomBar is a composable function that creates a bottom navigation bar
 *
 * TODO pass in the tabs instead of hardcoding them internally
 * TODO move this to :designsystem
 */
@Composable
fun PiggyBottomBar(
    current: Screen,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar {
        listOf(Screen.Expenses, Screen.Categories, Screen.Trends).forEach { screen ->
            NavigationBarItem(
                selected = screen == current,
                onClick = { onItemClick(screen) },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Expenses -> Icons.AutoMirrored.Outlined.List
                            Screen.Categories -> Icons.Outlined.Settings
                            Screen.Trends -> Icons.Outlined.Info
                            Screen.Login -> TODO()
                        },
                        contentDescription = screen.name
                    )
                },
                label = { Text(screen.name) }
            )
        }
    }
}
