package com.goldhardt.piggy.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.goldhardt.designsystem.R as DesignSystemR
import com.goldhardt.designsystem.components.PiggyNavigationBar
import com.goldhardt.designsystem.components.PiggyNavigationBarItem
import com.goldhardt.piggy.navigation.Screen

/**
 * Top-level bottom navigation bar for Piggy, delegating visual styling to
 * `:core:designsystem`'s [PiggyNavigationBar] and [PiggyNavigationBarItem].
 */
@Composable
fun PiggyBottomBar(
    current: Screen,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier,
) {
    PiggyNavigationBar(modifier = modifier) {
        listOf(Screen.Expenses, Screen.Categories, Screen.Trends).forEach { screen ->
            PiggyNavigationBarItem(
                selected = screen == current,
                onClick = { onItemClick(screen) },
                icon = {
                    Icon(
                        painter = painterResource(
                            id = when (screen) {
                                Screen.Expenses -> DesignSystemR.drawable.ic_receipt_long
                                Screen.Categories -> DesignSystemR.drawable.ic_pie_chart
                                Screen.Trends -> DesignSystemR.drawable.ic_trending_up
                                else -> DesignSystemR.drawable.ic_receipt_long
                            }
                        ),
                        contentDescription = screen.name,
                        modifier = Modifier.size(20.dp),
                    )
                },
                label = screen.name,
            )
        }
    }
}
