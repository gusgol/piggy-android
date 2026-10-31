package com.goldhardt.piggy.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    val name: String

    @Serializable
    data object Expenses : Screen {
        override val name: String = "Expenses"
    }

    @Serializable
    data object Categories : Screen {
        override val name: String = "Categories"
    }

    @Serializable
    data object Trends : Screen {
        override val name: String = "Trends"
    }

    @Serializable
    data object Login : Screen {
        override val name: String = "Login"
    }

    @Serializable
    data object ImportExpenses : Screen {
        override val name: String = "Import"
    }

    @Serializable
    data object Settings : Screen {
        override val name: String = "Settings"
    }
}