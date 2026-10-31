package com.goldhardt.piggy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.goldhardt.auth.LoginScreen
import com.goldhardt.auth.SettingsScreen
import com.goldhardt.feature.categories.CategoriesScreen
import com.goldhardt.feature.expenses.ExpensesListScreen
import com.goldhardt.feature.expenses.importexpenses.ImportExpensesScreen
import com.goldhardt.feature.trends.TrendsScreen
import com.goldhardt.piggy.ui.home.HomeScreen
import com.goldhardt.piggy.ui.home.HomeViewModel

@Composable
fun PiggyNavigation(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val user by homeViewModel.user.collectAsStateWithLifecycle()

    if (user == null) {
        LoginScreen()
        return
    }

    val backStack = rememberNavBackStack(Screen.Expenses)
    val currentScreen = backStack.lastOrNull() as? Screen ?: Screen.Expenses

    HomeScreen(
        user = user,
        current = currentScreen,
        onItemClick = { targetScreen ->
            if (targetScreen != currentScreen) {
                backStack.clear()
                backStack.add(targetScreen)
            }
        },
        onAvatarClick = {
            if (currentScreen != Screen.Settings) {
                backStack.add(Screen.Settings)
            }
        }
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = { key ->
                when (key) {
                    is Screen.Expenses -> NavEntry(key) {
                        ExpensesListScreen(
                            onNavigateToImport = {
                                backStack.add(Screen.ImportExpenses)
                            }
                        )
                    }
                    is Screen.Categories -> NavEntry(key) {
                        CategoriesScreen()
                    }
                    is Screen.Trends -> NavEntry(key) {
                        TrendsScreen()
                    }
                    is Screen.ImportExpenses -> NavEntry(key) {
                        ImportExpensesScreen(
                            onNavigateBack = {
                                backStack.removeLastOrNull()
                            },
                            onNavigateToTrends = {
                                backStack.clear()
                                backStack.add(Screen.Trends)
                            }
                        )
                    }
                    is Screen.Settings -> NavEntry(key) {
                        SettingsScreen(
                            onNavigateBack = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }
                    else -> error("Unexpected destination in authenticated graph: $key")
                }
            }
        )
    }
}
