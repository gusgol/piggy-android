package com.goldhardt.piggy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.goldhardt.auth.LoginScreen
import com.goldhardt.feature.expenses.ExpensesListScreen
import com.goldhardt.piggy.ui.home.CategoriesScreen
import com.goldhardt.piggy.ui.home.HomeScreen
import com.goldhardt.piggy.ui.home.TrendsScreen

@Composable
fun PiggyNavigation() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.Login) }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { screen ->
            when (screen) {
                is Screen.Login -> NavEntry(screen) {
                    LoginScreen {
                        backStack.add(Screen.Expenses)
                    }
                }
                is Screen.Expenses, is Screen.Categories, is Screen.Trends -> NavEntry(screen) {
                    HomeScreen(
                        current = screen,
                        onItemClick = { screen ->
                            backStack.removeLastOrNull()
                            backStack.add(screen)
                        }
                    ) {
                        // Content for the HomeScreen
                        when (screen) {
                            is Screen.Expenses -> ExpensesListScreen()
                            is Screen.Categories -> CategoriesScreen()
                            is Screen.Trends -> TrendsScreen()
                            else -> {}
                        }
                    }
                }
            }
        }
    )
}
