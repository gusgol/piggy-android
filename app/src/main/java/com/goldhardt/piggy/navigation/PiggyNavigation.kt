package com.goldhardt.piggy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.goldhardt.auth.LoginScreen
import com.goldhardt.feature.categories.CategoriesScreen
import com.goldhardt.feature.expenses.ExpensesListScreen
import com.goldhardt.feature.expenses.import_expenses.ImportExpensesScreen
import com.goldhardt.feature.trends.TrendsScreen
import com.goldhardt.piggy.ui.home.HomeScreen

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
                is Screen.ImportExpenses -> NavEntry(screen) {
                    HomeScreen(
                        current = screen,
                        onItemClick = { screen ->
                            backStack.removeLastOrNull()
                            backStack.add(screen)
                        }
                    ) {
                        ImportExpensesScreen(
                            onNavigateBack = {
                                backStack.removeLastOrNull()
                                backStack.add(Screen.Expenses)
                            }
                        )
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
                            is Screen.Expenses -> ExpensesListScreen(
                                onNavigateToImport = {
                                    backStack.add(Screen.ImportExpenses)
                                }
                            )
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
