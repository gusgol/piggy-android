package com.goldhardt.piggy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.goldhardt.auth.LoginScreen
import com.goldhardt.feature.categories.CategoriesScreen
import com.goldhardt.feature.expenses.ExpensesListScreen
import com.goldhardt.feature.expenses.import_expenses.ImportExpensesScreen
import com.goldhardt.feature.trends.TrendsScreen
import com.goldhardt.piggy.ui.home.HomeScreen
import com.goldhardt.piggy.ui.home.HomeState
import com.goldhardt.piggy.ui.home.HomeViewModel

@Composable
fun PiggyNavigation(
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by homeViewModel.state.collectAsStateWithLifecycle()

    when (val authState = state) {
        HomeState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        HomeState.SignedOut -> LoginScreen()
        is HomeState.SignedIn -> {
            val backStack = rememberNavBackStack(Screen.Expenses)
            val currentScreen = backStack.lastOrNull() as? Screen ?: Screen.Expenses

            HomeScreen(
                user = authState.user,
                current = currentScreen,
                onItemClick = { targetScreen ->
                    if (targetScreen != currentScreen) {
                        backStack.clear()
                        backStack.add(targetScreen)
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
                                    }
                                )
                            }
                            else -> error("Unexpected destination in authenticated graph: $key")
                        }
                    }
                )
            }
        }
    }
}
