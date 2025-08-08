package com.goldhardt.piggy.navigation

sealed interface Screen {
    val name: String

    object Expenses : Screen {
        override val name: String = "Expenses"
    }
    object Categories : Screen {
        override val name: String = "Categories"
    }
    object Trends : Screen {
        override val name: String = "Trends"
    }
    object Login : Screen {
        override val name: String = "Login"
    }
}