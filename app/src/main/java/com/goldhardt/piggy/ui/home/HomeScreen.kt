package com.goldhardt.piggy.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.goldhardt.piggy.navigation.Screen

@Composable
fun HomeScreen(
    current: Screen,
    onItemClick: (Screen) -> Unit,
    content: @Composable () -> Unit
) {

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            PiggyBottomBar(
                current,
                onItemClick
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}
